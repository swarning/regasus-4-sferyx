package de.regasus.impex.ods.dialog;

import java.util.Locale;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.CustomFieldConfigParameterSet;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;

public class ODSProfileImportWizardPage extends WizardPage {

	public static final String ID = "ODSProfileImportWizard";

	private LanguageCombo languageCombo;

	private Text fileText;
	private Button fileButton;

	private NullableSpinner maxErrorsNumberSpinner;

	private Button checkLastnameButton;
	private Button checkFirstnameButton;
	private Button checkEmailButton;
	private Button checkMainCityButton;

	private Combo separatorCombo;
	private Label separatorComboLabel;

	private String lastDirectoryPath = null;


	public ODSProfileImportWizardPage() {
		super(ID);
		setTitle(ImpexI18N.ODSProfileImportWizard_Title);
		setDescription(ImpexI18N.ODSImportWizardPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		setControl(container);

		Group importContentGroup = new Group(container, SWT.NONE);
		importContentGroup.setText(ImpexI18N.ODSImportWizardPage_ImportContentGroupText);
		importContentGroup.setLayout(new GridLayout(3, false));
		importContentGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Group doubletSearchGroup = new Group(container, SWT.NONE);
		doubletSearchGroup.setText(ImpexI18N.ODSImportWizardPage_DuplicateSearchGroupText);
		doubletSearchGroup.setLayout(new GridLayout(2, false));
		doubletSearchGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		try {
			Label languageLabel = new Label(importContentGroup, SWT.NONE);
			languageLabel.setText(ImpexI18N.ODSImportWizardPage_LanguageLabel);

			languageCombo = new LanguageCombo(importContentGroup, SWT.READ_ONLY);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			String language = Locale.getDefault().getLanguage();
			languageCombo.setLanguageCode(language);

			languageCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ODSProfileImportWizard.language = languageCombo.getLanguageCode();
					checkPageComplete();
				}
			});


			Label fileLabel = new Label(importContentGroup, SWT.NONE);
			fileLabel.setText(ImpexI18N.ODSImportWizardPage_FileLabel);

			fileText = new Text(importContentGroup, SWT.BORDER);
			fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			fileText.setText("");

			fileText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ODSProfileImportWizard.path = fileText.getText();
					checkPageComplete();
				}
			});

			fileButton = new Button(importContentGroup, SWT.RIGHT);
			fileButton.setText("...");
			fileButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Shell shell = Display.getDefault().getActiveShell();
					FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
					fileDialog.setText("Import");
					fileDialog.setFilterPath(lastDirectoryPath);
					String[] filterExt = {"*.ods;*.xls;*.csv", "*.ods", "*.xls", "*.csv"};
					fileDialog.setFilterExtensions(filterExt);

					String path = fileDialog.open();
					if (path != null) {
						fileText.setText(path);
					}
				}
			});

			Label maxErrorLabel = new Label(importContentGroup, SWT.NONE);
			maxErrorLabel.setText(ImpexI18N.ODSImportWizardPage_MaxErrorLabel);

			maxErrorsNumberSpinner = new NullableSpinner(importContentGroup, SWT.NONE);
			maxErrorsNumberSpinner.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
			maxErrorsNumberSpinner.setMinimum(0);
			maxErrorsNumberSpinner.setValue(0);
			maxErrorsNumberSpinner.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Integer maxErrors = maxErrorsNumberSpinner.getValueAsInteger();
					if (maxErrors != null){
						ODSProfileImportWizard.maxErrors = maxErrors;
						checkPageComplete();
					}
				}
			});

			Label checkLastnameLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkLastnameLabel.setText(ImpexI18N.ODSImportWizardPage_CheckLastNameLabel);

			checkLastnameButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkLastnameButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkLastnameButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSProfileImportWizard.isDubCheckLastname = checkLastnameButton.getSelection();
				}
			});

			Label checkFirstnameLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkFirstnameLabel.setText(ImpexI18N.ODSImportWizardPage_CheckFirstNameLabel);

			checkFirstnameButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkFirstnameButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkFirstnameButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSProfileImportWizard.isDubCheckFistname = checkFirstnameButton.getSelection();
				}
			});

			Label checkEmailLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkEmailLabel.setText(ImpexI18N.ODSImportWizardPage_CheckEmailLabel);

			checkEmailButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkEmailButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkEmailButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSProfileImportWizard.isDubCheckEmail = checkEmailButton.getSelection();
				}
			});

			Label checkMainCityLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkMainCityLabel.setText(ImpexI18N.ODSImportWizardPage_CheckMainCityLabel);

			checkMainCityButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkMainCityButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkMainCityButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSProfileImportWizard.isDubCheckMainCity = checkMainCityButton.getSelection();
				}
			});

			separatorComboLabel = new Label(importContentGroup, SWT.NONE);
			separatorComboLabel.setText(ImpexI18N.ODSImportWizardPage_SeparatorComboLabel);

			separatorCombo = new Combo(importContentGroup, SWT.READ_ONLY);
			separatorCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			separatorCombo.add(" ");
			separatorCombo.add(",");
			separatorCombo.add(";");
			separatorCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSProfileImportWizard.customFieldListValueSeparator = separatorCombo.getText();
				}
			});

		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			init();
		}
		super.setVisible(visible);
	}


	private void init() {
		try {
			if (ODSProfileImportWizard.path != null) {
				fileText.setText(ODSProfileImportWizard.path);
			}

			if (ODSProfileImportWizard.maxErrors != 0) {
				maxErrorsNumberSpinner.setValue(ODSProfileImportWizard.maxErrors);
			}

			checkLastnameButton.setSelection(ODSProfileImportWizard.isDubCheckLastname);

			checkFirstnameButton.setSelection(ODSProfileImportWizard.isDubCheckFistname);

			checkEmailButton.setSelection(ODSProfileImportWizard.isDubCheckEmail);

			checkMainCityButton.setSelection(ODSProfileImportWizard.isDubCheckMainCity);

			if (isProfileCustomFieldAllowed()) {
				if (ODSProfileImportWizard.customFieldListValueSeparator != null) {
					separatorCombo.setText(ODSProfileImportWizard.customFieldListValueSeparator);
				}
			}
			else {
				separatorComboLabel.setVisible(false);
				separatorCombo.setVisible(false);
			}

			checkPageComplete();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public boolean isPageComplete() {
		boolean complete =
			languageCombo.getLanguageCode() != null &&
			!fileText.getText().equals("");

		return complete;
	}


	private void checkPageComplete() {
		setPageComplete(isPageComplete());
	}


	private boolean isProfileCustomFieldAllowed() throws Exception {
		ConfigParameterSetModel model = ConfigParameterSetModel.getInstance();
		ConfigParameterSet configParameterSet = model.getConfigParameterSet();
		CustomFieldConfigParameterSet customFieldConfigParameterSet = configParameterSet.getProfile().getCustomField();
		boolean isCertificatePrintAllowed = customFieldConfigParameterSet.isVisible();
		return isCertificatePrintAllowed;
	}


}

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
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.event.EventModel;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;
import de.regasus.participant.state.combo.ParticipantStateCombo;
import de.regasus.participant.type.combo.ParticipantTypeCombo;


public class ODSParticipantImportWizardPage extends WizardPage {

	public static final String ID = "ODSParticipantImportWizardPage";

	private LanguageCombo languageCombo;
	private ParticipantStateCombo participantStateCombo;
	private ParticipantTypeCombo participantTypeCombo;
	private ParticipantTypeCombo groupManagerTypeCombo;

	private Text fileText;
	private Button fileButton;

	private NullableSpinner maxErrorsNumberText;

	private Button checkLastnameButton;
	private Button checkFirstnameButton;
	private Button checkEmailButton;
	private Button checkMainCityButton;

	private String lastDirectoryPath = null;

	private Combo separatorCombo;
	private Label separatorComboLabel;


	public ODSParticipantImportWizardPage() {
		super(ID);
		setTitle(ImpexI18N.ODSParticipantImportWizard_Title);
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

			languageCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ODSParticipantImportWizard.language = languageCombo.getLanguageCode();
					checkPageComplete();
				}
			});
			// language value is initialized in init() where the Event is known



			Label participantStateLabel = new Label(importContentGroup, SWT.NONE);
			participantStateLabel.setText(ImpexI18N.ODSImportWizardPage_ParticipantStateLabel);

			participantStateCombo = new ParticipantStateCombo(importContentGroup, SWT.READ_ONLY);
			participantStateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			participantStateCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ODSParticipantImportWizard.participantStatePK = participantStateCombo.getParticipantStateID();
					checkPageComplete();
				}
			});



			Label participantTypeLabel = new Label(importContentGroup, SWT.NONE);
			participantTypeLabel.setText(ImpexI18N.ODSImportWizardPage_ParticipantTypeLabel);

			participantTypeCombo = new ParticipantTypeCombo(importContentGroup, SWT.READ_ONLY);
			participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			participantTypeCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ODSParticipantImportWizard.participantTypePK = participantTypeCombo.getParticipantTypePK();
					checkPageComplete();
				}
			});



			Label groupManagerTypeLabel = new Label(importContentGroup, SWT.NONE);
			groupManagerTypeLabel.setText(ImpexI18N.ODSImportWizardPage_GroupManagerTypeLabel);

			groupManagerTypeCombo = new ParticipantTypeCombo(importContentGroup, SWT.READ_ONLY);
			groupManagerTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			groupManagerTypeCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ODSParticipantImportWizard.groupManagerTypePK = groupManagerTypeCombo.getParticipantTypePK();
					checkPageComplete();
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
					ODSParticipantImportWizard.customFieldListValueSeparator = separatorCombo.getText();
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
					ODSParticipantImportWizard.path = fileText.getText();
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

					// avoid space characters! (MIRCP-2897 - Use portable file extension filter for multiple extensions)
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

			maxErrorsNumberText = new NullableSpinner(importContentGroup, SWT.NONE);
			maxErrorsNumberText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
			maxErrorsNumberText.setMinimum(0);
			maxErrorsNumberText.setValue(0);
			maxErrorsNumberText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Integer maxErrors = maxErrorsNumberText.getValueAsInteger();
					if (maxErrors == null) {
						maxErrors = 0;
					}
					ODSParticipantImportWizard.maxErrors = maxErrors.intValue();

					checkPageComplete();
				}
			});



			Label checkLastnameLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkLastnameLabel.setText(ImpexI18N.ODSImportWizardPage_CheckLastNameLabel);

			checkLastnameButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkLastnameButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkLastnameButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSParticipantImportWizard.isDubCheckLastname = checkLastnameButton.getSelection();
				}
			});

			Label checkFirstnameLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkFirstnameLabel.setText(ImpexI18N.ODSImportWizardPage_CheckFirstNameLabel);

			checkFirstnameButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkFirstnameButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkFirstnameButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSParticipantImportWizard.isDubCheckFistname = checkFirstnameButton.getSelection();
				}
			});

			Label checkEmailLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkEmailLabel.setText(ImpexI18N.ODSImportWizardPage_CheckEmailLabel);

			checkEmailButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkEmailButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkEmailButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSParticipantImportWizard.isDubCheckEmail = checkEmailButton.getSelection();
				}
			});

			Label checkMainCityLabel = new Label(doubletSearchGroup, SWT.NONE);
			checkMainCityLabel.setText(ImpexI18N.ODSImportWizardPage_CheckMainCityLabel);

			checkMainCityButton = new Button(doubletSearchGroup, SWT.CHECK);
			checkMainCityButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			checkMainCityButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ODSParticipantImportWizard.isDubCheckMainCity = checkMainCityButton.getSelection();
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
			Long eventPK = ODSParticipantImportWizard.getEventPK();

			// init language
			String language = null;
			if (eventPK != null) {
				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
				if (eventVO != null) {
					language = eventVO.getLanguage();
				}
			}
			if (language == null) {
				language = Locale.getDefault().getLanguage();
			}
			languageCombo.setLanguageCode(language);


			if (eventPK != null) {
				participantTypeCombo.setEventID(eventPK);
				groupManagerTypeCombo.setEventID(eventPK);
			}

			if (ODSParticipantImportWizard.participantStatePK != null) {
				participantStateCombo.setParticipantStateID(ODSParticipantImportWizard.participantStatePK);
			}

			if (ODSParticipantImportWizard.participantTypePK != null) {
				participantTypeCombo.setParticipantTypePK(ODSParticipantImportWizard.participantTypePK);
			}

			if (ODSParticipantImportWizard.groupManagerTypePK != null) {
				groupManagerTypeCombo.setParticipantTypePK(ODSParticipantImportWizard.groupManagerTypePK);
			}

			if (ODSParticipantImportWizard.path != null) {
				fileText.setText(ODSParticipantImportWizard.path);
			}

			if (ODSParticipantImportWizard.maxErrors != 0) {
				maxErrorsNumberText.setValue(ODSParticipantImportWizard.maxErrors);
			}

			if (isParticipantCustomFieldAllowed(eventPK)) {
				if (ODSParticipantImportWizard.customFieldListValueSeparator != null) {
					separatorCombo.setText(ODSParticipantImportWizard.customFieldListValueSeparator);
				}
			}
			else {
				separatorComboLabel.setVisible(false);
				separatorCombo.setVisible(false);
			}

			checkLastnameButton.setSelection(ODSParticipantImportWizard.isDubCheckLastname);

			checkFirstnameButton.setSelection(ODSParticipantImportWizard.isDubCheckFistname);

			checkEmailButton.setSelection(ODSParticipantImportWizard.isDubCheckEmail);

			checkMainCityButton.setSelection(ODSParticipantImportWizard.isDubCheckMainCity);


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


	private boolean isParticipantCustomFieldAllowed(Long eventID) throws Exception {
		ConfigParameterSetModel model = ConfigParameterSetModel.getInstance();
		ConfigParameterSet configParameterSet = model.getConfigParameterSet(eventID);
		CustomFieldConfigParameterSet customFieldConfigParameterSet = configParameterSet.getEvent().getParticipant().getCustomField();
		boolean isCertificatePrintAllowed = customFieldConfigParameterSet.isVisible();
		return isCertificatePrintAllowed;
	}
}

package de.regasus.impex.eivfobi.dialog;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.EmailUtil;
import com.lambdalogic.util.StringHelper;
import de.regasus.core.error.RegasusErrorHandler;

import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;

public class EIVFoBiExportWizardPage extends WizardPage {

	public static final String ID = "EIVFoBiExportWizardPage";

	// fields to hold values to restore them the next time the page is opened
	private static String filePath;
	private static String email;
	private static boolean onlyNotExported = false;
	private static boolean markTransmitted = false;

	private boolean isValidEmail = true;

	// Widgets
	private Text fileText;
	private Button fileButton;
	private Text emailText;
	private Label invalidEmailIndicatorLabel;
	private Button onlyNotExportedButton;
	private Button markTransmittedButton;




	public EIVFoBiExportWizardPage() {
		super(ID);
		setTitle(ImpexI18N.EIVFoBiExportWizardPage_Title);
		setDescription(ImpexI18N.EIVFoBiExportWizardPage_Description);
	}


	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		setControl(container);

		try {
			// filePath
			{
				Label fileLabel = new Label(container, SWT.NONE);
				fileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				fileLabel.setText(ImpexI18N.EIVFoBiExportWizardPage_File);
				fileLabel.setToolTipText(ImpexI18N.EIVFoBiExportWizardPage_FileToolTip);

				fileText = new Text(container, SWT.BORDER);
				fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				fileText.setText("");
				fileText.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						filePath = fileText.getText();
						checkPageComplete();
					}
				});

				fileButton = new Button(container, SWT.RIGHT);
				fileButton.setText("...");
				fileButton.setToolTipText(ImpexI18N.EIVFoBiExportWizardPage_FileButtonToolTip);
				fileButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Shell shell = Display.getDefault().getActiveShell();
						FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
						fileDialog.setText("Export");

						if (StringHelper.isNotEmpty(filePath)) {
							fileDialog.setFilterPath(filePath);
						}

						String[] filterExt = {"*.xml"};
						fileDialog.setFilterExtensions(filterExt);

						String fileStr = fileDialog.open();
						if (StringHelper.isNotEmpty(fileStr)) {
							if (!fileStr.endsWith(".xml")) {
								fileStr += ".xml";
							}
							fileText.setText(fileStr);
						}
					}
				});
			}

			// Email
			{
				Label emailLabel = new Label(container, SWT.NONE);
				emailLabel.setText(ImpexI18N.EIVFoBiExportWizardPage_Email);
				emailLabel.setToolTipText(ImpexI18N.EIVFoBiExportWizardPage_EmailToolTip);
				emailLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

				emailText = new Text(container, SWT.BORDER);
				emailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				emailText.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						email = emailText.getText();
						if (StringHelper.isNotEmpty(email)) {
							isValidEmail = EmailUtil.isValidEmail(email);
						}
						else {
							isValidEmail = false;
						}
						invalidEmailIndicatorLabel.setVisible(!isValidEmail);
						checkPageComplete();
					}
				});

				invalidEmailIndicatorLabel = new Label(container, SWT.NONE);
				Font font = com.lambdalogic.util.rcp.Activator.getDefault().getFontFromRegistry(com.lambdalogic.util.rcp.Activator.DEFAULT_FONT_BOLD);
				invalidEmailIndicatorLabel.setFont(font);
				invalidEmailIndicatorLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
				invalidEmailIndicatorLabel.setText("!");
			}

			// onlyNotExported
			{
				// placeholder
				new Label(container, SWT.NONE);

				onlyNotExportedButton = new Button(container, SWT.CHECK);
				onlyNotExportedButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
				onlyNotExportedButton.setText(ImpexI18N.EIVFoBiExportWizardPage_OnlyNotExportedButton);
				onlyNotExportedButton.setToolTipText(ImpexI18N.EIVFoBiExportWizardPage_OnlyNotExportedButtonToolTip);
				onlyNotExportedButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						onlyNotExported = onlyNotExportedButton.getSelection();
					}
				});
			}

			// markTransmitted
			{
				// placeholder
				new Label(container, SWT.NONE);

				markTransmittedButton = new Button(container, SWT.CHECK);
				markTransmittedButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
				markTransmittedButton.setText(ImpexI18N.EIVFoBiExportWizardPage_MarkTransmittedButton);
				markTransmittedButton.setToolTipText(ImpexI18N.EIVFoBiExportWizardPage_MarkTransmittedButtonToolTip);
				markTransmittedButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						markTransmitted = markTransmittedButton.getSelection();
					}
				});
			}
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
			if (filePath != null) {
				fileText.setText(filePath);
			}

			if (email != null) {
				emailText.setText(email);
			}

			onlyNotExportedButton.setSelection(onlyNotExported);

			markTransmittedButton.setSelection(markTransmitted);

			checkPageComplete();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public boolean isPageComplete() {
		boolean complete =
			StringHelper.isNotEmpty(filePath) &&
			StringHelper.isNotEmpty(email) &&
			isValidEmail;

		return complete;
	}


	private void checkPageComplete() {
		setPageComplete(isPageComplete());
	}


	public File getFile() {
		File file = null;
		if (filePath != null) {
			file = new File(filePath);
		}
		return file;
	}


	public String getEmail() {
		return email;
	}


	public boolean isOnlyNotExported() {
		return onlyNotExported;
	}


	public boolean isMarkTransmitted() {
		return markTransmitted;
	}

}

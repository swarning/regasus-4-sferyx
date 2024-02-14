package de.regasus.event.dialog;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.combo.LanguageCombo;


/**
 * Dialog used for uploading templates for notifications and badges.
 */
public class UploadEventTemplateDialog extends TitleAreaDialog {

	private String language;
	private File file;

	private String title;

	// widgets
	private LanguageCombo languageCombo;
	private Button okButton;
	private FileSelectionComposite fileSelectionComposite;



	/**
	 * UploadEventTemplateDialog constructor
	 *
	 * @param shell
	 *          the parent shell
	 * @param initFile
	 * 			the initial File which is shown in the Dialog
	 */
	public UploadEventTemplateDialog(Shell shell, File initFile, String initLanguage, String title) {
		super(shell);

		this.file = initFile;
		this.language = initLanguage;

		if (title != null) {
			this.title = title;
		}
		else {
			this.title = CoreI18N.UploadTemplateDialog_Title;
		}
	}


	/**
	 * Closes the dialog box Override so we can dispose the image we created
	 */
	@Override
	public boolean close() {
		return super.close();
	}


	@Override
	protected void okPressed() {
		language = languageCombo.getLanguageCode();
		file = fileSelectionComposite.getFile();

		super.okPressed();
	}


	/**
	 * Creates the dialog's contents
	 *
	 * @param parent
	 *            the parent composite
	 * @return Control
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		// Set the title
		setTitle(title);

		return contents;
	}


	/**
	 * Creates the gray area
	 *
	 * @param parent
	 *            the parent composite
	 * @return Control
	 * @throws Exception
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		try {
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));

			// languageLabel
			Label languageLabel = new Label(composite, SWT.RIGHT);
			languageLabel.setText(UtilI18N.Language);

			// languageCombo
			languageCombo = new LanguageCombo(composite, SWT.NONE);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			languageCombo.setLanguageCode(language);

			// fileLabel
			Label fileLabel = new Label(composite, SWT.RIGHT);
			fileLabel.setText(UtilI18N.File);

			// fileSelectionComposite
			fileSelectionComposite = new FileSelectionComposite(composite, SWT.OPEN);

			// avoid space characters! (MIRCP-2897 - Use portable file extension filter for multiple extensions)
			String[] extensions = {"*.ods;*.odt;*.odg", "*.ods", "*.odt", "*.odg"};
			fileSelectionComposite.setFilterExtensions(extensions);
			fileSelectionComposite.setFile(file);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			fileSelectionComposite.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					refreshOkButtonState();
				}
			});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return composite;
	}


	/**
	 * Creates the buttons for the button bar
	 *
	 * @param parent
	 *            the parent composite
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		refreshOkButtonState();
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	private void refreshOkButtonState() {
		boolean fileOrDirExists = fileSelectionComposite.fileForOpenOrDirForSaveExists();
		okButton.setEnabled(fileOrDirExists);
	}


	public String getLanguage() {
		return language;
	}


	public File getFile() {
		return file;
	}

}

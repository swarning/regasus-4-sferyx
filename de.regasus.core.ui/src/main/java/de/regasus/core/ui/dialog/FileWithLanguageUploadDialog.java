package de.regasus.core.ui.dialog;

import java.io.File;
import java.util.Objects;

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

import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.combo.LanguageCombo;

public class FileWithLanguageUploadDialog extends TitleAreaDialog {

	private Button okButton;
	private String[] acceptExtensions;
	private String title;

	private String selectedLanguage;
	private String selectedFile;

	private boolean languageRequired = false;


	// widgets
	private LanguageCombo languageCombo;
	private FileSelectionComposite fileSelectionComposite;


	public FileWithLanguageUploadDialog(Shell parent, String title, String... acceptExtensions) {
		super(parent);

		this.title = Objects.requireNonNull(title);
		this.acceptExtensions = Objects.requireNonNull(acceptExtensions);
	}


	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(title);
		return contents;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		// create new Composite, because the layout of parent must not be changed
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		try {
   			new Label(composite, SWT.NONE).setText(UtilI18N.Language);
   			createLanguageCombo(composite);

			new Label(composite, SWT.NONE).setText(UtilI18N.File);
			createFileSelection(composite);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return composite;
	}


	private void createFileSelection(Composite composite) {
		fileSelectionComposite = new FileSelectionComposite(composite, SWT.OPEN);
		fileSelectionComposite.setFilterExtensions(acceptExtensions);
		fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fileSelectionComposite.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				selectedFile = fileSelectionComposite.getFilePath();
				refreshOkButtonState();
			}
		});
	}


	private void createLanguageCombo(Composite composite) throws Exception {
		languageCombo = new LanguageCombo(composite, SWT.NONE);
		languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		languageCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				selectedLanguage = languageCombo.getLanguageCode();
				refreshOkButtonState();
			}
		});
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		this.okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		refreshOkButtonState();
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	private void refreshOkButtonState() {
		boolean fileOK = selectedFile != null && selectedFile.length() > 0;

		boolean hasLanguage = selectedLanguage != null && selectedLanguage.length() > 0;
		boolean languageOK = !languageRequired || hasLanguage;

		okButton.setEnabled(fileOK && languageOK);
	}


	public boolean isLanguageRequired() {
		return languageRequired;
	}


	public void setLanguageRequired(boolean languageRequired) {
		this.languageRequired = languageRequired;
	}


	public String getLanguage() {
		return selectedLanguage;
	}


	public void setLanguage(String languageCode) {
		languageCombo.setLanguageCode(languageCode);
		// get language from widget, because languageCode might not exist
		selectedLanguage = languageCombo.getLanguageCode();
		refreshOkButtonState();
	}


	public String getFilePath() {
		return selectedFile;
	}


	public void setFilePath(String filePath) {
		fileSelectionComposite.setFile( new File(filePath) );
		selectedFile = filePath;
		refreshOkButtonState();
	}

}

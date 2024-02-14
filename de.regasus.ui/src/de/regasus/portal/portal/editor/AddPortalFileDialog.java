package de.regasus.portal.portal.editor;

import static com.lambdalogic.util.StringHelper.*;

import java.io.File;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.widget.FileSelectionComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.combo.LanguageCombo;
import de.regasus.ui.Activator;

public class AddPortalFileDialog extends TitleAreaDialog {

	private String mnemonic;
	private String language;
	private File file;

	// widgets
	private Button okButton;

	private Text mnemonicText;
	private LanguageCombo languageCombo;
	private FileSelectionComposite fileSelectionComposite;


	public AddPortalFileDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle()  | SWT.RESIZE );
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(I18N.AddPortalFileDialog_Title);
		setMessage(I18N.AddPortalFileDialog_Message);

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite mainComposite = new Composite(dialogArea, SWT.NONE);
		mainComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
		final int COL_COUNT = 2;
		mainComposite.setLayout(new GridLayout(COL_COUNT, false));

		try {
			/*** mnemonic ***/
			SWTHelper.createLabel(mainComposite, UtilI18N.Mnemonic, true);

			mnemonicText = new Text(mainComposite, SWT.BORDER);
			mnemonicText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			SWTHelper.makeBold(mnemonicText);
			mnemonicText.setTextLimit(50);
			mnemonicText.addModifyListener(modifyListener);


			/*** language ***/
			SWTHelper.createLabel(mainComposite, UtilI18N.Language, false);

			languageCombo = new LanguageCombo(mainComposite, SWT.NONE);
			languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			languageCombo.addModifyListener(modifyListener);


    		/*** file ***/
    		SWTHelper.createLabel(mainComposite, UtilI18N.File, true);

    		fileSelectionComposite = new FileSelectionComposite(mainComposite, SWT.OPEN);
    		fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    		fileSelectionComposite.addModifyListener(modifyListener);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return dialogArea;
	}


	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			updateButtonState();
		}
	};


	private void updateButtonState() {
		syncFieldsToWidgets();

		boolean okEnabled =
			   isNotEmpty(mnemonic)
			&& fileSelectionComposite.getFile() != null;

		okButton.setEnabled(okEnabled);
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(800, 375);
	}


//	@Override
//	protected void configureShell(Shell newShell) {
//		super.configureShell(newShell);
//		newShell.setText(EmailI18N.CitySelectionDialog_ShellText);
//	}


	private void syncFieldsToWidgets() {
		mnemonic = mnemonicText.getText();
		language = languageCombo.getLanguageCode();
		file = fileSelectionComposite.getFile();
	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK) {
			syncFieldsToWidgets();
		}
		super.buttonPressed(buttonId);
	}


	public String getMnemonic() {
		return mnemonic;
	}


	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
		if (mnemonicText != null && !mnemonicText.isDisposed()) {
			mnemonicText.setText( avoidNull(mnemonic) );
		}
	}


	public void setLanguageFilter(Collection<String> availableLanguageCodes) {
		languageCombo.setFilter(availableLanguageCodes);
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
		if (languageCombo != null && !languageCombo.isDisposed()) {
			languageCombo.setLanguageCode(language);
		}
	}


	public File getFile() {
		return file;
	}


	public void setFile(File file) {
		this.file = file;
		if (fileSelectionComposite != null && !fileSelectionComposite.isDisposed()) {
			fileSelectionComposite.setFile(file);
		}
	}

}

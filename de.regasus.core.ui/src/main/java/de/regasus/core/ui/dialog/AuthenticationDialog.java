package de.regasus.core.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.regasus.core.ui.CoreI18N;

public class AuthenticationDialog extends TitleAreaDialog {

	private Text userNameText;
	private Text passwordText;
	
	private String userName = ""; 
	private String password = ""; 
	
	private String title = CoreI18N.AuthenticationDialog_Title;
	private String message = CoreI18N.AuthenticationDialog_InitialMessage;

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public AuthenticationDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		// Set the title
		super.setTitle(title);

		// Set the message
		super.setMessage(message);

		
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		
		final Label userNameLabel = new Label(container, SWT.NONE);
		userNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		userNameLabel.setText(CoreI18N.AuthenticationDialog_UserNameLabel);

		userNameText = new Text(container, SWT.BORDER);
		final GridData gd_userNameText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_userNameText.widthHint = 100;
		userNameText.setLayoutData(gd_userNameText);

		final Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		passwordLabel.setText(CoreI18N.AuthenticationDialog_PasswordLabel);

		passwordText = new Text(container, SWT.PASSWORD | SWT.BORDER);
		final GridData gd_passwordText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_passwordText.widthHint = 100;
		passwordText.setLayoutData(gd_passwordText);

		//
		
		
		// initialize widgets
		
		/* Nach Erzeugen des Dialog über den Konstruktor existieren 
		 * die Widgets noch nicht, da createDialogArea() erst später 
		 * (nach einem Aufruf von Dialog.open()) aufgerufen wird.
		 * Falls nach dem Erzeugen und dem Aufruf von open() Werte
		 * gesetzt wurden, müssen diese jetzt übernommen werden.
		 */
		userNameText.setText(userName);
		passwordText.setText(password);
		
		container.setTabList(new Control[] {
			userNameText, 
			passwordText, 
		});
		
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

//	/**
//	 * Return the initial size of the dialog
//	 */
//	@Override
//	protected Point getInitialSize() {
//		return new Point(285, 236);
//	}

	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String value) {
		if (value == null) {
			value = ""; 
		}
		userName = value;
		if (userNameText != null && ! userNameText.isDisposed()) {
			userNameText.setText(userName);
		}
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String value) {
		if (value == null) {
			value = ""; 
		}
		password = value;
		if (passwordText != null && ! passwordText.isDisposed()) {
			passwordText.setText(password);
		}
	}


	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// save data from widgets
			userName = userNameText.getText();
			password = passwordText.getText();
		}
		super.buttonPressed(buttonId);
	}

	
	public void setTitle(String title) {
		this.title = title;
	}

	
	public void setMessage(String message) {
		this.message = message;
	}

	
	public void setInitialMessage() {
		setMessage(CoreI18N.AuthenticationDialog_InitialMessage);
	}
	
	
	public void setFailedMessage() {
		setMessage(CoreI18N.AuthenticationDialog_FailedMessage);
	}

}

package de.regasus.core.ui.dialog;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.util.StringHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class LoginDialog extends Dialog {

	private Image[] images;

	private Text userNameText;
	private Button rememberUserNameButton;
	private Text passwordText;
	private Button rememberPasswordButton;
	private Text hostText;
	private Button rememberHostButton;
	private Button automaticLoginButton;

	private String userName = "";
	private String password = "";
	private String host = "";
	private boolean rememberUserName = false;
	private boolean rememberPassword = false;
	private boolean rememberHost = false;
	private boolean automaticLogin = false;


	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public LoginDialog(Shell parentShell) {
		super(parentShell);
	}


	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String productName = null;
		IProduct product = Platform.getProduct();
		if (product != null) {
			// Get Product Name from Product Configuration
			productName = product.getName();

			// Getting Image from Product Configuration
			String bundleId = product.getDefiningBundle().getSymbolicName();
			String[] imageURLs = parseCSL(product.getProperty(IProductConstants.WINDOW_IMAGES));
			if (imageURLs != null && imageURLs.length > 0) {
				String url = null;
				try {
					images = new Image[imageURLs.length];
					for (int i = 0; i < imageURLs.length; i++) {
						url = imageURLs[i];
						ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(bundleId, url);
						images[i] = descriptor.createImage(true);
					}
					newShell.setImages(images);
				}
				catch (Exception e) {
					System.err.println("The image could not be created. Maybe the following URL could not be found: " + url);
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}

		// Set Dialog Title
		String dialogTitle = null;
		if (productName != null) {
			dialogTitle = productName + " " + CoreI18N.LoginDialog_Title;
		}
		else {
			dialogTitle = CoreI18N.LoginDialog_Title;
		}
		newShell.setText(dialogTitle);
	}


	@Override
	public boolean close() {
		boolean result = super.close();

		// dispose images
		if (images != null) {
			for (int i = 0; i < images.length; i++) {
				Image image = images[i];
				if (image != null) {
					image.dispose();
				}
			}
		}


		return result;
	}


	public static String[] parseCSL(String csl) {
		if (csl == null)
			return null;

		StringTokenizer tokens = new StringTokenizer(csl, ",");
		ArrayList<String> tokenList = new ArrayList<String>();
		while (tokens.hasMoreTokens())
			tokenList.add(tokens.nextToken().trim());

		return tokenList.toArray(new String[tokenList.size()]);
	}


	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		container.setLayout(gridLayout);

		final Label userNameLabel = new Label(container, SWT.NONE);
		userNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		userNameLabel.setText(CoreI18N.LoginDialog_UserNameLabel);

		userNameText = new Text(container, SWT.BORDER);
		final GridData gd_userNameText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_userNameText.widthHint = 100;
		userNameText.setLayoutData(gd_userNameText);

		rememberUserNameButton = new Button(container, SWT.CHECK);
		rememberUserNameButton.setText(CoreI18N.LoginDialog_RememberUserNameButton);

		final Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		passwordLabel.setText(CoreI18N.LoginDialog_PasswordLabel);

		/* Use normal Text widget with style SWT.PASSWORD to make the input unreadable.
		 * It is not necessary to reject invalid characters here, because this password field is to check only.
		 * It won't be persisted.
		 */
		passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		final GridData gd_passwordText = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_passwordText.widthHint = 100;
		passwordText.setLayoutData(gd_passwordText);

		rememberPasswordButton = new Button(container, SWT.CHECK);
		rememberPasswordButton.setText(CoreI18N.LoginDialog_RememberPasswordButton);

		final Label hostLabel = new Label(container, SWT.NONE);
		hostLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		hostLabel.setText(CoreI18N.LoginDialog_HostLabel);

		hostText = new Text(container, SWT.BORDER);
		final GridData gd_host = new GridData(SWT.FILL, SWT.CENTER, true, false);
		hostText.setLayoutData(gd_host);

		rememberHostButton = new Button(container, SWT.CHECK);
		rememberHostButton.setText(CoreI18N.LoginDialog_RememberHostButton);
		new Label(container, SWT.NONE);

		automaticLoginButton = new Button(container, SWT.CHECK);
		automaticLoginButton.setText(CoreI18N.LoginDialog_AutomaticLoginButton);

		new Label(container, SWT.NONE);

		// Enable the OK-Button only if there is a user name entered.
		// AND if there is also a password entered (MIRCP-923)
		ModifyListener textFieldModificationListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setOkButtonEnabledState();
			}
		};
		userNameText.addModifyListener(textFieldModificationListener);
		passwordText.addModifyListener(textFieldModificationListener);

		// Switch Automatic Login off, when Remember User Name is turned off.
		rememberUserNameButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( ! rememberUserNameButton.getSelection()) {
					automaticLoginButton.setSelection(false);
				}
			}
		});

		// Switch Automatic Login off, when Remember Password is turned off.
		rememberPasswordButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( ! rememberPasswordButton.getSelection()) {
					automaticLoginButton.setSelection(false);
				}
			}
		});

		// Switch Automatic Login off, when Remember Host is turned off.
		rememberHostButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ( ! rememberHostButton.getSelection()) {
					automaticLoginButton.setSelection(false);
				}
			}
		});

		// Switch Remember Buttons on, when Automatic Login is turned on.
		automaticLoginButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (automaticLoginButton.getSelection()) {
					rememberUserNameButton.setSelection(true);
					rememberPasswordButton.setSelection(true);
					rememberHostButton.setSelection(true);
				}
			}
		});

		// initialize widgets

		/* Nach Erzeugen des Dialog über den Konstruktor existieren
		 * die Widgets noch nicht, da createDialogArea() erst später
		 * (nach einem Aufruf von Dialog.open()) aufgerufen wird.
		 * Falls nach dem Erzeugen und dem Aufruf von open() Werte
		 * gesetzt wurden, müssen diese jetzt übernommen werden.
		 */
		userNameText.setText(userName);
		passwordText.setText(password);
		hostText.setText(host);
		rememberUserNameButton.setSelection(rememberUserName);
		rememberPasswordButton.setSelection(rememberPassword);
		rememberHostButton.setSelection(rememberHost);
		automaticLoginButton.setSelection(automaticLogin);

		// Tabulatorreihenfolge setzen
		container.setTabList(new Control[] {
			userNameText,
			passwordText,
			hostText,
			automaticLoginButton,
			rememberUserNameButton,
			rememberPasswordButton,
			rememberHostButton
		});

		activateLoginAdminShortcut();

		return container;
	}


	private void activateLoginAdminShortcut() {
		userNameText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if ((e.stateMask & SWT.COMMAND) != 0 && e.character == 'a') {
					userNameText.setText("admin");
					passwordText.setText("admin");
					hostText.setText("http://localhost:8080");
					e.doit = false;
				}
			}
		});

	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		setOkButtonEnabledState();
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


	public String getHost() {
		return host;
	}


	public void setHost(String value) {
		if (value == null) {
			value = "";
		}
		host = value;
		if (hostText != null && ! hostText.isDisposed()) {
			hostText.setText(host);
		}
	}


	public boolean isRememberUserName() {
		return rememberUserName;
	}


	public void setRememberUserName(boolean value) {
		rememberUserName = value;
		if (rememberUserNameButton != null && ! rememberUserNameButton.isDisposed()) {
			rememberUserNameButton.setSelection(rememberUserName);
		}
	}


	public boolean isRememberPassword() {
		return rememberPassword;
	}


	public void setRememberPassword(boolean value) {
		rememberPassword = value;
		if (rememberPasswordButton != null && ! rememberPasswordButton.isDisposed()) {
			rememberPasswordButton.setSelection(rememberPassword);
		}
	}


	public boolean isRememberHost() {
		return rememberHost;
	}


	public void setRememberHost(boolean value) {
		rememberHost = value;
		if (rememberHostButton != null && ! rememberHostButton.isDisposed()) {
			rememberHostButton.setSelection(rememberHost);
		}
	}


	public boolean isAutomaticLogin() {
		return automaticLogin;
	}


	public void setAutomaticLogin(boolean value) {
		automaticLogin = value;
		if (automaticLoginButton != null && ! automaticLoginButton.isDisposed()) {
			automaticLoginButton.setSelection(automaticLogin);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			// save data from widgets
			userName = userNameText.getText();
			password = passwordText.getText();
			host = hostText.getText();
			rememberUserName = rememberUserNameButton.getSelection();
			rememberPassword = rememberPasswordButton.getSelection();
			rememberHost = rememberHostButton.getSelection();
			automaticLogin = automaticLoginButton.getSelection();
		}
		super.buttonPressed(buttonId);
	}


	private void setOkButtonEnabledState() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null && !okButton.isDisposed()) {
			String userName = userNameText.getText();
			String password = passwordText.getText();

			boolean enabled = StringHelper.isNotEmpty(userName) && StringHelper.isNotEmpty(password);

			okButton.setEnabled(enabled);
		}
	}

}

package de.regasus.portal.dialog;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.Collection;
import java.util.List;

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

import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.i18n.LanguageWidget;
import com.lambdalogic.util.rcp.validation.VerifyAdapter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.core.validation.MnemonicValidator;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalType;
import de.regasus.portal.type.combo.PortalTypeCombo;
import de.regasus.ui.Activator;


public class CreatePortalDialog extends TitleAreaDialog {

	private boolean eventDependent;

	private PortalType portalType;
	private String mnemonic;
	private String name;
	private List<String> languageIds;


	// widgets
	private Button okButton;

	private PortalTypeCombo portalTypeCombo;
	private Text mnemonicText;
	private Text nameText;
	private LanguageWidget languageWidget;



	public CreatePortalDialog(Shell shell, boolean eventDependent) {
		super(shell);
		setShellStyle(getShellStyle()  | SWT.RESIZE );

		this.eventDependent = eventDependent;
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(I18N.CreatePortalDialog_Title);
		setMessage(I18N.CreatePortalDialog_Message);

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite mainComposite = new Composite(dialogArea, SWT.NONE);
		mainComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );
		final int COL_COUNT = 2;
		mainComposite.setLayout(new GridLayout(COL_COUNT, false));

		try {
			/*** portal type ***/
			SWTHelper.createLabel(mainComposite, Portal.PORTAL_TYPE.getString(), true);

			portalTypeCombo = new PortalTypeCombo(mainComposite, SWT.BORDER, eventDependent);
    		portalTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    		SWTHelper.makeBold(portalTypeCombo);
    		portalTypeCombo.setWithEmptyElement(false);
    		portalTypeCombo.addModifyListener(modifyListener);
    		// select first
    		Collection<PortalType> portalTypeList = portalTypeCombo.getEntities();
    		if ( notEmpty(portalTypeList) ) {
    			portalTypeCombo.setPortalType( portalTypeList.iterator().next() );
    		}


    		/*** mnemonic ***/
    		SWTHelper.createLabel(mainComposite, Portal.MNEMONIC.getString(), true);

    		mnemonicText = new Text(mainComposite, SWT.BORDER);
    		mnemonicText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    		SWTHelper.makeBold(mnemonicText);
    		mnemonicText.setTextLimit( Portal.MNEMONIC.getMaxLength() );
    		mnemonicText.addVerifyListener(new VerifyAdapter(new MnemonicValidator()));
    		mnemonicText.addModifyListener(modifyListener);


    		/*** name ***/
    		SWTHelper.createLabel(mainComposite, Portal.NAME.getString(), true);

    		nameText = new Text(mainComposite, SWT.BORDER);
    		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    		SWTHelper.makeBold(nameText);
    		nameText.setTextLimit( Portal.NAME.getMaxLength() );
    		nameText.addModifyListener(modifyListener);


    		/*** languages ***/
    		SWTHelper.createLabel(mainComposite, Portal.LANGUAGES.getString(), true);

    		languageWidget = new LanguageWidget(mainComposite, SWT.NONE, LanguageProvider.getInstance());
    		languageWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    		languageWidget.addModifyListener(modifyListener);
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
			   portalType != null
			&& isNotEmpty(mnemonic)
			&& isNotEmpty(name)
			&& CollectionsHelper.notEmpty(languageIds);

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
		portalType = portalTypeCombo.getPortalType();
		mnemonic = mnemonicText.getText();
		name = nameText.getText();
		languageIds = languageWidget.getLanguageCodeList();
	}



	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK) {
			syncFieldsToWidgets();
		}
		super.buttonPressed(buttonId);
	}


	public PortalType getPortalType() {
		return portalType;
	}


	public String getMnemonic() {
		return mnemonic;
	}


	public String getName() {
		return name;
	}


	public List<String> getLanguageIds() {
		return languageIds;
	}

}

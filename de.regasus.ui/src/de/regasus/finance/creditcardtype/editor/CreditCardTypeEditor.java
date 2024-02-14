package de.regasus.finance.creditcardtype.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.CreditCard;
import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.finance.FinanceI18N;
import de.regasus.ui.Activator;

public class CreditCardTypeEditor extends AbstractEditor<CreditCardTypeEditorInput> implements IRefreshableEditorPart,
	CacheModelListener<Integer> {

	public static final String ID = "CreditCardTypeEditor";

	// the entity
	private com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO creditCardTypeVO;

	// the model
	private de.regasus.core.CreditCardTypeModel creditCardTypeModel;

	// **************************************************************************
	// * Widgets
	// *

	private Text nameText;

	private Text mnemonicText;

	private NullableSpinner lengthSpinner;

	private Text datatransPaymentMethodText;

	private Text payEngineBrandText;


	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get models
		creditCardTypeModel = CreditCardTypeModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			creditCardTypeVO = creditCardTypeModel.getCreditCardTypeVO(key);

			// register at model
			creditCardTypeModel.addListener(this, key);
		}
		else {
			// create empty entity
			creditCardTypeVO = new CreditCardTypeVO();
		}
	}


	@Override
	public void dispose() {
		if (creditCardTypeModel != null && creditCardTypeVO.getPK() != null) {
			try {
				creditCardTypeModel.removeListener(this, creditCardTypeVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(CreditCardTypeVO creditCardTypeVO) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		creditCardTypeVO = creditCardTypeVO.clone();
		}
		this.creditCardTypeVO = creditCardTypeVO;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return CreditCard.CREDIT_CARD_TYPE.getString();
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = SWTHelper.createScrolledContentComposite(parent);
			mainComposite.setLayout(new GridLayout(2, false));


			// Name
			final Label nameLabel = new Label(mainComposite, SWT.NONE);
			nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			nameLabel.setText(UtilI18N.Name);
			SWTHelper.makeBold(nameLabel);

			nameText = new Text(mainComposite, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			SWTHelper.makeBold(nameText);

			// Mnemonic
			Label mnemonicLabel = new Label(mainComposite, SWT.NONE);
			mnemonicLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			mnemonicLabel.setText(UtilI18N.Mnemonic);
			SWTHelper.makeBold(mnemonicLabel);

			mnemonicText = new Text(mainComposite, SWT.BORDER);
			mnemonicText.setTextLimit(CreditCardTypeVO.MAX_LENGTH_MNEMONIC);
			mnemonicText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			SWTHelper.makeBold(mnemonicText);

			// length
			Label lengthLabel = new Label(mainComposite, SWT.NONE);
			lengthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			lengthLabel.setText(UtilI18N.Length);
			SWTHelper.makeBold(lengthLabel);

			lengthSpinner = new NullableSpinner(mainComposite, SWT.BORDER, true /*required*/);
			lengthSpinner.setMinimum(0);
			lengthSpinner.setMaximum(CreditCardTypeVO.MAX_NO_LENGTH);
			lengthSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			WidgetSizer.setWidth(lengthSpinner);

			// Datatrans-Payment-Method
			Label datatransPaymentMethodLabel = new Label(mainComposite, SWT.NONE);
			datatransPaymentMethodLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			datatransPaymentMethodLabel.setText(ContactLabel.creditCardType_DatatransPaymentMethod.getString());
			datatransPaymentMethodLabel.setToolTipText(ContactLabel.creditCardType_DatatransPaymentMethod_description.getString());

			datatransPaymentMethodText = new Text(mainComposite, SWT.BORDER);
			datatransPaymentMethodText.setTextLimit(CreditCardTypeVO.MAX_LENGTH_DATATRANS_PAYMENT_METHOD);
			datatransPaymentMethodText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// PayEngine-Payment-Method
			Label payEngineBrandLabel = new Label(mainComposite, SWT.NONE);
			payEngineBrandLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			payEngineBrandLabel.setText(ContactLabel.creditCardType_PayEngineBrand.getString());
			payEngineBrandLabel.setToolTipText(ContactLabel.creditCardType_PayEngineBrand_description.getString());

			payEngineBrandText = new Text(mainComposite, SWT.BORDER);
			payEngineBrandText.setTextLimit(CreditCardTypeVO.MAX_LENGTH_PAY_ENGINE_BRAND);
			payEngineBrandText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


			// sync widgets and groups to the entity
			setEntity(creditCardTypeVO);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);

			SWTHelper.refreshSuperiorScrollbar(mainComposite);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				creditCardTypeVO = creditCardTypeModel.create(creditCardTypeVO);

				// observe the CountryModel
				creditCardTypeModel.addListener(this, creditCardTypeVO.getPK());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(creditCardTypeVO.getPK());

				// set new entity
				setEntity(creditCardTypeVO);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				creditCardTypeModel.update(creditCardTypeVO);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (creditCardTypeVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {

						nameText.setText(StringHelper.avoidNull(creditCardTypeVO.getName()));
						mnemonicText.setText(StringHelper.avoidNull(creditCardTypeVO.getMnemonic()));
						lengthSpinner.setValue(creditCardTypeVO.getNoLength());
						datatransPaymentMethodText.setText(StringHelper.avoidNull(creditCardTypeVO.getDatatransPaymentMethod()));
						payEngineBrandText.setText(StringHelper.avoidNull(creditCardTypeVO.getPayEngineBrand()));

						setPartName(creditCardTypeVO.getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						setDirty(false);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncEntityToWidgets() {
		if (creditCardTypeVO != null) {
			creditCardTypeVO.setName(nameText.getText());
			creditCardTypeVO.setMnemonic(StringHelper.trim(mnemonicText.getText()));
			creditCardTypeVO.setNoLength(lengthSpinner.getValueAsInteger());
			creditCardTypeVO.setDatatransPaymentMethod(StringHelper.trim(datatransPaymentMethodText.getText()));
			creditCardTypeVO.setPayEngineBrand(StringHelper.trim(payEngineBrandText.getText()));
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		nameText.addModifyListener(listener);
		mnemonicText.addModifyListener(listener);
		lengthSpinner.addModifyListener(listener);
		datatransPaymentMethodText.addModifyListener(listener);
		payEngineBrandText.addModifyListener(listener);
	}


	@Override
	public void dataChange(CacheModelEvent<Integer> event) {
		try {
			if (event.getSource() == creditCardTypeModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (creditCardTypeVO != null) {
					creditCardTypeVO = creditCardTypeModel.getCreditCardTypeVO(creditCardTypeVO.getPK());
					if (creditCardTypeVO != null) {
						setEntity(creditCardTypeVO);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (creditCardTypeVO != null && creditCardTypeVO.getPK() != null) {
			creditCardTypeModel.refresh(creditCardTypeVO.getPK());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				creditCardTypeVO = creditCardTypeModel.getCreditCardTypeVO(creditCardTypeVO.getID());
				if (creditCardTypeVO != null) {
					setEntity(creditCardTypeVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (creditCardTypeVO != null && creditCardTypeVO.getName() != null) {
			name = creditCardTypeVO.getName();
		}
		if (StringHelper.isEmpty(name)) {
			name = FinanceI18N.CreditCardType_Editor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return FinanceI18N.CreditCardType_Editor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return creditCardTypeVO.getPK() == null;
	}

	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.Name,
			UtilI18N.ID,
			UtilI18N.EditDateTime,
		};

		// the values of the info dialog
		final String[] values = {
			creditCardTypeVO.getName(),
			String.valueOf(creditCardTypeVO.getPK()),
			formatHelper.formatDateTime(creditCardTypeVO.getEditTime()),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			CreditCard.CREDIT_CARD_TYPE.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}

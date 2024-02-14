package de.regasus.finance.paymentsystem.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.lang.invoke.MethodHandles;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.ui.Activator;

public class PaymentSystemSetupEditor
extends AbstractEditor<PaymentSystemSetupEditorInput>
implements IRefreshableEditorPart {

	public static final String ID = MethodHandles.lookup().lookupClass().getSimpleName();

	// the entity
	private PaymentSystemSetup paymentSystemSetup;
	private PaymentSystem paymentSystem;

	// the model
	private PaymentSystemSetupModel paymentSystemSetupModel;

	// **************************************************************************
	// * Widgets
	// *

	private Text paymentSystemText;
	private Text nameText;
	private PayEngineSetupGroup payEngineSetupGroup;
	private EasyCheckoutSetupGroup easyCheckoutSetupGroup;

	// *
	// * Widgets
	// **************************************************************************


	private CacheModelListener<Long> paymentSystemSetupModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (event.getOperation() == CacheModelOperation.DELETE) {
				closeBecauseDeletion();
			}
			else if (paymentSystemSetup != null) {
				paymentSystemSetup = paymentSystemSetupModel.getPaymentSystemSetup( paymentSystemSetup.getId() );
				if (paymentSystemSetup != null) {
					setEntity(paymentSystemSetup);
				}
				else if (ServerModel.getInstance().isLoggedIn()) {
					closeBecauseDeletion();
				}
			}
		}
	};


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		final Long key = editorInput.getKey();

		// get models
		paymentSystemSetupModel = PaymentSystemSetupModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// load entity
			paymentSystemSetup = paymentSystemSetupModel.getPaymentSystemSetup(key);
			paymentSystem = paymentSystemSetup.getPaymentSystem();

			// register at model
			paymentSystemSetupModel.addListener(paymentSystemSetupModelListener, key);
		}
		else {
			paymentSystem = editorInput.getPaymentSystem();

			// create empty entity
			paymentSystemSetup = new PaymentSystemSetup();
			paymentSystemSetup.setPaymentSystem(paymentSystem);
		}
	}


	@Override
	public void dispose() {
		if (paymentSystemSetupModel != null && paymentSystemSetup.getId() != null) {
			try {
				paymentSystemSetupModel.removeListener(paymentSystemSetupModelListener, paymentSystemSetup.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(PaymentSystemSetup paymentSystemSetup) {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		paymentSystemSetup = paymentSystemSetup.clone();
		}
		this.paymentSystemSetup = paymentSystemSetup;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return InvoiceLabel.PaymentSystemSetup.getString();
	}

//	@Override
//	protected String getInfoButtonToolTipText() {
//		return FinanceI18N.ProgrammePointEditor_InfoButtonToolTip;
//	}

	/**
	 * Create contents of the editor part
	 *
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;


			final int numColumns = 2;
			parent.setLayout(new GridLayout(numColumns, false));

			// Payment System
			{
				Label label = new Label(parent, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText( PaymentSystemSetup.PAYMENT_SYSTEM.getString() );

				paymentSystemText = new Text(parent, SWT.BORDER);
				paymentSystemText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				SWTHelper.disableTextWidget(paymentSystemText);
			}

			// Name
			{
				Label label = new Label(parent, SWT.NONE);
				label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				label.setText( PaymentSystemSetup.NAME.getString() );
				SWTHelper.makeBold(label);

				nameText = new Text(parent, SWT.BORDER);
				nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				nameText.setTextLimit( PaymentSystemSetup.NAME.getMaxLength() );
				SWTHelper.makeBold(nameText);
			}

			SWTHelper.verticalSpace(parent);

			// Setup Data
			if (paymentSystem == PaymentSystem.PAYENGINE) {
				payEngineSetupGroup = new PayEngineSetupGroup(parent, SWT.NONE);
				GridDataFactory.fillDefaults().span(numColumns, 1).applyTo(payEngineSetupGroup);
			}
			else if (paymentSystem == PaymentSystem.EASY_CHECKOUT) {
				easyCheckoutSetupGroup = new EasyCheckoutSetupGroup(parent, SWT.NONE);
				GridDataFactory.fillDefaults().span(numColumns, 1).applyTo(easyCheckoutSetupGroup);
			}


			// sync widgets and groups to the entity
			setEntity(paymentSystemSetup);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListenerToWidgets();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void addModifyListenerToWidgets() {
		nameText.addModifyListener(this);
		if (payEngineSetupGroup != null) {
			payEngineSetupGroup.addModifyListener(this);
		}
		if (easyCheckoutSetupGroup != null) {
			easyCheckoutSetupGroup.addModifyListener(this);
		}
	}


	@Override
	public void setFocus() {
		nameText.setFocus();
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
				paymentSystemSetup = paymentSystemSetupModel.create(paymentSystemSetup);

				create = false;

				// observe the CountryModel
				paymentSystemSetupModel.addListener(paymentSystemSetupModelListener, paymentSystemSetup.getId());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(paymentSystemSetup.getId());

				// set new entity
				setEntity(paymentSystemSetup);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				paymentSystemSetupModel.update(paymentSystemSetup);
			}

			monitor.worked(1);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (paymentSystemSetup != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						paymentSystemText.setText( avoidNull(paymentSystemSetup.getPaymentSystem().getString()) );
						nameText.setText( avoidNull(paymentSystemSetup.getName()) );
						if (payEngineSetupGroup != null) {
							payEngineSetupGroup.setEntity( paymentSystemSetup.getPayEngineSetup() );
						}
						if (easyCheckoutSetupGroup != null) {
							easyCheckoutSetupGroup.setEntity( paymentSystemSetup.getEasyCheckoutSetup() );
						}


						// set editor title
						setPartName( getName() );
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName( getName() );
						editorInput.setToolTipText( getToolTipText() );

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
		if (paymentSystemSetup != null) {
			paymentSystemSetup.setName( nameText.getText() );

			String xml = null;
			if (payEngineSetupGroup != null) {
				payEngineSetupGroup.syncEntityToWidgets();
				xml = payEngineSetupGroup.getEntity().toXML();
			}
			else if (easyCheckoutSetupGroup != null) {
				easyCheckoutSetupGroup.syncEntityToWidgets();
				xml = easyCheckoutSetupGroup.getEntity().toXML();
			}
			paymentSystemSetup.setSetupData(xml);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (paymentSystemSetup != null && paymentSystemSetup.getId() != null) {
			paymentSystemSetupModel.refresh( paymentSystemSetup.getId() );

			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				paymentSystemSetup = paymentSystemSetupModel.getPaymentSystemSetup( paymentSystemSetup.getId() );
				if (paymentSystemSetup != null) {
					setEntity(paymentSystemSetup);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;

		if (paymentSystemSetup != null && paymentSystemSetup.getName() != null) {
			name = paymentSystemSetup.getName();
		}

		if (StringHelper.isEmpty(name)) {
			name = FinanceI18N.PaymentSystemSetup_Editor_NewName;
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		return FinanceI18N.PaymentSystemSetup_Editor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return paymentSystemSetup.getId() == null;
	}

	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};


		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(paymentSystemSetup.getId()),
			paymentSystemSetup.getNewTime().getString(),
			paymentSystemSetup.getNewDisplayUserStr(),
			paymentSystemSetup.getEditTime().getString(),
			paymentSystemSetup.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			InvoiceLabel.PaymentSystemSetup.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}

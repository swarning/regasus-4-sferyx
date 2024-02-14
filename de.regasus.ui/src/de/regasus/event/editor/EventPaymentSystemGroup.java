package de.regasus.event.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.FinanceI18N;
import de.regasus.finance.PaymentSystem;
import de.regasus.finance.paymentsystem.combo.EasyCheckoutSetupCombo;
import de.regasus.finance.paymentsystem.combo.PayEngineSetupCombo;
import de.regasus.ui.Activator;

public class EventPaymentSystemGroup extends Group {

	// the entity
	private EventVO eventVO;

	private boolean withPayEngine;
	private boolean withEasyCheckout;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private StackLayout stackLayout;
	private Composite stackComposite;

	// Buttons for different Payment Systems
	private Button paymentSystemNoneButton;
	private Button paymentSystemDatatransButton;
	private Button paymentSystemPayEngineButton;
	private Button paymentSystemEasyCheckoutButton;

	// Widgets if no Payment System is selected
	private Composite noneComposite;

	// Widgets for PaymentSystem.DATATRANS
	private Composite datatransComposite;
	private NullableSpinner datatransMerchantIdSpinner;

	// Widgets for PaymentSystem.PAYENGINE
	private Composite payEngineComposite;
	private PayEngineSetupCombo payEngineSetupCombo;
	private PayEngineSetupCombo payEngineSetupWwwCombo;

	// Widgets for PaymentSystem.EASY_CHECKOUT
	private Composite easyCheckoutComposite;
	private EasyCheckoutSetupCombo easyCheckoutSetupCombo;

	// *
	// * Widgets
	// **************************************************************************


	public EventPaymentSystemGroup(Composite parent, int style) throws Exception {
		super(parent, style);

		/* init withPayEngine
		 * Event is not necessary because settings about invoice details are global only.
		 */
		ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();
		ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet();
		withPayEngine = configParameterSet.getInvoiceDetails().getPayEngine().isVisible();
		withEasyCheckout = configParameterSet.getInvoiceDetails().getEasyCheckout().isVisible();

		createWidgets();
	}


	private void createWidgets() throws Exception {
		setText( ParticipantLabel.Event_PaymentSystem.getString() );

		final int numColumns = 2;
		setLayout( new GridLayout(numColumns, false) );

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);


		{
			Label label = new Label(this, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(I18N.EventEditor_FinanceTab_PaymentProvider);
		}
		{
			Composite paymentSystemComposite = new Composite(this, SWT.NONE);
			widgetGridDataFactory.applyTo(paymentSystemComposite);
			paymentSystemComposite.setLayout(new RowLayout());
			{
				paymentSystemNoneButton = new Button(paymentSystemComposite, SWT.RADIO);
				paymentSystemNoneButton.setText(UtilI18N.None);
			}
			{
				paymentSystemDatatransButton = new Button(paymentSystemComposite, SWT.RADIO);
				paymentSystemDatatransButton.setText( PaymentSystem.DATATRANS.getString() );
			}

			if (withPayEngine) {
				paymentSystemPayEngineButton = new Button(paymentSystemComposite, SWT.RADIO);
				paymentSystemPayEngineButton.setText( PaymentSystem.PAYENGINE.getString() );
			}

			if (withEasyCheckout) {
				paymentSystemEasyCheckoutButton = new Button(paymentSystemComposite, SWT.RADIO);
				paymentSystemEasyCheckoutButton.setText( PaymentSystem.EASY_CHECKOUT.getString() );
			}
		}

		stackLayout = new StackLayout();
		stackComposite = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).span(numColumns, 1).applyTo(stackComposite);
		stackComposite.setLayout(stackLayout);


		noneComposite = new Composite(stackComposite, SWT.NONE);

		// Datatrans Fields
		datatransComposite = new Composite(stackComposite, SWT.NONE);
		datatransComposite.setLayout(new GridLayout(2, false));
		{
			Label label = new Label(datatransComposite, SWT.NONE);
			labelGridDataFactory.applyTo(label);
			label.setText(ParticipantLabel.Event_DatatransMerchantId.getString());

			datatransMerchantIdSpinner= new NullableSpinner(datatransComposite, SWT.BORDER);
			datatransMerchantIdSpinner.setMinimum(0);
			widgetGridDataFactory.applyTo(datatransMerchantIdSpinner);
		}


		// PayEngine Fields
		if (withPayEngine) {
			payEngineComposite = new Composite(stackComposite, SWT.NONE);
			payEngineComposite.setLayout(new GridLayout(2, false));
			{
				Label label = new Label(payEngineComposite, SWT.NONE);
				labelGridDataFactory.applyTo(label);
				label.setText(ParticipantLabel.Event_PayEnginePSPIDOffice.getString());
				label.setToolTipText(ParticipantLabel.Event_PayEnginePSPIDOffice_Desc.getString());

				payEngineSetupCombo = new PayEngineSetupCombo(payEngineComposite, SWT.BORDER);
				widgetGridDataFactory.applyTo(payEngineSetupCombo);
			}
			{
				Label label = new Label(payEngineComposite, SWT.NONE);
				labelGridDataFactory.applyTo(label);
				label.setText(ParticipantLabel.Event_PayEnginePSPIDWww.getString());
				label.setToolTipText(ParticipantLabel.Event_PayEnginePSPIDWww_Desc.getString());

				payEngineSetupWwwCombo = new PayEngineSetupCombo(payEngineComposite, SWT.BORDER);
				widgetGridDataFactory.applyTo(payEngineSetupWwwCombo);
			}
		}


		// EASY Checkout Fields
		if (withEasyCheckout) {
			easyCheckoutComposite = new Composite(stackComposite, SWT.NONE);
			easyCheckoutComposite.setLayout(new GridLayout(2, false));
			{
				Label label = new Label(easyCheckoutComposite, SWT.NONE);
				labelGridDataFactory.applyTo(label);
				label.setText(FinanceI18N.EasyCheckoutSetup);
				label.setToolTipText(ParticipantLabel.Event_PayEnginePSPIDOffice_Desc.getString());

				easyCheckoutSetupCombo = new EasyCheckoutSetupCombo(easyCheckoutComposite, SWT.BORDER);
				widgetGridDataFactory.applyTo(easyCheckoutSetupCombo);
			}
		}

		addModifyListenerToWidgets();
	}


	private void addModifyListenerToWidgets() {
		// Buttons to select the Payment System
		paymentSystemNoneButton.addSelectionListener(selectionListener);
		paymentSystemNoneButton.addSelectionListener(modifySupport);

		paymentSystemDatatransButton.addSelectionListener(selectionListener);
		paymentSystemDatatransButton.addSelectionListener(modifySupport);

		if (withPayEngine) {
    		paymentSystemPayEngineButton.addSelectionListener(selectionListener);
    		paymentSystemPayEngineButton.addSelectionListener(modifySupport);
		}

		if (withEasyCheckout) {
			paymentSystemEasyCheckoutButton.addSelectionListener(selectionListener);
			paymentSystemEasyCheckoutButton.addSelectionListener(modifySupport);
		}


		// widgets of the several Payment Systems
		datatransMerchantIdSpinner.addModifyListener(modifySupport);

		if (withPayEngine) {
    		payEngineSetupCombo.addModifyListener(modifySupport);
    		payEngineSetupWwwCombo.addModifyListener(modifySupport);
		}

		if (withEasyCheckout) {
			easyCheckoutSetupCombo.addModifyListener(modifySupport);
		}
	}


	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!ModifySupport.isDeselectedRadioButton(event)) {
				handlePaymentSystemSelection();
			}
		}
	};


	private void handlePaymentSystemSelection() {
		if (paymentSystemNoneButton.getSelection()) {
			stackLayout.topControl = noneComposite;
		}
		else if (paymentSystemDatatransButton.getSelection()) {
			stackLayout.topControl = datatransComposite;
		}
		else if (withPayEngine && paymentSystemPayEngineButton.getSelection()) {
			stackLayout.topControl = payEngineComposite;
		}
		else if (withEasyCheckout && paymentSystemEasyCheckoutButton.getSelection()) {
			stackLayout.topControl = easyCheckoutComposite;
		}

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				stackComposite.layout();
			}
		});
	}


	public void setEvent(EventVO eventVO) {
		this.eventVO = eventVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (eventVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// Buttons to select the Payment System
						paymentSystemNoneButton.setSelection(eventVO.getPaymentSystem() == null);
						paymentSystemDatatransButton.setSelection( eventVO.getPaymentSystem() == PaymentSystem.DATATRANS );
						if (withPayEngine) {
							paymentSystemPayEngineButton.setSelection(eventVO.getPaymentSystem() == PaymentSystem.PAYENGINE);
						}
						if (withEasyCheckout) {
							paymentSystemEasyCheckoutButton.setSelection(eventVO.getPaymentSystem() == PaymentSystem.EASY_CHECKOUT);
						}


						// widgets of the several Payment Systems
						datatransMerchantIdSpinner.setValue( eventVO.getDatatransMerchantId() );

						if (withPayEngine) {
							payEngineSetupCombo.setPaymentSystemSetupId( eventVO.getPaymentSystemSetupPK() );
							payEngineSetupWwwCombo.setPaymentSystemSetupId( eventVO.getPaymentSystemSetupWwwPK() );
						}

						if (withEasyCheckout) {
							easyCheckoutSetupCombo.setPaymentSystemSetupId( eventVO.getPaymentSystemSetupPK() );
						}


						// init visibility of payment type widgets
						handlePaymentSystemSelection();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (eventVO != null) {
			// Buttons to select the Payment System
			PaymentSystem paymentSystem = null;
			if (paymentSystemDatatransButton.getSelection()) {
				paymentSystem = PaymentSystem.DATATRANS;
			}
			else if (withPayEngine && paymentSystemPayEngineButton.getSelection()) {
				paymentSystem = PaymentSystem.PAYENGINE;
			}
			else if (withEasyCheckout && paymentSystemEasyCheckoutButton.getSelection()) {
				paymentSystem = PaymentSystem.EASY_CHECKOUT;
			}
			eventVO.setPaymentSystem(paymentSystem);


			// widgets of the several Payment Systems
			eventVO.setDatatransMerchantId( datatransMerchantIdSpinner.getValue() );

			// set either the values for PayEngine or EASY Checkout, because both Payment Systems use the same fields of Event
			if (paymentSystem == PaymentSystem.PAYENGINE) {
				eventVO.setPaymentSystemSetupPK( payEngineSetupCombo.getPaymentSystemSetupId() );
				eventVO.setPaymentSystemSetupWwwPK( payEngineSetupWwwCombo.getPaymentSystemSetupId() );
			}
			else if (paymentSystem == PaymentSystem.EASY_CHECKOUT) {
				eventVO.setPaymentSystemSetupPK( easyCheckoutSetupCombo.getPaymentSystemSetupId() );
				eventVO.setPaymentSystemSetupWwwPK(null);
			}
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

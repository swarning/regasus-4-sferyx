package de.regasus.finance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import com.lambdalogic.messeinfo.invoice.data.ClearingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.core.ServerModel;
import de.regasus.event.EventModel;
import de.regasus.finance.invoice.command.IncreaseReminderLevelCommandHandler;
import de.regasus.finance.invoice.command.RestartReminderCommandHandler;
import de.regasus.finance.invoice.command.ShowReminderCommandHandler;
import de.regasus.finance.invoice.command.StopReminderCommandHandler;
import de.regasus.finance.invoice.view.InvoiceSearchView;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.finance.FinanceComposite;

/**
 * This class provides variables in a Map whose values depend on the selected invoices and payments
 * in the tables of {@link FinanceComposite} and {@link InvoiceSearchView}. It enables the
 * finance-related command handlers to formulate conditions under which they are active.
 */
public class FinanceSourceProvider
extends AbstractSourceProvider
implements IPartListener, SelectionListener, ISelectionChangedListener {

	private static final boolean DEBUG = false;

	// **************************************************************************
	// * Constants for map keys values that control individual command handlers
	// *

	public static final String YES = "yes";
	public static final String NO = "no";


	/**
	 * aktiv, wenn RG oder RG-Positionen selektiert sind (auch für bereits gedruckte RG)
	 */
	public static final String IS_ENABLED_CLEAR_INVOICE_POSITIONS = "de.regasus.event.finance.isEnabledClearInvoicePositions";

	/**
	 * aktiv, wenn RG oder RG-Positionen selektiert sind (auch für bereits gedruckte RG)
	 */
	public static final String IS_ENABLED_PRINT_INVOICE = "de.regasus.event.finance.isEnabledPrintInvoice";

	/**
	 * active if one Invoice is selected
	 */
	public static final String IS_ENABLED_GENERATE_SAMPLE_INVOICE = "de.regasus.event.finance.isEnabledGenerateSampleInvoice";

	/**
	 * aktiv, wenn offene RG oder RG-Positionen selektiert sind
	 */
	public static final String IS_ENABLED_CLOSE_INVOICE = "de.regasus.event.finance.isEnabledCloseInvoice";


	/**
	 * aktiv, wenn RG oder RG-Positionen selektiert sind (auch bei geschlossenen RG)
	 */
	public static final String IS_ENABLED_REFRESH_ADDRESS = "de.regasus.event.finance.isEnabledRefreshAddress";

	/**
	 * Is true if exactly 1 Payment is selected that is not cancelled and is no clearing.
	 */
	public static final String IS_ENABLED_EDIT_PAYMENT = "de.regasus.event.finance.isEnabledEditPayment";

	/**
	 * Is true if exactly 1 Payment is selected that is not cancelled and is no clearing
	 * and not electronic payment.
	 * Electronic payments cannot be cancelled, because the electronic payment definitely took place. If an
	 * electronic payment if refunded, another Payment about the refund is created instead.
	 */
	public static final String IS_ENABLED_CANCEL_PAYMENT = "de.regasus.event.finance.isEnabledCancelPayment";

	/**
	 * Is true if the Event's payment system is "PayEngine"
	 */
	public static final String IS_ENABLED_PAYENGINE_HISTORY = "de.regasus.event.finance.isEnabledPayEngineHistory";

	/**
	 * Is true if the Event's payment system is "EasyCheckout"
	 */
	public static final String IS_ENABLED_EASY_CHECKOUT_HISTORY = "de.regasus.event.finance.isEnabledEasyCheckoutHistory";

	/**
	 * aktiv, wenn Verrechnungen selektiert sind
	 */
	public static final String IS_ENABLED_CANCEL_CLEARING = "de.regasus.event.finance.isEnabledCancelClearing";

	/**
	 * aktiv, wenn eine gedruckte RG oder RG-Positionen selektiert ist, die nicht nicht vollständig bezahlt (offener  Betrag > 0) ist
	 */
	public static final String IS_ENABLED_INCREASE_REMINDER_LEVEL = "de.regasus.event.finance.isEnabledIncreaseReminderLevel";

	/**
	 * aktiv, wenn eine RG oder RG-Position mit einer Mahnstufe >= 1 selektiert sind
	 */
	public static final String IS_ENABLED_SHOW_REMINDER = "de.regasus.event.finance.isEnabledShowReminder";

	/**
	 * aktiv, wenn eine gedruckte RG oder RG-Position selektiert ist, für die ein Zahlungsziel festgelegt ist (nextReminder != null)
	 */
	public static final String IS_ENABLED_STOP_REMINDER = "de.regasus.event.finance.isEnabledStopReminder";

	/**
	 * aktiv, wenn eine gedruckte RG oder RG-Positionen selektiert ist, die nicht nicht vollständig bezahlt (offener Betrag > 0) ist
	 */
	public static final String IS_ENABLED_RESTART_REMINDER = "de.regasus.event.finance.isEnabledRestartReminder";

	/**
	 * aktiv, wenn ein positiver Zahlungseingang oder eine Verrechnung selektiert sind
	 */
	public static final String IS_ENABLED_SHOW_PAYMENT_RECEIPT = "de.regasus.event.finance.isEnabledShowPaymentReceipt";

	/**
	 * aktiv, wenn ein negativer Zahlungseingang oder eine Verrechnung selektiert sind
	 */
	public static final String IS_ENABLED_SHOW_REFUND_RECEIPT = "de.regasus.event.finance.isEnabledShowRefundReceipt";

	// **************************************************************************
	// * Attributes
	// *

	/**
	 * In the eclipse workbench, there is always at most one selected {@link IWorkbenchPart}; in case this part is the
	 * ParticipantEditor, it is referred to in the variable, otherwise it is null.
	 */
	private ParticipantEditor currentParticipantEditor = null;

	private InvoiceSearchView currentInvoiceSearchView = null;

	private Map<String, String> stateMap = new HashMap<>(4);

	private EventModel eventModel = EventModel.getInstance();

	private InvoiceNoRangeModel inrModel = InvoiceNoRangeModel.getInstance();

	// **************************************************************************
	// * Constructors
	// *

	/**
	 * This SourceProvider gets constructed before a workbench window is opened, so we need to be informed when that
	 * moment has come to be able to hook ourself into that window as part listener.
	 */
	public FinanceSourceProvider() {
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

			@Override
			public void windowOpened(IWorkbenchWindow window) {
			}


			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
				printIfVerbose("FinanceSourceProvider.windowDeactivated(IWorkbenchWindow))");
				printIfVerbose("	window.getPartService().removePartListener(FinanceSourceProvider.this)");
				window.getPartService().removePartListener(FinanceSourceProvider.this);
			}


			@Override
			public void windowClosed(IWorkbenchWindow window) {
			}


			@Override
			public void windowActivated(IWorkbenchWindow window) {
				printIfVerbose("FinanceSourceProvider.windowActivated(IWorkbenchWindow)");
				printIfVerbose("	window.getPartService().addPartListener(FinanceSourceProvider.this);");
				window.getPartService().addPartListener(FinanceSourceProvider.this);
				partActivated(window.getActivePage().getActivePart());
			}
		});

	}


	// **************************************************************************
	// * Methods implementing the rest of the ISourceProvider interface
	// *

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {
			IS_ENABLED_CLEAR_INVOICE_POSITIONS,
			IS_ENABLED_PRINT_INVOICE,
			IS_ENABLED_GENERATE_SAMPLE_INVOICE,
			IS_ENABLED_CLOSE_INVOICE,
			IS_ENABLED_REFRESH_ADDRESS,
			IS_ENABLED_EDIT_PAYMENT,
			IS_ENABLED_CANCEL_PAYMENT,
			IS_ENABLED_PAYENGINE_HISTORY,
			IS_ENABLED_EASY_CHECKOUT_HISTORY,
			IS_ENABLED_CANCEL_CLEARING,
			IS_ENABLED_INCREASE_REMINDER_LEVEL,
			IS_ENABLED_SHOW_REMINDER,
			IS_ENABLED_STOP_REMINDER,
			IS_ENABLED_RESTART_REMINDER,
			IS_ENABLED_SHOW_PAYMENT_RECEIPT,
			IS_ENABLED_SHOW_REFUND_RECEIPT,
		};
	}


	@Override
	public Map<?, ?> getCurrentState() {
		return stateMap;
	}


	@Override
	public void dispose() {
	}


	// **************************************************************************
	// * Methods implementing the IPartListener interface
	// *

	/**
	 * When a ParticipantEditor is deactivated, we need deregister as listener so that it can be garbage collected, and
	 * until further activation of any other ParticipantEditor there is no finance selection.
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		printIfVerbose("FinanceSourceProvider.partDeactivated(IWorkbenchPart)");
		if (currentParticipantEditor != null && currentParticipantEditor == part) {
			printIfVerbose("	currentParticipantEditor.getTabFolder().removeSelectionListener(this)");
			currentParticipantEditor.getTabFolder().removeSelectionListener(this);
			currentParticipantEditor.removeFinanceSelectionChangedListener(this);

			currentParticipantEditor = null;
			refreshVariables();
		}
		else if (currentInvoiceSearchView != null && currentInvoiceSearchView == part) {
			printIfVerbose("	currentInvoiceSearchView.removeSelectionListener(this)");
			currentInvoiceSearchView.removeSelectionListener(this);
			currentInvoiceSearchView = null;
			refreshVariables();
		}
	}


	/**
	 * When a ParticipantEditor is activated, we need to keep an eye on the selected tabs and the possible selections in
	 * the FinanceComposite and keep the {@link #stateMap} always in sync.
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		printIfVerbose("FinanceSourceProvider.partActivated(IWorkbenchPart part)");
		if (part instanceof ParticipantEditor) {
			printIfVerbose("	part instanceof ParticipantEditor");
			currentParticipantEditor = (ParticipantEditor) part;
			printIfVerbose("	currentParticipantEditor.getTabFolder().addSelectionListener(this)");
			currentParticipantEditor.getTabFolder().addSelectionListener(this);
			currentParticipantEditor.addFinanceSelectionChangedListener(this);
			refreshVariables();
		}
		else if (part instanceof InvoiceSearchView) {
			printIfVerbose("	part instanceof InvoiceSearchView");
			currentInvoiceSearchView = (InvoiceSearchView) part;
			printIfVerbose("	currentInvoiceSearchView.addSelectionListener(this)");
			currentInvoiceSearchView.addSelectionListener(this);
			refreshVariables();
		}
	}


	/**
	 * After a part is brought to top, it gets still activated, so no action needed
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}


	/**
	 * Before a part gets closed, it gets already deactivated, so no action needed
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
	}


	/**
	 * After a part is opened, it gets still activated, so no action needed
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
	}


	// **************************************************************************
	// * Methods of SelectionListener which get called when the selected tab of
	// * the tab folder of the current participant editor changes
	// *

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		printIfVerbose("FinanceSourceProvider.widgetSelected(SelectionEvent)");

		/*
		 * When a tab for a new unsaved participant is selected, even the get()-
		 * method above does NOT return a currentFinanceComposite
		 */
		currentParticipantEditor.addFinanceSelectionChangedListener(this);

		refreshVariables();
	}


	// **************************************************************************
	// * Method of ISelectionListener which gets called when the selection in the
	// * viewer in the finance composite changes
	// *

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		printIfVerbose("FinanceSourceProvider.selectionChanged(SelectionChangedEvent event)");
		refreshVariables();
	}


	public void refreshVariables() {
		printIfVerbose("FinanceSourceProvider.refreshVariables()");

		// init all variables with NO
		for (String key : getProvidedSourceNames()) {
			stateMap.put(key, NO);
		}

		if (! ServerModel.getInstance().isLoggedIn() ) {
			return;
		}

		// Check if participant editor or invoice search view is active at all
		if (currentParticipantEditor == null && currentInvoiceSearchView == null) {
			fireSourceChanged(ISources.WORKBENCH, stateMap);
			return;
		}

		Collection<InvoiceVO> invoiceVOs = Collections.emptyList();
		Collection<InvoicePositionVO> invoicePositionVOs = Collections.emptyList();
		Collection<PaymentVO> paymentVOs = Collections.emptyList();
		Collection<ClearingVO> paymentClearingVOs = Collections.emptyList();
		Collection<ClearingVO> invoicePositionClearingVOs = Collections.emptyList();

		// ============ The initialization for ParticipantEditor ================

		if (currentParticipantEditor != null) {
			printIfVerbose("	currentParticipantEditor != null");
			// Check if finance tab is the currently selected tab
			boolean financeTabVisible = false;
			TabItem[] selectedTabItems = currentParticipantEditor.getTabFolder().getSelection();
			if (selectedTabItems.length > 0 && selectedTabItems[0].getControl() instanceof FinanceComposite) {
				financeTabVisible = true;
			}
//			if (selectedTabItems.length == 0 || !(selectedTabItems[0].getControl() instanceof FinanceComposite)) {
//				printIfVerbose("	nothing is selected because: selectedTabItems.length == 0 || !(selectedTabItems[0].getControl() instanceof FinanceComposite)");
//				fireSourceChanged(ISources.WORKBENCH, stateMap);
//				return;
//			}

			// Finance tab is indeed visible, check the selection in the table

			if (financeTabVisible) {
				// We fetch the selected invoices and payments
				invoiceVOs = currentParticipantEditor.getSelectedInvoiceVOs();
				paymentVOs = currentParticipantEditor.getSelectedPaymentVOs();
				invoicePositionVOs = currentParticipantEditor.getSelectedInvoicePositionVOs();
				paymentClearingVOs = currentParticipantEditor.getSelectedPaymentClearingVOs();
				invoicePositionClearingVOs = currentParticipantEditor.getSelectedInvoicePositionClearingVOs();

				printIfVerbose("	invoiceVOs.size(): " + invoiceVOs.size());
				printIfVerbose("	paymentVOs.size(): " + paymentVOs.size());
				printIfVerbose("	invoicePositionVOs.size(): " + invoicePositionVOs.size());
				printIfVerbose("	paymentClearingVOs.size(): " + paymentClearingVOs.size());
				printIfVerbose("	invoicePositionClearingVOs.size(): " + invoicePositionClearingVOs.size());
			}
		}
		// ============ The initialization for InvoiceSearchView ================
		else if (currentInvoiceSearchView != null) {
			printIfVerbose("	currentInvoiceSearchView != null");
			invoiceVOs = currentInvoiceSearchView.getSelectedInvoiceVOs();
		}


		// set if exactly one PaymentVO is selected and nothing else
		PaymentVO paymentVO = null;
		if (paymentVOs != null &&
			paymentVOs.size() == 1 &&
			(invoiceVOs == null || invoiceVOs.isEmpty()) &&
			(invoicePositionVOs == null || invoicePositionVOs.isEmpty())
		) {
			paymentVO = paymentVOs.iterator().next();
		}


		// Now to the individual command handlers


		// IS_ENABLED_PRINT_INVOICE: true if Invoices or Invoice Positions are selected
		if (invoiceVOs != null && !invoiceVOs.isEmpty() ) {
			// IS_ENABLED_PRINT_INVOICE: true, if open Invoices or Invoice Positions are selected
			stateMap.put(IS_ENABLED_PRINT_INVOICE, YES);
			for (InvoiceVO invoiceVO : invoiceVOs) {
				if (!invoiceVO.isClosed()) {
					stateMap.put(IS_ENABLED_CLOSE_INVOICE, YES);
					break;
				}
			}


			// IS_ENABLED_GENERATE_SAMPLE_INVOICE: true, if one Invoice is selected
			stateMap.put(IS_ENABLED_GENERATE_SAMPLE_INVOICE, YES);
			if (invoiceVOs.size() == 1) {
				stateMap.put(IS_ENABLED_GENERATE_SAMPLE_INVOICE, YES);
			}


			/* IS_ENABLED_REFRESH_ADDRESS: true if Invoices or Invoice Positions are selected and all of their
			 * Invoice Number Ranges have audit-proof turned off.
			 */
			try {
				// collect Invoice No Range PKs of all Invoices
				Set<Long> invoiceNoRangePKs = new HashSet<>();
				for (InvoiceVO invoiceVO : invoiceVOs) {
					invoiceNoRangePKs.add(invoiceVO.getInvoiceNoRangePK());
				}
				List<InvoiceNoRangeCVO> invoiceNoRangeCVOs = inrModel.getInvoiceNoRangeCVOs(invoiceNoRangePKs);

				String enableRefreshAddress = YES;
				// set canUpdateAddress to false if at least one INR has audit-proof turned on
				for (InvoiceNoRangeCVO invoiceNoRangeCVO : invoiceNoRangeCVOs) {
					if (invoiceNoRangeCVO.getVO().isAuditProof()) {
						enableRefreshAddress = NO;
						break;
					}
				}

				stateMap.put(IS_ENABLED_REFRESH_ADDRESS, enableRefreshAddress);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}


		/* IS_ENABLED_EDIT_PAYMENT is true if exactly 1 Payment is selected that is not cancelled and is no clearing.
		 */
		if (paymentVO != null &&
			!paymentVO.isCanceled() &&
			paymentVO.getType() != PaymentType.CLEARING
		) {
			stateMap.put(IS_ENABLED_EDIT_PAYMENT, YES);
		}


		/* IS_ENABLED_CANCEL_PAYMENT is true if exactly 1 Payment is selected that is not cancelled and is no clearing
		 * and not electronic payment.
		 * Electronic payments should not be cancelled unless the user cancelled the payment in the system
		 * of the payment provider as well. For that case users have to be able to cancel such payments as well.
		 *
		 * If an electronic payment if refunded, another Payment about the refund is created instead.
		 */
		if (paymentVO != null &&
			!paymentVO.isCanceled() &&
			paymentVO.getType() != PaymentType.CLEARING
		) {
			stateMap.put(IS_ENABLED_CANCEL_PAYMENT, YES);
		}

		if (currentParticipantEditor != null && currentParticipantEditor.getEventId() != null) {
			try {
				EventVO eventVO = eventModel.getEventVO(currentParticipantEditor.getEventId());
				if (eventVO.getPaymentSystem() == PaymentSystem.PAYENGINE) {
					stateMap.put(IS_ENABLED_PAYENGINE_HISTORY, YES);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (currentParticipantEditor != null && currentParticipantEditor.getEventId() != null) {
			try {
				EventVO eventVO = eventModel.getEventVO(currentParticipantEditor.getEventId());
				if (eventVO.getPaymentSystem() == PaymentSystem.EASY_CHECKOUT) {
					stateMap.put(IS_ENABLED_EASY_CHECKOUT_HISTORY, YES);
				}
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		// IS_ENABLED_CANCEL_CLEARING true, wenn nur Verrechnungen selektiert sind
		if ((invoiceVOs == null || invoiceVOs.isEmpty()) &&
			(invoicePositionVOs == null || invoicePositionVOs.isEmpty()) &&
			(
				(paymentClearingVOs != null && !paymentClearingVOs.isEmpty())
				||
				(invoicePositionClearingVOs != null && !invoicePositionClearingVOs.isEmpty())
			)
		) {
			stateMap.put(IS_ENABLED_CANCEL_CLEARING, YES);
		}


		/* IS_ENABLED_CLEAR_INVOICE_POSITIONS
		 * true if all of the following conditions are met
		 * - only Invoice Positions are selected
		 * - at least 2 Invoice Positions are selected
		 * - all selected Invoice Positions have the same currency
		 */
		if (invoicePositionVOs != null &&
			invoicePositionVOs.size() >= 2 &&
			(paymentVOs == null || paymentVOs.isEmpty())
		) {
			boolean enable = true;

    		// assure that all IPs have the same currency

    		String currency = null;
    		for (InvoicePositionVO invoicePositionVO : invoicePositionVOs) {
				if (currency == null) {
					currency = invoicePositionVO.getCurrency();
				}
				else if (!currency.equals( invoicePositionVO.getCurrency() )) {
					enable = false;
					break;
				}
			}

    		if (enable) {
    			stateMap.put(IS_ENABLED_CLEAR_INVOICE_POSITIONS, YES);
    		}
		}


		if (invoiceVOs != null &&
			!invoiceVOs.isEmpty() &&
			(paymentVOs == null || paymentVOs.isEmpty())
		) {
			/* IS_ENABLED_INCREASE_REMINDER_LEVEL
			 * true if all of the following conditions are met
			 * - only Invoices are selected
			 * - all Invoices have a positive amount (are no credits)
			 * - all Invoices are printed
			 * - all Invoices do not have the reminderState STOP, PAID or LEVEL5
			 * - no Invoice is balanced
			 */
			if (IncreaseReminderLevelCommandHandler.getApplicableCount(invoiceVOs) > 0) {
				stateMap.put(IS_ENABLED_INCREASE_REMINDER_LEVEL, YES);
			}


			/* IS_ENABLED_SHOW_REMINDER
			 * true if all of the following conditions are met
			 * - only Invoices are selected
			 * - all Invoices are have positive amount (are no credits)
			 * - all Invoices are printed
			 * - all Invoices have a reminderState between LEVEL1 and LEVEL5
			 * - no Invoice is balanced
			 */
			if (ShowReminderCommandHandler.isApplicable(invoiceVOs)) {
				stateMap.put(IS_ENABLED_SHOW_REMINDER, YES);
			}


			/* IS_ENABLED_STOP_REMINDER
			 * true if all of the following conditions are met
			 * - only Invoices are selected
			 * - all Invoices are have positive amount (are no credits)
			 * - all Invoices are printed
			 * - all Invoices have a nextReminder
			 * - all Invoices do not have the reminderState STOP
			 */
			if (StopReminderCommandHandler.getApplicableCount(invoiceVOs) > 0) {
				stateMap.put(IS_ENABLED_STOP_REMINDER, YES);
			}


			/* IS_ENABLED_RESTART_REMINDER
			 * true if all of the following conditions are met
			 * - only Invoices are selected
			 * - all Invoices are have positive amount (are no credits)
			 * - all Invoices are printed
			 * - all Invoices have a nextReminder
			 * - all Invoices have the reminderState STOP
			 * - no Invoice is balanced
			 */
			if (RestartReminderCommandHandler.getApplicableCount(invoiceVOs) > 0) {
				stateMap.put(IS_ENABLED_RESTART_REMINDER, YES);
			}

		}


		/* IS_ENABLED_SHOW_PAYMENT_RECEIPT
		 * true if all of the following conditions are met
		 * - only 1 Payment with positive amount is selected
		 * - the selected Payment is not canceled
		 */
		if (paymentVO != null &&
			paymentVO.getAmount() != null &&
			!paymentVO.isCanceled() &&
			paymentVO.getAmount().signum() == 1
		) {
			stateMap.put(IS_ENABLED_SHOW_PAYMENT_RECEIPT, YES);
		}

		/* IS_ENABLED_SHOW_REFUND_RECEIPT
		 * true if all of the following conditions are met
		 * - only 1 Payment with negative amount is selected
		 * - the selected Payment is not canceled
		 */
		if (paymentVO != null &&
			paymentVO.getAmount() != null &&
			!paymentVO.isCanceled() &&
			paymentVO.getAmount().signum() == -1
		) {
			stateMap.put(IS_ENABLED_SHOW_REFUND_RECEIPT, YES);
		}

		printIfVerbose("	IS_ENABLED_CLEAR_INVOICE_POSITIONS: " + stateMap.get(IS_ENABLED_CLEAR_INVOICE_POSITIONS));
		printIfVerbose("	IS_ENABLED_PRINT_INVOICE: " + stateMap.get(IS_ENABLED_PRINT_INVOICE));
		printIfVerbose("	IS_ENABLED_CLOSE_INVOICE: " + stateMap.get(IS_ENABLED_CLOSE_INVOICE));
		printIfVerbose("	IS_ENABLED_REFRESH_ADDRESS: " + stateMap.get(IS_ENABLED_REFRESH_ADDRESS));
		printIfVerbose("	IS_ENABLED_CANCEL_PAYMENT: " + stateMap.get(IS_ENABLED_CANCEL_PAYMENT));
		printIfVerbose("	IS_ENABLED_PAYENGINE_HISTORY: " + stateMap.get(IS_ENABLED_PAYENGINE_HISTORY));
		printIfVerbose("	IS_ENABLED_CANCEL_CLEARING: " + stateMap.get(IS_ENABLED_CANCEL_CLEARING));
		printIfVerbose("	IS_ENABLED_CREATE_REMINDER: " + stateMap.get(IS_ENABLED_INCREASE_REMINDER_LEVEL));
		printIfVerbose("	IS_ENABLED_PRINT_REMINDER: " + stateMap.get(IS_ENABLED_SHOW_REMINDER));
		printIfVerbose("	IS_ENABLED_STOP_REMINDER: " + stateMap.get(IS_ENABLED_STOP_REMINDER));
		printIfVerbose("	IS_ENABLED_RESTART_REMINDER: " + stateMap.get(IS_ENABLED_RESTART_REMINDER));
		printIfVerbose("	IS_ENABLED_SHOW_PAYMENT_RECEIPT: " + stateMap.get(IS_ENABLED_SHOW_PAYMENT_RECEIPT));
		printIfVerbose("	IS_ENABLED_SHOW_REFUND_RECEIPT: " + stateMap.get(IS_ENABLED_SHOW_REFUND_RECEIPT));

		fireSourceChanged(ISources.WORKBENCH, stateMap);
	}



	public static FinanceSourceProvider getInstance() {
		FinanceSourceProvider sourceProvider = null;

		IWorkbench workbench = PlatformUI.getWorkbench();
		ISourceProviderService service = workbench.getService(ISourceProviderService.class);
		ISourceProvider[] sourceProviders = service.getSourceProviders();

		for (ISourceProvider iSourceProvider : sourceProviders) {
			if (iSourceProvider instanceof FinanceSourceProvider) {
				sourceProvider = (FinanceSourceProvider) iSourceProvider;
				break;
			}
		}

		return sourceProvider;
	}

	private void printIfVerbose(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

}

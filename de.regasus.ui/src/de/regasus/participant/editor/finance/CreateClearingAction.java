package de.regasus.participant.editor.finance;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.lambdalogic.messeinfo.invoice.data.ClearingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.CurrencyAmount;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.AccountancyModel;
import de.regasus.finance.FinanceSourceProvider;
import de.regasus.finance.payment.dialog.ClearingAmountDialog;
import de.regasus.ui.Activator;

public class CreateClearingAction extends Action {

	private CurrencyAmount amount;
	private PaymentVO paymentVO;
	private InvoicePositionVO invoicePositionVO;
	private InvoiceVO invoiceVO;
	private List<ClearingVO> candidateClearingVOs;
	
	
	public static CreateClearingAction getInstance(CurrencyAmount currencyAmount, PaymentVO paymentVO, InvoicePositionVO invoicePositionVO) {
		CreateClearingAction action = new CreateClearingAction();
		
		action.amount = currencyAmount;
		action.paymentVO = paymentVO;
		action.invoicePositionVO = invoicePositionVO;
		
		if (currencyAmount != null) {
			action.setText( I18N.CreateClearingWith + " " + currencyAmount.toString());
		}
		else {
			action.setText( I18N.CreateClearingWith + UtilI18N.Ellipsis);
		}
		
		return action;
	}
	
	
	public static CreateClearingAction getInstance(CurrencyAmount currencyAmount, PaymentVO paymentVO, InvoiceVO invoiceVO, List<ClearingVO> candidateClearingVOs) {
		CreateClearingAction action = new CreateClearingAction();
		
		action.amount = currencyAmount;
		action.paymentVO = paymentVO;
		action.invoiceVO = invoiceVO;
		action.candidateClearingVOs = candidateClearingVOs;
		
		if (currencyAmount != null) {
			action.setText( I18N.CreateClearingWith + " " + currencyAmount.toString());
		}
		else {
			action.setText( I18N.CreateClearingWith + UtilI18N.Ellipsis);
		}
		
		return action;
	}

	
	private CreateClearingAction() {
	}

	
	@Override
	public void run() {
		if (amount == null) {
			ClearingAmountDialog amountDialog = new ClearingAmountDialog(Display.getCurrent().getActiveShell(), paymentVO);
			int open = amountDialog.open();
			if (open == Window.OK) {
				this.amount = new CurrencyAmount(amountDialog.getAmount(), paymentVO.getCurrency());
			}
		}
		
		if (amount != null) {
			if (invoicePositionVO != null) {
				ClearingVO clearingVO = new ClearingVO();
				clearingVO.setPaymentPK(paymentVO.getID());
				clearingVO.setInvoicePositionPK(invoicePositionVO.getID());
				clearingVO.setAmount(amount.getAmount());
				
				try {
					AccountancyModel.getInstance().createClearing(paymentVO, clearingVO);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
			
			if (invoiceVO != null && candidateClearingVOs  != null) {
				Map<Long, Collection<Long>> payment2invoicePositionsMap = new HashMap<Long, Collection<Long>>();
				for (ClearingVO clearingVO : candidateClearingVOs) {
					Long paymentPK = clearingVO.getPaymentPK();
					Long invoicePositionPK = clearingVO.getInvoicePositionPK();
					
					Collection<Long> invoicePositionPKs = payment2invoicePositionsMap.get(paymentPK);
					if (invoicePositionPKs == null) {
						invoicePositionPKs = new HashSet<Long>();
						payment2invoicePositionsMap.put(paymentPK, invoicePositionPKs);
					}
					invoicePositionPKs.add(invoicePositionPK);
				}

				Long participantPK = paymentVO.getPayerPK();
				try {
					AccountancyModel.getInstance().createClearing(participantPK, payment2invoicePositionsMap);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
			
		
			/* After execution, the set of available commands may have changed, so update 
			 * the FinanceSourceProvider.
			 * This is not nice, but the FinanceSourceProvider gets its information from
			 * GUI elements in the FinanceComposite. If the FinanceSourceProvider would
			 * listen to the ParticipantModel to get informed when changes happen, it is 
			 * not sure, that the FinanceComposite has already the actual data, cause
			 * it is a listener of the ParticipantModel, too, but may be informed later.
			 * So the FinanceSourceProvider would get old informations from the FinanceComposite.
			 * 
			 * The same problem with the same solution exists in AbstractFinanceCommand.
			 */
			FinanceSourceProvider.getInstance().refreshVariables();
		}
	}

}

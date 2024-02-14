package de.regasus.participant.editor.finance;

import static de.regasus.participant.editor.finance.AccountancyUIHelper.*;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import com.lambdalogic.messeinfo.invoice.data.ClearingVO;
import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.rcp.Activator;

import de.regasus.finance.PaymentType;

/**
 * @author manfred
 * 
 */
public class PaymentLabelProvider extends ColumnLabelProvider {

	private AccountancyHelper accountancyHelper;

	private PaymentVO paymentVO;

	private boolean even;


	public PaymentLabelProvider(PaymentVO paymentVO, AccountancyHelper accountancyHelper, boolean even) {
		this.accountancyHelper = accountancyHelper;
		this.paymentVO = paymentVO;
		this.even = even;

	}


	@Override
	public String getText(Object element) {
		BigDecimal amount = null;
		if (element instanceof InvoiceVO && ! paymentVO.isClearing()) {
			InvoiceVO invoiceVO = (InvoiceVO) element;
			// If there are clearings for the positions of this invoice, show the sum of their amounts
			amount = accountancyHelper.getCumulatedClearingAmount(invoiceVO, paymentVO);
		}
		else if (element instanceof InvoicePositionVO) {
			InvoicePositionVO invoicePositionVO = (InvoicePositionVO) element;
			// If there is a clearing with this invoice position, show it's amount
			List<ClearingVO> clearingVOs = accountancyHelper.getClearing(invoicePositionVO, paymentVO);
			if (clearingVOs != null) {
				for (ClearingVO clearingVO : clearingVOs) {
					if (amount == null) {
						amount = clearingVO.getAmount();		
					}
					else {
						amount = amount.add(clearingVO.getAmount());
					}
				}
			}
		}
		else if (element instanceof String) {
			// Row with Sum of i nvoices or amount of payment
			String currency = (String) element;
			if (currency.equals(paymentVO.getCurrency())) {
				amount = paymentVO.getAmount();
			}
		}
		else if (element instanceof UnbalancedRowIndicator) {
			// Bei stornierten Zahlungseingängen sollen keine unausgeglichenen Beträge ausgegeben werden.
			if (! paymentVO.isCanceled() && paymentVO.getType() != PaymentType.CLEARING) {
				String currency = ((UnbalancedRowIndicator) element).getCurrency();
				if (currency.equals(paymentVO.getCurrency()) && !paymentVO.isBalanced()) {
					amount = paymentVO.getOpenAmount();
				}
			}

		}
		if (amount != null) {
			CurrencyAmount currencyAmount = new CurrencyAmount(amount, paymentVO.getCurrency());
			return currencyAmount.format(false, true);
		}
		return "";
	}


	/**
	 * Makes the invoice rows shown bold
	 */
	@Override
	public Font getFont(Object element) {
		if (element instanceof InvoiceVO) {
			return Activator.getDefault().getFontFromRegistry(Activator.DEFAULT_FONT_BOLD);
		}
		if (element instanceof String || element instanceof UnbalancedRowIndicator) {
			return Activator.getDefault().getFontFromRegistry(Activator.DEFAULT_FONT_BOLD_UNDER);
		}
		else {
			return JFaceResources.getDefaultFont();
		}
	}


	/**
	 * Makes the payment columns alternating shades of green.
	 */
	@Override
	public Color getBackground(Object element) {
		if (element instanceof String) {
			return getColor(GREY);
		}
		else if (element instanceof UnbalancedRowIndicator) {
			String currency = ((UnbalancedRowIndicator) element).getCurrency();
			if (currency.equals(paymentVO.getCurrency()) && !paymentVO.isBalanced() && ! paymentVO.isCanceled()) {
				return getColor(GREY);
			}
			else {
				return null;
			}
		}
		else if (
			(element instanceof InvoiceVO ||element instanceof InvoicePositionVO) && 
			paymentVO.isClearing()) {
			
			Long invoicePK = null;

			if (element instanceof InvoiceVO) {
				InvoiceVO invoice = (InvoiceVO) element;
				invoicePK = invoice.getPK();
			}
			else if (element instanceof InvoicePositionVO) {
				InvoicePositionVO position = (InvoicePositionVO) element;
				invoicePK = position.getInvoicePK();
			}

			if (accountancyHelper != null && invoicePK != null) {
				if (accountancyHelper.hasEvenInvoiceIndex(invoicePK)) {
					return getColor(YELLOW);
				}
				else {
					return getColor(LIGHT_YELLOW);
				}
			} 
			else {
				return null;
			}
		}
		else {
			if (even) {
				return getColor(GREEN);
			}
			else {
				return getColor(LIGHT_GREEN);
			}
			
		}
	}


	@Override
	public Color getForeground(Object element) {
		if (paymentVO.isCanceled()) {
			return getColor(DARK_GREY);
		}
		if (element instanceof UnbalancedRowIndicator) {
			String currency = ((UnbalancedRowIndicator) element).getCurrency();
			if (currency.equals(paymentVO.getCurrency()) && !paymentVO.isBalanced() && !paymentVO.isCanceled()) {
				return getColor(DARK_GREEN);
			}
		}
		return null;
	}

}

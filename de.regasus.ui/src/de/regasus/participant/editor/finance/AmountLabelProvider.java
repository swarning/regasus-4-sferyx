package de.regasus.participant.editor.finance;

import static de.regasus.participant.editor.finance.AccountancyUIHelper.*;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.rcp.Activator;

public class AmountLabelProvider extends ColumnLabelProvider {

	
	private AccountancyHelper accountancyHelper;

	public AmountLabelProvider(AccountancyHelper accountancyHelper) {
		this.accountancyHelper = accountancyHelper;
	}


	
	/**
	 * Returns the total brutto amount of either the invoice or the invoice position
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof InvoiceVO) {
			InvoiceVO invoice = (InvoiceVO) element;
			return invoice.getAmountGrossAsCurrencyAmount().format(false, true);
		}
		else if (element instanceof InvoicePositionVO) {
			InvoicePositionVO position = (InvoicePositionVO) element;
			return position.getAmountGrossAsCurrencyAmount().format(false, true);
		}
		else if (element instanceof String){
			String currency = (String) element;
			CurrencyAmount currencyAmount = accountancyHelper.getTotalInvoiceAmountByCurrency(currency);
			return currencyAmount.format(false, true);
		} else {
			return "";
		}
	}

	/**
	 * Tells to use bold font for invoices
	 */

	@Override
	public Font getFont(Object element) {
		if (element instanceof InvoiceVO) {
			return Activator.getDefault().getFontFromRegistry(Activator.DEFAULT_FONT_BOLD);
		}
		else if (element instanceof String) {
			return Activator.getDefault().getFontFromRegistry(Activator.DEFAULT_FONT_BOLD_UNDER);
		}
		else {
			return JFaceResources.getDefaultFont();
		}
	}


	/**
	 * Tells to use common shades of pink for all rows belonging to one invoice, alternating by each new invoice
	 */
	@Override
	public Color getBackground(Object element) {

		if (element instanceof String) {
			return getColor(GREY);
		}
		
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
				return getColor(PINK);
			}
			else {
				return getColor(LIGHT_PINK);
			}
		} 
		return null;
	}

}

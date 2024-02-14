package de.regasus.participant.editor.finance;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.lambdalogic.messeinfo.invoice.data.InvoicePositionVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;

public class AccountancyContentProvider implements ITreeContentProvider {

	private static Object[] NO_OBJS = new Object[0];

	/**
	 * This method returns the root elements of the tree, being the Invoices that are contained in
	 * the AccountancyCVO, plus Strings containing the Currency for the sums in that currency, and
	 * a helper to indicate that unbalanced amounts need to be shown for at least some payment and
	 * currency.
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof AccountancyHelper) {
			AccountancyHelper accountancyHelper = (AccountancyHelper) inputElement;
			List<Object> invoicesAndCurrencies = new ArrayList<Object>();
			invoicesAndCurrencies.addAll(accountancyHelper.getInvoiceVOs());
			List<String> currencyList = accountancyHelper.getCurrencyList();
			for (String currency : currencyList) {
				invoicesAndCurrencies.add(currency);
				if (accountancyHelper.hasUnbalancedPayments()) {
					invoicesAndCurrencies.add(new UnbalancedRowIndicator(currency));
				}
			}
			return invoicesAndCurrencies.toArray();
		}
		return NO_OBJS;
	}

	/**
	 * This method returns the children of each tree node, being the positions for the invoices.
	 */
	@Override
	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof InvoiceVO) {
			InvoiceVO invoiceVO = (InvoiceVO) parentElement;

			// copy invoice positions to an array
			List<InvoicePositionVO> invoicePositionVOs = invoiceVO.getInvoicePositionVOs();
			InvoicePositionVO[] result = invoicePositionVOs.toArray(new InvoicePositionVO[0]);

			return result;
		}
		return NO_OBJS;
	}


	/**
	 * We may return null if we don't need programmatical expansion of nested tree nodes.
	 */
	@Override
	public Object getParent(Object element) {
		return null;
	}


	@Override
	public boolean hasChildren(Object element) {
		return element instanceof InvoiceVO;
	}


	@Override
	public void dispose() {
	}


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}

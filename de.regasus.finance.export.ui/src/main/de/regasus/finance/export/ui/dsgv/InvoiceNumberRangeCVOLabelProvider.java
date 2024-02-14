package de.regasus.finance.export.ui.dsgv;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;

public class InvoiceNumberRangeCVOLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof InvoiceNoRangeCVO) {
			InvoiceNoRangeCVO invoiceNoRangeCVO = (InvoiceNoRangeCVO) element;
			return invoiceNoRangeCVO.getVO().getName();
		}
		return super.getText(element);
	}
}

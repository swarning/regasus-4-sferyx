package de.regasus.finance.invoice.command;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceVO;

import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.participant.editor.ParticipantEditor;

final class InvoiceUtil {

	/**
	 * Determine if at least one Invoice belongs to an Invoice Number Range which with audit-proof accountancy turned on.
	 * @param invoiceVOs
	 * @return
	 * @throws Exception
	 */
	static boolean hasAuditProofOn(Collection<InvoiceVO> invoiceVOs) throws Exception {
		Collection<Long> invoiceNoRangePKs = getInvoiceNoRangePKs(invoiceVOs);
		List<InvoiceNoRangeCVO> inrCVOs = InvoiceNoRangeModel.getInstance().getInvoiceNoRangeCVOs(invoiceNoRangePKs);
		for (InvoiceNoRangeCVO invoiceNoRangeCVO : inrCVOs) {
			if (invoiceNoRangeCVO.getVO().isAuditProof()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if at least one Invoice belongs to an Invoice Number Range which with audit-proof accountancy turned off.
	 * @param invoiceVOs
	 * @return
	 * @throws Exception
	 */
	static boolean hasAuditProofOff(Collection<InvoiceVO> invoiceVOs) throws Exception {
		Collection<Long> invoiceNoRangePKs = getInvoiceNoRangePKs(invoiceVOs);
		List<InvoiceNoRangeCVO> inrCVOs = InvoiceNoRangeModel.getInstance().getInvoiceNoRangeCVOs(invoiceNoRangePKs);
		for (InvoiceNoRangeCVO invoiceNoRangeCVO : inrCVOs) {
			if ( ! invoiceNoRangeCVO.getVO().isAuditProof()) {
				return true;
			}
		}
		return false;
	}


	static Set<Long> getInvoiceNoRangePKs(Collection<InvoiceVO> invoiceVOs) {
		Set<Long> invoicePKs = new HashSet<>();
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePKs.add(invoiceVO.getInvoiceNoRangePK());
		}
		return Collections.unmodifiableSet(invoicePKs);
	}


	static Set<Long> getRecipientPKs(Collection<InvoiceVO> invoiceVOs) {
		Set<Long> invoicePks = new HashSet<>();
		for (InvoiceVO invoiceVO : invoiceVOs) {
			invoicePks.add(invoiceVO.getRecipientPK());
		}
		return Collections.unmodifiableSet(invoicePks);
	}


	static boolean saveParticipantEditors(Collection<Long> recipientPKs) {
		for (Long recipientPK : recipientPKs) {
			if ( ! ParticipantEditor.saveEditor(recipientPK)) {
				return false;
			}
		}
		return true;
	}

}

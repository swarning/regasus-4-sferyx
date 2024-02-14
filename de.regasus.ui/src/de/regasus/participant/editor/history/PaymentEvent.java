package de.regasus.participant.editor.history;

import java.util.Date;

import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.util.CurrencyAmount;

import de.regasus.I18N;
import de.regasus.history.IHistoryEvent;

public class PaymentEvent implements IHistoryEvent {

	private PaymentVO paymentVO;
	private boolean forCancellation;

	public PaymentEvent(PaymentVO paymentVO, boolean forCancellation) {
		this.paymentVO = paymentVO;
		this.forCancellation = forCancellation;
	}


	@Override
	public String getHtmlDescription() {
		StringBuilder sb = new StringBuilder("<DIV>");
		sb.append(I18N.Amount);
		sb.append(": ");
		CurrencyAmount currencyAmount = paymentVO.getCurrencyAmount();
		sb.append(currencyAmount.format(false, false));
		sb.append(", ");
		sb.append(I18N.Payment_Type);
		sb.append(": ");
		sb.append(paymentVO.getType().getString());
		sb.append("</DIV>");

		return sb.toString();
	}


	@Override
	public Date getTime() {
		if (forCancellation) {
			return paymentVO.getCancelationDate();
		}
		else {
			return paymentVO.getNewTime();
		}
	}


	@Override
	public String getType() {
		if (forCancellation) {
			return I18N.Payment_Cancellation;
		}
		else {
			return I18N.Payment_Booking;
		}
	}


	@Override
	public String getUser() {
		if (forCancellation) {
			return paymentVO.getEditDisplayUserStr();
		}
		else {
			return paymentVO.getNewDisplayUserStr();
		}
	}
}

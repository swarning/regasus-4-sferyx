package de.regasus.finance.paymentsystem.command;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;
import static de.regasus.LookupService.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.PaymentVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.chunk.ChunkExecutor;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.finance.PaymentSystem;
import de.regasus.finance.payengine.PayEngineResponse;
import de.regasus.finance.payengine.PayEngineStatus;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.ui.Activator;

public class PayEngineRefundCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
    		// Determine the Participants
    		List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
    		if (notEmpty(participantList)) {
    			String message = I18N.PayEngineRefundQuestion;
    			message = message.replaceFirst("<count>", String.valueOf(participantList.size()));

        		boolean confirmed = MessageDialog.openQuestion(
        			HandlerUtil.getActiveShell(event),
        			UtilI18N.Confirm,
        			message
        		);

        		if (confirmed) {
    				executeInChunks(
    					HandlerUtil.getActiveShell(event),
    					participantList
    				);
        		}
    		}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}


	private void executeInChunks (
		final Shell shell,
		final List<IParticipant> participantList
	) {
		final int[] counter = {0};

		ChunkExecutor<IParticipant> chunkExecutor = new ChunkExecutor<IParticipant>() {
			@Override
			protected void executeChunk(List<IParticipant> chunkList) throws Exception {
				// Each list contains only one element, but for the sake of form we still iterate over this list.
				for (IParticipant participant : chunkList) {
					refund(participant);
				}

				// add number of Participants that have just been processed
				counter[0] = counter[0] + chunkList.size();
			}


			@Override
			protected Collection<IParticipant> getItems() {
				return participantList;
			}
		};

		chunkExecutor.setChunkSize(1);

		// set operation message
		String operationMessage = I18N.PayEngineRefundOperationMessage;
		operationMessage = operationMessage.replaceFirst("<count>", String.valueOf(participantList.size()));
		chunkExecutor.setOperationMessage(operationMessage);

		chunkExecutor.executeInChunks();
	}


	private void refund(IParticipant participant) throws Exception {
		Long participantId = participant.getPK();
		Integer number = participant.getNumber();

		System.out.println(number);

		List<PaymentVO> allPayEnginePayments = getPayEnginePayments(participantId);

		List<PaymentVO> initialPayments = filterInitialPayments(allPayEnginePayments);

		for (PaymentVO initialPayment : initialPayments) {
			List<PaymentVO> otherPayments = filterOtherPayments(initialPayment, allPayEnginePayments);

			BigDecimal refundAmount = calcRefundAmount(initialPayment, otherPayments);

			if (refundAmount.compareTo(BigDecimal.ZERO) != 0) {
    			Long payID = findPayID(initialPayment);
    			System.out.println("    " + refundAmount + "   " + payID);

    			System.out.println("Start refund");
    			getPaymentMgr().refundViaPayEngine(refundAmount, initialPayment, payID, null /*emailTemplateID*/);
    			System.out.println("Refund done");
			}
		}
	}


	private List<PaymentVO> getPayEnginePayments(Long participantId) throws ErrorMessageException {
		List<PaymentVO> paymentVOs = getPaymentMgr().getPaymentVOsByPersonPK(participantId, false/*withCancelations*/);

		for (Iterator<PaymentVO> it = paymentVOs.iterator(); it.hasNext();) {
			PaymentVO paymentVO = it.next();
			if ( ! PaymentSystem.PAYENGINE.equals(paymentVO.getPaymentSystem()) ) {
				it.remove();
			}
		}

		return paymentVOs;
	}


	private List<PaymentVO> filterInitialPayments(List<PaymentVO> payments) {
		List<PaymentVO> initialPayments = new ArrayList<>();

		for (PaymentVO payment : payments) {
			if (payment.getInitialPaymentPK() == null) {
				initialPayments.add(payment);
			}
		}

		return initialPayments;
	}


	private List<PaymentVO> filterOtherPayments(PaymentVO initialPayment, List<PaymentVO> allPayments) {
		List<PaymentVO> otherPayments = new ArrayList<>();

		for (PaymentVO payment : allPayments) {
			if ( initialPayment.getID().equals( payment.getInitialPaymentPK() ) ) {
				otherPayments.add(payment);
			}
		}

		return otherPayments;
	}


	private BigDecimal calcRefundAmount(PaymentVO initialPayment, List<PaymentVO> otherPayments) {
		BigDecimal refundAmount = initialPayment.getAmount();
		for (PaymentVO otherPayment : otherPayments) {
			refundAmount = refundAmount.add( otherPayment.getAmount() );
		}

		return refundAmount;
	}


	private Long findPayID(PaymentVO initialPayment) throws Exception {
		String orderID = initialPayment.getDocumentNo();

		List<PayEngineResponse> payEngineResponses = getPayEngineResponseMgr().readByOrderID(orderID);
		for (PayEngineResponse payEngineResponse : payEngineResponses) {
			PayEngineStatus status = payEngineResponse.getStatus();
			if (status != null && status == PayEngineStatus.PAYMENT_REQUESTED) {
				return payEngineResponse.getPayID();
			}
		}

		return null;
	}

}

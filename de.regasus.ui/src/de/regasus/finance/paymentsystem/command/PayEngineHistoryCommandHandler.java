package de.regasus.finance.paymentsystem.command;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.dialog.BrowserDialog;

import de.regasus.core.INewTrackingEntity;
import de.regasus.core.NewTrackingEntityComparator;
import de.regasus.event.EventModel;
import de.regasus.finance.command.AbstractFinanceCommandHandler;
import de.regasus.finance.payengine.PayEngineRequest;
import de.regasus.finance.payengine.PayEngineResponse;
import de.regasus.finance.paymentsystem.PayEngineHistoryHtmlConverter;
import de.regasus.participant.editor.ParticipantEditor;

public class PayEngineHistoryCommandHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "PayEngineHistoryCommand";

	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		Participant participant = participantEditor.getParticipant();
		Long participantPK = participantEditor.getParticipantPK();
		Long eventPK = participantEditor.getEventId();

		List<PayEngineRequest> requests = getPayEngineRequestMgr().readByPayer(participantPK);

		Shell shell = HandlerUtil.getActiveShell(event);

		if (requests.size() == 0) {
			MessageDialog.openInformation(
				shell,
				UtilI18N.Info,
				InvoiceLabel.PayEngine_NoRequestsForUser.getString()
			);
			return;
		}


		List<INewTrackingEntity> requestsAndResponses = new ArrayList<>();

		Set<String> orderIDs = new HashSet<>();

		for (PayEngineRequest request : requests) {
			requestsAndResponses.add(request);

			String orderID = request.getOrderID();
			orderIDs.add(orderID);
		}


		List<PayEngineResponse> responsesByOrderID = getPayEngineResponseMgr().readByOrderIDs(orderIDs);
		requestsAndResponses.addAll(responsesByOrderID);

		Collections.sort(requestsAndResponses, NewTrackingEntityComparator.getInstance());

		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

		String html = PayEngineHistoryHtmlConverter.convert(eventVO, participant, requestsAndResponses);

		BrowserDialog dialog = new BrowserDialog(
			shell,
			InvoiceLabel.PayEngine_History.getString(),
			html
		);
		dialog.create();

		// set size of Dialog
		dialog.getShell().setSize(800, 600);

		dialog.open();
	}

}

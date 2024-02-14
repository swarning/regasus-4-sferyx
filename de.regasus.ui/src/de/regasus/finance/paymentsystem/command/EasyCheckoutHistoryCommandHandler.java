package de.regasus.finance.paymentsystem.command;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import de.regasus.finance.easycheckout.EasyCheckoutRequest;
import de.regasus.finance.easycheckout.EasyCheckoutResponse;
import de.regasus.finance.paymentsystem.EasyCheckoutHistoryHtmlConverter;
import de.regasus.participant.editor.ParticipantEditor;

public class EasyCheckoutHistoryCommandHandler extends AbstractFinanceCommandHandler {

	public static final String COMMAND_ID = "EasyCheckoutHistoryCommand";

	@Override
	protected void execute(ExecutionEvent event, ParticipantEditor participantEditor)
	throws Exception {
		Participant participant = participantEditor.getParticipant();
		Long participantPK = participantEditor.getParticipantPK();
		Long eventPK = participantEditor.getEventId();

		List<EasyCheckoutRequest> requests = getEasyCheckoutRequestMgr().readByParticipant(participantPK);

		Shell shell = HandlerUtil.getActiveShell(event);

		if (requests.size() == 0) {
			MessageDialog.openInformation(
				shell,
				UtilI18N.Info,
				InvoiceLabel.EasyCheckout_NoRequestsForUser.getString()
			);
			return;
		}


		List<EasyCheckoutRequest> requestList = getEasyCheckoutRequestMgr().readByParticipant(participantPK);
		List<EasyCheckoutResponse> responseList = getEasyCheckoutResponseMgr().readByParticipant(participantPK);

		List<INewTrackingEntity> requestsAndResponses = new ArrayList<>(requestList.size() + responseList.size());
		requestsAndResponses.addAll(requestList);
		requestsAndResponses.addAll(responseList);

		Collections.sort(requestsAndResponses, NewTrackingEntityComparator.getInstance());

		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

		String html = EasyCheckoutHistoryHtmlConverter.convert(eventVO, participant, requestsAndResponses);

		BrowserDialog dialog = new BrowserDialog(
			shell,
			InvoiceLabel.EasyCheckout_History.getString(),
			html
		);
		dialog.create();

		// set size of Dialog
		dialog.getShell().setSize(1024, 768);

		dialog.open();
	}

}

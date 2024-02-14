package de.regasus.workflow;

import static de.regasus.LookupService.getParticipantMgr;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVOSettings;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeBookingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammeOfferingCVOSettings;
import com.lambdalogic.messeinfo.participant.interfaces.ProgrammePointCVOSettings;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.ui.Activator;

public class StartOnsiteWorkflowCommandHandler extends AbstractHandler {

	private static final OnsiteWorkflowExecutor workflowExecutor = new OnsiteWorkflowExecutor();


	private static final ParticipantCVOSettings PARTICIPANT_CVO_SETTINGS = new ParticipantCVOSettings();
	static {
		PARTICIPANT_CVO_SETTINGS.withAccountancyCVO = true;
		PARTICIPANT_CVO_SETTINGS.withBadges = true;
		PARTICIPANT_CVO_SETTINGS.withNationalityName = true;
		PARTICIPANT_CVO_SETTINGS.withParticipantStateName = true;
		PARTICIPANT_CVO_SETTINGS.withParticipantTypeName = true;
		PARTICIPANT_CVO_SETTINGS.withParticipantCustomFields = true;
		PARTICIPANT_CVO_SETTINGS.withSecondPerson = true;

		PARTICIPANT_CVO_SETTINGS.programmeBookingCVOSettings = new ProgrammeBookingCVOSettings();
		PARTICIPANT_CVO_SETTINGS.programmeBookingCVOSettings.withOpenAmount = true;
		PARTICIPANT_CVO_SETTINGS.programmeBookingCVOSettings.programmeOfferingCVOSettings = new ProgrammeOfferingCVOSettings();
		PARTICIPANT_CVO_SETTINGS.programmeBookingCVOSettings.programmeOfferingCVOSettings.programmePointCVOSettings = new ProgrammePointCVOSettings();

		PARTICIPANT_CVO_SETTINGS.hotelBookingCVOSettings = new HotelBookingCVOSettings();
		PARTICIPANT_CVO_SETTINGS.hotelBookingCVOSettings.withOpenAmount = true;
		PARTICIPANT_CVO_SETTINGS.hotelBookingCVOSettings.withHotelName = true;
		PARTICIPANT_CVO_SETTINGS.hotelBookingCVOSettings.hotelOfferingCVOSettings = new HotelOfferingCVOSettings();
		PARTICIPANT_CVO_SETTINGS.hotelBookingCVOSettings.hotelOfferingCVOSettings.hotelContingentCVOSettings = new HotelContingentCVOSettings();
	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// We can trust that the current editor is a participant editor, because it is configured in plugin.xml
			ParticipantEditor participantEditor = (ParticipantEditor) HandlerUtil.getActiveEditor(event);
			Participant participant = participantEditor.getParticipant();

			// save editor
			boolean safe = participantEditor.save(true /*confirm*/);
			if (!safe) {
				return null;
			}

			// get id after saving the editor, because its data could be new
			Long id = participant.getID();

			/* Load ParticipantCVO from server, because the Participant from ParticipantModel does
			 * only contain Badges
			 */
			ParticipantCVO participantCVO = getParticipantMgr().getParticipantCVO(
				id,
				PARTICIPANT_CVO_SETTINGS
			);

			// Find the workflow script for the participant's event
			Long eventPK = participant.getEventId();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			String onsiteWorkflow = eventVO.getOnsiteWorkflow();

			workflowExecutor.run(onsiteWorkflow, participantCVO, shell);

			participantEditor.refresh();
		}
		catch(Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return null;
	}

}

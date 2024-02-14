package de.regasus.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;

import de.regasus.participant.ParticipantProvider;

public class EventSelectionHelper {

	public static List<Long> getEventIDs(ISelection selection) {
		List<Long> eventIDs = null;

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;

			eventIDs = new ArrayList<Long>();

			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();

				if (o instanceof EventIdProvider) {
					EventIdProvider eventProvider = (EventIdProvider) o;
					eventIDs.add(eventProvider.getEventId());
				}
				else if (o instanceof ParticipantProvider) {
					ParticipantProvider participantProvider = (ParticipantProvider) o;
					eventIDs.add(participantProvider.getIParticipant().getEventId());
				}
				else if (o instanceof ParticipantSearchData) {
					ParticipantSearchData participantSearchData = (ParticipantSearchData) o;
					eventIDs.add(participantSearchData.getEventId());
				}
			}
		}

		return eventIDs;
	}


	public static List<EventVO> getEventVOs(ISelection selection) throws Exception {
		List<EventVO> eventVOs = null;

		List<Long> eventPKs = getEventIDs(selection);
		if (eventPKs != null && !eventPKs.isEmpty()) {
			eventVOs = EventModel.getInstance().getEventVOs(eventPKs);
		}

		return eventVOs;
	}

	/**
	 * Helper-Method for CommandHandlers to determine the selected Events either
	 * from a IWorkbenchPart (e.g. ParticipantEditor) or the current selection
	 * (e.g. in the ParticipantSearchView or ParticipantTreeView).
	 *
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static List<Long> getEventIDs(ExecutionEvent event) throws Exception {
		// Determine the Participants
		List<Long> eventIDs = null;

		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart != null && activePart instanceof EventIdProvider) {
			// The active part is a ParticipantProvider: Get the Participant of the ParticipantProvider (e.g. ParticipantEditor).
			EventIdProvider eventProvider = (EventIdProvider) activePart;

			final Long eventPK = eventProvider.getEventId();
			if (eventPK != null) {
				eventIDs = Collections.singletonList(eventPK);
			}
		}
		else if (activePart != null && activePart instanceof ParticipantProvider) {
			// The active part is a ParticipantProvider: Get the Participant of the ParticipantProvider (e.g. ParticipantEditor).
			ParticipantProvider participantProvider = (ParticipantProvider) activePart;

			final IParticipant iParticipant = participantProvider.getIParticipant();
			if (iParticipant != null) {
				Long eventPK = iParticipant.getEventId();
				if (eventPK != null) {
					eventIDs = Collections.singletonList(eventPK);
				}
			}
		}
		else {
			// The active part is no ParticipantProvider: Get the selected Participant(s).
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null) {
				eventIDs = getEventIDs(selection);
			}
		}

		return eventIDs;
	}


	public static Long getEventID(ExecutionEvent event) throws Exception {
		Long eventID = null;
		List<Long> eventIDs = getEventIDs(event);
		if (eventIDs != null && eventIDs.size() == 1) {
			eventID = eventIDs.get(0);
		}
		return eventID;
	}


	public static List<EventVO> getEventVOs(ExecutionEvent event) throws Exception {
		List<EventVO> eventVOs = null;

		List<Long> eventPKs = getEventIDs(event);
		if (eventPKs != null && !eventPKs.isEmpty()) {
			eventVOs = EventModel.getInstance().getEventVOs(eventPKs);
		}

		return eventVOs;
	}


	public static EventVO getEventVO(ExecutionEvent event) throws Exception {
		EventVO eventVO = null;

		Long eventPK = getEventID(event);
		if (eventPK != null) {
			eventVO = EventModel.getInstance().getEventVO(eventPK);
		}

		return eventVO;
	}

}

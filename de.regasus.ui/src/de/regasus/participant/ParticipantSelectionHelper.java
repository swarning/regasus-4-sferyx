package de.regasus.participant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

public class ParticipantSelectionHelper {

	public static List<Long> getParticipantIDs(ISelection selection) {
		List<Long> participantIDs = null;
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			
			participantIDs = new ArrayList<Long>();
			
			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				
				if (o instanceof ParticipantProvider) {
					ParticipantProvider participantProvider = (ParticipantProvider) o;
					participantIDs.add(participantProvider.getParticipantPK());
				}
				else if (o instanceof IParticipant) {
					IParticipant iParticipant = (IParticipant) o;
					participantIDs.add(iParticipant.getPK());
				}
			}
		}
		
		return participantIDs;
	}

	
	public static List<IParticipant> getParticipants(ISelection selection) throws Exception {
		List<IParticipant> participants = getParticipants(selection, true);
		return participants;
	}

	public static List<IParticipant> getParticipants(ISelection selection, boolean throwExceptionIfNotAParticipant) throws Exception {
		ArrayList<IParticipant> participantList = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			
			participantList = new ArrayList<IParticipant>();
			
			Iterator<?> iterator = sselection.iterator();
			
			while (iterator.hasNext()) {
				Object o = iterator.next();
				
				if (o instanceof IParticipant) {
					participantList.add((IParticipant) o);
				}
				else if (o instanceof ParticipantProvider) {
					ParticipantProvider participantProvider = (ParticipantProvider) o;
					IParticipant participant = participantProvider.getIParticipant();
					if (participant != null) {
						participantList.add(participant);
					}
				}
				else {
					if (throwExceptionIfNotAParticipant) {
						throw new RuntimeException("Not a ParticipantSearchData or ParticipantProvider: " + o.getClass().getName());
					}
				}
			}
		}
		return participantList;
	}

	
	/**
	 * Helper-Method for Participant-CommandHandlers to determine the selected Participants either 
	 * from a IWorkbenchPart (e.g. ParticipantEditor) or the current selection 
	 * (e.g. in the ParticipantSearchView or ParticipantTreeView).
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static List<IParticipant> getParticipants(ExecutionEvent event) throws Exception {
		// Determine the Participants
		List<IParticipant> participantList = null;
		
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart != null && activePart instanceof ParticipantProvider) {
			// The active part is a ParticipantProvider: Get the Participant of the ParticipantProvider (e.g. ParticipantEditor).
			ParticipantProvider participantProvider = (ParticipantProvider) activePart;
			
			// if the active part is an editor: save it
			if (activePart instanceof IEditorPart) {
				final IEditorPart editorPart = (IEditorPart) activePart;
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveEditor(editorPart, true);
			}
			
			final IParticipant iParticipant = participantProvider.getIParticipant();
			if (iParticipant != null) {
				participantList = Collections.singletonList(iParticipant);
			}
		}
		else {
			// The active part is no ParticipantProvider: Get the selected Participant(s).
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null) {
				participantList = getParticipants(selection);
			}
		}
		
		return participantList;
	}
	
	
	/**
	 * Helper-Method for Participant-CommandHandlers to determine the selected Participant either 
	 * from a IWorkbenchPart (e.g. ParticipantEditor) or the current selection 
	 * (e.g. in the ParticipantSearchView or ParticipantTreeView).
	 * If the current selections contains more than one participants, a RuntimeException is thrown.
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public static IParticipant getParticipant(ExecutionEvent event) throws Exception {
		// Determine the Participants
		IParticipant participant = null;
		
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart != null && activePart instanceof ParticipantProvider) {
			// The active part is a ParticipantProvider: Get the Participant of the ParticipantProvider (e.g. ParticipantEditor).
			ParticipantProvider participantProvider = (ParticipantProvider) activePart;
			
			// if the active part is an editor: save it
			if (activePart instanceof IEditorPart) {
				final IEditorPart editorPart = (IEditorPart) activePart;
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveEditor(editorPart, true);
			}
			
			participant = participantProvider.getIParticipant();
		}
		else {
			// The active part is no ParticipantProvider: Get the selected Participant(s).
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			if (selection != null) {
				List<IParticipant> participantList = getParticipants(selection);
				if (participantList != null && !participantList.isEmpty()) {
					if (participantList.size() == 1) {
						participant = participantList.get(0);
					}
					else {
						throw new RuntimeException(
							"There are " + participantList.size() + " participants selected.\n" +
							"This command doesn't allow selections with more than one participant."
						);
					}
				}
			}
		}
		
		return participant;
	}

}

package de.regasus.programme.booking.command;

import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.programme.booking.dialog.CreateProgrammeBookingsWizard;
import de.regasus.programme.booking.dialog.CreateProgrammeBookingsWizardSeveralParticipantTypes;
import de.regasus.ui.Activator;

/**
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-104
 *
 * @author manfred
 *
 */
public class CreateProgrammeBookingsCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Wizard wizard = null;

			// Determine the Participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			if (participantList != null && !participantList.isEmpty()) {
				// try to save the editors
				List<Long> participantPKs = Participant.getIParticipantPKs(participantList);
				boolean editorSaveCkeckOK = ParticipantEditor.saveEditor(participantPKs);
				if (!editorSaveCkeckOK) {
					return null;
				}

				if (participantList.size() == 1) {
					IParticipant iParticipant = participantList.get(0);

					wizard = new CreateProgrammeBookingsWizard(
						iParticipant.getEventId(),
						Collections.singletonList(iParticipant),
						iParticipant.getParticipantTypePK()
					);
				}
				else {
					// Find out how many participant types there are in the selected participants
					TreeSet<Long> participantTypeSet = new TreeSet<Long>();
					Long eventPK = null;

					for (IParticipant iParticipant : participantList) {
						Long participantTypePK = iParticipant.getParticipantTypePK();
						participantTypeSet.add(participantTypePK);

						if (eventPK == null) {
							eventPK = iParticipant.getEventId();
						}
						else if (!eventPK.equals(iParticipant.getEventId())) {
							throw new Exception(I18N.SelectedParticipantsDontBelongToSameEvent);
						}
					}


					if (participantTypeSet.size() == 1) {
						wizard = new CreateProgrammeBookingsWizard(eventPK, participantList, participantTypeSet.first());
					}
					else {
						wizard = new CreateProgrammeBookingsWizardSeveralParticipantTypes(eventPK, participantList);
					}
				}
			}

			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize(900, 600);
			dialog.open();
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

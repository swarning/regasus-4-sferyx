package de.regasus.participant.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.dialog.AssignParticipantToProfileWizard;
import de.regasus.ui.Activator;


/**
 * Assign a Profile to a selected Participant.
 * For this purpose the Profile has to be determined by the user.
 */
public class AssignParticipantToProfileCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// determine the Participant
			IParticipant iParticipant = ParticipantSelectionHelper.getParticipant(event);
			if (iParticipant != null) {
				AssignParticipantToProfileWizard wizard = new AssignParticipantToProfileWizard(
					iParticipant.getLastName(),
					iParticipant.getFirstName(),
					iParticipant.getPK()
				);

				// open the wizard
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				dialog.getShell().setSize(700, 600);
				dialog.open();
			}
			else {
				MessageDialog.openInformation(shell, UtilI18N.Info, "There is no participant selected.\nEs ist kein Teilnehmer ausgewählt.");
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

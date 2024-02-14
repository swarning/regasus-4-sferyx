package de.regasus.participant.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.dialog.AssignGroupManagerWizard;
import de.regasus.ui.Activator;

public class AssignGroupManagerCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			final List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			if (participantList != null && !participantList.isEmpty()) {
				Shell shell = HandlerUtil.getActiveShellChecked(event);

				AssignGroupManagerWizard wizard = new AssignGroupManagerWizard(participantList);
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				dialog.getShell().setSize( wizard.getPreferredSize() );
				dialog.open();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

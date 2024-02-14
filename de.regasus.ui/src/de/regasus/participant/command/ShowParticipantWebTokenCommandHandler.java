package de.regasus.participant.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.dialog.ParticipantWebTokenDialog;
import de.regasus.ui.Activator;

public class ShowParticipantWebTokenCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell activeShell = HandlerUtil.getActiveShell(event);
			IParticipant participant = ParticipantSelectionHelper.getParticipant(event);
			ParticipantWebTokenDialog dialog = new ParticipantWebTokenDialog(activeShell, participant);
			dialog.open();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

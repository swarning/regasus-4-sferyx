package de.regasus.profile.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.dialog.AssignProfileToParticipantWizard;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.ui.Activator;


/**
 * Assign a Participant to a selected Profile.
 * For this purpose the Participant has to be determined by the user.
 */
public class AssignProfileToParticipantCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			// determine the Participant
			Profile profile = ProfileSelectionHelper.getProfile(event);
			if (profile != null) {
				AssignProfileToParticipantWizard wizard = new AssignProfileToParticipantWizard(
					profile.getLastName(),
					profile.getFirstName(),
					profile.getPK()
				);

				// open the wizard
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				dialog.getShell().setSize(700, 600);
				dialog.open();
			}
			else {
				MessageDialog.openInformation(shell, UtilI18N.Info, "There is no profile selected.\nEs ist kein Profil ausgewählt.");
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

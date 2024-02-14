package de.regasus.profile.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.person.PersonLinkModel;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.ui.Activator;


public class UnlinkProfileCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Profile
			final Profile profile = ProfileSelectionHelper.getProfile(event);

			if (profile != null) {
				String msg = I18N.UnlinkProfileCommandHandler_Confirmation;
				msg = msg.replaceFirst("<name>", profile.getName());
				
				boolean unlinkOK = MessageDialog.openQuestion(
					HandlerUtil.getActiveShell(event),
					UtilI18N.Question,
					msg
				);

				if (unlinkOK) {
					Long profileID = profile.getID();
					PersonLinkModel.getInstance().unlinkProfile(profileID);
				}
			}
		}			
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return null;
	}

}

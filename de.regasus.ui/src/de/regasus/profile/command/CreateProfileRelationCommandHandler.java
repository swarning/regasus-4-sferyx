package de.regasus.profile.command;

import java.util.Objects;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileSelectionHelper;
import de.regasus.profile.relation.CreateProfileRelationDialog;
import de.regasus.ui.Activator;

public class CreateProfileRelationCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Profile profile = ProfileSelectionHelper.getProfile(event);
			Shell shell = HandlerUtil.getActiveShell(event);
			openWizard(shell, profile);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return null;
	}


	public static void openWizard(Shell shell, Profile profile) {
		Objects.requireNonNull(profile);

		CreateProfileRelationDialog dialog = new CreateProfileRelationDialog(shell, profile);
		dialog.create();
		dialog.getShell().setSize( dialog.getPreferredSize() );
		dialog.open();
	}

}

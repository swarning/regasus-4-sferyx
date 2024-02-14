package de.regasus.profile.relation.view;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.core.ui.action.AbstractAction;
import de.regasus.profile.command.CreateProfileRelationCommandHandler;
import de.regasus.ui.Activator;

/**
 * This action is necessary in addition to the CreateProfileRelationCommandHandler because the latter
 * is following the ProfileSourceProvider. That means it is enabled or disabled whether the ProfileSourceProvider
 * has a Profile or not. In contrast, this Action is enabled if a Profile is set via its setProfile()
 * method. This allows the ProfileRelationView to control its enables status. This is necessary because
 * the Action should be enabled if there is Profile present in the ProfileRelationView.
 * If the CreateProfileRelationCommandHandler is used there it would be enabled / disabled every time
 * the current selection or part changes.
 */
public class CreateProfileRelationAction extends AbstractAction {
	public static final String ID = "de.regasus.profile.action.ProfileRelationView.CreateProfileRelationAction"; 

	private final IWorkbenchWindow window;

	private Profile profile = null;


	public CreateProfileRelationAction(IWorkbenchWindow window) {
		super();
		this.window = window;

		setId(ID);
		setText(I18N.CreateProfileRelation_Title);
		setToolTipText(I18N.CreateProfileRelation_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.CREATE_PROFILE_RELATION
		));

		setEnabled(false);
	}


	@Override
	public void run() {
		CreateProfileRelationCommandHandler.openWizard(window.getShell(), profile);
	}


	public void setProfile(Profile profile) {
		this.profile = profile;

		boolean enabled = (profile != null);
		setEnabled(enabled);
	}

}

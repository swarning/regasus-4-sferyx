package de.regasus.profile.role.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class ProfileRoleEditorInput extends AbstractEditorInput<Long> {
	
	public ProfileRoleEditorInput() {
	}
	
	
	public ProfileRoleEditorInput(Long profileRoleID) {
		key = profileRoleID;
	}
	

	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, null);
	}

}

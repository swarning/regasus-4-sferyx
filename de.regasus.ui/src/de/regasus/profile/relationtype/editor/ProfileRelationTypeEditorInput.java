package de.regasus.profile.relationtype.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class ProfileRelationTypeEditorInput extends AbstractEditorInput<Long> {
	
	public ProfileRelationTypeEditorInput() {
	}
	
	
	public ProfileRelationTypeEditorInput(Long profileRelationTypeID) {
		key = profileRelationTypeID;
	}
	

	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, null);
	}

}

package de.regasus.profile.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.ui.Activator;


public class ProfileEditorInput extends AbstractEditorInput<Long> {
	
	private Long copyProfileID;
	
	
	public ProfileEditorInput() {
	}


	public ProfileEditorInput(Long profileID) {
		this(profileID, false);
	}

	
	public ProfileEditorInput(Long profileID, boolean copy) {
		if (copy) {
			this.copyProfileID = profileID;
		}
		else {
			this.key = profileID;
		}
	}


	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PROFILE);
	}

	
	public boolean isCopy() {
		return copyProfileID != null;
	}
	
	
	public Long getCopyProfileID() {
		return copyProfileID;
	}
	
}

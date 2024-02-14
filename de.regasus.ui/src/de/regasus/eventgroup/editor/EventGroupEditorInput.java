package de.regasus.eventgroup.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.IImageKeys;
import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class EventGroupEditorInput extends AbstractEditorInput<Long> {

	public EventGroupEditorInput() {
	}


	public EventGroupEditorInput(Long eventGroupId) {
		key = eventGroupId;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.EVENT_GROUP);
	}

}

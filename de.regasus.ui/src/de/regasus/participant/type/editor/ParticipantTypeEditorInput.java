package de.regasus.participant.type.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.IImageKeys;
import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class ParticipantTypeEditorInput extends AbstractEditorInput<Long> {

	public ParticipantTypeEditorInput() {
	}
	
	
	public ParticipantTypeEditorInput(Long participantTypePK) {
		key = participantTypePK;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PARTICIPANT_TYPE);
	}

}

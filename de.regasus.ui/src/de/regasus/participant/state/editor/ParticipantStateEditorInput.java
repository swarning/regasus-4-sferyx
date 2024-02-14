/**
 * ParticipantStateEditorInput.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.IImageKeys;
import de.regasus.core.error.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

/**
 * @author huuloi
 *
 */
public class ParticipantStateEditorInput
extends AbstractEditorInput<Long> {
	
	
	public ParticipantStateEditorInput() {
	}
	
	public ParticipantStateEditorInput(Long participantStateID) {
		key = participantStateID;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PARTICIPANT_STATE);
	}

}

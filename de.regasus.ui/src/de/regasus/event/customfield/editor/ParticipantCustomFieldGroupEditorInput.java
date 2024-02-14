package de.regasus.event.customfield.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;

public class ParticipantCustomFieldGroupEditorInput
extends AbstractEditorInput<Long>
implements ILinkableEditorInput, EventIdProvider {

	private Long eventPK = null;
	private ParticipantCustomFieldGroupLocation location = null;


	private ParticipantCustomFieldGroupEditorInput() {
	}


	public static ParticipantCustomFieldGroupEditorInput getEditInstance(Long participantCustomFieldGroupID, Long eventPK) {
		ParticipantCustomFieldGroupEditorInput editorInput = new ParticipantCustomFieldGroupEditorInput();
		editorInput.key = participantCustomFieldGroupID;
		editorInput.eventPK = eventPK;
		return editorInput;
	}


	public static ParticipantCustomFieldGroupEditorInput getCreateInstance(
		Long eventPK,
		ParticipantCustomFieldGroupLocation location
	) {
		ParticipantCustomFieldGroupEditorInput editorInput = new ParticipantCustomFieldGroupEditorInput();
		editorInput.eventPK = eventPK;
		editorInput.location = location;
		return editorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(IImageKeys.MD_CUSTOM_FIELD_GROUP);
	}


	@Override
	public Class<?> getEntityType() {
		return ParticipantCustomFieldGroup.class;
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}


	public ParticipantCustomFieldGroupLocation getLocation() {
		return location;
	}

}

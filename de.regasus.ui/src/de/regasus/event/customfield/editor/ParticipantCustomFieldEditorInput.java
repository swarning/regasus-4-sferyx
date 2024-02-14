package de.regasus.event.customfield.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;

public class ParticipantCustomFieldEditorInput
extends AbstractEditorInput<Long>
implements ILinkableEditorInput, EventIdProvider {

	private Long eventPK = null;
	private Long customFieldGroupID = null;


	private ParticipantCustomFieldEditorInput() {
	}


	public static ParticipantCustomFieldEditorInput getEditInstance(Long participantCustomFieldID, Long eventPK) {
		ParticipantCustomFieldEditorInput editorInput = new ParticipantCustomFieldEditorInput();
		editorInput.key = participantCustomFieldID;
		editorInput.eventPK = eventPK;
		return editorInput;
	}


	public static ParticipantCustomFieldEditorInput getCreateInstance(Long eventPK, Long customFieldGroupID) {
		ParticipantCustomFieldEditorInput editorInput = new ParticipantCustomFieldEditorInput();
		editorInput.eventPK = eventPK;
		editorInput.customFieldGroupID = customFieldGroupID;
		return editorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(IImageKeys.MD_CUSTOM_FIELD);
	}


	@Override
	public Class<?> getEntityType() {
		return ParticipantCustomField.class;
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}


	public Long getCustomFieldGroupPK() {
		return customFieldGroupID;
	}

}

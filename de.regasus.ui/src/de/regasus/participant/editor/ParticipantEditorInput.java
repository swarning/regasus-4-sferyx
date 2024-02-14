package de.regasus.participant.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.ui.Activator;


public class ParticipantEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	private Long eventPK = null;
	private Long mainParticipantPK = null;
	private boolean groupManager = false;
	private Long groupManagerPK = null;
	private Long participantStatePK;
	private Long participantTypePK;
	private boolean link;


	private ParticipantEditorInput() {
	}


	public static ParticipantEditorInput getCreateInstance(Long eventPK) {
		ParticipantEditorInput participantEditorInput = new ParticipantEditorInput();
		participantEditorInput.eventPK = eventPK;
		return participantEditorInput;
	}


	public static ParticipantEditorInput getCreateGroupManagerInstance(Long eventPK) {
		ParticipantEditorInput participantEditorInput = new ParticipantEditorInput();
		participantEditorInput.groupManager = true;
		participantEditorInput.eventPK = eventPK;
		return participantEditorInput;
	}


	public static ParticipantEditorInput getCreateCompanionInstance(Long mainParticipantPK, Long eventPK) {
		ParticipantEditorInput participantEditorInput = new ParticipantEditorInput();
		participantEditorInput.mainParticipantPK = mainParticipantPK;
		participantEditorInput.eventPK = eventPK;
		return participantEditorInput;
	}


	public static ParticipantEditorInput getCreateGroupMemberInstance(Long groupManagerPK, Long eventPK) {
		ParticipantEditorInput participantEditorInput = new ParticipantEditorInput();
		participantEditorInput.groupManagerPK = groupManagerPK;
		participantEditorInput.eventPK = eventPK;
		return participantEditorInput;
	}


	public static ParticipantEditorInput getEditInstance(Long participantPK) {
		ParticipantEditorInput participantEditorInput = new ParticipantEditorInput();
		participantEditorInput.key = participantPK;
		return participantEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.PARTICIPANT);
	}


	public Long getEventPK() {
		return eventPK;
	}


	public Long getMainParticipantPK() {
		return mainParticipantPK;
	}


	public boolean isGroupManager() {
		return groupManager;
	}


	public Long getGroupManagerPK() {
		return groupManagerPK;
	}


	public Long getParticipantStatePK() {
		return participantStatePK;
	}


	public Long getParticipantTypePK() {
		return participantTypePK;
	}


	public boolean isLink() {
		return link;
	}


	@Override
	public Class<?> getEntityType() {
		return Participant.class;
	}

}

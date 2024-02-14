package de.regasus.event.command.copypaste;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.ParticipantCustomFieldGroupLocationTreeNode;
import de.regasus.event.view.ParticipantCustomFieldGroupTreeNode;
import de.regasus.event.view.ParticipantCustomFieldListTreeNode;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupLocationTreeNode;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupTreeNode;

class ParticipantCustomFieldGroupPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		if (targetTreeNode instanceof EventTreeNode) {
			EventTreeNode node = (EventTreeNode) targetTreeNode;
			Long eventPK = node.getEventId();
			ParticipantCustomFieldGroupModel.getInstance().copyParticipantCustomFieldGroup(sourceId, eventPK, null);
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldListTreeNode) {
			ParticipantCustomFieldListTreeNode node = (ParticipantCustomFieldListTreeNode) targetTreeNode;
			Long eventPK = node.getEventId();
			ParticipantCustomFieldGroupModel.getInstance().copyParticipantCustomFieldGroup(sourceId, eventPK, null);
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldGroupLocationTreeNode) {
			ParticipantCustomFieldGroupLocationTreeNode node = (ParticipantCustomFieldGroupLocationTreeNode) targetTreeNode;
			Long eventPK = node.getEventId();
			ParticipantCustomFieldGroupLocation location = node.getValue();
			ParticipantCustomFieldGroupModel.getInstance().copyParticipantCustomFieldGroup(sourceId, eventPK, location);
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
			ParticipantCustomFieldGroupTreeNode node = (ParticipantCustomFieldGroupTreeNode) targetTreeNode;
			Long eventPK = node.getEventId();
			ParticipantCustomFieldGroupModel.getInstance().copyParticipantCustomFieldGroup(sourceId, eventPK, null);
		}
		else if (targetTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
			ProfileCustomFieldGroupTreeNode node = (ProfileCustomFieldGroupTreeNode) targetTreeNode;
			ProfileCustomFieldGroupLocation location = node.getValue().getLocation();
			ProfileCustomFieldGroupModel.getInstance().copyFromParticipantCustomFieldGroup(sourceId, location);
		}
		else if (targetTreeNode instanceof ProfileCustomFieldGroupLocationTreeNode) {
			ProfileCustomFieldGroupLocationTreeNode node = (ProfileCustomFieldGroupLocationTreeNode) targetTreeNode;
			ProfileCustomFieldGroupLocation location = node.getValue();
			ProfileCustomFieldGroupModel.getInstance().copyFromParticipantCustomFieldGroup(sourceId, location);
		}
		else {
			return false;
		}

		// load children (even if the model does not have their data and the list of children is empty)
		// precondition that targetTreeNode.refreshChildren() refreshes the model at all
		targetTreeNode.getChildren();

		// let the TreeNode refresh the model data
		targetTreeNode.refreshChildren();

		return true;
	}
}

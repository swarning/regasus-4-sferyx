package de.regasus.event.command.copypaste;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.ParticipantCustomFieldGroupLocationTreeNode;
import de.regasus.event.view.ParticipantCustomFieldGroupTreeNode;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupLocationTreeNode;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupTreeNode;

class ProfileCustomFieldGroupPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		if (targetTreeNode instanceof ProfileCustomFieldGroupLocationTreeNode) {
			ProfileCustomFieldGroupLocationTreeNode locationTreeNode = (ProfileCustomFieldGroupLocationTreeNode) targetTreeNode;
			ProfileCustomFieldGroupLocation location = locationTreeNode.getValue();

			ProfileCustomFieldGroupModel.getInstance().copyProfileCustomFieldGroup(sourceId, location);
		}
		else if (targetTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
			ProfileCustomFieldGroupTreeNode groupTreeNode = (ProfileCustomFieldGroupTreeNode) targetTreeNode;
			ProfileCustomFieldGroupLocation location = groupTreeNode.getValue().getLocation();

			ProfileCustomFieldGroupModel.getInstance().copyProfileCustomFieldGroup(sourceId, location);
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldGroupLocationTreeNode) {
			ParticipantCustomFieldGroupLocationTreeNode locationTreeNode = (ParticipantCustomFieldGroupLocationTreeNode) targetTreeNode;
			Long eventPK = locationTreeNode.getEventId();
			ParticipantCustomFieldGroupLocation location = locationTreeNode.getValue();

			ParticipantCustomFieldGroupModel.getInstance().copyFromProfileCustomFieldGroup(sourceId, eventPK, location);
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
			ParticipantCustomFieldGroupTreeNode groupTreeNode = (ParticipantCustomFieldGroupTreeNode) targetTreeNode;
			Long eventPK = groupTreeNode.getEventId();
			ParticipantCustomFieldGroupLocation location = groupTreeNode.getValue().getLocation();

			ParticipantCustomFieldGroupModel.getInstance().copyFromProfileCustomFieldGroup(sourceId, eventPK, location);
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

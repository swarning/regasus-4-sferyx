package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.ParticipantCustomFieldGroupTreeNode;
import de.regasus.event.view.ParticipantCustomFieldTreeNode;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupTreeNode;
import de.regasus.profile.customfield.view.ProfileCustomFieldTreeNode;

class ProfileCustomFieldPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		ProfileCustomFieldModel model = ProfileCustomFieldModel.getInstance();

		if (targetTreeNode instanceof ProfileCustomFieldTreeNode) {
			ProfileCustomFieldTreeNode node = (ProfileCustomFieldTreeNode) targetTreeNode;
			Long groupID = null;
			TreeNode<?> parent = node.getParent();
			if (parent instanceof ProfileCustomFieldGroupTreeNode) {
				ProfileCustomFieldGroupTreeNode groupNode = (ProfileCustomFieldGroupTreeNode) parent;
				groupID = groupNode.getProfileCustomFieldGroupID();
			}
			model.copyProfileCustomField(sourceId, groupID);
		}
		else if (targetTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
			ProfileCustomFieldGroupTreeNode node = (ProfileCustomFieldGroupTreeNode) targetTreeNode;
			Long groupID = node.getProfileCustomFieldGroupID();
			model.copyProfileCustomField(sourceId, groupID);
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
			ParticipantCustomFieldGroupTreeNode node = (ParticipantCustomFieldGroupTreeNode) targetTreeNode;
			ParticipantCustomFieldModel.getInstance().copyFromProfileCustomField(sourceId, node.getValue().getID());
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldTreeNode) {
			ParticipantCustomFieldTreeNode node = (ParticipantCustomFieldTreeNode) targetTreeNode;
			ParticipantCustomFieldModel.getInstance().copyFromProfileCustomField(sourceId, node.getGroupPK());
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

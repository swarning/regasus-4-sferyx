package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.ParticipantCustomFieldGroupTreeNode;
import de.regasus.event.view.ParticipantCustomFieldTreeNode;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.profile.customfield.view.ProfileCustomFieldGroupTreeNode;
import de.regasus.profile.customfield.view.ProfileCustomFieldTreeNode;

class ParticipantCustomFieldPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		if (targetTreeNode instanceof ParticipantCustomFieldTreeNode) {
			ParticipantCustomFieldTreeNode pcftn = (ParticipantCustomFieldTreeNode) targetTreeNode;

			TreeNode<?> parentTreeNode = pcftn.getParent();
			Long groupID = null;
			if (parentTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
				ParticipantCustomFieldGroupTreeNode node = (ParticipantCustomFieldGroupTreeNode) parentTreeNode;
				groupID = node.getParticipantCustomFieldGroupID();
			}
			Long eventPK = pcftn.getEventId();
			ParticipantCustomFieldModel.getInstance().copyParticipantCustomField(sourceId, eventPK, groupID);
		}
		else if (targetTreeNode instanceof ParticipantCustomFieldGroupTreeNode) {
			ParticipantCustomFieldGroupTreeNode node = (ParticipantCustomFieldGroupTreeNode) targetTreeNode;
			Long eventPK = node.getEventId();
			Long groupID = node.getParticipantCustomFieldGroupID();
			ParticipantCustomFieldModel.getInstance().copyParticipantCustomField(sourceId, eventPK, groupID);
		}
		else if (targetTreeNode instanceof ProfileCustomFieldGroupTreeNode) {
			ProfileCustomFieldGroupTreeNode node = (ProfileCustomFieldGroupTreeNode) targetTreeNode;
			ProfileCustomFieldModel.getInstance().copyFromParticipantCustomField(sourceId, node.getProfileCustomFieldGroupID());
		}
		else if (targetTreeNode instanceof ProfileCustomFieldTreeNode) {
			ProfileCustomFieldTreeNode node = (ProfileCustomFieldTreeNode) targetTreeNode;
			ProfileCustomFieldModel.getInstance().copyFromParticipantCustomField(sourceId, node.getGroupPK());
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

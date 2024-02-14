package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.ProgrammeOfferingTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.ProgrammeOfferingModel;

class ProgrammeOfferingPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		// get the id of the destination PP
		Long destProgrammePointPK = null;
		if (targetTreeNode instanceof ProgrammePointTreeNode) {
			Long key = (Long) targetTreeNode.getKey();
			destProgrammePointPK = key;
		}
		else if (targetTreeNode instanceof ProgrammeOfferingTreeNode) {
			Long key = (Long) targetTreeNode.getParent().getKey();
			destProgrammePointPK = key;
		}

		if (destProgrammePointPK != null) {
			ProgrammeOfferingModel.getInstance().copyProgrammeOffering(sourceId, destProgrammePointPK);

            // load children (even if the model does not have their data and the list of children is empty)
            // precondition that targetTreeNode.refreshChildren() refreshes the model at all
			targetTreeNode.getChildren();

			// let the TreeNode refresh the model data
			targetTreeNode.refreshChildren();

			return true;
		}
		return false;
	}

}
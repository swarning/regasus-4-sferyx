package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.ProgrammePointListTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.ProgrammePointModel;

class ProgrammePointPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		if (   targetTreeNode instanceof ProgrammePointListTreeNode
			|| targetTreeNode instanceof ProgrammePointTreeNode
		) {
			// get the id of the destination Event
			Long destEventPK = PasteCommandHandler.findEventPKInAncesters(targetTreeNode);
			if (destEventPK != null) {
				ProgrammePointModel.getInstance().copyProgrammePoint(sourceId, destEventPK);

				// load children (even if the model does not have their data and the list of children is empty)
	            // precondition that targetTreeNode.refreshChildren() refreshes the model at all
				targetTreeNode.getChildren();

				// let the TreeNode refresh the model data
				targetTreeNode.refreshChildren();

				return true;
			}
		}
		return false;
	}

}

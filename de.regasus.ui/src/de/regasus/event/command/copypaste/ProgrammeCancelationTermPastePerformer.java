package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.ProgrammeCancelationTermTreeNode;
import de.regasus.event.view.ProgrammeOfferingTreeNode;
import de.regasus.programme.ProgrammeCancelationTermModel;

class ProgrammeCancelationTermPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		// get the id of the destination PO
		Long destProgOfferingPK = null;
		if (targetTreeNode instanceof ProgrammeOfferingTreeNode) {
			Long key = (Long) targetTreeNode.getKey();
			destProgOfferingPK = key;
		}
		else if (targetTreeNode instanceof ProgrammeCancelationTermTreeNode) {
			Long key = (Long) targetTreeNode.getParent().getKey();
			destProgOfferingPK = key;
		}

		if (destProgOfferingPK != null) {
			ProgrammeCancelationTermModel model = ProgrammeCancelationTermModel.getInstance();
			model.copyProgrammeCancelationTerm(sourceId, destProgOfferingPK);

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

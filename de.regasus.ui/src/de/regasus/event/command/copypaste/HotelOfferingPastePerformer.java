package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.HotelContingentTreeNode;
import de.regasus.event.view.HotelOfferingTreeNode;
import de.regasus.hotel.HotelOfferingModel;

class HotelOfferingPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		// get the id of the destination PP
		Long destHotelContingentPK = null;
		if (targetTreeNode instanceof HotelContingentTreeNode) {
			Long key = (Long) targetTreeNode.getKey();
			destHotelContingentPK = key;
		}
		else if (targetTreeNode instanceof HotelOfferingTreeNode) {
			Long key = (Long) targetTreeNode.getParent().getKey();
			destHotelContingentPK = key;
		}

		if (destHotelContingentPK != null) {
			HotelOfferingModel.getInstance().copyHotelOffering(sourceId, destHotelContingentPK);

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

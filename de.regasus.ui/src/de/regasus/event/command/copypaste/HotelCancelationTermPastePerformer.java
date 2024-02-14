package de.regasus.event.command.copypaste;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.event.view.HotelCancelationTermTreeNode;
import de.regasus.event.view.HotelOfferingTreeNode;
import de.regasus.hotel.HotelCancelationTermModel;

class HotelCancelationTermPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		// get the id of the destination PO
		Long destHotelOfferingPK = null;
		if (targetTreeNode instanceof HotelOfferingTreeNode) {
			Long key = (Long) targetTreeNode.getKey();
			destHotelOfferingPK = key;
		}
		else if (targetTreeNode instanceof HotelCancelationTermTreeNode) {
			Long key = (Long) targetTreeNode.getParent().getKey();
			destHotelOfferingPK = key;
		}

		if (destHotelOfferingPK != null) {
			HotelCancelationTermModel.getInstance().copyHotelCancelationTerm(sourceId, destHotelOfferingPK);

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

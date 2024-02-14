package de.regasus.event.command.copypaste;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.hotel.view.tree.HotelTreeNode;
import de.regasus.hotel.view.tree.RoomDefinitionTreeNode;

class RoomDefinitionPastePerformer implements PastePerformer {

	@Override
	public <T extends TreeNode<?>> boolean perform(T targetTreeNode, Long sourceId) throws Exception {
		Long destHotelPK = null;
		if (targetTreeNode instanceof HotelTreeNode) {
			Long key = (Long) targetTreeNode.getKey();
			destHotelPK = key;
		}
		else if (targetTreeNode instanceof RoomDefinitionTreeNode) {
			destHotelPK = ((Hotel) targetTreeNode.getParent().getValue()).getID();
		}

		if (destHotelPK != null) {
			RoomDefinitionModel.getInstance().copyRoomDefinition(sourceId, destHotelPK);

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

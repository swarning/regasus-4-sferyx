package de.regasus.hotel.view.tree;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.ui.Activator;


/**
 * A node representing a Room Definition.
 */
public class RoomDefinitionTreeNode extends TreeNode<RoomDefinitionVO> {
	
	// *************************************************************************
	// * Attributes
	// *

	private Long roomDefinitionPK;

	/* Just used to refresh the data of this Room Definition.
	 * Observing this Room Definition is not necessary, because the parent TreeNode is observing all 
	 * its Room Definitions. On any change the value of this TreeNode is set and refreshTreeNode() 
	 * of the parent is called.
	 */
	private RoomDefinitionModel roomDefinitionModel = RoomDefinitionModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructor
	// *

	public RoomDefinitionTreeNode(
		TreeViewer treeViewer,
		HotelTreeNode parent,
		RoomDefinitionVO roomDefinitionVO
	) {
		super(treeViewer, parent, roomDefinitionVO);
		
		roomDefinitionPK = roomDefinitionVO.getID();
	}

	// *
	// * Constructor
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *
	
	@Override
	public Class<?> getEntityType() {
		return RoomDefinitionVO.class;
	}
	

	@Override
	public Object getKey() {
		return roomDefinitionPK;
	}

	
	@Override
	public String getText() {
		String text = "";
		if (value != null) {
			text = value.getName().getString();
		}
		return StringHelper.avoidNull(text);
	}

	
	@Override
	public String getToolTipText() {
		return HotelLabel.RoomDefinition.getString();
	}

	
	@Override
	public Image getImage() {
		return IconRegistry.getImage("/icons/room16x11.png");
	}

	
	@Override
	public boolean isLeaf() {
		return true;
	}

	
	@Override
	public void refresh() {
		/*
		 * The parent node takes the responsibility to refresh this node, therefore we don't have to
		 * be listeners ourselves, but just fire the refresh request to the model. 
		 */
		
		try {
			// refresh data of this TreeNode
			roomDefinitionModel.refresh(roomDefinitionPK);
			
			// no child TreeNodes to refresh
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}
	
	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

}

package de.regasus.hotel.view.tree;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.ui.Activator;


/**
 * A node representing a Hotel.
 */
public class HotelTreeNode extends TreeNode<Hotel> implements CacheModelListener<Long> {
	
	// *************************************************************************
	// * Attributes
	// *
	
	private Long hotelPK;
	
	/* Just used to refresh the data of this Hotel.
	 * Observing is not necessary, because the parent TreeNode is observing all its Hotels. 
	 * On any change the value of this TreeNode is set and the parent calls refreshTreeNode().
	 */
	private HotelModel hotelModel = HotelModel.getInstance();
	
	// data of child TreeNodes
	private RoomDefinitionModel roomDefinitionModel = RoomDefinitionModel.getInstance();

	/* ignore ModifyEvent from RoomDefinitionModel that are fired when this TreeNode requests data from them
	 */
	private boolean ignoreDataChange = false;
	
	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public HotelTreeNode(
		TreeViewer treeViewer,
		HotelCityTreeNode parent,
		Hotel hotel
	) {
		super(treeViewer, parent, hotel);
		
		hotelPK = hotel.getID();
		
		// observe Gates that belong to this Location
		roomDefinitionModel.addForeignKeyListener(this, hotelPK);
	}

	
	@Override
	public void dispose() {
		// disconnect from models
		try {
			roomDefinitionModel.removeForeignKeyListener(this, hotelPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		super.dispose();
	}
	
	// *
	// * Constructors and dispose()
	// *************************************************************************
	
	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *
	
	@Override
	public Class<?> getEntityType() {
		return Hotel.class;
	}
	

	@Override
	public Object getKey() {
		return hotelPK;
	}

	
	@Override
	public String getText() {
		String text = "";
		if (value != null) {
			text = value.getName().replace("\n", ", ");
		}
		return StringHelper.avoidNull(text);
	}

	
	@Override
	public String getToolTipText() {
		return HotelLabel.Hotel.getString();
	}

	
	@Override
	public Image getImage() {
		return IconRegistry.getImage("/icons/hotel16x11.png");
	}

	
	@Override
	protected void loadChildren() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			public void run() {
				_loadChildren();
			}
		});
	}

	
	private void _loadChildren() {
		try {
			ignoreDataChange = true;

			// get data from model
			List<RoomDefinitionVO> roomDefinitionList = 
				roomDefinitionModel.getUndeletedRoomDefinitionVOsByHotelPK(hotelPK);
			if (roomDefinitionList == null) {
				roomDefinitionList = CollectionsHelper.emptyList();
			}
			
			/* 
			 * Do not just remove all child-nodes and build new ones, because this will close
			 * all nodes, the user already opened. Instead replace the data of all nodes that 
			 * still exist, remove nodes of entities that do not exist anymore and create new 
			 * nodes for new entities.  
			 */
			
			// If there aren't any children create a TreeNode for every Gate.
			if (!hasChildren()) {
				// resize children-List
				ensureCapacityOfChildren(roomDefinitionList.size());
				
				for (RoomDefinitionVO roomDefinitionVO : roomDefinitionList) {
					// create new TreeNode
					RoomDefinitionTreeNode roomDefinitionTreeNode = new RoomDefinitionTreeNode(
						treeViewer, 
						this,		// parent
						roomDefinitionVO
					);

					// add TreeNode to list of children
					addChild(roomDefinitionTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.
				
				// put the list data of value into a map
				Map<Long, RoomDefinitionVO> map = RoomDefinitionVO.abstractVOs2Map(roomDefinitionList);
				
				// remove/refresh TreeNodes
				
				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<RoomDefinitionTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (RoomDefinitionTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					RoomDefinitionVO roomDefinitionVO = map.get(treeNode.getKey());
					
					if (roomDefinitionVO != null) {
						// Set new data to the TreeNode
						treeNode.setValue(roomDefinitionVO);
						// Remove data from map, so after the for-block the map
						// only contains new values
						map.remove(roomDefinitionVO.getID());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it. 
						removeChild(treeNode);
						treeNode.dispose();
					}
				}
				
				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + map.size());
				
				// add new TreeNodes for each new value
				for (RoomDefinitionVO roomDefinitionVO : map.values()) {
					RoomDefinitionTreeNode roomDefinitionTreeNode = new RoomDefinitionTreeNode(
						treeViewer, 
						this, 
						roomDefinitionVO
					);
					
					// add TreeNode to list of children
					addChild(roomDefinitionTreeNode);
				}
			}
		} 
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			hotelModel.refresh(hotelPK);
			
			// refresh data of child TreeNodes
			refreshChildren();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
    			// refresh data of children
    			roomDefinitionModel.refreshForeignKey(hotelPK);
    
    			// refresh data of our grandchildren
    			refreshGrandChildren();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

	// *************************************************************************
	// * Implementation of interfaces
	// *

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!ServerModel.getInstance().isLoggedIn()) {
			// do nothing, because all TreeNodes will be removed from root TreeNode 
			return;
		}
		
		if (ignoreDataChange) {
			return;
		}

		reloadChildren();
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

}

package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.EventHotelInfoVO;
import com.lambdalogic.messeinfo.hotel.data.EventHotelKey;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO_Position_Name_Comparator;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.ui.Activator;


/**
 * A node representing an Event Hotel Info.
 */
public class EventHotelInfoTreeNode extends TreeNode<Hotel> implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;

	private HotelModel hotelModel = HotelModel.getInstance();
	private HotelContingentModel hcModel = HotelContingentModel.getInstance();

	/* ignore ModifyEvent from GateModel that are fired when this TreeNode requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************


	private CacheModelListener<Long> hotelContingentModelListener = new CacheModelListener<Long>() {
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
	};


	// *************************************************************************
	// * Constructors and dispose()
	// *

	public EventHotelInfoTreeNode(
		TreeViewer treeViewer,
		EventHotelInfoListTreeNode parent,
		Hotel hotel,
		Long eventPK
	) {
		super(treeViewer, parent, hotel);

		this.eventPK = eventPK;

		// observe Hotel Contingent that belong to this Event
		hcModel.addForeignKeyListener(hotelContingentModelListener, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			hcModel.removeForeignKeyListener(hotelContingentModelListener, eventPK);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
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
		return EventHotelInfoVO.class;
	}


	@Override
	public EventHotelKey getKey() {
		return new EventHotelKey(eventPK, getHotelPK());
	}


	@Override
	public String getText() {
		String text = null;
		if (getHotel() != null) {
			text = getHotel().getName().replace("\n", ", ");
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		return HotelLabel.Hotel.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.HOTEL_EVENT);
	}


	@Override
	protected void loadChildren() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				_loadChildren();
			}
		});
	}


	private void _loadChildren() {
		try {
			ignoreDataChange = true;

    		// get data from model
    		List<HotelContingentCVO> hotelContingentCVOs = hcModel.getHotelContingentCVOsByEventAndHotel(
    			eventPK,
    			getHotelPK()
    		);
    		if (hotelContingentCVOs == null) {
    			hotelContingentCVOs = emptyList();
    		}

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every HotelContingent.
    		if (!hasChildren()) {
    			// resize children-List
				ensureCapacityOfChildren(hotelContingentCVOs.size());
				for (HotelContingentCVO hotelContingentCVO : hotelContingentCVOs) {
					HotelContingentTreeNode hcTreeNode = new HotelContingentTreeNode(
						treeViewer,
						this,
						hotelContingentCVO
					);

    				// add TreeNode to list of children
					addChild(hcTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, HotelContingentCVO> hcMap = AbstractCVO.abstractCVOs2Map(hotelContingentCVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
    			 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
    			 * between loadChildren() and _loadChildren()!
    			 */
				List<HotelContingentTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (HotelContingentTreeNode treeNode  : treeNodeList) {
					// get new data for this TreeNode
					HotelContingentCVO hotelContingentCVO = hcMap.get(treeNode.getKey());

					if (hotelContingentCVO != null) {
						// Set new data to the TreeNode
						treeNode.setValue(hotelContingentCVO);
						// Remove data from map, so after the for-block the map
						// only contains new values
						hcMap.remove(hotelContingentCVO.getPK());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + hcMap.size());

				// add new TreeNodes for each new value
				for (HotelContingentCVO hotelContingentCVO : hcMap.values()) {
					HotelContingentTreeNode hcTreeNode = new HotelContingentTreeNode(
						treeViewer,
						this,
						hotelContingentCVO
					);

					// add TreeNode to list of children
					addChild(hcTreeNode);
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
			/* Update data of this TreeNode by refreshing the corresponding Model.
			 * This leads to a call of dataChange() in the parent TreeNode, who gets the refreshed
			 * data from the Model and sets it to this TreeNode by calling setValue().
			 */
			hotelModel.refresh(getHotelPK());

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
    			/* Refresh data of children.
    			 * Refresh all hotel contingents of this event, because it is
    			 * not possible to refresh only those of this hotel.
    			 */
    			hcModel.refreshForeignKey(eventPK);

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
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return HotelContingentCVO_Position_Name_Comparator.getInstance().compare(
			(HotelContingentCVO) treeNode1.getValue(),
			(HotelContingentCVO) treeNode2.getValue()
		);
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	@Override
	public Long getEventId() {
		return eventPK;
	}


	public Long getHotelPK() {
		return getHotel().getID();
	}


	public Hotel getHotel() {
		return value;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

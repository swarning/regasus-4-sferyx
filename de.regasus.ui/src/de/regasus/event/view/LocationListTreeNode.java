package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.event.LocationModel;
import de.regasus.ui.Activator;


/**
 * A node representing the list of Locations of an Event.
 */
public class LocationListTreeNode
extends TreeNode<List<LocationVO>>
implements EventIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;

	// data of child TreeNodes
	private LocationModel locationModel = LocationModel.getInstance();

	// ignore ModifyEvent from LocationModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// **************************************************************************
	// * Constructors and dispose
	// *

	public LocationListTreeNode(TreeViewer treeViewer, EventTreeNode parent) {
		super(treeViewer, parent);

		// get eventPK from parent TreeNode
		eventPK = parent.getEventId();

		// observe Locations that belong to this Event
		locationModel.addForeignKeyListener(this, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			locationModel.removeForeignKeyListener(this, eventPK);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}

	// *
	// * Constructors and dispose
	// **************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		// this is a folder node that doesn't correspond to any entity
		return null;
	}


	@Override
	public Object getKey() {
		// this is a folder node that doesn't correspond to any entity
		return null;
	}


	@Override
	public String getText() {
		StringBuilder text = new StringBuilder(100);
		text.append(I18N.Locations);

		/* get number of Locations from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		Integer count = null;
		try {
			ignoreDataChange = true;
			count = locationModel.getLocationVOsByEventPK(eventPK).size();

			text.append(" (");
			text.append(count);
			text.append(")");
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}

		return text.toString();
	}


	@Override
	public String getToolTipText() {
		return null;
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.FOLDER);
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
			List<LocationVO> locationVOs = locationModel.getLocationVOsByEventPK(eventPK);
			if (locationVOs == null) {
				locationVOs = emptyList();
			}

			/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

			// If there aren't any children create a TreeNode for every Location.
			if (!hasChildren()) {
				// resize children-List
				ensureCapacityOfChildren(locationVOs.size());

				for (LocationVO locationVO : locationVOs) {
					LocationTreeNode locationTreeNode = new LocationTreeNode(
						treeViewer,
						LocationListTreeNode.this,
						locationVO
					);

					// add TreeNode to list of children
					addChild(locationTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, LocationVO> locationMap = AbstractVO.abstractVOs2Map(locationVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<LocationTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (LocationTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					LocationVO locationVO = locationMap.get(treeNode.getLocationPK());

					if (locationVO != null) {
						// set new data to the TreeNode
						treeNode.setValue(locationVO);
						// Remove data from map, so after the for-block the map
						// only contains new values
						locationMap.remove(locationVO.getID());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + locationMap.size());

				// add new TreeNodes for each new value
				for (LocationVO locationVO : locationMap.values()) {
					LocationTreeNode locationTreeNode = new LocationTreeNode(
						treeViewer,
						LocationListTreeNode.this,
						locationVO
					);

					// add TreeNode to list of children
					addChild(locationTreeNode);
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
		// no own data to refresh

		// refresh data of child TreeNodes
		refreshChildren();
	}


	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
    			// refresh data of all Locations of the current Event
    			locationModel.refreshForeignKey(eventPK);

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

	/* (non-Javadoc)
	 * @see com.lambdalogic.util.model.CacheModelListener#dataChange(com.lambdalogic.util.model.CacheModelEvent)
	 */
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

	// *************************************************************************
	// * Getter and setter
	// *

	@Override
	public Long getEventId() {
		return eventPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

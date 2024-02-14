package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.GateVO;
import com.lambdalogic.messeinfo.participant.data.LocationVO;
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
import de.regasus.event.GateModel;
import de.regasus.event.LocationModel;
import de.regasus.ui.Activator;


/**
 * A node representing a Location.
 */
public class LocationTreeNode
extends TreeNode<LocationVO>
implements EventIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long locationPK;

	/* Just used to refresh the data of this Location.
	 * Observing is not necessary, because the parent TreeNode is observing all its Locations.
	 * On any change the value of this TreeNode is set and the parent calls refreshTreeNode().
	 */
	private LocationModel locationModel = LocationModel.getInstance();

	// data of child TreeNodes
	private GateModel gateModel = GateModel.getInstance();

	/* ignore ModifyEvent from GateModel that are fired when this TreeNode requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public LocationTreeNode(
		TreeViewer treeViewer,
		LocationListTreeNode parent,
		LocationVO locationVO
	) {
		super(treeViewer, parent, locationVO);

		locationPK = value.getID();

		// observe Gates that belong to this Location
		gateModel.addForeignKeyListener(this, locationPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			gateModel.removeForeignKeyListener(this, locationPK);
		}
		catch (Exception e) {
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
		return LocationVO.class;
	}


	@Override
	public Object getKey() {
		return locationPK;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			text = value.getName();
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		return ParticipantLabel.Location.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.LOCATION);
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
			List<GateVO> gateVOs = gateModel.getGateVOsByLocationPK(locationPK);
			if (gateVOs == null) {
				gateVOs = emptyList();
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
				ensureCapacityOfChildren(gateVOs.size());

				for (GateVO gateVO : gateVOs) {
					// create new TreeNode
					GateTreeNode gateTreeNode = new GateTreeNode(
						treeViewer,
						this,		// parent
						gateVO
					);

					// add TreeNode to list of children
					addChild(gateTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, GateVO> gateMap = AbstractVO.abstractVOs2Map(gateVOs);


				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<GateTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (GateTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					GateVO gateVO = gateMap.get(treeNode.getGatePK());

					if (gateVO != null) {
						// set new data to the TreeNode
						treeNode.setValue(gateVO);
						// Remove data from map, so after the for-block the map
						// only contains new values
						gateMap.remove(gateVO.getID());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + gateMap.size());

				// add new TreeNodes for each new value
				for (GateVO gateVO : gateMap.values()) {
					GateTreeNode treeNode = new GateTreeNode(
						treeViewer,
						this, 		// parent
						gateVO
					);

					// add TreeNode to list of children
					addChild(treeNode);
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
			locationModel.refresh(locationPK);

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
    			gateModel.refreshForeignKey(locationPK);

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


	public Long getLocationPK() {
		return locationPK;
	}


	@Override
	public Long getEventId() {
		return value.getEventPK();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

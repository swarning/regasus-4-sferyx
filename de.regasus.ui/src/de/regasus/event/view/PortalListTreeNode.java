package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.portal.view.PortalTreeRoot;
import de.regasus.ui.Activator;

public class PortalListTreeNode
extends TreeNode<List<Portal>>
implements EventIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;

	// data of child TreeNodes
	private PortalModel portalModel = PortalModel.getInstance();

	// ignore ModifyEvent from PortalModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public PortalListTreeNode(TreeViewer treeViewer, EventTreeNode parent) {
		super(treeViewer, parent);

		// get eventPK from parent TreeNode
		eventPK = parent.getEventId();

		// observe PortalModels that belong to this Event
		portalModel.addForeignKeyListener(this, eventPK);
	}


	public PortalListTreeNode(TreeViewer treeViewer, PortalTreeRoot parent) {
		super(treeViewer, parent);

		// observe PortalModels that belong to this Event
		portalModel.addForeignKeyListener(this, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			portalModel.removeForeignKeyListener(this, eventPK);
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
		text.append(PortalI18N.Portals.getString());

		/* get number of Portals from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		try {
			ignoreDataChange = true;
			Integer count = portalModel.getPortalsByEvent(eventPK).size();

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
    		List<Portal> portalList = portalModel.getPortalsByEvent(eventPK);
    		if (portalList == null) {
    			portalList = emptyList();
    		}

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every Portal.
    		if (!hasChildren()) {
    			// resize children-List
    			ensureCapacityOfChildren(portalList.size());

    			for (Portal portal : portalList) {
    				// create new TreeNode
    				PortalTreeNode treeNode = new PortalTreeNode(
    					treeViewer,
    					this,
    					portal
    				);

    				// add TreeNode to list of children
    				addChild(treeNode);
    			}
    		}
    		else {
    			// If there are already children, we've to match the new List with the existing children.

    			// put the list data of value into a map
    			Map<Long, Portal> portalMap = Portal.getEntityMap(portalList);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
    			List<PortalTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
    			for (PortalTreeNode treeNode : treeNodeList) {
    				// get new data for this TreeNode
    				Portal portal = portalMap.get(treeNode.getKey());

    				if (portal != null) {
    					// Set new data to the TreeNode
    					treeNode.setValue(portal);
    					// remove data from map, so after the for block the map only contains new values
    					portalMap.remove(portal.getId());
    				}
    				else {
    					// The data doesn't exist anymore: Remove the TreeNode
    					// from the children-List and dispose it.
    					removeChild(treeNode);
    					treeNode.dispose();
    				}
    			}

    			// resize children-List if necessary
    			ensureCapacityOfChildren(getChildCount() + portalMap.size());

    			// add new TreeNodes for each new value
    			for (Portal portal : portalMap.values() ) {
    				PortalTreeNode treeNode = new PortalTreeNode(
    					treeViewer,
    					this,
    					portal
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
		// no own data to refresh

		// refresh data of child TreeNodes
		refreshChildren();
	}


	@Override
	public void refreshChildren() {
		try {
			if ( isChildrenLoaded() ) {
    			// refresh data of all Portals of the current Event
    			portalModel.refreshForeignKey(eventPK);

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
	 * @see com.lambdalogic.util.rcp.model.CacheModelListener#dataChange(com.lambdalogic.util.rcp.model.CacheModelEvent)
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

		/* If we receive an Event from PortalModel, children are loaded.
		 * Event for update events (that have no effect of the structure) reloadChildren() is called,
		 * because it sets the fresh Portal entities to the PortalTreeNodes. So the
		 * PortalTreeNodes don't have to observe the PortalModel.
		 */
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

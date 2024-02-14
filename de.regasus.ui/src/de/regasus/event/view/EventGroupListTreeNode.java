package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventGroup;
import de.regasus.event.EventGroupModel;
import de.regasus.ui.Activator;

/**
 * The visible root node of the event master data tree.
 * It is the node which contains the event nodes.
 */
public class EventGroupListTreeNode extends TreeNode<Collection<EventVO>> {

	// *************************************************************************
	// * Attributes
	// *

	// data of child TreeNodes
	private EventGroupModel eventGroupModel = EventGroupModel.getInstance();

	// ignore ModifyEvent from EventModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public EventGroupListTreeNode(TreeViewer treeViewer, EventTreeRoot parent) {
		super(treeViewer, parent);

		// Connect to model
		eventGroupModel.addListener(eventGroupModelListener);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			eventGroupModel.removeListener(eventGroupModelListener);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}


	private CacheModelListener<Long> eventGroupModelListener = new CacheModelListener<>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long> event) {
    		if ( !ServerModel.getInstance().isLoggedIn() ) {
    			// do nothing, because all TreeNodes will be removed from root TreeNode
    			return;
    		}

    		if (ignoreDataChange) {
    			return;
    		}

    		// if we receive an Event from EventModel, children are loaded
    		reloadChildren();
    	}
	};

	// *
	// * Constructors and dispose()
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return null;
	}


	@Override
	public Object getKey() {
		return null;
	}


	@Override
	public String getText() {
		return ParticipantLabel.EventGroups.getString();
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
    		List<EventGroup> eventGroups = eventGroupModel.getAllEventGroups();
    		if (eventGroups == null) {
    			eventGroups = emptyList();
    		}

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every Event.
    		if (!hasChildren()) {
    			// resize children-List
				ensureCapacityOfChildren( eventGroups.size() );

	    		for (EventGroup eventGroup : eventGroups) {
	    			EventGroupTreeNode eventGroupTreeNode = new EventGroupTreeNode(treeViewer, this, eventGroup);
	    			addChild(eventGroupTreeNode);
	    		}
    		}
    		else {
    			// build map from eventGroupPK to EventGroup
    			Map<Long, EventGroup> eventGroupMap = EventGroup.getEntityMap(eventGroups);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
    			List<EventGroupTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
    			for (EventGroupTreeNode treeNode : treeNodeList) {
    				// get new data for this TreeNode
    				EventGroup eventGroup = eventGroupMap.get( treeNode.getEventGroupId() );

    				if (eventGroup != null) {
    					// Set new data to the TreeNode
    					treeNode.setValue(eventGroup);
    					// remove data from map, so after the for block the map only contains new values
    					eventGroupMap.remove(eventGroup.getId());
    				}
    				else {
    					// The data doesn't exist anymore: Remove the TreeNode
    					// from the children-List and dispose it.
    					removeChild(treeNode);
    					treeNode.dispose();
    				}
    			}

    			// resize children-List if necessary
    			ensureCapacityOfChildren(getChildCount() + eventGroupMap.size());

    			// add new TreeNodes for each new eventVOs
    			for (EventGroup eventGroup : eventGroupMap.values()) {
    				EventGroupTreeNode eventTreeNode = new EventGroupTreeNode(treeViewer, this, eventGroup);

					// add TreeNode
    				addChild(eventTreeNode);
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
    			// refresh data of all child TreeNodes (EventTreeNode)
    			eventGroupModel.refresh();

    			// refresh grandchildren
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
	public String toString() {
		return getClass().getSimpleName();
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************


}

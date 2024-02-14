package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.observer.DefaultEvent;
import com.lambdalogic.util.observer.Observer;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventFilter;
import de.regasus.event.EventGroup;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;

/**
 * The visible tree node for EventGroups of the event master data tree.
 * The child nodes of this tree node are the EventTreeNodes of the Events of this EventGroup.
 * There is one EventGroupTreeNode for Events that do not belong to any EventGroup.
 */
public class EventGroupTreeNode extends TreeNode<EventGroup> {

	// *************************************************************************
	// * Attributes
	// *

	/**
	 * ID of this EventGroup.
	 * If null, this EventGroupTreeNode is the parent node for all Events that do not belong to any EventGroup.
	 */
	private Long eventGroupId;

	// Model for child TreeNodes
	private EventModel eventModel = EventModel.getInstance();

	// ignore ModifyEvent from EventModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	private EventFilter eventFilter;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public EventGroupTreeNode(TreeViewer treeViewer, EventGroupListTreeNode parent, EventGroup eventGroup) {
		super(treeViewer, parent);

		value = eventGroup;

		if (value != null) {
			eventGroupId = value.getId();
		}

		eventModel.addForeignKeyListener(eventModelListener, eventGroupId);

		// observe EventFilter
		eventFilter = (EventFilter) treeViewer.getData( EventFilter.class.getName() );
		if (eventFilter != null) {
			eventFilter.addObserver(eventFilterObserver);
		}
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			eventModel.removeForeignKeyListener(eventModelListener, eventGroupId);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}


		super.dispose();
	}


	private Observer<DefaultEvent> eventFilterObserver = new Observer<>() {
		@Override
		public void update(Object source, DefaultEvent event) {
			// update the text label of this TreeNode because its number of Events could have changed
			refreshTreeViewer();

			reloadChildren();
		}
	};


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<>() {
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
		return EventGroup.class;
	}


	@Override
	public Object getKey() {
		return eventGroupId;
	}


	public Long getEventGroupId() {
		return eventGroupId;
	}


	private int getEventCount() throws Exception {
		try {
			ignoreDataChange = true;
			Collection<EventVO> eventVOs = eventModel.getEventVOsByGroup(eventGroupId);
			if (eventFilter != null) {
				eventVOs = eventFilter.filter(eventVOs);
			}
			return eventVOs.size();
		}
		finally {
			ignoreDataChange = false;
		}
	}


	@Override
	public String getText() {
		StringBuilder text = new StringBuilder(256);

		if (value != null) {
			text.append( value.getName().getString() );
		}

		/* get number of Events from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		try {
			int eventCount = getEventCount();

			text.append(" (");
			text.append(eventCount);
			text.append(")");
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return text.toString();
	}


	@Override
	public String getToolTipText() {
		if (value != null) {
			LanguageString description = value.getDescription();
			if (description != null) {
				return description.getString();
			}
		}

		return null;
	}


	@Override
	public Image getImage() {
		int eventCount = 0;
		try {
			eventCount = getEventCount();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleSilentError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		if (eventCount > 0) {
			return IconRegistry.getImage(IImageKeys.EVENT_GROUP);
		}
		else {
			return IconRegistry.getImage(IImageKeys.EVENT_GROUP_EMPTY);
		}
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


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void _loadChildren() {
		try {
			ignoreDataChange = true;

    		// get data from model
			boolean onlyUnclosedEvents = ! eventModel.isShowClosedEvents();
    		Collection<EventVO> eventVOs = eventModel.getEventVOsByGroup(eventGroupId, onlyUnclosedEvents);
    		if (eventVOs == null) {
    			eventVOs = emptyList();
    		}
    		else if (eventFilter != null) {
    			eventVOs = eventFilter.filter(eventVOs);
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
				ensureCapacityOfChildren(eventVOs.size());

    			for (EventVO eventVO : eventVOs) {
    				// create new TreeNode
    				EventTreeNode eventTreeNode = new EventTreeNode(
    					treeViewer,
    					this,
    					eventVO
    				);

    				// add TreeNode to list of children
    				addChild(eventTreeNode);
    			}
    		}
    		else {
    			// build map from eventPK to EventVO
    			Map<Long, EventVO> eventMap = EventVO.abstractVOs2Map(eventVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<EventTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
    			for (EventTreeNode treeNode : treeNodeList) {
    				// get new data for this TreeNode
    				EventVO eventVO = eventMap.get(treeNode.getEventId());

    				if (eventVO != null) {
    					// Set new data to the TreeNode
    					treeNode.setValue(eventVO);
    					// remove data from map, so after the for block the map only contains new values
    					eventMap.remove(eventVO.getID());
    				}
    				else {
    					// The data doesn't exist anymore: Remove the TreeNode
    					// from the children-List and dispose it.
    					removeChild(treeNode);
    					treeNode.dispose();
    				}
    			}

    			// resize children-List if necessary
    			ensureCapacityOfChildren(getChildCount() + eventMap.size());

    			// add new TreeNodes for each new eventVOs
    			for (EventVO eventVO : eventMap.values()) {
    				EventTreeNode eventTreeNode = new EventTreeNode(
    					treeViewer,
    					this,
    					eventVO
    				);

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
    			eventModel.refreshForeignKey(eventGroupId);

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
		return new StringBuilder(128)
			.append( getClass().getSimpleName() )
			.append(" (").append( getText() ).append(")")
			.toString();
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************


}

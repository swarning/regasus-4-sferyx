package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.tree.DefaultTreeNode;
import com.lambdalogic.util.rcp.tree.TreeHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.event.EventModel;
import de.regasus.ui.Activator;


/**
 * The internal root node of the event master data tree.
 * This node is the technical root node that is not visible.
 */
public class EventTreeRoot extends DefaultTreeNode<Object> {

	private EventGroupListTreeNode eventGroupListTreeNode;

	private ServerModel serverModel = ServerModel.getInstance();
	private EventModel eventModel = EventModel.getInstance();


	/**
	 * Observe {@link ServerModel} to handle login ad logout.
	 */
	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;

			if ( serverModelEvent.getType() == ServerModelEventType.LOGIN ) {
				handleLogin();
			}
			else if ( serverModelEvent.getType() == ServerModelEventType.LOGOUT ) {
				handleLogout();
			}
		}
	};


	public EventTreeRoot(TreeViewer treeViewer, ModifySupport modifySupport) {
		super(treeViewer, null);

		setModifySupport(modifySupport);

		initTree();

		serverModel.addListener(serverModelListener);
	}


	private void initTree() {
		// load all Events in advance to avoid multiple requests later
		try {
			eventModel.getAllEventVOs();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		eventGroupListTreeNode = new EventGroupListTreeNode(treeViewer, this);
		addChild(eventGroupListTreeNode);
	}


	private void handleLogout() {
		removeAll();
		refreshTreeViewer();
	}


	private void handleLogin() {
		initTree();
		refreshTreeViewer();

		// open node to show Events
		if (getChildren() != null && !getChildren().isEmpty()) {

			TreeHelper.runWhenNotBusy(
				new Runnable() {
					@Override
					public void run() {
						treeViewer.setExpandedState(getChildren().get(0), true);
					}
				},
				treeViewer
			);
		}
	}


	@Override
	public void dispose() {
		// disconnect from ServerModel
		try {
			serverModel.removeListener(serverModelListener);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}

}

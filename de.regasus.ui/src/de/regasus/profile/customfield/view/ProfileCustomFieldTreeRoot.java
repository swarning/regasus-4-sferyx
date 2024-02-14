/**
 * ProfileCustomFieldTreeRoot.java
 * created on 21.11.2013 14:55:51
 */
package de.regasus.profile.customfield.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.tree.DefaultTreeNode;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;
import de.regasus.ui.Activator;

public class ProfileCustomFieldTreeRoot extends DefaultTreeNode<Object> {

	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;

			if (serverModelEvent.getType() == ServerModelEventType.LOGIN) {
				handleLogin();
			}
			else if (serverModelEvent.getType() == ServerModelEventType.LOGOUT) {
				handleLogout();
			}
		}
	};


	public ProfileCustomFieldTreeRoot(TreeViewer treeViewer, ModifySupport modifySupport) {
		super(treeViewer, null);
		setModifySupport(modifySupport);
		ServerModel.getInstance().addListener(serverModelListener);
	}


	private void handleLogout() {
		removeAll();
		refreshTreeViewer();
	}


	private void handleLogin() {
		refreshTreeViewer();
	}


	@Override
	public void dispose() {
		// disconnect from ServerModel
		try {
			ServerModel.getInstance().removeListener(serverModelListener);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		super.dispose();
	}


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
		return null;
	}


	@Override
	public String getToolTipText() {
		return null;
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.MD_CUSTOM_FIELD_LIST);
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
		/**
		 * ProfileCustomFieldListTreeNode has always a fixed number children, which are folders, whose children in turn
		 * are lazily loaded. So we create them here once, and that's it.
		 */
		if (!isChildrenLoaded()) {
			// determine ProfileCustomFieldGroupLocations
			ProfileCustomFieldGroupLocation[] locations = ProfileCustomFieldGroupLocation.values();

			// resize children-List if necessary
			ensureCapacityOfChildren(locations.length);

			// create one child node for every ProfileCustomFieldGroupLocation
			for (ProfileCustomFieldGroupLocation location : locations) {
				TreeNode<?> locationTreeNode = new ProfileCustomFieldGroupLocationTreeNode(treeViewer, this, location);
				addChild(locationTreeNode);
			}
		}
	}


	@Override
	public void refresh() {
		refreshChildren();
	}


	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
				/*
				 * Refreshing ProfileCustomFieldModel and ProfileCustomFieldGroupModel will update all subsequent nodes.
				 * No further recursive refresh operations are necessary, because all subsequent data depends on these 2
				 * Models.
				 *
				 * Refreshing all Custom Field Groups here is more performance than traversing the whole sub-tree and
				 * calling refreshChildren() for each node.
				 */
				ProfileCustomFieldModel.getInstance().refresh();
				ProfileCustomFieldGroupModel.getInstance().refresh();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		/*
		 * All child TreeNodes are of type ProfileCustomFieldGroupLocationTreeNode. Their values are enums of the type
		 * ProfileCustomFieldGroupLocation. Compare the values by their ordinal numbers.
		 */
		ProfileCustomFieldGroupLocationTreeNode locationTreeNode1 = (ProfileCustomFieldGroupLocationTreeNode) treeNode1;
		ProfileCustomFieldGroupLocationTreeNode locationTreeNode2 = (ProfileCustomFieldGroupLocationTreeNode) treeNode2;

		ProfileCustomFieldGroupLocation location1 = locationTreeNode1.getValue();
		ProfileCustomFieldGroupLocation location2 = locationTreeNode2.getValue();

		return location1.compareTo(location2);
	}

}

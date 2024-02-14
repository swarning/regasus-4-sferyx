package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.kernel.AbstractEntity;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.ui.Activator;

/**
 * A node representing the list of registration forms of an event.
 */
public class RegistrationFormConfigListTreeNode
extends TreeNode<List<RegistrationFormConfig>>
implements EventIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;

	// data of child TreeNodes
	private RegistrationFormConfigModel rfcModel = RegistrationFormConfigModel.getInstance();

	// ignore ModifyEvent from RegistrationFormConfigModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public RegistrationFormConfigListTreeNode(TreeViewer treeViewer, EventTreeNode parent) {
		super(treeViewer, parent);

		// get eventPK from parent TreeNode
		eventPK = parent.getEventId();

		// observe Registration Form Configs that belong to this Event
		rfcModel.addForeignKeyListener(this, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			rfcModel.removeForeignKeyListener(this, eventPK);
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
		text.append(OnlineFormI18N.WebsiteConfigurations);

		/* get number of Registration Form Configs from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		Integer count = null;
		try {
			ignoreDataChange = true;
			count = rfcModel.getRegistrationFormConfigsByEventPK(eventPK).size();

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
			// get data from model
			List<RegistrationFormConfig> registrationFormConfigs = rfcModel.getRegistrationFormConfigsByEventPK(eventPK);
			if (registrationFormConfigs == null) {
				registrationFormConfigs = emptyList();
			}

			/*
			 * Do not just remove all child-nodes and build new ones, because this will close
			 * all nodes, the user already opened. Instead replace the data of all nodes that
			 * still exist, remove nodes of entities that do not exist anymore and create new
			 * nodes for new entities.
			 */


			// If there aren't any children create a TreeNode for every RegistrationFormConfig.
			if (!hasChildren()) {
				// resize children-List
				ensureCapacityOfChildren(registrationFormConfigs.size());

				for (RegistrationFormConfig config : registrationFormConfigs) {
					RegistrationFormConfigTreeNode configTreeNode = new RegistrationFormConfigTreeNode(
						treeViewer,
						this,
						config
					);

					// add TreeNode to list of children
					addChild(configTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, RegistrationFormConfig> configMap = AbstractEntity.getEntityMap(registrationFormConfigs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<RegistrationFormConfigTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (RegistrationFormConfigTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					RegistrationFormConfig config = configMap.get(treeNode.getKey());

					if (config != null) {
						// Set new data to the TreeNode
						treeNode.setValue(config);
						// Remove data from map, so after the for-block the map
						// only contains new values
						configMap.remove(config.getId());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + configMap.size());

				// add new TreeNodes for each new value
				for (RegistrationFormConfig config : configMap.values() ) {
					RegistrationFormConfigTreeNode configTreeNode = new RegistrationFormConfigTreeNode(
						treeViewer,
						this,
						config
					);

					// add TreeNode to list of children
					addChild(configTreeNode);
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
    			// refresh data of all Registration Form Configs of the current Event
    			rfcModel.refreshForeignKey(eventPK);

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

		// If we receive an Event from RegistrationFormConfigModel, children are loaded
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

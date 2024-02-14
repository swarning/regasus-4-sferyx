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
import de.regasus.portal.Page;
import de.regasus.portal.PageComparator;
import de.regasus.portal.PageModel;
import de.regasus.portal.PortalI18N;
import de.regasus.ui.Activator;

public class PageListTreeNode extends TreeNode<List<Page>> implements CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long portalPK;

	// data of child TreeNodes
	private PageModel pageModel = PageModel.getInstance();

	// ignore ModifyEvent from PageModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public PageListTreeNode(TreeViewer treeViewer, PortalTreeNode parent) {
		super(treeViewer, parent);

		// get portalPK from parent TreeNode
		portalPK = parent.getPortalId();

		// observe PageModels that belong to this Portal
		pageModel.addForeignKeyListener(this, portalPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			pageModel.removeForeignKeyListener(this, portalPK);
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
		text.append(PortalI18N.Pages.getString());

		/* get number of Pages from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		Integer count = null;
		try {
			ignoreDataChange = true;
			count = pageModel.getPagesByPortal(portalPK).size();

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
    		List<Page> pageList = pageModel.getPagesByPortal(portalPK);

    		if (pageList == null) {
    			pageList = emptyList();
    		}

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every Page.
    		if (!hasChildren()) {
    			// resize children-List
    			ensureCapacityOfChildren(pageList.size());

    			for (Page page : pageList) {
    				// create new TreeNode
    				PageTreeNode treeNode = new PageTreeNode(treeViewer, this, page);

    				// add TreeNode to list of children
    				addChild(treeNode);
    			}
    		}
    		else {
    			// If there are already children, we've to match the new List with the existing children.

    			// put the list data of value into a map
    			Map<Long, Page> pageMap = Page.getEntityMap(pageList);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
    			List<PageTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
    			for (PageTreeNode treeNode : treeNodeList) {
    				// get new data for this TreeNode
    				Page page = pageMap.get(treeNode.getKey());

    				if (page != null) {
    					// Set new data to the TreeNode
    					treeNode.setValue(page);
    					// remove data from map, so after the for block the map only contains new values
    					pageMap.remove(page.getId());
    				}
    				else {
    					// The data doesn't exist anymore: Remove the TreeNode
    					// from the children-List and dispose it.
    					removeChild(treeNode);
    					treeNode.dispose();
    				}
    			}

    			// resize children-List if necessary
    			ensureCapacityOfChildren(getChildCount() + pageMap.size());

    			// add new TreeNodes for each new value
    			for (Page page : pageMap.values() ) {
    				PageTreeNode treeNode = new PageTreeNode(
    					treeViewer,
    					this,
    					page
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
			if (isChildrenLoaded()) {
    			// refresh data of all Pages of the current Portal
    			pageModel.refreshForeignKey(portalPK);

    			// refresh data of our grandchildren
    			refreshGrandChildren();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		return PageComparator.getInstance().compare(
			((PageTreeNode) treeNode1).getValue(),
			((PageTreeNode) treeNode2).getValue()
		);
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

		/* If we receive an Event from PageModel, children are loaded.
		 * Event for update events (that have no effect of the structure) reloadChildren() is called,
		 * because it sets the fresh Page entities to the PageTreeNodes. So the
		 * PageTreeNodes don't have to observe the PageModel.
		 */
		reloadChildren();
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	public Long getPortalPK() {
		return portalPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

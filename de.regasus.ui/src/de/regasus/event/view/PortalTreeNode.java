package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;

public class PortalTreeNode extends TreeNode<Portal> implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long portalId;

	/* Just used to refresh the data of this Portal.
	 * Observing this Portal is not necessary, because the parent PortalListTreeNode is observing
	 * all Portals. On any change the value of this PortalTreeNode is set and refreshTreeNode()
	 * of the parent PortalListTreeNode is called.
	 */
	private PortalModel portalModel = PortalModel.getInstance();

	/**
	 * A child tree node that is always there, showing a folder, and under it the list of Page Layouts as soon as the
	 * folder gets opened.
	 */
	private PageLayoutListTreeNode pageLayoutListTreeNode;
	private PageListTreeNode pageListTreeNode;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors
	// *

	public PortalTreeNode(
		TreeViewer treeViewer,
		PortalListTreeNode parent,
		Portal portal
	) {
		super(treeViewer, parent);

		value = portal;

		portalId = value.getId();
	}

	// *
	// * Constructors
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return Portal.class;
	}


	@Override
	public Object getKey() {
		return portalId;
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
		return PortalI18N.Portal.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.PORTAL);
	}


	@Override
	public boolean hasChildren() {
		return true;
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
			/**
			 * PortalNode has a fixed number of children which are folders, whose children in turn are lazily loaded.
			 * So we create them here once, and that's it.
			 */
			if (!isChildrenLoaded()) {
				// The folder for Page Layouts
				pageLayoutListTreeNode = new PageLayoutListTreeNode(treeViewer, this);
				addChild(pageLayoutListTreeNode);

				// The folder for Pages
				pageListTreeNode = new PageListTreeNode(treeViewer, this);
				addChild(pageListTreeNode);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			portalModel.refresh(portalId);

			// refresh data of child TreeNodes
			refreshChildren();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refreshChildren() {
		if (isChildrenLoaded()) {
    		// the data and the structure of our direct children is fix, so there is nothing to refresh

    		// refresh grandchildren
    		refreshGrandChildren();
		}
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************


	// *************************************************************************
	// * Getter and setter
	// *

	public Long getPortalId() {
		return portalId;
	}


	@Override
	public Long getEventId() {
		return value.getEventId();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

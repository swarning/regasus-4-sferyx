package de.regasus.event.view;

import java.util.Collection;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Page;
import de.regasus.portal.PageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalConfig;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;

public class PageTreeNode extends TreeNode<Page> {

	// *************************************************************************
	// * Attributes
	// *

	private Long id;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors
	// *

	public PageTreeNode(
		TreeViewer treeViewer,
		PageListTreeNode parent,
		Page page
	) {
		super(treeViewer, parent);

		value = page;

		id = value.getId();
	}

	// *
	// * Constructors
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return Page.class;
	}


	@Override
	public Object getKey() {
		return id;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			text = value.getName().getString();
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		return PortalI18N.Page.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.PAGE);
	}


	@Override
	public boolean isStrikeOut() {
		boolean strikeOut = false;

		try {
    		String pageKey = value.getKey();
    		Long portalId = value.getPortalId();

    		Portal portal = PortalModel.getInstance().getPortal(portalId);
    		PortalConfig portalConfig = portal.getPortalConfig();
    		Collection<String> enabledPageKeys = portalConfig.getEnabledPageKeys();

    		strikeOut = enabledPageKeys != null && !enabledPageKeys.contains(pageKey);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return strikeOut;
	}


	@Override
	public boolean isLeaf() {
		return true;
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			PageModel.getInstance().refresh(id);

			// refresh data of child TreeNodes
			// this TreeNode doesn't have any children
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************


	// *************************************************************************
	// * Getter and setter
	// *

	public Long getPageId() {
		return id;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

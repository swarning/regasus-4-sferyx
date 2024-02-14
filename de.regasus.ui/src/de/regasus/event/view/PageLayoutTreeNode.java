package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;
import de.regasus.portal.PageLayoutModel;
import de.regasus.portal.PortalI18N;
import de.regasus.ui.Activator;

public class PageLayoutTreeNode extends TreeNode<PageLayout> {

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

	public PageLayoutTreeNode(
		TreeViewer treeViewer,
		PageLayoutListTreeNode parent,
		PageLayout pageLayout
	) {
		super(treeViewer, parent);

		value = pageLayout;

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
		return PageLayout.class;
	}


	@Override
	public Object getKey() {
		return id;
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
		return PortalI18N.PageLayout.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.PAGE_LAYOUT);
	}


	@Override
	public boolean isLeaf() {
		return true;
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			PageLayoutModel.getInstance().refresh(id);

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

	public Long getPageLayoutId() {
		return id;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

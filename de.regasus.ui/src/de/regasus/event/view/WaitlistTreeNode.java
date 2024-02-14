package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.programme.WaitList;
import de.regasus.programme.WaitListModel;
import de.regasus.ui.Activator;

public class WaitlistTreeNode
extends TreeNode<Long>
implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	// Just used to refresh the data of this Wait List.
	private WaitListModel wlModel = WaitListModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors
	// *

	public WaitlistTreeNode(
		TreeViewer treeViewer,
		ProgrammePointTreeNode parent,
		Long programmePointPK
	) {
		// set programmePointPK as value
		super(treeViewer, parent, programmePointPK);
	}

	// *
	// * Constructors
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return WaitList.class;
	}


	@Override
	public Object getKey() {
		// value is Long of Programme Point
		return value;
	}


	@Override
	public String getText() {
		return I18N.WaitlistTreeNode_Text;
	}


	@Override
	public String getToolTipText() {
		return null;
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.WAITLIST);
	}


	@Override
	public boolean isLeaf() {
		return true;
	}


	@Override
	public void refresh() {
		/*
		 * The parent node takes the responsibility to refresh this node, therefore we don't have to
		 * be listeners ourselves, but just fire the refresh request to the model.
		 */

		try {
			// refresh data of this TreeNode (value == Programme Point Long)
			wlModel.refresh(value);

			// no child TreeNodes to refresh
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

	@Override
	public Long getEventId() {
		// parent is always a ProgrammePointTreeNode because of the constructor
		ProgrammePointTreeNode ppTreeNode = (ProgrammePointTreeNode) getParent();
		return ppTreeNode.getEventId();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

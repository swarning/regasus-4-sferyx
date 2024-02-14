package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.programme.WorkGroupModel;
import de.regasus.programme.workgroup.IWorkGroupIdProvider;
import de.regasus.ui.Activator;

public class WorkGroupTreeNode extends TreeNode<WorkGroupVO> implements EventIdProvider, IWorkGroupIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long workGroupID;

	/* Just used to refresh the data of this Work Group.
	 * Observing this Work Group is not necessary, because the parent TreeNode
	 * is observing all its WorkGroups. On any change the value of this TreeNode is set and
	 * refreshTreeNode() of the parent is called.
	 */
	private WorkGroupModel wgModel = WorkGroupModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// **************************************************************************
	// * Constructors
	// *

	public WorkGroupTreeNode(
		TreeViewer treeViewer,
		ProgrammePointTreeNode parent,
		WorkGroupVO workGroupVO
	) {
		super(treeViewer, parent);

		value = workGroupVO;
		workGroupID = workGroupVO.getID();
	}

	// *
	// * Constructors
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return WorkGroupVO.class;
	}


	@Override
	public Long getKey() {
		return value.getID();
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
		return ParticipantLabel.WorkGroup.getString();
	}


	@Override
	public Image getImage() {
		Image image = null;

		if ( value.isCancelled() ) {
			image = IconRegistry.getImage(IImageKeys.WORK_GROUP_CANCELLED);
		}
		else {
			image = IconRegistry.getImage(IImageKeys.WORK_GROUP);
		}

		return image;
	}


	@Override
	public boolean isStrikeOut() {
		return value.isCancelled();
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
			// refresh data of this TreeNode
			wgModel.refresh(workGroupID);

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

	public void setWorkGroupVO(WorkGroupVO workGroupVO) {
		value = workGroupVO;
	}


	public Long getProgrammeOfferingPK() {
		if (value != null) {
			return value.getProgrammePointPK();
		}
		else {
			return null;
		}
	}


	@Override
	public Long getEventId() {
		// parent is always a ProgrammePointTreeNode because of the constructor
		return ((ProgrammePointTreeNode) getParent()).getEventId();
	}


	@Override
	public Long getWorkGroupId() {
		return workGroupID;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

package de.regasus.event.view;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.GateVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.tree.TreeNode;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.event.GateModel;
import de.regasus.ui.Activator;


/**
 * A node representing a Gate.
 */
public class GateTreeNode
extends TreeNode<GateVO>
implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long gatePK;

	/* Just used to refresh the data of this Gate.
	 * Observing this Gate is not necessary, because the parent TreeNode is observing all its Gates.
	 * On any change the value of this TreeNode is set and refreshTreeNode() of the parent is called.
	 */
	private GateModel gateModel = GateModel.getInstance();

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructor
	// *

	public GateTreeNode(
		TreeViewer treeViewer,
		LocationTreeNode parent,
		GateVO gateVO
	) {
		super(treeViewer, parent, gateVO);

		gatePK = gateVO.getID();
	}

	// *
	// * Constructor
	// *************************************************************************

	// *************************************************************************
	// * Implementation of abstract methods from TreeNode
	// *

	@Override
	public Class<?> getEntityType() {
		return GateVO.class;
	}


	@Override
	public Object getKey() {
		return gatePK;
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
		return ParticipantLabel.Gate.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.GATE);
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
			gateModel.refresh(gatePK);

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
		// parent is always a LocationTreeNode because of the constructor
		return ((LocationTreeNode) getParent()).getEventId();
	}


	public Long getLocationPK() {
		return value.getLocationPK();
	}


	public Long getGatePK() {
		return gatePK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

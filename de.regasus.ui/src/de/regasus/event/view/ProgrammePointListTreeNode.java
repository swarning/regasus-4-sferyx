package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO_Position_Comparator;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

/**
 * A node representing the list of Programme Points of an Event.
 */
public class ProgrammePointListTreeNode extends TreeNode<List<ProgrammePointVO>> implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;

	// data of child TreeNodes
	private ProgrammePointModel programmePointModel = ProgrammePointModel.getInstance();

	// ignore ModifyEvent from ProgrammePointModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************


	private CacheModelListener<Long> programmePointModelListener = new CacheModelListener<Long>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long> event) {
    		if (!ServerModel.getInstance().isLoggedIn()) {
    			// do nothing, because all TreeNodes will be removed from root TreeNode
    			return;
    		}

    		if (ignoreDataChange) {
    			return;
    		}

    		/* If we receive an Event from ProgrammePointModel, children are loaded.
    		 * Event for update events (that have no effect of the structure) reloadChildren() is called,
    		 * because it sets the fresh ProgrammePointVOs to the ProgrammePointTreeNodes.
    		 * So the ProgrammePointTreeNodes don't have to observe the ProgrammePointModel.
    		 */
    		reloadChildren();
    	}
	};


	// *************************************************************************
	// * Constructors and dispose()
	// *

	public ProgrammePointListTreeNode(TreeViewer treeViewer, EventTreeNode parent) {
		super(treeViewer, parent);

		// get eventPK from parent TreeNode
		eventPK = parent.getEventId();

		// observe Programme Points that belong to this Event
		programmePointModel.addForeignKeyListener(programmePointModelListener, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			programmePointModel.removeForeignKeyListener(programmePointModelListener, eventPK);
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
		text.append(ParticipantLabel.ProgrammePoints.getString());

		/* get number of Programme Points from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		Integer count = null;
		try {
			ignoreDataChange = true;
			count = programmePointModel.getProgrammePointVOsByEventPK(eventPK, true).size();

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
		return IconRegistry.getImage(IImageKeys.MD_PROGRAMME_POINT_FOLDER);
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
    		List<ProgrammePointVO> programmePointVOs = programmePointModel.getProgrammePointVOsByEventPK(eventPK, true);
    		if (programmePointVOs == null) {
    			programmePointVOs = emptyList();
    		}

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every ProgrammePoint.
    		if (!hasChildren()) {
    			// resize children-List
    			ensureCapacityOfChildren(programmePointVOs.size());

    			for (ProgrammePointVO programmePointVO : programmePointVOs) {
    				// create new TreeNode
    				ProgrammePointTreeNode ppTreeNode = new ProgrammePointTreeNode(
    					treeViewer,
    					this,
    					programmePointVO
    				);

    				// add TreeNode to list of children
    				addChild(ppTreeNode);
    			}
    		}
    		else {
    			// If there are already children, we've to match the new List with the existing children.

    			// put the list data of value into a map
    			Map<Long, ProgrammePointVO> ppMap = AbstractVO.abstractVOs2Map(programmePointVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
    			List<ProgrammePointTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
    			for (ProgrammePointTreeNode treeNode : treeNodeList) {
    				// get new data for this TreeNode
    				ProgrammePointVO ppVO = ppMap.get(treeNode.getProgrammePointPK());

    				if (ppVO != null) {
    					// Set new data to the TreeNode
    					treeNode.setValue(ppVO);
    					// remove data from map, so after the for block the map only contains new values
    					ppMap.remove(ppVO.getID());
    				}
    				else {
    					// The data doesn't exist anymore: Remove the TreeNode
    					// from the children-List and dispose it.
    					removeChild(treeNode);
    					treeNode.dispose();
    				}
    			}

    			// resize children-List if necessary
    			ensureCapacityOfChildren(getChildCount() + ppMap.size());

    			// add new TreeNodes for each new value
    			for (ProgrammePointVO programmePointVO : ppMap.values() ) {
    				ProgrammePointTreeNode ppTreeNode = new ProgrammePointTreeNode(
    					treeViewer,
    					this,
    					programmePointVO
    				);

    				// add TreeNode to list of children
    				addChild(ppTreeNode);
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
    			// refresh data of all Programme Points of the current Event
    			programmePointModel.refreshForeignKey(eventPK);

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
		return ProgrammePointVO_Position_Comparator.getInstance().compare(
			(ProgrammePointVO) treeNode1.getValue(),
			(ProgrammePointVO) treeNode2.getValue()
		);
	}

	// *
	// * Implementation of abstract methods from TreeNode
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

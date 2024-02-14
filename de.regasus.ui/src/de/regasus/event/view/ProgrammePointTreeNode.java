package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVOComparator;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.WorkGroupModel;
import de.regasus.programme.programmepoint.IProgrammePointIdProvider;
import de.regasus.ui.Activator;

public class ProgrammePointTreeNode
	extends TreeNode<ProgrammePointVO>
	implements EventIdProvider, IProgrammePointIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long programmePointPK;

	private WaitlistTreeNode waitlistTreeNode = null;
	private boolean withWaitList = false;

	/* Just used to refresh the data of this Programme Point.
	 * Observing is not necessary, because the parent TreeNode is observing all Programme Points of
	 * this Event. On any change the value of this TreeNode is set and the parent calls refreshTreeNode().
	 */
	/* Though our parent TreeNode is observing all Programme Points of this Event and sets our value
	 * on any change and calls its refreshTreeNode(), we observe our Programme Point anyway. The
	 * reason is that its value of isWaitList() could have changed. In that case the structure of
	 * our children changes and we have to call reloadChildren().
	 */
	private ProgrammePointModel ppModel = ProgrammePointModel.getInstance();

	// data of child TreeNodes
	private ProgrammeOfferingModel poModel = ProgrammeOfferingModel.getInstance();
	private WorkGroupModel wgModel = WorkGroupModel.getInstance();

	/* ignore ModifyEvent from ProgrammeOfferingModel and WorkGroupModel that are fired when this
	 * TreeNode requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public ProgrammePointTreeNode(
		TreeViewer treeViewer,
		ProgrammePointListTreeNode parent,
		ProgrammePointVO programmePointVO
	) {
		super(treeViewer, parent);

		value = programmePointVO;

		programmePointPK = value.getID();

		// observe this Programme Point
		ppModel.addListener(this, programmePointPK);

		// observe Programme Offerings that belong to this Programme Point
		poModel.addForeignKeyListener(this, programmePointPK);

		// observe Work Groups that belong to this Programme Point
		wgModel.addForeignKeyListener(this, programmePointPK);

		// load ConfigParameterSet to decide if waitList is visible
		ConfigParameterSet configParameterSet;
		try {
			ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();
			configParameterSet = configParameterSetModel.getConfigParameterSet(getEventId());

			withWaitList =  configParameterSet.getEvent().getProgramme().getWaitList().isVisible();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dispose() {
		try {
			ppModel.removeListener(this, programmePointPK);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			poModel.removeForeignKeyListener(this, programmePointPK);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			wgModel.removeForeignKeyListener(this, programmePointPK);
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
		return ProgrammePointVO.class;
	}


	@Override
	public Object getKey() {
		return programmePointPK;
	}


	@Override
	public String getText() {
		return value.getName().getString();

		/* Showing the number of Offerings in brackets after the name is nice but not performant,
		 * because it leads to one server call per ProgrammePointTreeNode. Before reactivating this
		 * code, let the ProgrammeOfferingModel load all Programme Offerings of the Event in
		 * advance with one server call. Unfortunately this operation is not that easy to implement.
		 */
//		StringBuilder text = new StringBuilder(100);
//		text.append(value.getName().getString());
//
//		/* get number of Programme Offerings from Model and not from getChildCount(),
//		 * because the latter returns 0 if children are not loaded yet
//		 */
//		Integer count = null;
//		try {
//			ignoreDataChange = true;
//			count = poModel.getProgrammeOfferingVOsByProgrammePointPK(programmePointPK).size();
//
//			text.append(" (");
//			text.append(count);
//			text.append(")");
//		}
//		catch (Exception e) {
//			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//		}
//		finally {
//			ignoreDataChange = false;
//		}
//
//		return text.toString();
	}


	@Override
	public String getToolTipText() {
		return ParticipantLabel.ProgrammePoint.getString();
	}


	@Override
	public Image getImage() {
		Image image = null;

		try {
			// load CVO to know if the Programme Point is fully booked
			ProgrammePointCVO programmePointCVO = ppModel.getProgrammePointCVO(programmePointPK);

			if ( programmePointCVO.isCancelled() ) {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT_CANCELLED);
			}
			else if ( programmePointCVO.isFullyBooked() ) {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT_FULLY_BOOKED);
			}
			else if ( programmePointCVO.isWarnNumberExceeded() ) {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT_BOOKING_EXCEEDED);
			}
			else {
				image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return image;
	}


	@Override
	public boolean isStrikeOut() {
		return value.isCancelled();
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

			List<ProgrammeOfferingVO> poVOs = poModel.getProgrammeOfferingVOsByProgrammePointPK(
				programmePointPK
			);
			if (poVOs == null) {
				poVOs = emptyList();
			}


			List<WorkGroupVO> wgVOs = wgModel.getWorkGroupVOsByProgrammePointPK(
				programmePointPK
			);
			if (wgVOs == null) {
				wgVOs = emptyList();
			}


			boolean waitListIsNew = false;
			if (!withWaitList || !value.isWaitList()) {
				waitlistTreeNode = null;
			}
			else if (waitlistTreeNode == null) {
				waitlistTreeNode = new WaitlistTreeNode(treeViewer, this, value.getPK());
				waitListIsNew = true;
			}


			// evaluate the amount of children
			int childCount = poVOs.size() + wgVOs.size();

			// If wait list tree node exist, resize the child size by one
			if (waitlistTreeNode != null) {
				++childCount;
			}

			/*
			 * Do not just remove all child-nodes and build new ones, because this will close
			 * all nodes, the user already opened. Instead replace the data of all nodes that
			 * still exist, remove nodes of entities that do not exist anymore and create new
			 * nodes for new entities.
			 */


			// If there aren't any children create a TreeNode for every ProgrammeOffering and WorkGroup.
			if (!hasChildren()) {
				// resize children-List
				ensureCapacityOfChildren(childCount);

				// programme offering
				for (ProgrammeOfferingVO poVO : poVOs) {
					ProgrammeOfferingTreeNode poTreeNode = new ProgrammeOfferingTreeNode(
						treeViewer,
						this,
						poVO
					);

					// add TreeNode to list of children
					addChild(poTreeNode);
				}

				// work group
				for (WorkGroupVO wgVO : wgVOs) {
					WorkGroupTreeNode wgTreeNode = new WorkGroupTreeNode(
						treeViewer,
						this,
						wgVO
					);

					// add TreeNode to list of children
					addChild(wgTreeNode);
				}

				// If wait list tree node exist, add TreeNode to list of children
				if (waitlistTreeNode != null) {
					addChild(waitlistTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, ProgrammeOfferingVO> poMap = AbstractVO.abstractVOs2Map(poVOs);
				Map<Long, WorkGroupVO> wgMap = AbstractVO.abstractVOs2Map(wgVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<TreeNode<?>> treeNodeList = createArrayList( getLoadedChildren() );
				for (TreeNode<?> treeNode : treeNodeList) {
					if (treeNode instanceof ProgrammeOfferingTreeNode) {
						ProgrammeOfferingTreeNode poTreeNode = (ProgrammeOfferingTreeNode) treeNode;

						// get new data for this TreeNode
						ProgrammeOfferingVO poVO = poMap.get(poTreeNode.getKey());

						if (poVO != null) {
							// Set new data to the TreeNode
							poTreeNode.setValue(poVO);
							// Remove data from map, so after the for-block the map
							// only contains new values
							poMap.remove(poVO.getID());
						}
						else {
							// The data doesn't exist anymore: Remove the TreeNode
							// from the children-List and dispose it.
							removeChild(poTreeNode);
							poTreeNode.dispose();
						}
					}
					else if (treeNode instanceof WorkGroupTreeNode) {
						WorkGroupTreeNode wgTreeNode = (WorkGroupTreeNode) treeNode;

						// get new data for this TreeNode
						WorkGroupVO wgVO = wgMap.get(wgTreeNode.getKey());

						if (wgVO != null) {
							// Set new data to the TreeNode
							wgTreeNode.setWorkGroupVO(wgVO);
							// Remove data from map, so after the for-block the map
							// only contains new values
							wgMap.remove(wgVO.getID());
						}
						else {
							// The data doesn't exist anymore: Remove the TreeNode
							// from the children-List and dispose it.
							removeChild(wgTreeNode);
							wgTreeNode.dispose();
						}
					}
					else if (treeNode instanceof WaitlistTreeNode) {
						WaitlistTreeNode wlTreeNode = (WaitlistTreeNode) treeNode;

						/* No need to set any data to wlTreeNode, because its data is the
						 * Programme Point Long that cannot change.
						 */

						if (waitlistTreeNode == null) {
							/* The Programme Point has no wait list anymore.
							 * So, remove the TreeNode from the children-List and dispose it.
							 */
							removeChild(wlTreeNode);
							wlTreeNode.dispose();
						}
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + poMap.size() + wgMap.size());

				// add new TreeNodes for each new programmeOffering value
				for(ProgrammeOfferingVO poVO : poMap.values() ) {
					ProgrammeOfferingTreeNode poTreeNode = new ProgrammeOfferingTreeNode(
						treeViewer,
						this,
						poVO
					);

					// add TreeNode to list of children
					addChild(poTreeNode);
				}

				// add new TreeNodes for each new workgroup value
				for(WorkGroupVO wgVO : wgMap.values() ) {
					WorkGroupTreeNode wgTreeNode = new WorkGroupTreeNode(
						treeViewer,
						this,
						wgVO
					);

					// add TreeNode to list of children
					addChild(wgTreeNode);
				}

				// add new TreeNode for new waitlist value
				if (waitListIsNew){
					addChild(waitlistTreeNode);
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
		try {
			// refresh data of this TreeNode
			ppModel.refresh(programmePointPK);

			// refresh data of child TreeNodes
			refreshChildren();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refreshChildren() {
		try {
			if (isChildrenLoaded()) {
    			// refresh data of children
    			poModel.refreshForeignKey(programmePointPK);

    			wgModel.refreshForeignKey(programmePointPK);

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

	@Override
	public void dataChange(CacheModelEvent<Long> event) throws Exception {
		if (!ServerModel.getInstance().isLoggedIn()) {
			// do nothing, because all TreeNodes will be removed from root TreeNode
			return;
		}

		if (ignoreDataChange) {
			return;
		}

		if (event.getSource() == ppModel) {
			/* get new ProgrammePointVO, because if value has already been set by parent depends on
			 * the order of listeners in ProgrammePointModel
			 */
			value = ppModel.getProgrammePointVO(programmePointPK);
		}

		// for changes in every Models we have to call reloadChildren()
		reloadChildren();
	}


	/**
	 * This method is called to compare different types of children.
	 *
	 * WaitListTreeNode is always the first. After this there are the WorkGroupsTreeNodes.
	 * At last the ProgrameOfferingTreeNodes, that are compared by value (position).
	 */
	@Override
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		// WaitListTreeNode is always the first
		if (treeNode1 instanceof WaitlistTreeNode) {
			return -1;
		}
		else if (treeNode2 instanceof WaitlistTreeNode) {
			return 1;
		}
		// compare same classes
		else if (treeNode1.getClass() == treeNode2.getClass()) {
			// ProgrammeOfferingTreeNodes are compared by value
			if (treeNode1 instanceof ProgrammeOfferingTreeNode) {
				return ProgrammeOfferingVOComparator.getInstance().compare(
					((ProgrammeOfferingTreeNode) treeNode1).getValue(),
					((ProgrammeOfferingTreeNode) treeNode2).getValue()
				);
			}
			// others are compared by text
			else {
				return compareByText(treeNode1, treeNode2);
			}
		}
		// WorkGroupTreeNode always before others
		else if (treeNode1 instanceof WorkGroupTreeNode) {
			return -1;
		}
		else {
			return 1;
		}
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	public Long getProgrammePointPK() {
		return programmePointPK;
	}


	@Override
	public void setValue(ProgrammePointVO programmePointVO) {
		value = programmePointVO;
	}


	@Override
	public Long getEventId() {
		return value.getEventPK();
	}


	@Override
	public Long getProgrammePointId() {
		return programmePointPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

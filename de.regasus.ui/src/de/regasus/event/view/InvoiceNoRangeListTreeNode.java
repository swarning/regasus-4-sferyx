package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeCVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO_Name_Comparator;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.util.ObjectComparator;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.ui.Activator;

/**
 * A node representing the list of Invoice Number Ranges of an Event.
 */
public class InvoiceNoRangeListTreeNode
extends TreeNode<List<InvoiceNoRangeCVO>>
implements EventIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;

	// data of child TreeNodes
	private InvoiceNoRangeModel inrModel = InvoiceNoRangeModel.getInstance();

	// ignore ModifyEvent from InvoiceNoRangeModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// ******************************************************************************
	// * Constructors and dispose
	//

	public InvoiceNoRangeListTreeNode(TreeViewer treeViewer, EventTreeNode parent) {
		super(treeViewer, parent);

		// get eventPK from parent TreeNode
		eventPK = parent.getEventId();

		// observe Invoice Number Ranges that belong to this Event
		inrModel.addForeignKeyListener(this, eventPK);
	}


	@Override
	public void dispose() {
		try {
			inrModel.removeForeignKeyListener(this, eventPK);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}

	//
	// * Constructors and dispose
	// ******************************************************************************

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
		text.append(I18N.InvoiceNoRangeListTreeNode_Text);

		/* get number of Invoice Number Ranges from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		Integer count = null;
		try {
			ignoreDataChange = true;
			count = inrModel.getInvoiceNoRangeCVOsByEventPK(eventPK).size();

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
		return IconRegistry.getImage(IImageKeys.MD_INVOICERANGE_FOLDER);
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
			List<InvoiceNoRangeCVO> invoiceNoRangeCVOs = inrModel.getInvoiceNoRangeCVOsByEventPK(eventPK);
			if (invoiceNoRangeCVOs == null) {
				invoiceNoRangeCVOs = emptyList();
    		}

			/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */


			// If there aren't any children create a TreeNode for every InvoiceNoRange.
			if (!hasChildren()) {
    			// resize children-List
				ensureCapacityOfChildren(invoiceNoRangeCVOs.size());

				for (InvoiceNoRangeCVO invoiceNoRangeCVO : invoiceNoRangeCVOs) {
					InvoiceNoRangeTreeNode treeNode = new InvoiceNoRangeTreeNode(
						treeViewer,
						InvoiceNoRangeListTreeNode.this,
						invoiceNoRangeCVO
					);

					// add TreeNode to list of children
					addChild(treeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, InvoiceNoRangeCVO> invoiceNoRangeMap = AbstractCVO.abstractCVOs2Map(invoiceNoRangeCVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<InvoiceNoRangeTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (InvoiceNoRangeTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					InvoiceNoRangeCVO invoiceNoRangeCVO = invoiceNoRangeMap.get(
						treeNode.getInvoiceNoRangePK()
					);

					if (invoiceNoRangeCVO != null) {
						// Set new data to the TreeNode
						treeNode.setValue(invoiceNoRangeCVO);
						// Remove data from map, so after the for-block the map
						// only contains new values
						invoiceNoRangeMap.remove(invoiceNoRangeCVO.getPK());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + invoiceNoRangeMap.size());

				// add new TreeNodes for each new value
				for (InvoiceNoRangeCVO invoiceNoRangeCVO : invoiceNoRangeMap.values()) {
					InvoiceNoRangeTreeNode invoiceNoRangeTreeNode = new InvoiceNoRangeTreeNode(
						treeViewer,
						InvoiceNoRangeListTreeNode.this,
						invoiceNoRangeCVO
					);

					// add TreeNode to list of children
					addChild(invoiceNoRangeTreeNode);
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
    			// refresh data of all Invoice Number Ranges of the current Event
    			inrModel.refreshForeignKey(eventPK);

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
	public int compareChildTreeNodes(TreeNode<?> treeNode1, TreeNode<?> treeNode2) {
		InvoiceNoRangeTreeNode inrTreeNode1 = (InvoiceNoRangeTreeNode) treeNode1;
		InvoiceNoRangeTreeNode inrTreeNode2 = (InvoiceNoRangeTreeNode) treeNode2;

		InvoiceNoRangeVO invoiceNoRange1VO = inrTreeNode1.getValue().getVO();
		InvoiceNoRangeVO invoiceNoRange2VO = inrTreeNode2.getValue().getVO();

		// compare startNo
		Integer startNo1 = invoiceNoRange1VO.getStartNo();
		Integer startNo2 = invoiceNoRange2VO.getStartNo();
		int result = ObjectComparator.getInstance().compare(startNo1, startNo2);

		if (result == 0) {
			// compare name (and finally id)
			result = InvoiceNoRangeVO_Name_Comparator.getInstance().compare(invoiceNoRange1VO, invoiceNoRange2VO);
		}

		return result;
	}


	/* (non-Javadoc)
	 * @see com.lambdalogic.util.model.CacheModelListener#dataChange(com.lambdalogic.util.model.CacheModelEvent)
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

package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVOComparator;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.ui.Activator;

public class HotelContingentTreeNode extends TreeNode<HotelContingentCVO> implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long hotelContingentPK;

	private Long eventPK;

	/* Just used to refresh the data of this Hotel Contingent.
	 * Observing is not necessary, because the parent TreeNode is observing all its Hotel Contingents.
	 * On any change the value of this TreeNode is set and the parent calls refreshTreeNode().
	 */
	private HotelContingentModel hcModel = HotelContingentModel.getInstance();

	// data of child TreeNodes
	private HotelOfferingModel hoModel = HotelOfferingModel.getInstance();

	/* ignore ModifyEvent from HotelOfferingModel that are fired when this TreeNode
	 * requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************


	private CacheModelListener<Long> hotelOfferingModelListener = new CacheModelListener<Long>() {
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
	};


	// *************************************************************************
	// * Constructors and dispose()
	// *

	public HotelContingentTreeNode(
		TreeViewer treeViewer,
		EventHotelInfoTreeNode parent,
		HotelContingentCVO hotelContingentCVO
	) {
		super(treeViewer, parent, hotelContingentCVO);

		hotelContingentPK = value.getPK();

		eventPK = value.getEventPK();

		// observe Hotel Offerings that belong to this Hotel Contingent
		hoModel.addForeignKeyListener(hotelOfferingModelListener, hotelContingentPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			hoModel.removeForeignKeyListener(hotelOfferingModelListener, hotelContingentPK);
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
		return HotelContingentCVO.class;
	}


	@Override
	public Long getKey() {
		return hotelContingentPK;
	}


	@Override
	public String getText() {
		/* Build the text label like this:
		 * <Hotel Contingent name>, <startDate> - <endDate>
		 */
		StringBuilder text = new StringBuilder(100);

		try {
			final int CHAR_COUNT = 50;

			// fist CHAR_COUNT characters of Hotel Contingent name
			String name = value.getVO().getName();
			if (name != null) {
				if (name.length() <= CHAR_COUNT) {
					text.append(name);
				}
				else {
					name = name.substring(0, CHAR_COUNT);
					text.append(name);
					text.append("...");
				}
			}

			// start and end of the Hotel Contingent (first and last Volume)
			I18NDate startDate = value.getFirstDay();
			if (startDate != null) {
				StringHelper.appendIfNeeded(text, ", ");
				text.append( startDate.format() );
				text.append(" - ");
			}

			I18NDate endDate = value.getLastDay();
			if (endDate != null) {
				text.append( endDate.format() );
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return text.toString();
	}


	@Override
	public String getToolTipText() {
		return HotelLabel.HotelContingent.getString();
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.CONTINGENT);
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

    		// load child data from model
			List<HotelOfferingVO> hotelOfferingVOs = hoModel.getHotelOfferingVOsByHotelContingentPK(value.getPK());
    		if (hotelOfferingVOs == null) {
    			hotelOfferingVOs = emptyList();
    		}

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every HotelOffering
    		if (!hasChildren()) {
    			// resize children-List
				ensureCapacityOfChildren(hotelOfferingVOs.size());

				for (HotelOfferingVO hoVO : hotelOfferingVOs) {
					HotelOfferingTreeNode hoTreeNode = new HotelOfferingTreeNode(
						treeViewer,
						this,
						hoVO
					);

					// add TreeNode to list of children
					addChild(hoTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<Long, HotelOfferingVO> hoMap = AbstractVO.abstractVOs2Map(hotelOfferingVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<HotelOfferingTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (HotelOfferingTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					HotelOfferingVO hoVO = hoMap.get(treeNode.getKey());

					if (hoVO != null) {
						// Set new data to the TreeNode
						treeNode.setValue(hoVO);
						// Remove data from map, so after the for-block the map
						// only contains new values
						hoMap.remove(hoVO.getID());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + hoMap.size());

				// add new TreeNodes for each new value
				for(HotelOfferingVO hoVO : hoMap.values() ) {
					HotelOfferingTreeNode treeNode = new HotelOfferingTreeNode(
						treeViewer,
						this,
						hoVO
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
		try {
			// refresh data of this TreeNode
			hcModel.refresh(getKey());

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
    			hoModel.refreshForeignKey(hotelContingentPK);

    			// refresh grandchildren
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
	public int compareChildTreeNodes(TreeNode treeNode1, TreeNode treeNode2) {
		return HotelOfferingVOComparator.getInstance().compare(
			(HotelOfferingVO) treeNode1.getValue(),
			(HotelOfferingVO) treeNode2.getValue()
		);
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

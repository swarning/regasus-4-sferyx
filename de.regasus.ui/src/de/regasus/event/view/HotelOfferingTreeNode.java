package de.regasus.event.view;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.hotel.data.RoomType;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
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
import de.regasus.hotel.HotelCancelationTermModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.ui.Activator;

public class HotelOfferingTreeNode
extends TreeNode<HotelOfferingVO>
implements EventIdProvider, CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private Long hotelOfferingPK;

	/* Just used to refresh the data of this Hotel Offering.
	 * Observing is not necessary, because the parent TreeNode is observing all its Hotel Offerings.
	 * On any change the value of this TreeNode is set and the parent calls refreshTreeNode().
	 */
	private HotelOfferingModel hoModel = HotelOfferingModel.getInstance();

	// data of child TreeNodes
	private HotelCancelationTermModel hctModel = HotelCancelationTermModel.getInstance();

	// used to build the text label (observed to keep the label up-to-date)
	private RoomDefinitionModel rdModel = RoomDefinitionModel.getInstance();

	/* ignore ModifyEvent from HotelCancelationTermModel that are fired when this TreeNode
	 * requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public HotelOfferingTreeNode(
		TreeViewer treeViewer,
		HotelContingentTreeNode parent,
		HotelOfferingVO hotelOfferingVO
	) {
		super(treeViewer, parent, hotelOfferingVO);

		hotelOfferingPK = value.getPK();

		// observe Hotel Cancelation Terms that belong to this Hotel Offering
		hctModel.addForeignKeyListener(this, hotelOfferingPK);

		// ovserve all Room Definitions to keep text label up-to-date
		rdModel.addListener(this);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			hctModel.removeForeignKeyListener(this, hotelOfferingPK);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			rdModel.removeListener(this);
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
		return HotelOfferingVO.class;
	}


	@Override
	public Long getKey() {
		return hotelOfferingPK;
	}


	@Override
	public String getText() {
		/* Build the text label like this:
		 * <Offering description | Room Definition description | Room Definition name>, <total price per night>
		 */
		StringBuilder text = new StringBuilder(100);

		try {
			final int CHAR_COUNT = 50;

			// append fist CHAR_COUNT characters of Offering description (if available)
			LanguageString description = value.getDescription();
			if (description != null) {
				String s = description.getString();
				if (s != null) {
					if (s.length() > CHAR_COUNT) {
						s = s.substring(0, CHAR_COUNT) + "...";
					}
					if (s.contains("\n")) {
						s = s.substring(0, s.indexOf("\n")) + "...";
					}
					text.append(s);
				}
			}

			// if there is no description, add name of RoomDefinition instead
			if (text.length() == 0) {
				Long roomDefinitionPK = value.getRoomDefinitionPK();

				RoomDefinitionVO roomDefinitionVO = rdModel.getRoomDefinitionVO(roomDefinitionPK);
				LanguageString name = roomDefinitionVO.getName();
				if (name != null) {
					String s = name.getString();
					if (s != null) {
						text.append(s);
					}
				}

				// if there is no name, add RoomType
				if (text.length() == 0) {
					RoomType roomType = roomDefinitionVO.getRoomType();
					text.append(roomType.getString());
				}
			}

			StringHelper.appendIfNeeded(text, ", ");

			// add total price per night
			String amount = value.getCurrencyAmountGross().format(false, true);
			text.append(amount);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}

		return text.toString();
	}


	@Override
	public String getToolTipText() {
		return HotelLabel.HotelOffering.getString();
	}

	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.EURO);
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
			List<HotelCancelationTermVO> hctVOs = hctModel.getHotelCancelationTermVOsByHotelOfferingPK(value.getID());
    		if (hctVOs == null) {
    			hctVOs = emptyList();
    		}

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every HotelCancelationTerm.
    		if (!hasChildren()) {
    			// resize children-List
				ensureCapacityOfChildren(hctVOs.size());

    			for (HotelCancelationTermVO hctVO : hctVOs) {
    				HotelCancelationTermTreeNode treeNode = new HotelCancelationTermTreeNode(
    					treeViewer,
    					this,
    					hctVO
    				);

    				// add TreeNode to list of children
					addChild(treeNode);
    			}
    		}
    		else {
    			// If there are already children, we've to match the new List with the existing children.

    			// put the list data of value into a map
    			Map<Long, HotelCancelationTermVO> hctMap = AbstractVO.abstractVOs2Map(hctVOs);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
    			List<HotelCancelationTermTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
    			for (HotelCancelationTermTreeNode treeNode : treeNodeList) {
    				// get new data for this TreeNode
    				HotelCancelationTermVO hctVO = hctMap.get(treeNode.getHotelCancelationTermPK());

    				if (hctVO != null) {
    					// Set new data to the TreeNode
    					treeNode.setValue(hctVO);
    					// Remove data from map, so after the for-block the map
    					// only contains new values
    					hctMap.remove(hctVO.getID());
    				}
    				else {
    					// The data doesn't exist anymore: Remove the TreeNode
    					// from the children-List and dispose it.
    					removeChild(treeNode);
    					treeNode.dispose();
    				}
    			}

    			// resize children-List if necessary
    			ensureCapacityOfChildren(getChildCount() + hctMap.size());

    			// add new TreeNodes for each new value
    			for (HotelCancelationTermVO hctVO : hctMap.values() ) {
    				HotelCancelationTermTreeNode treeNode = new HotelCancelationTermTreeNode(
    					treeViewer,
    					this,
    					hctVO
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
			hoModel.refresh(hotelOfferingPK);

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
    			hctModel.refreshForeignKey(hotelOfferingPK);

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


			if (event.getSource() == hctModel) {
				// If we receive an Event from HotelCancelationTermModel, children are reloaded
			reloadChildren();
		}
		else if (event.getSource() == rdModel) {
			// check if the Room Definition referenced by this Hotel Contingent has chnaged
			if ( event.getKeyList().contains( value.getRoomDefinitionPK() ) ) {
				// refresh text label
				updateTreeViewer();
			}
		}
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	@Override
	public Long getEventId() {
		// parent is always a HotelContongentTreeNode because of the constructor
		return ((HotelContingentTreeNode) getParent()).getEventId();
	}


	public Long getHotelContingentPK() {
		return value.getHotelContingentPK();
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

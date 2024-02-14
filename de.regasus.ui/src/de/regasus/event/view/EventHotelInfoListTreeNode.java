package de.regasus.event.view;


import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventIdProvider;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.ui.Activator;


/**
 * A node representing the list of Event Hotel Infos of an Event.
 *
 * The list of children (EventHotelInfoTreeNodes) does not correspond to the list of persisted
 * Event Hotel Infos. It is instead the list of the Hotels of all Hotel Contingents of the current
 * Event.
 */
public class EventHotelInfoListTreeNode extends TreeNode<List<Hotel>> implements EventIdProvider {

	// *************************************************************************
	// * Attributes
	// *

	private Long eventPK;

	// data of child TreeNodes
	private HotelContingentModel hotelContingentModel = HotelContingentModel.getInstance();
	private HotelModel hotelModel = HotelModel.getInstance();

	// ignore ModifyEvent from HotelContingentModel that are fired when this TreeNode requests data from it
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************


	private CacheModelListener<Long> hotelContingentModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			if (!ServerModel.getInstance().isLoggedIn()) {
				// do nothing, because all TreeNodes will be removed from root TreeNode
				return;
			}

			if (ignoreDataChange) {
				return;
			}

			if (event.getOperation() == CacheModelOperation.CREATE ||
				event.getOperation() == CacheModelOperation.DELETE ||
				event.getOperation() == CacheModelOperation.REFRESH
			) {
				// reload children if there is a chance for new or deleted Hotel Contingents
				reloadChildren();
			}
		}
	};


	// *************************************************************************
	// * Constructors and dispose()
	// *

	public EventHotelInfoListTreeNode(TreeViewer treeViewer, EventTreeNode parent) {
		super(treeViewer, parent);

		// get eventPK from parent TreeNode
		this.eventPK = parent.getEventId();

		/* observe Hotel Contingents that belong to this Event
		 * The Hotel Contingents of the Event define the the list of child TrreeNodes.
		 */
		hotelContingentModel.addForeignKeyListener(hotelContingentModelListener, eventPK);
	}


	@Override
	public void dispose() {
		// disconnect from models
		try {
			hotelContingentModel.removeForeignKeyListener(hotelContingentModelListener, eventPK);
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
		text.append(HotelLabel.Hotels.getString());

		/* get number of Programme Points from Model and not from getChildCount(),
		 * because the latter returns 0 if children are not loaded yet
		 */
		Integer count = null;
		try {
			ignoreDataChange = true;
			count = hotelContingentModel.getHotelPKsByEventPK(eventPK).size();

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
		return I18N.HotelsWithContingentsForThisEvent;
	}


	@Override
	public Image getImage() {
		return IconRegistry.getImage(IImageKeys.MD_HOTEL_FOLDER);
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
			Collection<Long> hotelIDs = HotelContingentModel.getInstance().getHotelPKsByEventPK(eventPK);
			List<Hotel> hotels = hotelModel.getHotels(hotelIDs);
			if (hotels == null) {
				hotels = emptyList();
			}

			/*
			 * Do not just remove all child-nodes and build new ones, because this will close
			 * all nodes, the user already opened. Instead replace the data of all nodes that
			 * still exist, remove nodes of entities that do not exist anymore and create new
			 * nodes for new entities.
			 */

			// If there aren't any children create a TreeNode for every Hotel.
			if (!hasChildren()) {
				// resize children-List
				ensureCapacityOfChildren(hotels.size());

				for (Hotel hotel : hotels) {
					EventHotelInfoTreeNode hotelTreeNode = new EventHotelInfoTreeNode(
						treeViewer,
						this,
						hotel,
						eventPK
					);

					// add TreeNode to list of children
					addChild(hotelTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// build map from hotelPK to Hotel
				Map<Long, Hotel> hotelMap = Hotel.abstractEntities2Map(hotels);

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<EventHotelInfoTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (EventHotelInfoTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					Hotel hotel = hotelMap.get(treeNode.getKey().getHotelPK());

					if (hotel != null) {
						// Set new data to the TreeNode
						treeNode.setValue(hotel);
						// Remove data from map, so after the for-block the map
						// only contains new values
						hotelMap.remove(hotel.getID());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + hotelMap.size());

				// add new TreeNodes for each new value
				for (Hotel hotel : hotelMap.values()) {
					EventHotelInfoTreeNode hotelTreeNode = new EventHotelInfoTreeNode(
						treeViewer,
						this,
						hotel,
						eventPK
					);

					// add TreeNode to list of children
					addChild(hotelTreeNode);
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
    			/* The data of our child TreeNodes depend on two Models:
    			 * HotelContingentModel and HotelModel.
    			 * Therefore some manual work is necessary.
    			 */

    			/* Ignore CacheModelEvents when refreshing HotelContingentModel, because dhild TreeNodes
    			 * should be reloaded not before the HotelModel has been refreshed, too.
    			 */
    			ignoreDataChange = true;

    			// refresh data of all Hotel Contingents of the current Event
    			hotelContingentModel.refreshForeignKey(eventPK);

    			// get new list of all Hotels that belong to this Event
    			Collection<Long> hotelIDs = hotelContingentModel.getHotelPKsByEventPK(eventPK);

    			// refresh affected Hotels
    			hotelModel.refresh(hotelIDs);

    			// reload child TreeNodes manually, because CacheModelEvents have been ignored
    			reloadChildren();


    			// refresh data of our grandchildren
    			refreshGrandChildren();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
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
		return eventPK;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

package de.regasus.hotel.view.tree;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.CountryCitiesModel;
import de.regasus.common.CountryCity;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.hotel.HotelModel;
import de.regasus.ui.Activator;


/**
 * A node representing a city where a Hotel exists for.
 */
public class HotelCityTreeNode extends TreeNode<CountryCity> implements CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *
	
	private String city;

	/* Just used to refresh the data of this city.
	 * Observing is necessary, because the parent TreeNode is NOT observing its Countrys. 
	 */
	private CountryCitiesModel countryCitiesModel = CountryCitiesModel.getInstance();

	// data of child TreeNodes
	private HotelModel hotelModel = HotelModel.getInstance();

	/* ignore ModifyEvent from HotelModel that are fired when this TreeNode requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public HotelCityTreeNode(
		TreeViewer treeViewer,
		HotelCountryTreeNode parent,
		CountryCity countryCity
	) {
		super(treeViewer, parent, countryCity);
		
		city = countryCity.getCity();
		
		// observe Hotels that belong to this city
		hotelModel.addForeignKeyListener(HotelCityTreeNode.this, value);
	}
	
	
	@Override
	public void dispose() {
		// disconnect from models
		try {
			if (hotelModel != null) {
				hotelModel.removeForeignKeyListener(this, value);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
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
		return CountryCity.class;
	}
	
	
	@Override
	public Object getKey() {
		return city;
	}
	
	
	@Override
	public String getText() {
		return city;
	}
	
	
	@Override
	public String getToolTipText() {
		return null;
	}
	
	
	@Override
	public Image getImage() {
		return IconRegistry.getImage("/icons/city16x11.png");
	}
	
	
	@Override
	protected void loadChildren() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			public void run() {
				_loadChildren();
			}
		});
	}

	
	private void _loadChildren() {
		try {
			ignoreDataChange = true;

			// get data from model
			List<Hotel> hotelList = hotelModel.getHotelsByCityKey(value);
			if (hotelList == null) {
				hotelList = CollectionsHelper.emptyList();
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
				ensureCapacityOfChildren(hotelList.size());
				
				for (Hotel hotel : hotelList) {
					// create new TreeNode
					HotelTreeNode hotelTreeNode = new HotelTreeNode(
						treeViewer, 
						this,		// parent
						hotel
					);
					
					// add TreeNode to list of children
					addChild(hotelTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.
				
				// put the list data of value into a map
				Map<Long, Hotel> hotelMap = Hotel.getEntityMap(hotelList);

				
				// remove/refresh TreeNodes
				
				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<HotelTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (HotelTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					Hotel hotel = hotelMap.get(treeNode.getKey());
					
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
					HotelTreeNode hotelTreeNode = new HotelTreeNode(treeViewer, this, hotel);
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
		try {
			// refresh data of this TreeNode
			countryCitiesModel.refresh(city);
			
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
    			hotelModel.refreshForeignKey(value);
    
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
	
	public String getHotelCityName() {
		String hotelCityName = "";
		if (value != null) {
			hotelCityName = value.getCity();
		}
		return hotelCityName;
	}

	// *
	// * Getter and setter
	// *************************************************************************

}

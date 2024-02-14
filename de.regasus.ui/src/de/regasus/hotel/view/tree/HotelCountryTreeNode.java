package de.regasus.hotel.view.tree;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.tree.TreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Country;
import de.regasus.common.CountryCities;
import de.regasus.common.CountryCitiesModel;
import de.regasus.common.CountryCity;
import de.regasus.core.CountryModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.ui.Activator;


/**
 * A node representing a country where a Hotel exists for.
 */
public class HotelCountryTreeNode extends TreeNode<Country> implements CacheModelListener<Long> {

	// *************************************************************************
	// * Attributes
	// *

	private String countryPK;

	/* Just used to refresh the data of this Country.
	 * Observing is necessary, because the parent TreeNode is NOT observing its Countrys.
	 */
	private CountryModel countryModel = CountryModel.getInstance();

	// data of child TreeNodes
	private CountryCitiesModel countryCitiesModel = CountryCitiesModel.getInstance();

	/* ignore ModifyEvent from CountryCitiesModel that are fired when this TreeNode requests data from them
	 */
	private boolean ignoreDataChange = false;

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public HotelCountryTreeNode(
		TreeViewer treeViewer,
		HotelTreeRoot parent,
		Country country
	) {
		super(treeViewer, parent, country);

		countryPK = value.getId();

		// observe CountryCities that belong to this Country
		countryCitiesModel.addListener(this, countryPK);

		// observe this Country to keep text label up-to-date
		countryModel.addListener(this, countryPK);
	}


	@Override
	public void dispose() {
		// disconnect from models

		try {
			countryModel.removeListener(this, countryPK);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		try {
			countryCitiesModel.removeListener(this, countryPK);
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
		return Country.class;
	}


	@Override
	public Object getKey() {
		return countryPK;
	}


	@Override
	public String getText() {
		String text = null;
		if (value != null) {
			text = value.getName().getString();
		}
		return StringHelper.avoidNull(text);
	}


	@Override
	public String getToolTipText() {
		return null;
	}


	@Override
	public Image getImage() {
		Image image = null;
		if (value != null) {
			String fileName = value.getId().toLowerCase() + ".png";
			image = IconRegistry.getImage("/icons/flags/" + fileName);
		}
		return image;
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
			CountryCities countryCities =  countryCitiesModel.getCountryCities(countryPK);
			if (countryCities == null) {
				countryCities = new CountryCities(countryPK);
			}

			List<String> cityList = countryCities.getCityList();

			/*
			 * Do not just remove all child-nodes and build new ones, because this will close
			 * all nodes, the user already opened. Instead replace the data of all nodes that
			 * still exist, remove nodes of entities that do not exist anymore and create new
			 * nodes for new entities.
			 */

			// If there aren't any children create a TreeNode for every Gate.
			if (!hasChildren()) {
    			// resize children-List
				ensureCapacityOfChildren(cityList.size());

				for (String city : cityList) {
					// create new TreeNode
					CountryCity countryCity = new CountryCity(city, countryCities.getCountryCode());
					HotelCityTreeNode hotelCityTreeNode = new HotelCityTreeNode(
						treeViewer,
						this,
						countryCity
					);

					// add TreeNode to list of children
					addChild(hotelCityTreeNode);
				}
			}
			else {
				// If there are already children, we've to match the new List with the existing children.

				// put the list data of value into a map
				Map<String, CountryCity> map = MapHelper.createHashMap(cityList.size() + 5);
				for (String city : cityList) {
					CountryCity countryCity = new CountryCity(city, countryCities.getCountryCode());
					map.put(city, countryCity);
				}

				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<HotelCityTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (HotelCityTreeNode treeNode : treeNodeList) {
					// get new data for this TreeNode
					CountryCity countryCity = map.get(treeNode.getHotelCityName());

					if (countryCity != null) {
						// Set new data to the TreeNode
						treeNode.setValue(countryCity);
						// Remove data from map, so after the for-block the map only contains new values
						map.remove(countryCity.getCity());
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

				// resize children-List if necessary
				ensureCapacityOfChildren(getChildCount() + map.size());

				// add new TreeNodes for each new value
				for (CountryCity countryCity : map.values()) {
					HotelCityTreeNode hotelCityTreeNode = new HotelCityTreeNode(
						treeViewer,
						this,
						countryCity
					);

					// add TreeNode to list of children
					addChild(hotelCityTreeNode);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			ignoreDataChange = false;
		}
	}


	@Override
	public void refresh() {
		try {
			// refresh data of this TreeNode
			countryModel.refresh(countryPK);

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
				countryCitiesModel.refresh(countryPK);

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

		if (event.getSource() == countryModel) {
			value = countryModel.getCountry(countryPK);

			// refresh text label
			updateTreeViewer();
		}
		else if (event.getSource() == countryCitiesModel) {
			// If we receive an Event from countryCitiesModel, children are loaded
			reloadChildren();
		}
	}

	// *
	// * Implementation of interfaces
	// *************************************************************************

//	@Override
//	public boolean equals(Object other) {
//		if (other == this) {
//			return true;
//		}
//
//		if (other == null || other.getClass() != this.getClass()) {
//			return false;
//		}
//
//		HotelCountryTreeNode otherTreeNode = (HotelCountryTreeNode) other;
//
//		boolean result;
//		if (value == null) {
//			result = otherTreeNode.getValue() == null;
//		}
//		else {
//			result = value.equals(otherTreeNode.getValue());
//		}
//		return result;
//	}

}

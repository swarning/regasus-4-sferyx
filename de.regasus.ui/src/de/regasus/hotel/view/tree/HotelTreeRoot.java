package de.regasus.hotel.view.tree;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.tree.DefaultTreeNode;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.Country;
import de.regasus.core.CountryModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.hotel.HotelCountriesModel;
import de.regasus.ui.Activator;


/**
 * The internal root node of the {@link HotelTreeView}.
 * This node is the technical root node and is not visible.
 */
public class HotelTreeRoot extends DefaultTreeNode<Object> {

	// *************************************************************************
	// * Attributes
	// *

	private ServerModel serverModel = ServerModel.getInstance();
	private HotelCountriesModel hotelCountriesModel = HotelCountriesModel.getInstance();

	/* used to get the country names
	 * Not observed, the HotelCountryTreeNodes observe their Countries
	 */
	private CountryModel countryModel = CountryModel.getInstance();

	/* ignore ModifyEvent from HotelCountriesModel that are fired when this TreeNode requests data from them
	 */
	private boolean ignoreDataChange = false;

	/**
	 * Observe {@link ServerModel} to handle login ad logout.
	 */
	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;
			if ( serverModelEvent.getType() == ServerModelEventType.LOGIN ) {
				handleLogin();
			}
			else if ( serverModelEvent.getType() == ServerModelEventType.LOGOUT ) {
				handleLogout();
			}
		}
	};


	/**
	 * Observe {@link ServerModel} to handle login ad logout.
	 */
	private ModelListener hotelCountriesModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			if (serverModel.isLoggedIn()) {
				// do nothing, because all TreeNodes will be removed from root TreeNode
				return;
			}

			if (ignoreDataChange) {
				return;
			}

			// if we receive an Event from EventModel, children are loaded
			reloadChildren();
		}
	};

	// *
	// * Attributes
	// *************************************************************************

	// *************************************************************************
	// * Constructors and dispose()
	// *

	public HotelTreeRoot(TreeViewer treeViewer, ModifySupport modifySupport) {
		super(treeViewer, null);

		setModifySupport(modifySupport);

		initTree();


		// observe HotelCountriesModel
		hotelCountriesModel.addListener(hotelCountriesModelListener);

		// observe ServerModel
		serverModel.addListener(serverModelListener);
	}


	private void initTree() {
		// load children, because this TreeNode is not visible and cannot be opened by user
		loadChildren();
	}


	private void handleLogout() {
		removeAll();
		refreshTreeViewer();
	}


	private void handleLogin() {
		initTree();
		refreshTreeViewer();
	}


	@Override
	public void dispose() {
		// disconnect from HotelCountriesModel
		try {
			hotelCountriesModel.removeListener(hotelCountriesModelListener);
		}
		catch (Throwable e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		// disconnect from ServerModel
		try {
			serverModel.removeListener(serverModelListener);
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
	protected void loadChildren() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					_loadChildren();
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private void _loadChildren() {
		try {
			ignoreDataChange = true;

			// get data from model
			List<String> countryCodes = hotelCountriesModel.getModelData();
    		if (countryCodes == null) {
    			countryCodes = emptyList();
    		}

			// load all Countries at once to avoid multiple server calls
			countryModel.getCountrys(countryCodes);

    		/*
    		 * Do not just remove all child-nodes and build new ones, because this will close
    		 * all nodes, the user already opened. Instead replace the data of all nodes that
    		 * still exist, remove nodes of entities that do not exist anymore and create new
    		 * nodes for new entities.
    		 */

    		// If there aren't any children create a TreeNode for every Event.
    		if (!hasChildren()) {
    			// resize children-List
				ensureCapacityOfChildren(countryCodes.size());

				for (String countryCode : countryCodes) {
					// get Country
					Country country = countryModel.getCountry(countryCode);

					// create new TreeNode
					HotelCountryTreeNode treeNode = new HotelCountryTreeNode(
						treeViewer,
						this,		// parent
						country
					);

					// add TreeNode to list of children
					addChild(treeNode);
				}
			}
			else {
				// remove/refresh TreeNodes

				/* Iterate over existing child-TreeNodes.
				 * Do NOT call getChildren(), because it will cause an infinite ping-pong game
				 * between loadChildren() and _loadChildren()!
				 */
				List<HotelCountryTreeNode> treeNodeList = (List) createArrayList( getLoadedChildren() );
				for (HotelCountryTreeNode treeNode : treeNodeList) {
    				// get new data for this TreeNode
					String countryCode = (String) treeNode.getKey();
					Country country = countryModel.getCountry(countryCode);

					if (country != null) {
						// Set new data to the TreeNode
						treeNode.setValue(country);
						// Remove data from countryCodes, so after the for-block the List contains only new values
						countryCodes.remove(countryCode);
					}
					else {
						// The data doesn't exist anymore: Remove the TreeNode
						// from the children-List and dispose it.
						removeChild(treeNode);
						treeNode.dispose();
					}
				}

    			// resize children-List if necessary
    			ensureCapacityOfChildren(getChildCount() + countryCodes.size());

				// add new TreeNodes for each new value
				for (String countryCode : countryCodes) {
					// load data from model
					Country country = countryModel.getCountry(countryCode);

					// build TreeNode
					HotelCountryTreeNode treeNode = new HotelCountryTreeNode(
						treeViewer,
						this,
						country
					);

					// add TreeNode
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

	// *
	// * Implementation of abstract methods from TreeNode
	// *************************************************************************

}

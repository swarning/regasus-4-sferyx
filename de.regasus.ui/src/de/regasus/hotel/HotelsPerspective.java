package de.regasus.hotel;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.chain.view.HotelChainView;
import de.regasus.hotel.contingent.view.HotelContingentSearchView;
import de.regasus.hotel.contingent.view.pref.HotelContingentSearchViewPreference;
import de.regasus.hotel.view.search.HotelSearchView;
import de.regasus.hotel.view.search.pref.HotelSearchViewPreference;
import de.regasus.hotel.view.tree.HotelTreeView;
import de.regasus.ui.Activator;

public class HotelsPerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.HotelsPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		try {
			IFolderLayout leftLayout = layout.createFolder(
				ID + ".left",
				IPageLayout.LEFT,
				0.4f,
				layout.getEditorArea()
			);

			// add hotel views if they shall be visible
			try {
				ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet();
				if (configParameterSet == null || configParameterSet.getHotel().isVisible()) {
					leftLayout.addView(HotelTreeView.ID);
					leftLayout.addView(HotelChainView.ID);

					HotelSearchViewPreference.getInstance().initialize(); // delete previously saved preferences
					leftLayout.addView(HotelSearchView.ID);

					HotelContingentSearchViewPreference.getInstance().initialize(); // delete previously saved preferences
					leftLayout.addView(HotelContingentSearchView.ID);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}


		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}

}
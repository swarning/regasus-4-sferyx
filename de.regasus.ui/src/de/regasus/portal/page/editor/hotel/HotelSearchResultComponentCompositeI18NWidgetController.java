package de.regasus.portal.page.editor.hotel;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.hotel.HotelSearchResultComponent;

public class HotelSearchResultComponentCompositeI18NWidgetController
	implements I18NWidgetController<HotelSearchResultComponent> {

	// the entity
	private HotelSearchResultComponent component;

	// widget Maps
	private Map<String, Text> viewModeLabel = new HashMap<>();
	private Map<String, Text> tableViewLabel = new HashMap<>();
	private Map<String, Text> mapViewLabel = new HashMap<>();
	private Map<String, Text> tableStartPriceLabel = new HashMap<>();
	private Map<String, Text> distanceToVenueLabel = new HashMap<>();
	private Map<String, Text> availableRoomsLabel = new HashMap<>();
	private Map<String, Text> mapStartPriceLabel = new HashMap<>();
	private Map<String, Text> venueLabel = new HashMap<>();
	private Map<String, Text> goToHotelLabel = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		viewModeLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.VIEW_MODE_LABEL).build());
		tableViewLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.TABLE_VIEW_LABEL).build());
		mapViewLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.MAP_VIEW_LABEL).build());
		tableStartPriceLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.TABLE_START_PRICE_LABEL).build());
		distanceToVenueLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.DISTANCE_TO_VENUE_LABEL).build());
		availableRoomsLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.AVAILABLE_ROOMS_LABEL).build());
		mapStartPriceLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.MAP_START_PRICE_LABEL).build());
		venueLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.VENUE_LABEL).build());
		goToHotelLabel.put(lang, builder.fieldMetadata(HotelSearchResultComponent.GO_TO_HOTEL_LABEL).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public HotelSearchResultComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(HotelSearchResultComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getViewModeLabel(), viewModeLabel);
						setLanguageStringToTextWidget(component.getTableViewLabel(), tableViewLabel);
						setLanguageStringToTextWidget(component.getMapViewLabel(), mapViewLabel);
						setLanguageStringToTextWidget(component.getTableStartPriceLabel(), tableStartPriceLabel);
						setLanguageStringToTextWidget(component.getDistanceToVenueLabel(), distanceToVenueLabel);
						setLanguageStringToTextWidget(component.getAvailableRoomsLabel(), availableRoomsLabel);
						setLanguageStringToTextWidget(component.getMapStartPriceLabel(), mapStartPriceLabel);
						setLanguageStringToTextWidget(component.getVenueLabel(), venueLabel);
						setLanguageStringToTextWidget(component.getGoToHotelLabel(), goToHotelLabel);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (component != null) {
			component.setViewModeLabel( buildLanguageString(viewModeLabel) );
			component.setTableViewLabel( buildLanguageString(tableViewLabel) );
			component.setMapViewLabel( buildLanguageString(mapViewLabel) );
			component.setTableStartPriceLabel( buildLanguageString(tableStartPriceLabel) );
			component.setDistanceToVenueLabel( buildLanguageString(distanceToVenueLabel) );
			component.setAvailableRoomsLabel( buildLanguageString(availableRoomsLabel) );
			component.setMapStartPriceLabel( buildLanguageString(mapStartPriceLabel) );
			component.setVenueLabel( buildLanguageString(venueLabel) );
			component.setGoToHotelLabel( buildLanguageString(goToHotelLabel) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

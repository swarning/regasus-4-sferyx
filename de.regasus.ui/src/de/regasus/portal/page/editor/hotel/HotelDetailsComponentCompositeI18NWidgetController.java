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
import de.regasus.portal.component.hotel.HotelDetailsComponent;

public class HotelDetailsComponentCompositeI18NWidgetController
	implements I18NWidgetController<HotelDetailsComponent> {

	// the entity
	private HotelDetailsComponent component;

	// widget Maps
	private Map<String, Text> hotelLabel = new HashMap<>();
	private Map<String, Text> mapLabel = new HashMap<>();
	private Map<String, Text> satelliteLabel = new HashMap<>();
	private Map<String, Text> travelTypeLabel = new HashMap<>();
	private Map<String, Text> travelTypeDrivingLabel = new HashMap<>();
	private Map<String, Text> travelTypeTransitLabel = new HashMap<>();
	private Map<String, Text> travelTypeWalkingLabel = new HashMap<>();
	private Map<String, Text> travelTypeBicyclingLabel = new HashMap<>();
	private Map<String, Text> hotelDescription = new HashMap<>();
	private Map<String, Text> hotelFacilitiesLabel = new HashMap<>();
	private Map<String, Text> roomFacilitiesLabel = new HashMap<>();

	private Map<String, Text> roomTypeLabel = new HashMap<>();
	private Map<String, Text> ratePerNightLabel = new HashMap<>();
	private Map<String, Text> actionLabel = new HashMap<>();
	private Map<String, Text> proceedToBookingLabel = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		hotelLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.HOTEL_LABEL).bold(true).build());
		mapLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.MAP_LABEL).bold(true).build());
		satelliteLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.SATELLITE_LABEL).bold(true).build());
		travelTypeLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.TRAVEL_TYPE_LABEL).bold(true).build());
		travelTypeDrivingLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.TRAVEL_TYPE_DRIVING_LABEL).bold(true).build());
		travelTypeTransitLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.TRAVEL_TYPE_TRANSIT_LABEL).bold(true).build());
		travelTypeWalkingLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.TRAVEL_TYPE_WALKING_LABEL).bold(true).build());
		travelTypeBicyclingLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.TRAVEL_TYPE_BICYCLING_LABEL).bold(true).build());
		hotelDescription.put(lang, builder.fieldMetadata(HotelDetailsComponent.HOTEL_DESCRIPTION_LABEL).bold(true).build());
		hotelFacilitiesLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.HOTEL_FACILITIES_LABEL).bold(true).build());
		roomFacilitiesLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.ROOM_FACILITIES_LABEL).bold(true).build());

		roomTypeLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.ROOM_TYPE_LABEL).bold(true).build());
		ratePerNightLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.RATE_PER_NIGHT_LABEL).bold(true).build());
		actionLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.ACTION_LABEL).bold(true).build());
		proceedToBookingLabel.put(lang, builder.fieldMetadata(HotelDetailsComponent.PROCEED_TO_BOOKING_LABEL).bold(true).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public HotelDetailsComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(HotelDetailsComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getHotelLabel(), hotelLabel);
						setLanguageStringToTextWidget(component.getMapLabel(), mapLabel);
						setLanguageStringToTextWidget(component.getSatelliteLabel(), satelliteLabel);
						setLanguageStringToTextWidget(component.getTravelTypeLabel(), travelTypeLabel);
						setLanguageStringToTextWidget(component.getTravelTypeDrivingLabel(), travelTypeDrivingLabel);
						setLanguageStringToTextWidget(component.getTravelTypeTransitLabel(), travelTypeTransitLabel);
						setLanguageStringToTextWidget(component.getTravelTypeWalkingLabel(), travelTypeWalkingLabel);
						setLanguageStringToTextWidget(component.getTravelTypeBicyclingLabel(), travelTypeBicyclingLabel);
						setLanguageStringToTextWidget(component.getHotelDescription(), hotelDescription);
						setLanguageStringToTextWidget(component.getHotelFacilitiesLabel(), hotelFacilitiesLabel);
						setLanguageStringToTextWidget(component.getRoomFacilitiesLabel(), roomFacilitiesLabel);

						setLanguageStringToTextWidget(component.getRoomTypeLabel(), roomTypeLabel);
						setLanguageStringToTextWidget(component.getRatePerNightLabel(), ratePerNightLabel);
						setLanguageStringToTextWidget(component.getActionLabel(), actionLabel);
						setLanguageStringToTextWidget(component.getProceedToBookingLabel(), proceedToBookingLabel);
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
			component.setHotelLabel( buildLanguageString(hotelLabel) );
			component.setMapLabel( buildLanguageString(mapLabel) );
			component.setSatelliteLabel( buildLanguageString(satelliteLabel) );
			component.setTravelTypeLabel( buildLanguageString(travelTypeLabel) );
			component.setTravelTypeDrivingLabel( buildLanguageString(travelTypeDrivingLabel) );
			component.setTravelTypeTransitLabel( buildLanguageString(travelTypeTransitLabel) );
			component.setTravelTypeWalkingLabel( buildLanguageString(travelTypeWalkingLabel) );
			component.setTravelTypeBicyclingLabel( buildLanguageString(travelTypeBicyclingLabel) );
			component.setHotelDescription( buildLanguageString(hotelDescription) );
			component.setHotelFacilitiesLabel( buildLanguageString(hotelFacilitiesLabel) );
			component.setRoomFacilitiesLabel( buildLanguageString(roomFacilitiesLabel) );

			component.setRoomTypeLabel( buildLanguageString(roomTypeLabel) );
			component.setRatePerNightLabel( buildLanguageString(ratePerNightLabel) );
			component.setActionLabel( buildLanguageString(actionLabel) );
			component.setProceedToBookingLabel( buildLanguageString(proceedToBookingLabel) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

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
import de.regasus.portal.component.hotel.HotelBookingComponent;

public class HotelBookingComponentCompositeI18NWidgetController
	implements I18NWidgetController<HotelBookingComponent> {

	// the entity
	private HotelBookingComponent component;

	// widget Maps
	private Map<String, Text> hotelLabel = new HashMap<>();
	private Map<String, Text> arrivalLabel = new HashMap<>();
	private Map<String, Text> departureLabel = new HashMap<>();
	private Map<String, Text> roomTypeLabel = new HashMap<>();
	private Map<String, Text> specialRequestLabel = new HashMap<>();
	private Map<String, Text> lateArrivalLabel = new HashMap<>();
	private Map<String, Text> arrivalNoteLabel = new HashMap<>();
	private Map<String, Text> twinRoomLabel = new HashMap<>();
	private Map<String, Text> commentLabel = new HashMap<>();
	private Map<String, Text> additionalGuestsLabel = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		hotelLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.HOTEL_LABEL).bold(true).build());
		arrivalLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.ARRIVAL_LABEL).bold(true).build());
		departureLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.DEPARTURE_LABEL).bold(true).build());
		roomTypeLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.ROOM_TYPE_LABEL).bold(true).build());
		specialRequestLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.SPECIAL_REQUEST_LABEL).bold(true).build());
		lateArrivalLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.LATE_ARRIVAL_LABEL).bold(true).build());
		arrivalNoteLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.ARRIVAL_NOTE_LABEL).bold(true).build());
		twinRoomLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.TWIN_ROOM_LABEL).bold(true).build());
		commentLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.COMMENT_LABEL).bold(true).build());
		additionalGuestsLabel.put(lang, builder.fieldMetadata(HotelBookingComponent.ADDITIONAL_GUESTS_LABEL).bold(true).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public HotelBookingComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(HotelBookingComponent component) {
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
						setLanguageStringToTextWidget(component.getArrivalLabel(), arrivalLabel);
						setLanguageStringToTextWidget(component.getDepartureLabel(), departureLabel);
						setLanguageStringToTextWidget(component.getRoomTypeLabel(), roomTypeLabel);
						setLanguageStringToTextWidget(component.getSpecialRequestLabel(), specialRequestLabel);
						setLanguageStringToTextWidget(component.getLateArrivalLabel(), lateArrivalLabel);
						setLanguageStringToTextWidget(component.getArrivalNoteLabel(), arrivalNoteLabel);
						setLanguageStringToTextWidget(component.getTwinRoomLabel(), twinRoomLabel);
						setLanguageStringToTextWidget(component.getCommentLabel(), commentLabel);
						setLanguageStringToTextWidget(component.getAdditionalGuestsLabel(), additionalGuestsLabel);
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
			component.setArrivalLabel( buildLanguageString(arrivalLabel) );
			component.setDepartureLabel( buildLanguageString(departureLabel) );
			component.setRoomTypeLabel( buildLanguageString(roomTypeLabel) );
			component.setSpecialRequestLabel( buildLanguageString(specialRequestLabel) );
			component.setLateArrivalLabel( buildLanguageString(lateArrivalLabel) );
			component.setArrivalNoteLabel( buildLanguageString(arrivalNoteLabel) );
			component.setTwinRoomLabel( buildLanguageString(twinRoomLabel) );
			component.setCommentLabel( buildLanguageString(commentLabel) );
			component.setAdditionalGuestsLabel( buildLanguageString(additionalGuestsLabel) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

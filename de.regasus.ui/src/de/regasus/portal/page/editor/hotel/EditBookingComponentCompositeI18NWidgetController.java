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
import de.regasus.portal.component.hotel.EditBookingComponent;

public class EditBookingComponentCompositeI18NWidgetController
	implements I18NWidgetController<EditBookingComponent> {

	// the entity
	private EditBookingComponent component;

	// widget Maps
	private Map<String, Text> participantLabel = new HashMap<>();
	private Map<String, Text> hotelLabel = new HashMap<>();
	private Map<String, Text> roomTypeLabel = new HashMap<>();
	private Map<String, Text> changeHotelLabel = new HashMap<>();
	private Map<String, Text> arrivalLabel = new HashMap<>();
	private Map<String, Text> departureLabel = new HashMap<>();
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

		participantLabel.put(lang, builder.fieldMetadata(EditBookingComponent.PARTICIPANT_LABEL).bold(true).build());
		hotelLabel.put(lang, builder.fieldMetadata(EditBookingComponent.HOTEL_LABEL).bold(true).build());
		roomTypeLabel.put(lang, builder.fieldMetadata(EditBookingComponent.ROOM_TYPE_LABEL).bold(true).build());
		changeHotelLabel.put(lang, builder.fieldMetadata(EditBookingComponent.CHANGE_HOTEL_LABEL).bold(true).build());
		arrivalLabel.put(lang, builder.fieldMetadata(EditBookingComponent.ARRIVAL_LABEL).bold(true).build());
		departureLabel.put(lang, builder.fieldMetadata(EditBookingComponent.DEPARTURE_LABEL).bold(true).build());
		specialRequestLabel.put(lang, builder.fieldMetadata(EditBookingComponent.SPECIAL_REQUEST_LABEL).bold(true).build());
		lateArrivalLabel.put(lang, builder.fieldMetadata(EditBookingComponent.LATE_ARRIVAL_LABEL).bold(true).build());
		arrivalNoteLabel.put(lang, builder.fieldMetadata(EditBookingComponent.ARRIVAL_NOTE_LABEL).bold(true).build());
		twinRoomLabel.put(lang, builder.fieldMetadata(EditBookingComponent.TWIN_ROOM_LABEL).bold(true).build());
		commentLabel.put(lang, builder.fieldMetadata(EditBookingComponent.COMMENT_LABEL).bold(true).build());
		additionalGuestsLabel.put(lang, builder.fieldMetadata(EditBookingComponent.ADDITIONAL_GUESTS_LABEL).bold(true).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public EditBookingComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(EditBookingComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getParticipantLabel(), participantLabel);
						setLanguageStringToTextWidget(component.getHotelLabel(), hotelLabel);
						setLanguageStringToTextWidget(component.getRoomTypeLabel(), roomTypeLabel);
						setLanguageStringToTextWidget(component.getChangeHotelLabel(), changeHotelLabel);
						setLanguageStringToTextWidget(component.getArrivalLabel(), arrivalLabel);
						setLanguageStringToTextWidget(component.getDepartureLabel(), departureLabel);
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
			component.setParticipantLabel( buildLanguageString(participantLabel) );
			component.setHotelLabel( buildLanguageString(hotelLabel) );
			component.setRoomTypeLabel( buildLanguageString(roomTypeLabel) );
			component.setChangeHotelLabel( buildLanguageString(changeHotelLabel) );
			component.setArrivalLabel( buildLanguageString(arrivalLabel) );
			component.setDepartureLabel( buildLanguageString(departureLabel) );
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

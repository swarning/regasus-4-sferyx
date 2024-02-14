package de.regasus.portal.page.editor.hotel;

import static com.lambdalogic.util.rcp.i18n.I18NWidgetControllerHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.i18n.I18NWidgetTextBuilder;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.component.hotel.HotelBookingTableComponent;


public class HotelBookingTableComponentLabelsI18NWidgetController implements I18NWidgetController<HotelBookingTableComponent>{

	// the entity
	private HotelBookingTableComponent component;


	// widget Maps

	// table column names
	private Map<String, Text> participantColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> arrivalColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> departureColumnNameWidgetMap = new HashMap<>();
	private Map<String, Text> hotelColumnNameWidgetMap = new HashMap<>();

	// button labels and tooltips
	private Map<String, Text> addBookingLabelWidgetMap = new HashMap<>();
	private Map<String, Text> addBookingDescriptionWidgetMap = new HashMap<>();

	private Map<String, Text> editBookingLabelWidgetMap = new HashMap<>();
	private Map<String, Text> editBookingDescriptionWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(1, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).modifyListener(modifySupport);

		GridDataFactory groupGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_NamesOfTableColumns);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		participantColumnNameWidgetMap.put(		lang, builder.label(ParticipantLabel.Participant).build());
    		arrivalColumnNameWidgetMap.put(		lang, builder.label(HotelLabel.HotelBooking_Arrival).build());
    		departureColumnNameWidgetMap.put(			lang, builder.label(HotelLabel.HotelBooking_Departure).build());
    		hotelColumnNameWidgetMap.put(	lang, builder.label(HotelLabel.Hotel).build());
		}

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_ButtonToAddNewGroupMember);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		addBookingLabelWidgetMap.put(		lang, builder.label(I18N.Label).build());
    		addBookingDescriptionWidgetMap.put(	lang, builder.label(I18N.Tooltip).build());
		}

		{
    		Group group = new Group(parent, SWT.BORDER);
    		groupGridDataFactory.applyTo(group);
    		group.setText(I18N.GroupMemberTable_GroupText_ButtonToEditGroupMembersProgrammeBookings);
    		group.setLayout( new GridLayout(2, false) );
    		builder.parent(group);

    		editBookingLabelWidgetMap.put(		lang, builder.label(I18N.Label).build());
    		editBookingDescriptionWidgetMap.put(lang, builder.label(I18N.Tooltip).build());
		}
	}


	@Override
	public void dispose() {
	}


	@Override
	public HotelBookingTableComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(HotelBookingTableComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getParticipantColumnName(), 	participantColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getArrivalColumnName(), 		arrivalColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getDepartureColumnName(),		departureColumnNameWidgetMap);
						setLanguageStringToTextWidget(component.getHotelColumnName(),			hotelColumnNameWidgetMap);

						setLanguageStringToTextWidget(component.getAddBookingLabel(),			addBookingLabelWidgetMap);
						setLanguageStringToTextWidget(component.getAddBookingDescription(),		addBookingDescriptionWidgetMap);

						setLanguageStringToTextWidget(component.getEditBookingLabel(),			editBookingLabelWidgetMap);
						setLanguageStringToTextWidget(component.getEditBookingDescription(),	editBookingDescriptionWidgetMap);

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
			component.setParticipantColumnName(	buildLanguageString(participantColumnNameWidgetMap) );
			component.setArrivalColumnName(		buildLanguageString(arrivalColumnNameWidgetMap) );
			component.setDepartureColumnName(	buildLanguageString(departureColumnNameWidgetMap) );
			component.setHotelColumnName(	 	buildLanguageString(hotelColumnNameWidgetMap) );

			component.setAddBookingLabel(		buildLanguageString(addBookingLabelWidgetMap) );
			component.setAddBookingDescription(	buildLanguageString(addBookingDescriptionWidgetMap) );

			component.setEditBookingLabel(		 buildLanguageString(editBookingLabelWidgetMap) );
			component.setEditBookingDescription( buildLanguageString(editBookingDescriptionWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

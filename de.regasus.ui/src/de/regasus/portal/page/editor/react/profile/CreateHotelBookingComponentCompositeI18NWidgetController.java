package de.regasus.portal.page.editor.react.profile;

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
import de.regasus.portal.component.react.profile.CreateHotelBookingComponent;

public class CreateHotelBookingComponentCompositeI18NWidgetController
	implements I18NWidgetController<CreateHotelBookingComponent> {

	// the entity
	private CreateHotelBookingComponent component;

	// widget Maps
	private Map<String, Text> buttonLabelWithoutHotelBookingWidgetMap = new HashMap<>();
	private Map<String, Text> buttonLabelWithHotelBookingWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		buttonLabelWithoutHotelBookingWidgetMap.put(lang, builder.fieldMetadata(CreateHotelBookingComponent.BUTTON_LABEL_WITHOUT_HOTEL_BOOKING).build());
		buttonLabelWithHotelBookingWidgetMap.put(lang, builder.fieldMetadata(CreateHotelBookingComponent.BUTTON_LABEL_WITH_HOTEL_BOOKING).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public CreateHotelBookingComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(CreateHotelBookingComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getButtonLabelWithoutHotelBooking(), buttonLabelWithoutHotelBookingWidgetMap);
						setLanguageStringToTextWidget(component.getButtonLabelWithHotelBooking(), buttonLabelWithHotelBookingWidgetMap);
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
			component.setButtonLabelWithoutHotelBooking( buildLanguageString(buttonLabelWithoutHotelBookingWidgetMap) );
			component.setButtonLabelWithHotelBooking( buildLanguageString(buttonLabelWithHotelBookingWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

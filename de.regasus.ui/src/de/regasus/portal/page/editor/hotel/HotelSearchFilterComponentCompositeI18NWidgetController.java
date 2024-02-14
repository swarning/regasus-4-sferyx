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
import de.regasus.portal.component.hotel.HotelSearchFilterComponent;

public class HotelSearchFilterComponentCompositeI18NWidgetController
	implements I18NWidgetController<HotelSearchFilterComponent> {

	// the entity
	private HotelSearchFilterComponent component;

	// widget Maps
	private Map<String, Text> numberOfRoomsLabel = new HashMap<>();
	private Map<String, Text> distanceToVenueLabel = new HashMap<>();
	private Map<String, Text> priceLabel = new HashMap<>();
	private Map<String, Text> starsLabel = new HashMap<>();
	private Map<String, Text> hotelFacilitiesLabel = new HashMap<>();
	private Map<String, Text> notCategorizedLabel = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		numberOfRoomsLabel.put(lang, builder.fieldMetadata(HotelSearchFilterComponent.NUMBER_OF_ROOMS_LABEL).bold(true).build());
		distanceToVenueLabel.put(lang, builder.fieldMetadata(HotelSearchFilterComponent.DISTANCE_TO_VENUE_LABEL).bold(true).build());
		priceLabel.put(lang, builder.fieldMetadata(HotelSearchFilterComponent.PRICE_LABEL).bold(true).build());
		starsLabel.put(lang, builder.fieldMetadata(HotelSearchFilterComponent.STARS_LABEL).bold(true).build());
		hotelFacilitiesLabel.put(lang, builder.fieldMetadata(HotelSearchFilterComponent.HOTEL_FACILITIES_LABEL).bold(true).build());
		notCategorizedLabel.put(lang, builder.fieldMetadata(HotelSearchFilterComponent.NOT_CATEGORIZED_LABEL).bold(true).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public HotelSearchFilterComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(HotelSearchFilterComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getNumberOfRoomsLabel(), numberOfRoomsLabel);
						setLanguageStringToTextWidget(component.getDistanceToVenueLabel(), distanceToVenueLabel);
						setLanguageStringToTextWidget(component.getPriceLabel(), priceLabel);
						setLanguageStringToTextWidget(component.getStarsLabel(), starsLabel);
						setLanguageStringToTextWidget(component.getHotelFacilitiesLabel(), hotelFacilitiesLabel);
						setLanguageStringToTextWidget(component.getNotCategorizedLabel(), notCategorizedLabel);
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
			component.setNumberOfRoomsLabel( buildLanguageString(numberOfRoomsLabel) );
			component.setDistanceToVenueLabel( buildLanguageString(distanceToVenueLabel) );
			component.setPriceLabel( buildLanguageString(priceLabel) );
			component.setStarsLabel( buildLanguageString(starsLabel) );
			component.setHotelFacilitiesLabel( buildLanguageString(hotelFacilitiesLabel) );
			component.setNotCategorizedLabel( buildLanguageString(notCategorizedLabel) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

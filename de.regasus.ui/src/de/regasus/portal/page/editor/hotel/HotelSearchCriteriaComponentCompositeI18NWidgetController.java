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
import de.regasus.portal.component.hotel.HotelSearchCriteriaComponent;

public class HotelSearchCriteriaComponentCompositeI18NWidgetController
	implements I18NWidgetController<HotelSearchCriteriaComponent> {

	// the entity
	private HotelSearchCriteriaComponent component;

	// widget Maps
	private Map<String, Text> arrivalLabelWidgetMap = new HashMap<>();
	private Map<String, Text> departureLabelWidgetMap = new HashMap<>();
	private Map<String, Text> minRoomCountLabelWidgetMap = new HashMap<>();

	private Map<String, Text> sortOptionsLabelWidgetMap = new HashMap<>();

	private Map<String, Text> sortByNameAscLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sortByNameDescLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sortByStarsAscLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sortByStarsDescLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sortByPriceAscLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sortByPriceDescLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sortByDistanceAscLabelWidgetMap = new HashMap<>();
	private Map<String, Text> sortByDistanceDescLabelWidgetMap = new HashMap<>();

	private Map<String, Text> searchLabelWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String lang) {
		parent.setLayout( new GridLayout(2, false) );

		I18NWidgetTextBuilder builder = new I18NWidgetTextBuilder(parent).bold(true).modifyListener(modifySupport);

		arrivalLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.ARRIVAL_LABEL).bold(true).build());
		departureLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.DEPARTURE_LABEL).bold(true).build());
		minRoomCountLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.MIN_ROOM_COUNT_LABEL).bold(true).build());

		sortOptionsLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_OPTIONS_LABEL).bold(true).build());

		sortByNameAscLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_NAME_ASC_LABEL).bold(true).build());
		sortByNameDescLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_NAME_DESC_LABEL).bold(true).build());
		sortByStarsAscLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_STARS_ASC_LABEL).bold(true).build());
		sortByStarsDescLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_STARS_DESC_LABEL).bold(true).build());
		sortByPriceAscLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_PRICE_ASC_LABEL).bold(true).build());
		sortByPriceDescLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_PRICE_DESC_LABEL).bold(true).build());
		sortByDistanceAscLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_DISTANCE_ASC_LABEL).bold(true).build());
		sortByDistanceDescLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SORT_BY_DISTANCE_DESC_LABEL).bold(true).build());

		searchLabelWidgetMap.put(lang, builder.fieldMetadata(HotelSearchCriteriaComponent.SEARCH_LABEL).bold(true).build());
	}


	@Override
	public void dispose() {
	}


	@Override
	public HotelSearchCriteriaComponent getEntity() {
		return component;
	}


	@Override
	public void setEntity(HotelSearchCriteriaComponent component) {
		this.component = component;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (component != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						setLanguageStringToTextWidget(component.getArrivalLabel(), arrivalLabelWidgetMap);
						setLanguageStringToTextWidget(component.getDepartureLabel(), departureLabelWidgetMap);
						setLanguageStringToTextWidget(component.getMinRoomCountLabel(), minRoomCountLabelWidgetMap);

						setLanguageStringToTextWidget(component.getSortOptionsLabel(), sortOptionsLabelWidgetMap);

						setLanguageStringToTextWidget(component.getSortByNameAscLabel(), sortByNameAscLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSortByNameDescLabel(), sortByNameDescLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSortByStarsAscLabel(), sortByStarsAscLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSortByStarsDescLabel(), sortByStarsDescLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSortByPriceAscLabel(), sortByPriceAscLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSortByPriceDescLabel(), sortByPriceDescLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSortByDistanceAscLabel(), sortByDistanceAscLabelWidgetMap);
						setLanguageStringToTextWidget(component.getSortByDistanceDescLabel(), sortByDistanceDescLabelWidgetMap);

						setLanguageStringToTextWidget(component.getSearchLabel(), searchLabelWidgetMap);
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
			component.setArrivalLabel( buildLanguageString(arrivalLabelWidgetMap) );
			component.setDepartureLabel( buildLanguageString(departureLabelWidgetMap) );
			component.setMinRoomCountLabel( buildLanguageString(minRoomCountLabelWidgetMap) );

			component.setSortOptionsLabel( buildLanguageString(sortOptionsLabelWidgetMap) );

			component.setSortByNameAscLabel( buildLanguageString(sortByNameAscLabelWidgetMap) );
			component.setSortByNameDescLabel( buildLanguageString(sortByNameDescLabelWidgetMap) );
			component.setSortByStarsAscLabel( buildLanguageString(sortByStarsAscLabelWidgetMap) );
			component.setSortByStarsDescLabel( buildLanguageString(sortByStarsDescLabelWidgetMap) );
			component.setSortByPriceAscLabel( buildLanguageString(sortByPriceAscLabelWidgetMap) );
			component.setSortByPriceDescLabel( buildLanguageString(sortByPriceDescLabelWidgetMap) );
			component.setSortByDistanceAscLabel( buildLanguageString(sortByDistanceAscLabelWidgetMap) );
			component.setSortByDistanceDescLabel( buildLanguageString(sortByDistanceDescLabelWidgetMap) );

			component.setSearchLabel( buildLanguageString(searchLabelWidgetMap) );
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

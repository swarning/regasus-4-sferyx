package de.regasus.hotel.view.search.pref;

import static de.regasus.hotel.view.search.pref.HotelSearchViewPreferenceDefinition.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class HotelSearchViewPreference extends AbstractPreference {

	private static final HotelSearchViewPreference INSTANCE = new HotelSearchViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static HotelSearchViewPreference getInstance() {
		return INSTANCE;
	}


	private HotelSearchViewPreference() {
	}


	@Override
	public ScopedPreferenceStore getPreferenceStore() {
		return PREFERENCE_STORE;
	}


	public void save() {
		System.out.println("Save " + QUALIFIER  + " preferences to: " + PreferenceHelper.extractLocation( getPreferenceStore() ));

		try {
			getPreferenceStore().save();
		}
		catch (IOException e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	public void initialize() {
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), HotelSearchViewPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

	public Long getEventId() {
		return getLong(EVENT_ID);
	}


	public void setEventId(Long eventId) {
		setValue(EVENT_ID, eventId);
	}


	public String getEventFilter() {
		return getString(EVENT_FILTER);
	}


	public void setEventFilter(String eventFilter) {
		setValue(EVENT_FILTER, eventFilter);
	}


	public String getSearchFields() {
		return getString(SEARCH_FIELDS);
	}


	public void setSearchFields(String searchFields) {
		setValue(SEARCH_FIELDS, searchFields);
	}


	public int[] getColumnOrder() {
		List<Integer> integerList = getIntegerList(COLUMN_ORDER);
		int[] intArray = TypeHelper.toIntArrayFromIntegerColl(integerList);
		return intArray;
	}


	public void setColumnOrder(int[] columnOrder) {
		List<Integer> list = TypeHelper.toList(columnOrder);
		setIntegerListValue(COLUMN_ORDER, list);
	}


	public int[] getColumnWidths() {
		List<Integer> integerList = getIntegerList(COLUMN_WIDTHS);
		int[] intArray = TypeHelper.toIntArrayFromIntegerColl(integerList);
		return intArray;
	}


	public void setColumnWidths(int[] columnWidths) {
		List<Integer> list = TypeHelper.toList(columnWidths);
		setIntegerListValue(COLUMN_WIDTHS, list);
	}


	public boolean isResultCountCheckboxSelected() {
		return getBoolean(RESULT_COUNT_CHECKBOX);
	}


	public void setResultCountCheckboxSelected(boolean resultCountCheckbox) {
		setValue(RESULT_COUNT_CHECKBOX, resultCountCheckbox);
	}


	public Integer getResultCount() {
		return getInteger(RESULT_COUNT);
	}


	public void setResultCount(Integer resultCount) {
		setValue(RESULT_COUNT, resultCount);
	}


	public I18NDate getArrival() {
		return getI18NDate(ARRIVAL);
	}


	public void setArrival(I18NDate arrival) {
		setValue(ARRIVAL, arrival);
	}


	public I18NDate getDeparture() {
		return getI18NDate(DEPARTURE);
	}


	public void setDeparture(I18NDate departure) {
		setValue(DEPARTURE, departure);
	}


	public Integer getRoomCount() {
		return getInteger(ROOM_COUNT);
	}


	public void setRoomCount(Integer roomCount) {
		setValue(ROOM_COUNT, roomCount);
	}

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}

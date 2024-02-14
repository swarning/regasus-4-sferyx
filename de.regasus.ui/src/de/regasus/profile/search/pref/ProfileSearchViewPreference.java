package de.regasus.profile.search.pref;

import static de.regasus.profile.search.pref.ProfileSearchViewPreferenceDefinition.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.pref.AbstractPreference;
import com.lambdalogic.util.rcp.pref.PreferenceHelper;
import com.lambdalogic.util.rcp.pref.PreferenceInitializerHelper;

public class ProfileSearchViewPreference extends AbstractPreference {

	private static final ProfileSearchViewPreference INSTANCE = new ProfileSearchViewPreference();

	private static final ScopedPreferenceStore PREFERENCE_STORE = new ScopedPreferenceStore(
		SCOPE_CONTEXT,
    	QUALIFIER
	);


	public static ProfileSearchViewPreference getInstance() {
		return INSTANCE;
	}


	private ProfileSearchViewPreference() {
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
		PreferenceInitializerHelper.initializePreferences(getPreferenceStore(), ProfileSearchViewPreferenceDefinition.class);
	}


	// *****************************************************************************************************************
	// * Getter / Setter
	// *

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

	// *
	// * Getter / Setter
	// *****************************************************************************************************************

}

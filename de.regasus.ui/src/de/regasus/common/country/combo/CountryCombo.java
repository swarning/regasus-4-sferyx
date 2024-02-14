package de.regasus.common.country.combo;

import java.util.Collection;
import java.util.List;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

import de.regasus.common.Country;
import de.regasus.core.CountryModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

@SuppressWarnings("rawtypes")
public class CountryCombo extends AbstractComboComposite<Country> implements CacheModelListener {

	private static final Country EMPTY_COUNTRY = new Country();

	// Model
	private CountryModel model;


	public CountryCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Country getEmptyEntity() {
		return EMPTY_COUNTRY;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				Country country = (Country) element;
				return LanguageString.toStringAvoidNull(country.getName());
			}
		};
	}


	@Override
	protected Collection<Country> getModelData() throws Exception {
		List<Country> modelData = model.getAllUndeletedCountries();
		return modelData;
	}


	@Override
	protected void initModel() {
		model = CountryModel.getInstance();
		model.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (model != null) {
			model.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public String getCountryCode() {
		String countryCode = null;
		if (entity != null) {
			countryCode = entity.getId();
		}
		return countryCode;
	}


	public void setCountryCode(String countryCode) {
		try {
			Country country = null;
			if (countryCode != null) {
				country = model.getCountry(countryCode);
			}
			setEntity(country);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

package de.regasus.hotel.contingent.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.sql.HotelSearch;
import com.lambdalogic.messeinfo.kernel.interfaces.SQLOperator;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.common.CountryCity;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.view.search.HotelSearchComposite;
import de.regasus.ui.Activator;

/**
 * This page corresponds to the hotel search from MIRCP-1527, with search fields "Country", "City" 
 * and "Name 1" already filled in. Either the country is taken from the event or, if a hotel or 
 * contingent is selected, together with city and name 1 from that hotel.
 */
public class HotelSearchWizardPage extends WizardPage implements ISelectionChangedListener {

	// **************************************************************************
	// * Attributes
	// *

	private CountryCity countryCity;
	private String name1;
	private Hotel hotel;

	// **************************************************************************
	// * Widgets
	// *

	private HotelSearchComposite hotelSearchComposite;
	
	// **************************************************************************
	// * Constructors
	// *

	public HotelSearchWizardPage(CountryCity countryCity, String name1) {
		super(HotelSearchWizardPage.class.getName());
		
		this.countryCity = countryCity;
		this.name1 = name1;
		
		setTitle(HotelLabel.Hotel.getString());
	}
	
	
	// **************************************************************************
	// * Overridden Methods
	// *
	
	@Override
	public void createControl(Composite parent) {
		hotelSearchComposite = new HotelSearchComposite(
			parent, 
			SelectionMode.SINGLE_SELECTION,
			SWT.NONE,	// style
			true		// useDetachedSearchModelInstance
		);

		// Search fields "Country", "City" and "Name 1" already filled in.
		List<SQLParameter> sqlParameters = new ArrayList<SQLParameter>(3);
		try {
			String countryCode = countryCity.getCountryCode();
			SQLParameter countrySQLParameter = HotelSearch.COUNTRY.getSQLParameter(countryCode, SQLOperator.EQUAL);
			countrySQLParameter.setActive(StringHelper.isNotEmpty(countryCode));
			sqlParameters.add(countrySQLParameter);
			
			String city = countryCity.getCity();
			SQLParameter citySQLParameter = HotelSearch.CITY.getSQLParameter(city, SQLOperator.EQUAL);
			citySQLParameter.setActive(StringHelper.isNotEmpty(city));
			sqlParameters.add(citySQLParameter);

			SQLParameter name1SQLParameter = HotelSearch.NAME1.getSQLParameter(name1, SQLOperator.FUZZY_LOWER_ASCII);
			name1SQLParameter.setActive(StringHelper.isNotEmpty(name1));
			sqlParameters.add(name1SQLParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		hotelSearchComposite.setInitialSQLParameters(sqlParameters);
		hotelSearchComposite.setEventPK(null); // initial parameters are evaluated in this method!
		hotelSearchComposite.addPostSelectionChangedListener(this);
 		setControl(hotelSearchComposite);
 		
 		setPageComplete(false);
	}

	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			if (hotel == null) {
				// Opening the page the first time executes the search with the pre-filled values.
				hotelSearchComposite.doSearch();	
			}
			else {
				// Opening the page after coming back, don't search anew
			}
		}
	}
	

	/**
	 *  The button "Next" is only active when one hotel is selected. 
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		hotel = SelectionHelper.getUniqueSelected(event.getSelection());
		if (hotel != null) {
			// Tell the wizard which was the selected hotel
			((CreateHotelContingentWizard)getWizard()).setHotel(hotel);

			setPageComplete(true);
		}
		else {
			setPageComplete(false);
		}
	}

}

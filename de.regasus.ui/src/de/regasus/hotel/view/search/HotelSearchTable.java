package de.regasus.hotel.view.search;

import org.eclipse.swt.widgets.Table;

import de.regasus.common.Country;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.CountryModel;

enum HotelTableColumns {NAME1, CITY, COUNTRY};

public class HotelSearchTable extends SimpleTable<Hotel, HotelTableColumns> {

	private CountryModel countryModel = CountryModel.getInstance();

	public HotelSearchTable(Table table) {
		super(table, HotelTableColumns.class);
	}


	@Override
	public String getColumnText(Hotel hotel, HotelTableColumns column) {
		String label = null;

		switch (column) {
			case NAME1:
				label = hotel.getName1();
				break;
			case CITY:
				label = hotel.getMainAddress().getCity();
				break;
			case COUNTRY:
				String countryCode = hotel.getMainAddress().getCountryPK();
				try {
					Country country = countryModel.getCountry(countryCode);
					label = country.getName().getString();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					label = countryCode;
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}

	@Override
	protected HotelTableColumns getDefaultSortColumn() {
		return HotelTableColumns.NAME1;
	}

}

package de.regasus.hotel.contingent.view;

import org.eclipse.swt.widgets.Table;

import de.regasus.common.Country;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.CountryModel;
import de.regasus.hotel.HotelModel;

enum HotelContingentTableColumns {HOTEL_NAME1, NAME, HOTEL_COUNTRY, HOTEL_CITY};

public class HotelContingentSearchTable extends SimpleTable<HotelContingentVO, HotelContingentTableColumns> {

	private HotelModel hotelModel = HotelModel.getInstance();
	private CountryModel countryModel = CountryModel.getInstance();

	public HotelContingentSearchTable(Table table) {
		super(table, HotelContingentTableColumns.class);
	}


	@Override
	public String getColumnText(HotelContingentVO hotelContingentVO, HotelContingentTableColumns column) {
		String label = null;

		switch (column) {
			case HOTEL_NAME1:
				try {
					Long hotelPK = hotelContingentVO.getHotelPK();
					Hotel hotel = hotelModel.getHotel(hotelPK);
					label = hotel.getName1();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				break;
			case NAME:
				label = hotelContingentVO.getName();
				break;
			case HOTEL_COUNTRY:
				try {
					Long hotelPK = hotelContingentVO.getHotelPK();
					Hotel hotel = hotelModel.getHotel(hotelPK);
					String countryCode = hotel.getMainAddress().getCountryPK();
					Country country = countryModel.getCountry(countryCode);
					label = country.getName().getString();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				break;
			case HOTEL_CITY:
				try {
					Long hotelPK = hotelContingentVO.getHotelPK();
					Hotel hotel = hotelModel.getHotel(hotelPK);
					label = hotel.getMainAddress().getCity();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected HotelContingentTableColumns getDefaultSortColumn() {
		return HotelContingentTableColumns.HOTEL_NAME1;
	}

}

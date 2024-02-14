package de.regasus.hotel.contingent.editor;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.OptionalHotelBookingVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;


enum OptionalHotelBookingTableColumns {HOTEL, NAME, ARRIVAL, DEPARTURE, COUNT, EXPIRATION_TIME, EXPIRATION_ENABLED}

public class OptionalHotelBookingTable extends SimpleTable<OptionalHotelBookingVO, OptionalHotelBookingTableColumns> {

	// models
	private HotelModel hModel = HotelModel.getInstance();
	private HotelContingentModel hcModel = HotelContingentModel.getInstance();


	public OptionalHotelBookingTable(Table table) {
		super(table, OptionalHotelBookingTableColumns.class, true, false);
	}


	@Override
	public String getColumnText(OptionalHotelBookingVO optionalHotelBookingVO, OptionalHotelBookingTableColumns column) {
		String label = null;

		switch (column) {
			case HOTEL:
				try {
					// get HotelContingentCVO
					Long hotelContingentPK = optionalHotelBookingVO.getHotelContingentPK();
					HotelContingentCVO hcCVO = hcModel.getHotelContingentCVO(hotelContingentPK);
					// get Hotel
					Hotel hotel = hModel.getHotel(hcCVO.getHotelPK());
					label = hotel.getName1();
				}
				catch (Exception e) {
					System.err.println(e);
				}
				break;
			case NAME:
				label = optionalHotelBookingVO.getName();
				break;
			case ARRIVAL:
				I18NDate arrival = optionalHotelBookingVO.getArrival();
				if (arrival != null) {
					label = arrival.format();
				}
				break;
			case DEPARTURE:
				I18NDate departure = optionalHotelBookingVO.getDeparture();
				if (departure != null) {
					label = departure.format();
				}
				break;
			case COUNT:
				Integer count = optionalHotelBookingVO.getCount();
				label = String.valueOf(count);
				break;
			case EXPIRATION_TIME:
				I18NDate expiration = optionalHotelBookingVO.getExpiration();
				if (expiration != null) {
					label = expiration.format();
				}
				break;
			case EXPIRATION_ENABLED:
				if (optionalHotelBookingVO.isExpirationEnabled()) {
					label = KernelLabel.Yes.getString();
				}
				else {
					label = KernelLabel.No.getString();
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected Comparable<?> getColumnComparableValue(OptionalHotelBookingVO optionalHotelBookingVO, OptionalHotelBookingTableColumns column) {
		/* Return values that a not of type String, e.g. Date Integer.
		 * Values of type String can be ordered by super.getColumnComparableValue(),
		 * because their visible value returned by getColumnText() is equal to their sort value.
		 */
		switch (column) {
    		case ARRIVAL:
    			return optionalHotelBookingVO.getArrival();
    		case DEPARTURE:
    			return optionalHotelBookingVO.getDeparture();
    		case COUNT:
    			return optionalHotelBookingVO.getCount();
    		case EXPIRATION_TIME:
    			return optionalHotelBookingVO.getExpiration();
    		default:
    			return super.getColumnComparableValue(optionalHotelBookingVO, column);
		}
	}


	@Override
	protected OptionalHotelBookingTableColumns getDefaultSortColumn() {
		return OptionalHotelBookingTableColumns.NAME;
	}

}

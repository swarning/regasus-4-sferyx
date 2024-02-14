package de.regasus.hotel.offering.dialog;

import static com.lambdalogic.util.StringHelper.*;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.ui.Activator;


enum HotelOfferingTableColumns {HOTEL_NAME, CONTINGENT_NAME, OFFERING_DESCRIPTION, GUEST_COUNT, PRICE}

public class HotelOfferingTable extends SimpleTable<HotelOfferingVO, HotelOfferingTableColumns> {

	private static final HotelContingentModel hcModel = HotelContingentModel.getInstance();
	private static final HotelModel hModel = HotelModel.getInstance();
	private static final RoomDefinitionModel rdModel = RoomDefinitionModel.getInstance();


	public HotelOfferingTable(Table table) {
		super(table, HotelOfferingTableColumns.class);
	}


	@Override
	public String getColumnText(HotelOfferingVO hotelOfferingVO, HotelOfferingTableColumns column) {
		try {

			switch (column) {
				case HOTEL_NAME: {
					Long contingentPK = hotelOfferingVO.getHotelContingentPK();
					HotelContingentVO contingentVO = hcModel.getHotelContingentVO(contingentPK);
					Hotel hotel = hModel.getHotel( contingentVO.getHotelPK() );
					String hotelName = hotel.getName1();
					return avoidNull(hotelName);
				}

				case CONTINGENT_NAME: {
					Long contingentPK = hotelOfferingVO.getHotelContingentPK();
					HotelContingentVO contingentVO = hcModel.getHotelContingentVO(contingentPK);
					return contingentVO.getName();
				}

				case OFFERING_DESCRIPTION: {
					String label = EMPTY_STRING;

					LanguageString hoDesc = hotelOfferingVO.getDescription();
					if (hoDesc != null) {
						label = hoDesc.getString();
					}

					if ( isEmpty(label) ) {
						Long roomDefinitionPK = hotelOfferingVO.getRoomDefinitionPK();
						RoomDefinitionVO roomDefinitionVO = rdModel.getRoomDefinitionVO(roomDefinitionPK);
						LanguageString rdName = roomDefinitionVO.getName();
						if (rdName != null) {
							label = rdName.getString();
						}
					}

					return avoidNull(label);
				}

				case GUEST_COUNT: {
					String label = EMPTY_STRING;
					if (hotelOfferingVO.getBedCount() != null) {
						label = String.valueOf( hotelOfferingVO.getBedCount() );
					}
					return label;
				}

				case PRICE: {
					return hotelOfferingVO.getCurrencyAmountGross().format(false, false);
				}

				default:
					return "";
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return EMPTY_STRING;
	}


	@Override
	public Comparable<? extends Object> getColumnComparableValue(
		HotelOfferingVO hotelOfferingVO,
		HotelOfferingTableColumns column
	) {
		try {
    		switch (column) {

    			case GUEST_COUNT:
   					return hotelOfferingVO.getBedCount();

    			case PRICE:
    				return hotelOfferingVO.getAmountGross();

    			default:
    				return super.getColumnComparableValue(hotelOfferingVO, column);
    		}
    	}
    	catch (Exception e) {
    		RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    	}

		return null;
	}


	@Override
	protected HotelOfferingTableColumns getDefaultSortColumn() {
		return HotelOfferingTableColumns.HOTEL_NAME;
	}

}

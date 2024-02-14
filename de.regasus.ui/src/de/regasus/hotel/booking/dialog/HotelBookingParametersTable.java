package de.regasus.hotel.booking.dialog;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingParameter;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;


enum HotelBookingParametersColumns {
	BOOK,
	PARTICIPANT_NR,
	PARTICIPANT,
	INVOICE_RECIPIENT,
	HOTEL,
	TOTAL_AMOUNT
}

/**
 * A table that shows for participants, what hotel bookings are to be done soon.
 * <p>
 * {@link https://mi2.lambdalogic.de/jira/browse/MIRCP-84 }
 */
public class HotelBookingParametersTable
extends SimpleTable<HotelBookingParameter, HotelBookingParametersColumns> {

	private Map<HotelBookingParameter, Boolean> bookingMap;


	public HotelBookingParametersTable(Table table, Map<HotelBookingParameter, Boolean> bookingMap) {
		super(table, HotelBookingParametersColumns.class);
		this.bookingMap = bookingMap;
	}


	@Override
	public Image getColumnImage(HotelBookingParameter hbp, HotelBookingParametersColumns column) {
		switch (column) {
    		case BOOK:
    			if (Boolean.TRUE.equals(bookingMap.get(hbp))) {
    				return IconRegistry.getImage(IImageKeys.CHECKED);
    			}
    			else {
    				return IconRegistry.getImage(IImageKeys.UNCHECKED);
    			}

    		default:
    			return null;
		}
	}


	@Override
	public String getColumnText(
		HotelBookingParameter hbp,
		HotelBookingParametersColumns column
	) {
		switch (column) {
    		case PARTICIPANT:
    			return hbp.getGuestNames();

    		case PARTICIPANT_NR:
    			return TypeHelper.toString( hbp.getBenefitRecipient().getNumber() );

    		case INVOICE_RECIPIENT:
    			return hbp.getInvoiceRecipient().getName();

    		case HOTEL:
    			return hbp.getHotel();

    		case TOTAL_AMOUNT: {
    			int nightCount = hbp.getArrival().until( hbp.getDeparture() ).getDays();
    			BigDecimal totalAmount = hbp.getHotelOfferingVO().getAmountGross(nightCount);

				// build CurrencyAmount to format
				CurrencyAmount currencyAmount = new CurrencyAmount(
					totalAmount,
					hbp.getHotelOfferingVO().getCurrency()
				);

				return currencyAmount.format(false, false);
    		}

    		default:
    			return "";
		}
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		HotelBookingParameter hbp,
		HotelBookingParametersColumns column
	) {
		switch (column) {
    		case PARTICIPANT_NR:
    			return hbp.getBenefitRecipient().getNumber();

    		case TOTAL_AMOUNT: {
    			int nightCount = hbp.getArrival().until( hbp.getDeparture() ).getDays();
    			BigDecimal totalAmount = hbp.getHotelOfferingVO().getAmountGross(nightCount);
				return totalAmount;
    		}

    		default:
    			return super.getColumnComparableValue(hbp, column);
		}

	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, HotelBookingParametersColumns column) {
		switch (column) {
    		case BOOK:
    			return new CheckboxCellEditor(parent, SWT.CENTER);

    		default:
    			return null;
		}
	}


	@Override
	public Object getColumnEditValue(HotelBookingParameter hbp, HotelBookingParametersColumns column) {
		switch (column) {
    		case BOOK:
    			return bookingMap.get(hbp);

    		default:
    			return null;
		}
	}


	@Override
	public boolean setColumnEditValue(
		HotelBookingParameter hbp,
		HotelBookingParametersColumns column,
		Object value
	) {
		switch (column) {
    		case BOOK:
    			bookingMap.put(hbp, (Boolean) value);
    			return true;

    		default:
    			return false;
		}
	}

}

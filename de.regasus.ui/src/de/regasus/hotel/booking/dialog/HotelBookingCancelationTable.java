package de.regasus.hotel.booking.dialog;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.hotel.data.HotelBookingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelCancelationTermVO;
import com.lambdalogic.messeinfo.invoice.data.CancelationTermVO_Zero_Valid_Amount_Comparator;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.TableHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelCancelationTermModel;
import de.regasus.ui.Activator;


enum HotelBookingCancelationColumns {
	DESCRIPTION, TERM
}

/**
 * A table in which a checkbox (image) is used for setting that an offering is booked, the counted places can
 * additionaly be increased, and the price may possibly be edited.
 *
 * @author manfred
 */
public class HotelBookingCancelationTable extends
	SimpleTable<HotelBookingCVO, HotelBookingCancelationColumns> {

	// *************************************************************************
	// * Attributes to buffer the user input
	// *
	private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

	private HotelCancelationTermModel hotelCancelationTermModel = HotelCancelationTermModel.getInstance();

	private List<HotelBookingCVO> bookingsCVO;

	private HashMap<HotelBookingCVO, Integer> booking2ChosenCancelationTermIndexMap =
		new HashMap<>();

	private HashMap<HotelBookingCVO, List<HotelCancelationTermVO>> booking2CancelationTermListMap =
		new HashMap<>();

	private ComboBoxCellEditor comboBoxCellEditor;

	private HotelCancelationTermVO DUMMY_CANCELLATION_TERM = new HotelCancelationTermVO();


	public HotelBookingCancelationTable(Table table, List<HotelBookingCVO> bookingsCVO) {
		super(table, HotelBookingCancelationColumns.class, true, true);
		this.bookingsCVO = bookingsCVO;

		PriceVO priceVO = new PriceVO();
		priceVO.setAmount(BigDecimal.ZERO);
		priceVO.setCurrency(bookingsCVO.get(0).getCurrency());
		DUMMY_CANCELLATION_TERM.setPriceVO(priceVO);
		DUMMY_CANCELLATION_TERM.setPriceEditable(false);

		try {
			setupDataAndMaps();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		setInput(bookingsCVO);

	}


	// *************************************************************************
	// * Overridden table specific methods
	// *

	@Override
	public String getColumnText(HotelBookingCVO hotelBookingCVO, HotelBookingCancelationColumns column) {
		String label = null;

		switch (column) {
			case DESCRIPTION: {
				StringBuffer sb = new StringBuffer();
				sb.append( hotelBookingCVO.getLabelForHotelContingent() );
				sb.append(" - ");
				sb.append( hotelBookingCVO.getLabelForOffering() );

				label = sb.toString();

				break;
			}
			case TERM: {
				if (hotelBookingCVO.isCanceled()) {
					label = I18N.CancelCancel;
				}
				else {
					HotelCancelationTermVO hotelCancelationTermVO = getChosenCancelationTermForBooking(hotelBookingCVO);

					if (hotelCancelationTermVO == null) {
						label = DUMMY_CANCELLATION_TERM.getLabel(dateFormat);
					}
					else {
						label = hotelCancelationTermVO.getLabel(dateFormat);
					}
				}
				break;
			}
		}

		if (label == null) {
			label = "";
		}

		return label;
	}



	@Override
	public CellEditor getColumnCellEditor(Composite parent, HotelBookingCancelationColumns column) {
		switch (column) {
		case TERM:
			comboBoxCellEditor = new ComboBoxCellEditor(parent, new String[0], SWT.READ_ONLY);
			TableHelper.prepareComboBoxCellEditor(comboBoxCellEditor);
			return comboBoxCellEditor;

		default:
			return null;
		}
	}


	@Override
	public Image getColumnImage(HotelBookingCVO hotelBookingCVO, HotelBookingCancelationColumns column) {
		if (column == HotelBookingCancelationColumns.TERM) {
			if (hotelBookingCVO.isCanceled()) {
				return IconRegistry.getImage(IImageKeys.EMPTY);
			}
			List<HotelCancelationTermVO> termsForBooking = booking2CancelationTermListMap.get(hotelBookingCVO);

			if (termsForBooking.size() == 1) {
				return IconRegistry.getImage(IImageKeys.EMPTY);
			}
			else {
				return IconRegistry.getImage(IImageKeys.NEXT);
			}
		}
		return null;
	}


	@Override
	public boolean isColumnEditable(HotelBookingCVO hotelBookingCVO, HotelBookingCancelationColumns column) {
		boolean editable = !hotelBookingCVO.isCanceled();
		return editable;
	}


	@Override
	public Object getColumnEditValue(HotelBookingCVO hotelBookingCVO, HotelBookingCancelationColumns column) {
		switch (column) {
			case TERM: {
				int indexForBooking = 0;

				// Check whether there is something to edit after all
				List<HotelCancelationTermVO> hctVOs = booking2CancelationTermListMap.get(hotelBookingCVO);
				if ( ! hctVOs.isEmpty()) {
					// Populate the combobox with strings for each cancellation term
					String items[] = new String[hctVOs.size()];
					for (int i = 0; i < items.length; i++) {
						items[i] = hctVOs.get(i).getLabel(dateFormat);
					}
					comboBoxCellEditor.setItems(items);

					// Tell the editor which one is currently selected
					indexForBooking = booking2ChosenCancelationTermIndexMap.get(hotelBookingCVO);
				}
				return indexForBooking;
			}
			default:
				return null;
		}
	}


	@Override
	public boolean setColumnEditValue(
		HotelBookingCVO hotelBookingCVO,
		HotelBookingCancelationColumns column,
		Object value
	) {
		switch (column) {
			case TERM:
				booking2ChosenCancelationTermIndexMap.put(hotelBookingCVO, (Integer) value);
			default:
				return true;
		}
	}

	// *
	// * Overridden table specific methods
	// *************************************************************************


	private void setupDataAndMaps() throws Exception {
		// Iterate through all selected bookings
		for (HotelBookingCVO hotelBookingCVO : bookingsCVO) {
			if (hotelBookingCVO.isCanceled()) {
				continue;
			}

			// Get all the cancelation terms of the offering of the booking from the model
			Long hotelOfferingPK = hotelBookingCVO.getHotelOfferingCVO().getPK();
			List<HotelCancelationTermVO> cancelationTermVOs =
				hotelCancelationTermModel.getHotelCancelationTermVOsByHotelOfferingPK(hotelOfferingPK);

			cancelationTermVOs = new ArrayList<>(cancelationTermVOs);

			/* If there is no cancellation term at all, or only one with a price not equal 0, enable additionally the
			 * 0 dummy cancellation.
			 */

			// check if cancelationTermVOs contains one with an amount of 0
			boolean hasZeroAmount = false;
			for (HotelCancelationTermVO cancelationTermVO : cancelationTermVOs) {
				if (cancelationTermVO.getPriceVO().getAmount().signum() == 0) {
					hasZeroAmount = true;
					break;
				}
			}

			if ( ! hasZeroAmount) {
				cancelationTermVOs.add(0, DUMMY_CANCELLATION_TERM);
			}

			// Sort the list
			Collections.sort(cancelationTermVOs, CancelationTermVO_Zero_Valid_Amount_Comparator.getInstance());

			// Put that offering and the list in the map of cancellation terms
			booking2CancelationTermListMap.put(hotelBookingCVO, cancelationTermVOs);

			// Start with having the first (cheapest) cancellation term chosen
			booking2ChosenCancelationTermIndexMap.put(hotelBookingCVO, 0);
		}
	}


	public  HotelCancelationTermVO getChosenCancelationTermForBooking(HotelBookingCVO hotelBookingCVO) {
		if (hotelBookingCVO.isCanceled()) {
			return null;
		}
		Integer indexForBooking = booking2ChosenCancelationTermIndexMap.get(hotelBookingCVO);
		List<HotelCancelationTermVO> termsForBooking = booking2CancelationTermListMap.get(hotelBookingCVO);
		HotelCancelationTermVO hotelCancelationTermVO = null;
		if (indexForBooking >= 0 &&
			CollectionsHelper.notEmpty(termsForBooking) &&
			termsForBooking.size() > indexForBooking
		) {
			hotelCancelationTermVO = termsForBooking.get(indexForBooking.intValue());
		}

		if (hotelCancelationTermVO == DUMMY_CANCELLATION_TERM) {
			return null;
		}
		else {
			return hotelCancelationTermVO;
		}
	}

}

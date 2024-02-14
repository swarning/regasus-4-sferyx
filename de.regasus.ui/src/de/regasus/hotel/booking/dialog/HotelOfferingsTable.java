package de.regasus.hotel.booking.dialog;

import static com.lambdalogic.util.StringHelper.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.VolumeVO;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.ui.Activator;


/**
 * A table in which a checkbox (image) is used for setting that an offering is booked, the counted places can
 * additionally be increased, and the price and deposit may possibly be edited.
 *
 * @author manfred
 */
public class HotelOfferingsTable extends SimpleTable<HotelOfferingCVO, HotelOfferingsTableColumns> {

	// *************************************************************************
	// * Attributes to change cell property
	// *

	private boolean bookColumnCheckBox = true;

	// *************************************************************************
	// * Attributes to buffer the user input
	// *

	private HotelOfferingCVO bookedOffering = null;

	private Long bookedOfferingPK = null;
	private Integer bookedCount = 1;
	private I18NDate firstNight;

	private TreeMap<Long, PriceVO> depositMap;


	// *************************************************************************
	// * Constructor
	// *

	public HotelOfferingsTable(
		Table table,
		TreeMap<Long, PriceVO> depositMap
	) {
		super(table, HotelOfferingsTableColumns.class);
		this.depositMap = depositMap;
	}


	// *************************************************************************
	// * Overridden table specific methods
	// *

	@Override
	public Image getColumnImage(HotelOfferingCVO element, HotelOfferingsTableColumns column) {
		switch (column) {
			case BOOK:
				if ( isBookColumnCheckBox() ){
					if (element.getPK().equals(bookedOfferingPK)) {
						return IconRegistry.getImage(IImageKeys.CHECKED);
					}
					else {
						return IconRegistry.getImage(IImageKeys.UNCHECKED);
					}
				}

			default:
				return null;
		}
	}


	@Override
	public String getColumnText(HotelOfferingCVO hotelOfferingCVO, HotelOfferingsTableColumns column) {
		try {

			HotelOfferingVO hoVO = hotelOfferingCVO.getVO();

			switch (column) {
				case COUNT: {
					String label = EMPTY_STRING;
					if (hoVO.getPK().equals(bookedOfferingPK)) {
						label = String.valueOf(bookedCount);
					}
					return label;
				}

				case HOTEL: {
					String label = hotelOfferingCVO.getHotelContingentCVO().getHotelName();
					return avoidNull(label);
				}

				case CONTINGENT: {
					return hotelOfferingCVO.getHotelContingentCVO().getHcName();
				}

				case GUEST_COUNT: {
					String label = EMPTY_STRING;
					if (hoVO.getBedCount() != null) {
						label = String.valueOf( hoVO.getBedCount() );
					}
					return label;
				}

				case DESCRIPTION: {
					String label = EMPTY_STRING;

					LanguageString hoDesc = hoVO.getDescription();
					if (hoDesc != null) {
						label = hoDesc.getString();
					}

					if ( isEmpty(label) ) {
						LanguageString rdName = hotelOfferingCVO.getRoomDefinitionVO().getName();
						if (rdName != null) {
							label = rdName.getString();
						}
					}

					return avoidNull(label);
				}

				case CATEGORY: {
					return avoidNull( hoVO.getCategory() );
				}

				case DEPOSIT: {
					PriceVO priceVO = depositMap.get(hoVO.getPK());
					CurrencyAmount depositAmountBrutto = priceVO.getCurrencyAmountGross();
					return depositAmountBrutto.format(false, false);
				}

				case LODGE_PRICE: {
					return hoVO.getLodgePriceVO().getCurrencyAmountGross().format(false, false);
				}

				case BF_PRICE: {
					String label = EMPTY_STRING;
					if (hoVO.isWithBfPrice()) {
						label = hoVO.getBfPriceVO().getCurrencyAmountGross().format(false, false);
					}
					return label;
				}

				case ADD_PRICE_1: {
					String label = EMPTY_STRING;
					if (hoVO.isWithAdd1Price()) {
	    				label = hoVO.getAdd1PriceVO().getCurrencyAmountGross().format(false, false);
					}
					return label;
				}

				case ADD_PRICE_2: {
					String label = EMPTY_STRING;
					if (hotelOfferingCVO.getVO().isWithAdd2Price()) {
						label = hoVO.getAdd2PriceVO().getCurrencyAmountGross().format(false, false);
					}
					return label;
				}

				case TOTAL_PRICE: {
					return hoVO.getCurrencyAmountGross().format(false, false);
				}

//			case VOLUMES: {
//				Map<Date, Integer> dayCountMap = new TreeMap<Date, Integer>();
//
//				List<VolumeVO> volumeList = hotelOfferingCVO.getHotelContingentCVO().getVolumes();
//				for (VolumeVO volumeVO : volumeList) {
//					if (volumeVO.getFree() != null) {
//						dayCountMap.put(volumeVO.getDay(), volumeVO.getFree());
//					}
//				}
//
//				StringBuilder sb = new StringBuilder();
//
//				Date day = firstNight;
//
//				while (! day.after(lastNight) ) {
//					Integer integer = dayCountMap.get(day);
//					if (integer == null) {
//						sb.append("-,");
//					}
//					else {
//						sb.append(integer + "-");
//					}
//					day = DateHelper.addDays(day, 1);
//				}
//				if (sb.length() > 1) {
//					sb.deleteCharAt(sb.length() - 1);
//				}
//				return sb.toString();
//			}



//				case NIGHT00: return getNightCount(hotelOfferingCVO, firstNight, 0);
//				case NIGHT01: return getNightCount(hotelOfferingCVO, firstNight, 1);
//				case NIGHT02: return getNightCount(hotelOfferingCVO, firstNight, 2);
//				case NIGHT03: return getNightCount(hotelOfferingCVO, firstNight, 3);
//				case NIGHT04: return getNightCount(hotelOfferingCVO, firstNight, 4);
//				case NIGHT05: return getNightCount(hotelOfferingCVO, firstNight, 5);
//				case NIGHT06: return getNightCount(hotelOfferingCVO, firstNight, 6);
//				case NIGHT07: return getNightCount(hotelOfferingCVO, firstNight, 7);
//				case NIGHT08: return getNightCount(hotelOfferingCVO, firstNight, 8);
//				case NIGHT09: return getNightCount(hotelOfferingCVO, firstNight, 9);

				default:
					int i = column.ordinal() - HotelOfferingsTableColumns.NIGHT00.ordinal();
					return getNightCount(hotelOfferingCVO, firstNight, i);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return EMPTY_STRING;
	}


	/**
	 * @param hotelOfferingCVO
	 * @param firstTableDay Datum des ersten Tages der Tabelle
	 * @param dayIndex Index des abgefragten Wertes, 0 = erste Tabellenspalte Datum des ersten Tages
	 * @return
	 */
	private String getNightCount(HotelOfferingCVO hotelOfferingCVO, I18NDate firstTableDay, int dayIndex) {
		String count = null;

		List<VolumeVO> volumeVOs = hotelOfferingCVO.getHotelContingentCVO().getVolumes();
		if (volumeVOs != null && !volumeVOs.isEmpty()) {
			VolumeVO firstVolumeVO = volumeVOs.get(0);
			I18NDate firstVolumeDay = firstVolumeVO.getDay();
			// Number of days the contingent starts after the beginning of the table
			int dayDiff = firstTableDay.until(firstVolumeDay).getDays();
			// Index of the contingent volume: can be negative if the contingent starts later than the table
			int volumeIndex = dayIndex - dayDiff;
			if (volumeIndex >= 0 && volumeIndex < volumeVOs.size()) {
				VolumeVO volumeVO = volumeVOs.get(volumeIndex);
				count = volumeVO.getBookFree().toString();
			}
			else {
				count = "";
			}
		}

		return count;
	}


	@Override
	public Comparable<? extends Object> getColumnComparableValue(
		HotelOfferingCVO hotelOfferingCVO,
		HotelOfferingsTableColumns column
	) {
		try {
    		HotelOfferingVO hoVO = hotelOfferingCVO.getVO();

    		switch (column) {
    			case BOOK:
    				return Boolean.valueOf(hoVO.getPK().equals(bookedOfferingPK));

    			case COUNT:
    				if (hoVO.getPK().equals(bookedOfferingPK)) {
    					return bookedCount;
    				}
    				return 0;

    			case GUEST_COUNT:
   					return hoVO.getBedCount();

    			case DEPOSIT:
    				return depositMap.get(hoVO.getPK()).getAmountGross();

    			case LODGE_PRICE:
    				return hoVO.getLodgePriceVO().getAmountGross();

    			case BF_PRICE:
    				if (hoVO.isWithBfPrice()) {
    					return hoVO.getBfPriceVO().getAmountGross();
    				}
    				return null;

    			case ADD_PRICE_1:
    				if (hoVO.isWithAdd1Price()) {
    					return hoVO.getAdd1PriceVO().getAmountGross();
    				}
    				return null;

    			case ADD_PRICE_2:
    				if (hoVO.isWithAdd2Price()) {
    					return hoVO.getAdd2PriceVO().getAmountGross();
    				}
    				return null;

    			case TOTAL_PRICE:
    				return hoVO.getAmountGross();

    			default:
    				return super.getColumnComparableValue(hotelOfferingCVO, column);
    		}
    	}
    	catch (Exception e) {
    		RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    	}

		return null;
	}


	@Override
	protected HotelOfferingsTableColumns getDefaultSortColumn() {
		return HotelOfferingsTableColumns.HOTEL;
	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, HotelOfferingsTableColumns column) {
		switch (column) {
			case BOOK:
				return new CheckboxCellEditor(parent, SWT.CENTER);

			case COUNT:
				return new TextCellEditor(parent, SWT.RIGHT);

			case DEPOSIT:
				return new TextCellEditor(parent, SWT.RIGHT);

			default:
				return null;
		}
	}


	@Override
	public Object getColumnEditValue(HotelOfferingCVO element, HotelOfferingsTableColumns column) {
		switch (column) {
			case BOOK: {
				Boolean value = Boolean.valueOf(element.getPK().equals(bookedOfferingPK));
				return value;
			}

			case COUNT: {
				String value = null;
				if (bookedOffering == element) {
					value = String.valueOf(bookedCount);
				}
				return value;
			}

			case DEPOSIT:
				return depositMap.get(element.getPK()).getCurrencyAmountGross().getAmountAsString();

			default:
				return null;
		}
	}


	@Override
	public boolean setColumnEditValue(HotelOfferingCVO element, HotelOfferingsTableColumns column, Object value) {
		switch (column) {
			case BOOK: {
				if (Boolean.TRUE.equals(value)) {
					bookedOffering = element;
					bookedOfferingPK = element.getPK();
				}
				return true;
			}

			case COUNT: {
				try {
					bookedCount = TypeHelper.toInteger(value);
					return true;
				}
				catch (Exception e) {
					System.err.println(e);
				}
				// Beep if count couldn't be parsed or was negative
				Display.getCurrent().beep();
				return false;
			}

			case DEPOSIT: {
				try {
					// convert String to BigDecimal
					BigDecimal amount = TypeHelper.toBigDecimal(value).setScale(2, RoundingMode.HALF_UP);
					if (amount.signum() >= 0) {
						PriceVO priceVO = depositMap.get(element.getPK());
						priceVO.setAmount(amount, true);
						return true;
					}
				}
				catch (ParseException e) {
					System.err.println(e);
				}
				// Beep if amount couldn't be parsed or was negative or price cannot be edited anyway
				Display.getCurrent().beep();
				return false;
			}

			default:
				return false;
		}
	}


	public boolean isBookColumnCheckBox() {
		return bookColumnCheckBox;
	}


	public void setBookColumnCheckBox(boolean bookColumnCheckBox) {
		this.bookColumnCheckBox = bookColumnCheckBox;
	}


	public boolean isAnythingBooked() {
		return bookedOffering != null;
	}


	public void setFirstNight(I18NDate firstNight) {
		this.firstNight = firstNight;
	}


	public HotelOfferingCVO getBookedOffering() {
		return bookedOffering;
	}


	public Integer getBookedCount() {
		return bookedCount;
	}


	public PriceVO getDeposit() {
		return depositMap.get(bookedOfferingPK);
	}

}

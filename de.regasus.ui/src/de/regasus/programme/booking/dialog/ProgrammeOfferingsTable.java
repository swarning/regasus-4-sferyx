package de.regasus.programme.booking.dialog;

import static com.lambdalogic.util.CollectionsHelper.empty;
import static com.lambdalogic.util.StringHelper.EMPTY_STRING;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.ObjectComparator;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.TableHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.programme.WorkGroupModel;
import de.regasus.ui.Activator;


enum ProgrammeOfferingsTableColumns {
	BOOK,
	COUNT,
	DESCRIPTION,
	WORKGROUP,
	MAIN_PRICE,
	ADD_PRICE_1,
	ADD_PRICE_2,
	TOTAL_PRICE,
	NUMBER_OF_BOOKINGS
}


/**
 * A table in which a checkbox (image) is used for setting that an offering is booked,
 * the counted places can additionaly be increased, and the price may possibly be edited.
 */
public class ProgrammeOfferingsTable extends SimpleTable<ProgrammeOfferingCVO, ProgrammeOfferingsTableColumns> {

	private ModifySupport modifySupport;

	private static final String EMPTY = "";

	// *************************************************************************
	// * Attributes to buffer the user input
	// *

	private TreeMap<ProgrammeOfferingCVO, Integer> bookingMap;

	private TreeMap<ProgrammeOfferingCVO, PriceVO> priceMap;

	private TreeMap<ProgrammeOfferingCVO, Long> workGroupMap;

	private WorkGroupModel workGroupModel = WorkGroupModel.getInstance();

	private ComboBoxCellEditor comboBoxCellEditor;

	// *************************************************************************
	// * Constructor
	// *

	public ProgrammeOfferingsTable(
		Table table,
		TreeMap<ProgrammeOfferingCVO, Integer> bookingMap,
		TreeMap<ProgrammeOfferingCVO, PriceVO> priceMap,
		TreeMap<ProgrammeOfferingCVO, Long> workGroupMap
	) {
		super(table, ProgrammeOfferingsTableColumns.class, true, true);
		this.bookingMap = bookingMap;
		this.priceMap = priceMap;
		this.workGroupMap = workGroupMap;

		modifySupport = new ModifySupport(table);
	}

	// *************************************************************************
	// * Overridden table specific methods
	// *

	@Override
	public Image getColumnImage(ProgrammeOfferingCVO element, ProgrammeOfferingsTableColumns column) {
		switch (column) {
    		case BOOK:
    			if ( isBooked(element) ) {
    				return IconRegistry.getImage(IImageKeys.CHECKED);
    			}
    			else {
    				return IconRegistry.getImage(IImageKeys.UNCHECKED);
    			}

    		case MAIN_PRICE:
    			if (element.getOfferingVO().isPriceEditable()) {
    				return IconRegistry.getImage(IImageKeys.EDIT);
    			}
    			else {
    				return null;
    			}

    		default:
    			return null;
		}
	}


	@Override
	public String getColumnText(ProgrammeOfferingCVO programmeOfferingCVO, ProgrammeOfferingsTableColumns column) {
		try {
			ProgrammeOfferingVO poVO = programmeOfferingCVO.getVO();
			PriceVO priceVO = priceMap.get(programmeOfferingCVO);

    		switch (column) {
    			case COUNT: {
    				String label = EMPTY_STRING;
    				if (isBooked(programmeOfferingCVO)) {
    					label = String.valueOf( getCount(programmeOfferingCVO) );
    				}
    				return label;
    			}

    			case DESCRIPTION: {
    				String label = programmeOfferingCVO.getShortLabel().getString();
    				label = StringHelper.removeLineBreaks(label);
    				return label;
    			}

    			case WORKGROUP: {
    				String label = EMPTY_STRING;
    				Long workGroupPK = workGroupMap.get(programmeOfferingCVO);
    				if (workGroupPK != null) {
    					if (workGroupPK.equals(ProgrammeBookingParameter.AUTO_WORK_GROUP)) {
    						label = ParticipantLabel.AUTO.getString();
    					}
    					else {
    						try {
    							label = workGroupModel.getWorkGroupVO(workGroupPK).getName();
    						}
    						catch (Exception e) {
    							com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
    							label = "*** " + UtilI18N.Error + " ***";
    						}
    					}
    				}
    				return label;
    			}

    			case MAIN_PRICE: {
    				String label = priceVO.getCurrencyAmountGross().format(false, false);
    				return label;
    			}

				case ADD_PRICE_1: {
					String label = EMPTY_STRING;
					if (poVO.isWithAdd1Price()) {
	    				label = poVO.getAdd1PriceVO().getCurrencyAmountGross().format(false, false);
					}
					return label;
				}

				case ADD_PRICE_2: {
					String label = EMPTY_STRING;
					if (poVO.isWithAdd2Price()) {
	    				label = poVO.getAdd2PriceVO().getCurrencyAmountGross().format(false, false);
					}
					return label;
				}

				case TOTAL_PRICE: {
    				CurrencyAmount totalPrice = priceVO.getCurrencyAmountGross();

    				if (poVO.isWithAdd1Price()) {
    					totalPrice = totalPrice.add(poVO.getAdd1PriceVO().getCurrencyAmountGross());
    				}

    				if (poVO.isWithAdd2Price()) {
    					totalPrice = totalPrice.add(poVO.getAdd2PriceVO().getCurrencyAmountGross());
    				}

    				return totalPrice.format(false, false);
				}

    			case NUMBER_OF_BOOKINGS: {
    				// Do not show the number of bookings and max number of the offering, but of the programme point
    				// which is normally the desired behaviour (and also corresponds to the Swing client)
    				ProgrammePointCVO programmePointCVO = programmeOfferingCVO.getProgrammePointCVO();
    				String number = String.valueOf(programmePointCVO.getNumberOfBookings());

    				Integer maxNumber = programmePointCVO.getVO().getMaxNumber();
    				if (maxNumber != null) {
    					number += " / " + maxNumber;
    				}

    				return number;
    			}

    			default:
    				return EMPTY_STRING;
    		}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return "";
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		ProgrammeOfferingCVO programmeOfferingCVO,
		ProgrammeOfferingsTableColumns column
	) {
		try {
			ProgrammeOfferingVO poVO = programmeOfferingCVO.getVO();
			PriceVO priceVO = priceMap.get(programmeOfferingCVO);

    		switch (column) {
    			// BOOK --> see getColumnComparator

    			case COUNT:
    				if ( isBooked(programmeOfferingCVO) ) {
    					return getCount(programmeOfferingCVO);
    				}
   					return 0;

    			case MAIN_PRICE:
    				return priceVO.getAmountGross();

    			case ADD_PRICE_1:
    				if (poVO.isWithAdd1Price()) {
    					return poVO.getAdd1PriceVO().getAmountGross();
    				}
    				return null;

    			case ADD_PRICE_2:
    				if (poVO.isWithAdd2Price()) {
    					return poVO.getAdd2PriceVO().getAmountGross();
    				}
    				return null;

    			case TOTAL_PRICE:
    				BigDecimal totalPrice = priceVO.getAmountGross();

    				if (poVO.isWithAdd1Price()) {
    					totalPrice = totalPrice.add(poVO.getAdd1PriceVO().getAmountGross());
    				}

    				if (poVO.isWithAdd2Price()) {
    					totalPrice = totalPrice.add(poVO.getAdd2PriceVO().getAmountGross());
    				}

    				return totalPrice;

    			case NUMBER_OF_BOOKINGS:
    				// Do not show the number of bookings and max number of the offering, but of the programme point
    				// which is normally the desired behavior (and also corresponds to the Swing client)
    				ProgrammePointCVO programmePointCVO = programmeOfferingCVO.getProgrammePointCVO();
    				return programmePointCVO.getNumberOfBookings();

    			default:
    				return super.getColumnComparableValue(programmeOfferingCVO, column);
    		}
    	}
    	catch (Exception e) {
    		RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    	}

		return null;
	}


	private static final Function<ProgrammeOfferingCVO, Integer> ppPositionFunction = new Function<>() {
		@Override
		public Integer apply(ProgrammeOfferingCVO programmeOfferingCVO) {
			return programmeOfferingCVO.getProgrammePointCVO().getProgrammePointVO().getPosition();
		}
	};


	// Sort (1) by position of Programme Point and (2) by position of Programme Offering
	private static final Comparator<ProgrammeOfferingCVO> BOOK_COLUMN_COMPARATOR = Comparator
		.comparing(    ppPositionFunction,                ObjectComparator.getInstance())
		.thenComparing(ProgrammeOfferingCVO::getPosition, ObjectComparator.getInstance());


	@Override
	protected Comparator<ProgrammeOfferingCVO> getColumnComparator(final ProgrammeOfferingsTableColumns column) {
		try {
    		switch (column) {
    			case BOOK: {
    				return BOOK_COLUMN_COMPARATOR;
    			}

    			default:
    				return super.getColumnComparator(column);
    		}
    	}
    	catch (Exception e) {
    		RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    	}

		return null;

	}


	@Override
	protected ProgrammeOfferingsTableColumns getDefaultSortColumn() {
		return ProgrammeOfferingsTableColumns.BOOK;
	}


	@Override
	public boolean isColumnEditable(ProgrammeOfferingCVO element, ProgrammeOfferingsTableColumns column) {
		switch (column) {
			case BOOK:
			case COUNT:
				return true;

			case MAIN_PRICE:
				return element.getVO().isPriceEditable();

			case WORKGROUP:
				return true;

			default:
				return false;
		}
	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, ProgrammeOfferingsTableColumns column) {
		switch (column) {
    		case BOOK:
    			return new CheckboxCellEditor(parent);

    		case COUNT:
    			return new TextCellEditor(parent, SWT.RIGHT);

    		case MAIN_PRICE:
    			return new TextCellEditor(parent, SWT.RIGHT);

    		case WORKGROUP:
    			comboBoxCellEditor = new ComboBoxCellEditor(parent, new String[0], SWT.READ_ONLY);
    			TableHelper.prepareComboBoxCellEditor(comboBoxCellEditor);
    			return comboBoxCellEditor;

    		default:
    			return null;
		}
	}


	@Override
	public Object getColumnEditValue(ProgrammeOfferingCVO offeringCVO, ProgrammeOfferingsTableColumns column) {
		switch (column) {
    		case BOOK:
    			return Boolean.valueOf( isBooked(offeringCVO) );

    		case COUNT:
    			return String.valueOf(getCount(offeringCVO));

    		case MAIN_PRICE:
    			if (offeringCVO.getVO().isPriceEditable()) {
    				return priceMap.get(offeringCVO).getCurrencyAmountGross().getAmountAsString();
    			}
    			return null;

    		case WORKGROUP:
    			try {
    				Long programmePointPK = offeringCVO.getProgrammeOfferingVO().getProgrammePointPK();
    				List<WorkGroupVO> workGroupVOs = workGroupModel.getWorkGroupVOsByProgrammePointPK(programmePointPK);
    				if ( empty(workGroupVOs) ) {
    					return null;
    				}

    				List<String> workGroupNames = new ArrayList<String>();
    				for (WorkGroupVO workGroupVO : workGroupVOs) {
    					workGroupNames.add(workGroupVO.getName());
    				}

    				Collections.sort(workGroupNames, Collator.getInstance());
    				workGroupNames.add(0, EMPTY);
    				workGroupNames.add(1, ParticipantLabel.AUTO.getString());

    				comboBoxCellEditor.setItems(workGroupNames.toArray(new String[0]));

    				return 0;
    			}
    			catch (Exception e) {
    				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    			}

			default:
				return null;
		}
	}


	@Override
	public boolean setColumnEditValue(ProgrammeOfferingCVO element, ProgrammeOfferingsTableColumns column, Object value) {
		switch (column) {
    		case BOOK:
    			if (Boolean.FALSE.equals(value)) {
    				setBooked(element, 0);
    			}
    			else {
    				setBooked(element, 1);
    			}

    			return true;

    		case COUNT:
    			try {
    				int count = Integer.parseInt(String.valueOf(value));
    				setBooked(element, count);
    				return true;
    			}
    			catch (Exception e) {
    				System.err.println(e);
    			}
    			// Beep if count couldn't be parsed or was negative
    			Display.getCurrent().beep();
    			return false;

    		case MAIN_PRICE:
    			if (element.getVO().isPriceEditable()) {
    				try {
    					BigDecimal amount = TypeHelper.toBigDecimal(value).setScale(2, RoundingMode.HALF_UP);
    					PriceVO priceVO = priceMap.get(element);
    					priceVO.setAmount(amount, true);
    					modifySupport.fire();
    					return true;
    				}
    				catch (ParseException e) {
    					System.err.println(e);
    				}
    			}
    			// Beep if amount couldn't be parsed or was negative or price cannot be edited anyway
    			Display.getCurrent().beep();
    			return false;

    		case WORKGROUP:
    			// We receive the index of the selected combo item
    			int index = Integer.parseInt(String.valueOf(value));
    			// If empty selection, remove from map
    			if (index == 0) {
    				workGroupMap.remove(element);
    				return true;
    			}
    			// If "AUTO" selection, add the dummy-Long
    			else if (index == 1) {
    				workGroupMap.put(element, ProgrammeBookingParameter.AUTO_WORK_GROUP);
    				return true;
    			}
    			// Otherwise, find the workgroup with the selected name
    			else {
    				String nameOfSelectedWorkGroup = comboBoxCellEditor.getItems()[index];

    				try {
    					Long programmePointPK = element.getProgrammeOfferingVO().getProgrammePointPK();
    					List<WorkGroupVO> workGroupVOs = workGroupModel.getWorkGroupVOsByProgrammePointPK(programmePointPK);
    					if (! CollectionsHelper.empty(workGroupVOs)) {
    						for (WorkGroupVO workGroupVO : workGroupVOs) {
    							if (nameOfSelectedWorkGroup.equals(workGroupVO.getName())) {
    								workGroupMap.put(element, workGroupVO.getPK());
    								return true;
    							}
    						}
    					}
    				}
    				catch (Exception e) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    				}
    			}

			default:
				return false;
		}
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}


	// *************************************************************************
	// * Private helper methods
	// *

	protected boolean isAnythingBooked() {
		for(Integer count : bookingMap.values()) {
			if (count.intValue() > 0) {
				return true;
			}
		}
		return false;
	}


	private boolean isBooked(ProgrammeOfferingCVO element) {
		return Boolean.valueOf(bookingMap.get(element).intValue() > 0);
	}


	private Integer getCount(ProgrammeOfferingCVO element) {
		return bookingMap.get(element);
	}


	private void setBooked(ProgrammeOfferingCVO element, int count) {
		bookingMap.put(element, count);
	}

}

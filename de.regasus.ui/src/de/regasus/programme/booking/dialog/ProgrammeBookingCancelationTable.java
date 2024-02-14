package de.regasus.programme.booking.dialog;

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

import com.lambdalogic.messeinfo.invoice.data.CancelationTermVO_Zero_Valid_Amount_Comparator;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.TableHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.programme.ProgrammeCancelationTermModel;
import de.regasus.ui.Activator;

enum ProgrammeBookingCancelationColumns {
	DESCRIPTION, TERM
}

/**
 * A table in which a checkbox (image) is used for setting that an offering is booked, the counted places can
 * additionaly be increased, and the price may possibly be edited.
 */
public class ProgrammeBookingCancelationTable extends SimpleTable<ProgrammeBookingCVO, ProgrammeBookingCancelationColumns> {

	// *************************************************************************
	// * Attributes to buffer the user input
	// *
	private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

	private ProgrammeCancelationTermModel programmeCancelationTermModel = ProgrammeCancelationTermModel.getInstance();

	private List<ProgrammeBookingCVO> bookingsCVO;

	private HashMap<ProgrammeBookingCVO, Integer> booking2ChosenCancelationTermIndexMap =
		new HashMap<>();

	private HashMap<ProgrammeBookingCVO, List<ProgrammeCancelationTermVO>> booking2CancelationTermListMap =
		new HashMap<>();

	private ComboBoxCellEditor comboBoxCellEditor;

	private ProgrammeCancelationTermVO DUMMY_CANCELLATION_TERM = new ProgrammeCancelationTermVO();


	public ProgrammeBookingCancelationTable(Table table, List<ProgrammeBookingCVO> bookingsCVOs) {
		super(table, ProgrammeBookingCancelationColumns.class, true, true);
		this.bookingsCVO = bookingsCVOs;

		PriceVO priceVO = new PriceVO();
		priceVO.setAmount(BigDecimal.ZERO);
		String sampleCurrency = bookingsCVOs.get(0).getBookingVO().getCurrency();
		priceVO.setCurrency(sampleCurrency);
		DUMMY_CANCELLATION_TERM.setPriceVO(priceVO);
		DUMMY_CANCELLATION_TERM.setPriceEditable(false);

		try {
			setupDataAndMaps();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		setInput(bookingsCVOs);

	}


	// *************************************************************************
	// * Overridden table specific methods
	// *

	@Override
	public String getColumnText(ProgrammeBookingCVO programmeBookingCVO, ProgrammeBookingCancelationColumns column) {
		try {
			switch (column) {
				case DESCRIPTION: {
					String description = programmeBookingCVO.getDescription().getString();
					// Removing possibly newlines, without introducing glued-together-words or double-spaces
					description = description.replace("(\n", "(");
					description = description.replace("\n", " ");
					description = description.replace("  ", " ");
					return description;
				}
				case TERM: {
					if (programmeBookingCVO.isCanceled()) {
						return I18N.CancelCancel;
					}
					ProgrammeCancelationTermVO programmeCancelationTermVO =
						getChosenCancelationTermForBooking(programmeBookingCVO);

					if (programmeCancelationTermVO == null) {
						return DUMMY_CANCELLATION_TERM.getLabel(dateFormat);
					}
					else {
						return programmeCancelationTermVO.getLabel(dateFormat);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return "";
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		ProgrammeBookingCVO programmeBookingCVO,
		ProgrammeBookingCancelationColumns column
	) {
		try {
			switch (column) {
				case TERM: {
					if (programmeBookingCVO.isCanceled() ) {
						return null;
					}
					ProgrammeCancelationTermVO programmeCancelationTermVO =
							getChosenCancelationTermForBooking(programmeBookingCVO);

					if (programmeCancelationTermVO == null) {
						return null;
					}
					else {
						return programmeCancelationTermVO.getStartTime();
					}
				}
				default:
					return super.getColumnComparableValue(programmeBookingCVO, column);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, ProgrammeBookingCancelationColumns column) {
		switch (column) {
			case TERM: {
				comboBoxCellEditor = new ComboBoxCellEditor(parent, new String[0], SWT.READ_ONLY);
				TableHelper.prepareComboBoxCellEditor(comboBoxCellEditor);
				return comboBoxCellEditor;
			}
			default:
				return null;
		}
	}


	@Override
	public Image getColumnImage(ProgrammeBookingCVO programmeBookingCVO, ProgrammeBookingCancelationColumns column) {
		if (column == ProgrammeBookingCancelationColumns.TERM) {
			if (programmeBookingCVO.isCanceled()) {
				return IconRegistry.getImage(IImageKeys.EMPTY);
			}
			List<ProgrammeCancelationTermVO> termsForBooking = booking2CancelationTermListMap.get(programmeBookingCVO);

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
	public boolean isColumnEditable(ProgrammeBookingCVO programmeBookingCVO, ProgrammeBookingCancelationColumns column) {
		boolean editable = !programmeBookingCVO.isCanceled();
		return editable;
	}


	@Override
	public Object getColumnEditValue(ProgrammeBookingCVO programmeBookingCVO, ProgrammeBookingCancelationColumns column) {
		switch (column) {
			case TERM: {
				int indexForBooking = 0;

				// Check whether there is something to edit after all
				List<ProgrammeCancelationTermVO> pctVOs = booking2CancelationTermListMap.get(programmeBookingCVO);
				if ( ! pctVOs.isEmpty()) {
					// Populate the comboBox with strings for each cancellation term
					String items[] = new String[pctVOs.size()];
					for (int i = 0; i < items.length; i++) {
						items[i] = pctVOs.get(i).getLabel(dateFormat);
					}
					comboBoxCellEditor.setItems(items);

					// Tell the editor which one is currently selected
					indexForBooking = booking2ChosenCancelationTermIndexMap.get(programmeBookingCVO);
				}
				return indexForBooking;
			}
			default:
				return null;
		}
	}


	@Override
	public boolean setColumnEditValue(
		ProgrammeBookingCVO programmeBookingCVO,
		ProgrammeBookingCancelationColumns column,
		Object value
	) {
		if (column == ProgrammeBookingCancelationColumns.TERM) {
			booking2ChosenCancelationTermIndexMap.put(programmeBookingCVO, (Integer) value);
			return true;
		}

		return false;
	}

	// *
	// * Overridden table specific methods
	// *************************************************************************


	private void setupDataAndMaps() throws Exception {
		// Iterate through all selected bookings
		for (ProgrammeBookingCVO programmeBookingCVO : bookingsCVO) {
			if (programmeBookingCVO.isCanceled()) {
				continue;
			}

			// Get all the cancellation terms of the offering of the booking from the model
			Long programmeOfferingPK = programmeBookingCVO.getProgrammeOfferingCVO().getPK();
			List<ProgrammeCancelationTermVO> cancelationTermVOs =
				programmeCancelationTermModel.getProgrammeCancelationTermVOsByProgrammeOfferingPK(programmeOfferingPK);

			cancelationTermVOs = new ArrayList<>(cancelationTermVOs);

			/* If there is no cancellation term at all, or only one with a price not equal 0, enable additionally the
			 * 0 dummy cancellation.
			 */

			// check if cancelationTermVOs contains one with an amount of 0
			boolean hasZeroAmount = false;
			for (ProgrammeCancelationTermVO cancelationTermVO : cancelationTermVOs) {
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
			booking2CancelationTermListMap.put(programmeBookingCVO, cancelationTermVOs);

			// Start with having the first (cheapest) cancellation term chosen
			booking2ChosenCancelationTermIndexMap.put(programmeBookingCVO, 0);
		}
	}


	public ProgrammeCancelationTermVO getChosenCancelationTermForBooking(ProgrammeBookingCVO programmeBookingCVO) {
		if (programmeBookingCVO.isCanceled()) {
			return null;
		}
		Integer indexForBooking = booking2ChosenCancelationTermIndexMap.get(programmeBookingCVO);
		List<ProgrammeCancelationTermVO> termsForBooking = booking2CancelationTermListMap.get(programmeBookingCVO);
		ProgrammeCancelationTermVO programmeCancelationTermVO = null;
		if (indexForBooking >= 0 &&
			CollectionsHelper.notEmpty(termsForBooking) &&
			termsForBooking.size() > indexForBooking
		) {
			programmeCancelationTermVO = termsForBooking.get(indexForBooking.intValue());
		}

		if (programmeCancelationTermVO == DUMMY_CANCELLATION_TERM) {
			return null;
		}
		else {
			return programmeCancelationTermVO;
		}
	}

}

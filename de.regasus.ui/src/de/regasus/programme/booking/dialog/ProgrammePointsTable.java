package de.regasus.programme.booking.dialog;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingParameter;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.TableHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.programme.WorkGroupModel;
import de.regasus.ui.Activator;

enum ProgrammePointsTableColumns {
	BOOK, COUNT, DESCRIPTION, BOOKING_NO, WORKGROUP
}

/**
 * A table in which a checkbox (image) is used for setting that an offering is booked, the counted places can
 * additionaly be increased, and the price may possibly be edited.
 *
 * @author manfred
 */
public class ProgrammePointsTable extends SimpleTable<ProgrammePointCVO, ProgrammePointsTableColumns> {

	private static final String EMPTY = "";

	// *************************************************************************
	// * Attributes to buffer the user input
	// *

	private TreeMap<ProgrammePointCVO, Integer> bookingMap;

	private TreeMap<ProgrammePointCVO, Long> workGroupMap;

	private WorkGroupModel workGroupModel = WorkGroupModel.getInstance();

	private ComboBoxCellEditor comboBoxCellEditor;

	// *************************************************************************
	// * Constructor
	// *

	public ProgrammePointsTable(Table table, TreeMap<ProgrammePointCVO, Integer> bookingMap, TreeMap<ProgrammePointCVO, Long> workGroupMap) {
		super(table, ProgrammePointsTableColumns.class, true, true);
		this.bookingMap = bookingMap;
		this.workGroupMap = workGroupMap;
	}


	// *************************************************************************
	// * Overridden table specific methods
	// *

	@Override
	public String getColumnText(ProgrammePointCVO programmePointCVO, ProgrammePointsTableColumns column) {
		switch (column) {
		case COUNT:
			if (isBooked(programmePointCVO)) {
				return String.valueOf(getCount(programmePointCVO));
			}
			else {
				return "";
			}
		case DESCRIPTION:
			return programmePointCVO.getPpName().getString();
		case BOOKING_NO:
			// Do not show the number of bookings and max number of the offering, but of the programme point
			// which is normally the desired behaviour (and also corresponds to the Swing client)
			String number = String.valueOf(programmePointCVO.getNumberOfBookings());
			Integer maxNumber = programmePointCVO.getVO().getMaxNumber();
			if (maxNumber != null) {
				return number + "/" + maxNumber;
			}
			else {
				return number;
			}
		case WORKGROUP:
			Long pk = workGroupMap.get(programmePointCVO);
			if (pk != null) {
				if (pk.equals(ProgrammeBookingParameter.AUTO_WORK_GROUP)) {
					return ParticipantLabel.AUTO.getString();
				} else {
					try {
						return workGroupModel.getWorkGroupVO(pk).getName();
					}
					catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
						return "*** " + UtilI18N.Error + " ***";
					}
				}
			}
		default:
			return null;
		}
	}


	@Override
	public Comparable<? extends Object> getColumnComparableValue(
		ProgrammePointCVO programmePointCVO,
		ProgrammePointsTableColumns column) {

		switch (column) {
		case COUNT:
			if (isBooked(programmePointCVO)) {
				return getCount(programmePointCVO);
			}
		case BOOKING_NO:
			return programmePointCVO.getNumberOfBookings();
		default:
			return super.getColumnComparableValue(programmePointCVO, column);
		}

	}

	@Override
	public CellEditor getColumnCellEditor(Composite parent, ProgrammePointsTableColumns column) {
		switch (column) {
		case BOOK:
			return new CheckboxCellEditor(parent, SWT.CENTER);
		case COUNT:
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
	public Object getColumnEditValue(ProgrammePointCVO element, ProgrammePointsTableColumns column) {
		switch (column) {
		case BOOK:
			return Boolean.valueOf(isBooked(element));
		case COUNT:
			return String.valueOf(getCount(element));
		case WORKGROUP:
			try {
				Long programmePointPK = element.getPK();
				List<WorkGroupVO> workGroupVOs = workGroupModel.getWorkGroupVOsByProgrammePointPK(programmePointPK);
				if (CollectionsHelper.empty(workGroupVOs)) {
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
	public Image getColumnImage(ProgrammePointCVO element, ProgrammePointsTableColumns column) {
		switch (column) {
		case BOOK:
			if (isBooked(element)) {
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
	public boolean setColumnEditValue(ProgrammePointCVO element, ProgrammePointsTableColumns column, Object value) {
		switch (column) {
		case BOOK:
			setBooked(element, value);
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
					Long programmePointPK = element.getPK();
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
					// We don't know if something changed, so better refresh viewer nonetheless
					return true;
				}

			}
		default:
			return false;
		}
	}


	// *************************************************************************
	// * Private helper methods
	// *

	protected boolean isAnythingBooked() {
		for (Integer count : bookingMap.values()) {
			if (count.intValue() > 0) {
				return true;
			}
		}
		return false;
	}


	private boolean isBooked(ProgrammePointCVO element) {
		return bookingMap.get(element).intValue() > 0;
	}


	private Integer getCount(ProgrammePointCVO element) {
		return bookingMap.get(element);
	}


	private void setBooked(ProgrammePointCVO element, Object value) {
		if (Boolean.FALSE.equals(value)) {
			setBooked(element, 0);
		}
		else {
			setBooked(element, 1);
		}

	}


	private void setBooked(ProgrammePointCVO element, int count) {
		bookingMap.put(element, count);
	}

}

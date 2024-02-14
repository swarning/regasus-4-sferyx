package de.regasus.hotel.contingent.editor;

import static com.lambdalogic.util.StringHelper.EMPTY_STRING;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.hotel.data.VolumeVO;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.ui.Activator;

enum VolumeTableColumns {
	DATE, MANDATORY, TRUE_SIZE, BOOK_SIZE, PUBLIC_SIZE, BOOKED, FREE
}

public class VolumeTable extends SimpleTable<VolumeVO, VolumeTableColumns> {


	private static final FormatHelper FORMAT_HELPER = FormatHelper.getDefaultLocaleInstance();


	public VolumeTable(Table table) {
		super(
			table,
			VolumeTableColumns.class,
			false,	// sortable
			true	// editable
		);
	}


	@Override
	public Image getColumnImage(VolumeVO element, VolumeTableColumns column) {
		switch (column) {
    		case MANDATORY:
    			if ( element.isMandatory() ) {
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
	public String getColumnText(VolumeVO volumeVO, VolumeTableColumns column) {
		switch (column) {
    		case DATE:
    			return FORMAT_HELPER.formatDate(volumeVO.getDate());
    		case TRUE_SIZE: {
    			String label = "";
    			Integer trueSize = volumeVO.getTrueSize();
    			if (trueSize != null) {
    				label = String.valueOf(trueSize);
    			}
    			return label;
    		}
    		case BOOK_SIZE: {
    			String label = String.valueOf(volumeVO.getBookToTrueSizeDifference());
    			return label;
    		}
    		case PUBLIC_SIZE: {
    			String label = "";
    			Integer publicSize = volumeVO.getPublicSize();
				if (publicSize != null) {
    				label = String.valueOf(publicSize);
    			}
    			return label;
    		}
    		case BOOKED: {
    			String label = "";
    			Integer used = volumeVO.getUsed();
    			if (used != null) {
    				label = String.valueOf(used);
    			}
    			return label;
    		}
    		case FREE: {
    			String label = "";
    			Integer free = volumeVO.getBookFree();
    			if (free != null) {
    				label = String.valueOf(free);
    			}
    			return label;
    		}

    		default:
    			return EMPTY_STRING;
		}
	}


	@Override
	public boolean isColumnEditable(VolumeVO volumeVO, VolumeTableColumns column) {
		switch (column) {
			case MANDATORY:
    		case TRUE_SIZE:
    		case BOOK_SIZE:
    		case PUBLIC_SIZE:
				return true;

			default:
				return false;
		}
	}


	/**
	 * When a column returns a CellEditor, all cells in it are editable.
	 */
	@Override
	public CellEditor getColumnCellEditor(Composite parent, VolumeTableColumns column) {
		switch (column) {
			case MANDATORY:
				return new CheckboxCellEditor(parent);

    		case TRUE_SIZE:
    		case BOOK_SIZE:
    		case PUBLIC_SIZE:
    			return new TextCellEditor(parent, SWT.NONE);
    		default:
    			return null;
    		}
	}


	@Override
	public Object getColumnEditValue(VolumeVO volumeVO, VolumeTableColumns column) {
		switch (column) {
    		case MANDATORY:
    			return Boolean.valueOf( volumeVO.isMandatory() );

    			/*
    			 * The default implementation works fine, because it returns the columnText as String
    			 * and the CellEditor for all these columns is a TextCellEditor.
    			 */
    		case TRUE_SIZE:
    		case BOOK_SIZE:
    		case PUBLIC_SIZE:
    			return super.getColumnEditValue(volumeVO, column);

			default:
				return null;
		}
	}


	@Override
	public boolean setColumnEditValue(VolumeVO volumeVO, VolumeTableColumns column, Object value) {
		switch (column) {
    		case MANDATORY:
    			boolean mandatory = TypeHelper.toBoolean(value, false);
   				volumeVO.setMandatory(mandatory);
    			return true;

    		case TRUE_SIZE:
    			try {
    				// safe old value of trueSize
    				Integer oldTrueSize = volumeVO.getTrueSize();

    				// set new value of trueSize
    				Integer newTrueSize = 0;

    				String strValue = (String) value;
    				if (StringHelper.isNotEmpty(strValue)) {
        				newTrueSize = Integer.parseInt(strValue);
    				}

    				// calculate the difference between the new and the old value of trueSize
    				int diff = newTrueSize - oldTrueSize;

    				if (diff != 0) {
        				volumeVO.setTrueSize(newTrueSize);

        				/* Because the table shows not the bookSize but the difference between bookSize
        				 * and trueSize, bookSize must change, too.
        				 */
        				int oldBookSize = volumeVO.getBookSize();
        				int newBookSize = oldBookSize + diff;
        				volumeVO.setBookSize(newBookSize);

        				// data changed
        				return true;
    				}
    			}
    			catch (NumberFormatException e) {
    				Display.getCurrent().beep();
    			}

    			// nothing changed
    			return false;

    		case BOOK_SIZE:
    			try {
    				Integer diff = null;
    				String strValue = (String) value;
    				if (StringHelper.isNotEmpty(strValue)) {
    					diff = Integer.parseInt(strValue);
    				}

    				if (diff == null) {
    					// no diff means 0, equal to trueSize
    					diff = 0;
    				}
    				else if (diff < 0) {
    					// bookSize must not be smaller than number of bookings
    					Shell shell = super.getViewer().getTable().getShell();
    					MessageDialog.openInformation(
    						shell,
    						I18N.VolumeTable_BookSizeNotNegative_Title,
    						I18N.VolumeTable_BookSizeNotNegative_Message
    					);
    					return false;
    				}

    				Integer trueSize = volumeVO.getTrueSize();

    				// calculate bookSize
    				Integer bookSize = trueSize + diff;

    				if (bookSize.intValue() < volumeVO.getUsed().intValue()) {
    					// bookSize must not be smaller than number of bookings
    					Shell shell = super.getViewer().getTable().getShell();
    					MessageDialog.openInformation(
    						shell,
    						I18N.VolumeTable_BookSizeNotSmallerThanNumberOfBookings_Title,
    						I18N.VolumeTable_BookSizeNotSmallerThanNumberOfBookings_Message
    					);
    					return false;
    				}

    				if ( ! EqualsHelper.isEqual(bookSize, volumeVO.getBookSize())) {
    					volumeVO.setBookSize(bookSize);

        				// data changed
        				return true;
    				}
    			}
    			catch (NumberFormatException e) {
    				Display.getCurrent().beep();
    			}

    			// nothing changed
    			return false;

    		case PUBLIC_SIZE:
    			try {
    				Integer publicSize = null;

    				String strValue = (String) value;
    				if (StringHelper.isNotEmpty(strValue)) {
    					publicSize = Integer.parseInt(strValue);
    				}

    				if ( ! EqualsHelper.isEqual(publicSize, volumeVO.getPublicSize())) {
    					volumeVO.setPublicSize(publicSize);

        				// data changed
    					return true;
    				}
    			}
    			catch (NumberFormatException e) {
    				Display.getCurrent().beep();
    			}

    			// nothing changed
    			return false;

    		default:
    			return false;
		}
	}


//	@Override
//	public Color getBackground(Object element, int columnIndex) {
//		Color color = null;
//
//		try {
//			VolumeTableColumns column = VolumeTableColumns.values()[columnIndex];
//			if (column == VolumeTableColumns.BOOKED) {
//				VolumeVO volumeVO = (VolumeVO) element;
//				if (volumeVO.isOverbooked()) {
//					color = getViewer().getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
//				}
//			}
//		}
//		catch (Exception e) {
//			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//		}
//
//		return color;
//	}


	@Override
	public Color getForeground(Object element, int columnIndex) {
		Color color = null;

		try {
			VolumeTableColumns column = VolumeTableColumns.values()[columnIndex];
			if (column == VolumeTableColumns.BOOKED) {
				VolumeVO volumeVO = (VolumeVO) element;
				if (volumeVO.isOverbooked()) {
					color = getViewer().getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return color;
	}

}

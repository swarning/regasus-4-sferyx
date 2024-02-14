package de.regasus.onlineform.dialog;

import java.util.Collection;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.data.FormProgrammePointTypeConfigVO;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.ui.IconRegistry;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.programme.ProgrammePointTypeModel;

enum FormProgrammePointTypeConfigColumn {
	NAME, SINGLE, REQUIRED, MAX_BOOKING_COUNT, TEXT_HEADER, TEXT_SUBTOTAL_LINE;
};


/**
 * A table that shows for each FormProgrammePointTypeConfigVO a row with several editable columns with checkboxes to
 * switch single and/or required booking, the max number of bookings, and international texts for header and subtotal
 * line.
 *
 * @author manfred
 */
public class FormProgrammePointTypeConfigTable extends
	SimpleTable<FormProgrammePointTypeConfigVO, FormProgrammePointTypeConfigColumn> {

	public FormProgrammePointTypeConfigTable(Table table) {
		super(table, FormProgrammePointTypeConfigColumn.class, false /* not sortable */);
	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, FormProgrammePointTypeConfigColumn column) {
		switch (column) {
		case SINGLE:
			return new CheckboxCellEditor(parent, SWT.CENTER);
		case REQUIRED:
			return new CheckboxCellEditor(parent, SWT.CENTER);
		case MAX_BOOKING_COUNT:
			return new TextCellEditor(parent, SWT.RIGHT);
		case TEXT_HEADER:
			return new LanguageStringDialogCellEditor(parent, OnlineFormI18N.TextHeader);
		case TEXT_SUBTOTAL_LINE:
			return new LanguageStringDialogCellEditor(parent, OnlineFormI18N.TextSubtotalLine);
		default:
			return null;
		}
	}


	@Override
	public Image getColumnImage(FormProgrammePointTypeConfigVO configVO, FormProgrammePointTypeConfigColumn column) {
		switch (column) {
		case SINGLE:
			if (configVO.isSingleBooking()) {
				return IconRegistry.getImage("icons/checked.gif");
			}
			else {
				return IconRegistry.getImage("icons/unchecked.gif");
			}
		case REQUIRED:
			if (configVO.isRequiredBooking()) {
				return IconRegistry.getImage("icons/checked.gif");
			}
			else {
				return IconRegistry.getImage("icons/unchecked.gif");
			}
		default:
			// No other column shows any image
			return null;
		}
	}


	@Override
	public String getColumnText(FormProgrammePointTypeConfigVO configVO, FormProgrammePointTypeConfigColumn column) {
		switch (column) {
		case NAME:
			Long pk = configVO.getProgrammePointTypePK();
			if (pk == null) {
				return OnlineFormI18N.Undefined;
			}
			else {
				try {
					return ProgrammePointTypeModel.getInstance().getProgrammePointTypeVO(pk).getName().getString();
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					return e.getClass().getSimpleName() + "!";
				}
			}
		case MAX_BOOKING_COUNT:
			return String.valueOf(configVO.getMaxBookingCount());
		case TEXT_HEADER:
			LanguageString textHeaderI18n = configVO.getTextHeaderI18n();
			if (textHeaderI18n != null) {
				System.out.println("Returning " +textHeaderI18n.getDataString());
			}
			return getStrings(configVO.getTextHeaderI18n());
		case TEXT_SUBTOTAL_LINE:
			return getStrings(configVO.getTextSubtotalLineI18n());

		default:
			// No other column shows any text
			return null;
		}
	}


	private String getStrings(LanguageString languageString) {
		if (languageString == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		Collection<String> languageCodes = languageString.getLanguageCodes();

		for (String code : languageCodes) {
			sb.append(languageString.getString(code));
			sb.append(" (");
			sb.append(code);
			sb.append("), ");

		}
		if (sb.length() > 2) {
			sb.delete(sb.length()-2, sb.length()-1);
		}
		return sb.toString();

	}


	@Override
	public Object getColumnEditValue(
		FormProgrammePointTypeConfigVO configVO,
		FormProgrammePointTypeConfigColumn column) {
		switch (column) {
		case SINGLE:
			return Boolean.valueOf(configVO.isSingleBooking());
		case REQUIRED:
			return Boolean.valueOf(configVO.isRequiredBooking());
		case MAX_BOOKING_COUNT:
			return String.valueOf(configVO.getMaxBookingCount());
		case TEXT_HEADER:
			return configVO.getTextHeaderI18n();
		case TEXT_SUBTOTAL_LINE:
			return configVO.getTextSubtotalLineI18n();
		default:
			// The other columns (NAME) are not editable
			return null;
		}
	}


	@Override
	public boolean setColumnEditValue(
		FormProgrammePointTypeConfigVO configVO,
		FormProgrammePointTypeConfigColumn column,
		Object value
	) {
		switch (column) {
		case SINGLE:
			configVO.setSingleBooking(Boolean.TRUE.equals(value));
			return true;
		case REQUIRED:
			configVO.setRequiredBooking(Boolean.TRUE.equals(value));
			return true;
		case MAX_BOOKING_COUNT:
			try {
				Integer count = TypeHelper.toInteger(value);
				if (count >= FormProgrammePointTypeConfigVO.MIN_MAX_BOOKING_COUNT && count <= FormProgrammePointTypeConfigVO.MAX_MAX_BOOKING_COUNT) {
					configVO.setMaxBookingCount(count);
					return true;
				}
			}
			catch (Exception e) {
				System.err.println(e);
			}
			// Beep if count couldn't be parsed or was negative
			Display.getCurrent().beep();
			return false;
		case TEXT_HEADER:
			configVO.setTextHeaderI18n((LanguageString)value);
			return true;
		case TEXT_SUBTOTAL_LINE:
			configVO.setTextSubtotalLineI18n((LanguageString)value);
			return true;
		default:
			// The other columns (NAME) are not editable
			return false;
		}
	}

}

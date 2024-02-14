package de.regasus.event.editor;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Objects;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.time.DateTimeFormatterCache;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;

enum TemplateTableColumns {AUTO_UPLOAD, LANGUAGE, FILE, DIR, EDIT_USER, EDIT_TIME}

public class TemplateTable extends SimpleTable<de.regasus.common.File, TemplateTableColumns> {

	private static final String AUTO_UPLOAD_KEY = "AUTO_UPLOAD";
	private static final String IN_EDIT_KEY = "IN_EDIT";


	public TemplateTable(Table table) {
		super(table, TemplateTableColumns.class);
	}


	/**
	 * Determine the value for auto-upload for a {@link DataStoreVO}.
	 * @param template
	 * @return
	 * 	Boolean.TRUE: auto-upload is activated
	 *  Boolean.FALSE: auto-upload is de-activated
	 *  null: auto-upload is not available
	 */
	public static Boolean getAutoUpload(de.regasus.common.File template) {
		Objects.requireNonNull(template);

		Boolean autoUpload = (Boolean) template.get(TemplateTable.AUTO_UPLOAD_KEY);
		return autoUpload;
	}


	/**
	 * Set the value for auto-upload for a {@link DataStoreVO}.
	 * @param template
	 * @param autoUpload
	 * 	Boolean.TRUE: auto-upload is activated
	 *  Boolean.FALSE: auto-upload is de-activated
	 *  null: auto-upload is not available
	 * @return
	 *  true, if the value has changed
	 */
	public static boolean setAutoUpload(de.regasus.common.File template, Boolean autoUpload) {
		Objects.requireNonNull(template);

		Boolean oldAutoUpload = getAutoUpload(template);
		template.put(TemplateTable.AUTO_UPLOAD_KEY, autoUpload);
		return !EqualsHelper.isEqual(autoUpload, oldAutoUpload);
	}


	/**
	 * Determine the value for in-edit for a {@link DataStoreVO}.
	 * @param template
	 * @return
	 * 	true: the file is currently in edit
	 *  false: the file is not in edit
	 */
	public static boolean isInEdit(de.regasus.common.File template) {
		Objects.requireNonNull(template);

		Boolean inEdit = (Boolean) template.get(TemplateTable.IN_EDIT_KEY);
		return inEdit == Boolean.TRUE;
	}


	/**
	 * Set the value for in-edit for a {@link DataStoreVO}.
	 * @param template
	 * @param inEdit
	 * 	true: the file is currently in edit
	 *  false: the file is not in edit
	 * @return
	 *  true, if the value has changed
	 */
	public static boolean setInEdit(de.regasus.common.File template, boolean inEdit) {
		Objects.requireNonNull(template);

		boolean oldInEdit = isInEdit(template);
		template.put(TemplateTable.IN_EDIT_KEY, inEdit);
		return inEdit != oldInEdit;
	}


	@Override
	public String getColumnText(de.regasus.common.File template, TemplateTableColumns column) {
		switch (column) {

			case LANGUAGE:
				return template.getLanguage();

			case FILE:
				try {
					String fileName = template.getExternalFileName();
					return fileName;
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}

			case DIR:
				try {
					String externalPath = template.getExternalPath();
					java.io.File file = new java.io.File(externalPath);
					if ( file.exists() ) {
						String parentDir = file.getParent();
						return parentDir;
					}
					return "";
				}
				catch (Exception e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}

			case EDIT_USER:
				return template.getEditDisplayUserStr();

			case EDIT_TIME:
				DateTimeFormatter formatter = DateTimeFormatterCache.getDateTimeFormatter(
					FormatStyle.SHORT,
					FormatStyle.SHORT,
					Locale.getDefault()
				);
				return formatter.format( template.getEditTime() );

			default:
				return "";
		}
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(de.regasus.common.File template, TemplateTableColumns column) {
		switch (column) {
			case AUTO_UPLOAD:
				return getAutoUpload(template);

			case EDIT_TIME:
    			return template.getEditTime();

    		default:
    			return super.getColumnComparableValue(template, column);
		}
	}


	@Override
	protected TemplateTableColumns getDefaultSortColumn() {
		return TemplateTableColumns.FILE;
	}


	@Override
	public Image getColumnImage(de.regasus.common.File template, TemplateTableColumns column) {
		switch (column) {
//			case IN_EDIT:
//				boolean inEdit = isInEdit(template);
//				if (inEdit) {
//					return IconRegistry.getImage(IImageKeys.EDIT);
//				}
//				return null;

    		case AUTO_UPLOAD:
    			Boolean autoUpload = getAutoUpload(template);

    			if (autoUpload == Boolean.TRUE) {
   					return IconRegistry.getImage(IImageKeys.CHECKED);
    			}
    			else if (autoUpload == Boolean.FALSE) {
   					return IconRegistry.getImage(IImageKeys.UNCHECKED);
    			}
   				return null;

    		case FILE:
    			boolean inEdit = isInEdit(template);
    			if (inEdit) {
    				return IconRegistry.getImage(IImageKeys.EDIT);
    			}
   				return null;

    		default:
    			return super.getColumnImage(template, column);
		}
	}


	@Override
	public boolean isColumnEditable(de.regasus.common.File template, TemplateTableColumns column) {
		switch (column) {
			case AUTO_UPLOAD:
				Boolean autoUpload = getAutoUpload(template);
				return autoUpload != null;

			default:
				return false;
		}
	}


	@Override
	public CellEditor getColumnCellEditor(Composite parent, TemplateTableColumns column) {
		switch (column) {
    		case AUTO_UPLOAD:
    			return new CheckboxCellEditor(parent);

    		default:
    			return null;
		}
	}


	@Override
	public Object getColumnEditValue(de.regasus.common.File template, TemplateTableColumns column) {
		switch (column) {
    		case AUTO_UPLOAD:
    			Boolean autoUpload = getAutoUpload(template);
    			return autoUpload != null && autoUpload.booleanValue();

			default:
				return null;
		}
	}


	@Override
	public boolean setColumnEditValue(de.regasus.common.File template, TemplateTableColumns column, Object value) {
		switch (column) {
    		case AUTO_UPLOAD:
				String externalPath = template.getExternalPath();
				java.io.File file = new java.io.File(externalPath);
				if ( file.exists() ) {
    				if (Boolean.FALSE.equals(value)) {
    					setAutoUpload(template, Boolean.FALSE);
        			}
        			else {
        				setAutoUpload(template, Boolean.TRUE);
        			}
    				return true;
				}
				else {
					return false;
				}

			default:
				return false;
		}
	}

}

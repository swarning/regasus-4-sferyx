package de.regasus.common.customfield;

import java.util.Locale;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.Tuple;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum KeyValueTableColumns {
	KEY, VALUE;
};

public class KeyValueTable extends SimpleTable<Tuple<String, LanguageString>, KeyValueTableColumns> {

	private String language;
	
	private ModifySupport modifySupport;

	
	public KeyValueTable(Table table) {
		super(table, KeyValueTableColumns.class, false, true);
		
		modifySupport = new ModifySupport(table);
		
		language = Locale.getDefault().getLanguage();
	}


	
	
	
	/**
	 * Cells can only show text or image, so the domain object's attributes need to be formatted or converted.
	 */
	@Override
	public String getColumnText(Tuple<String, LanguageString> tuple, KeyValueTableColumns column) {
		switch (column) {
		case KEY:
			return tuple.getA();
		case VALUE:
			return tuple.getB().getString(language);
		default:
			return null;
		}
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}
	
	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

	
	/**
	 * When a column returns a CellEditor, all cells in it are editable.
	 */
	@Override
	public CellEditor getColumnCellEditor(Composite parent, KeyValueTableColumns column) {
		switch (column) {
		case KEY:
			return null;
		case VALUE:
			return new TextCellEditor(parent);
		default:
			return null;
		}
	}


	/**
	 * When editing has finished, the CellEditors give different types of values, even different from the text and also
	 * from the comparableValue.
	 * <ul>
	 * <li>TextCellEditor: Strings</li>
	 * <li>CheckboxCellEditor: Booleans</li>
	 * <li>ComboBoxCellEditor: Integers</li>
	 * </ul>
	 * The Strings may have to be parsed into other types.
	 * 
	 * @return true when the cell needs to be refreshed.
	 */
	@Override
	public boolean setColumnEditValue(
		Tuple<String, LanguageString> tuple,
		KeyValueTableColumns column,
		Object value
	) {
		boolean change = false;
		String enteredString = (String) value;
		switch (column) {
		case KEY:
			String oldKey = tuple.getA();
			if (!enteredString.equals(oldKey)) {
				tuple.setA(enteredString);
				change = true;
			}
			break;
		case VALUE:
			LanguageString currentValue = tuple.getB();
			if (!enteredString.equals(currentValue.getString(language))) {
				currentValue.put(language, enteredString);
				change = true;
			}
			break;
		}

		if (change) {
			modifySupport.fire();
		}
		
		return change;
	}

	
	public void setLanguage(String lang) {
		this.language = lang;
		getViewer().refresh();
	}
	
}

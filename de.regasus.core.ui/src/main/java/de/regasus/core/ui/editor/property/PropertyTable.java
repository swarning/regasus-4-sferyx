package de.regasus.core.ui.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.PasswordHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.Property;

/**
 * The getter of the SimpleTable control their behaviour by this enum, which gives them the information which column is
 * about to be shown.
 *
 * @author manfred
 *
 */
enum EPropertyTableColumns {
	KEY, VALUE;
};

public class PropertyTable extends SimpleTable<Property, EPropertyTableColumns> {

	private ModifySupport modifySupport;

	public PropertyTable(Table table) {
		super(table, EPropertyTableColumns.class, true, true);

		modifySupport = new ModifySupport(table);
	}


	/**
	 * Cells can only show text or image, so the domain object's attributes need to be formatted or converted.
	 */
	@Override
	public String getColumnText(Property property, EPropertyTableColumns column) {
		String text = null;

		switch (column) {
		case KEY:
			text = property.getKey();
			break;
		case VALUE:
			String value = property.getValue();
			if (value != null && property.getKey().endsWith(".password")) {
				text = PasswordHelper.getReplacementForPassword(value);
			}
			else {
				text = value;
			}
			break;
		}

		if (text == null) {
			text = "";
		}

		return text;
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
	public CellEditor getColumnCellEditor(Composite parent, EPropertyTableColumns column) {
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
	public boolean setColumnEditValue(Property property, EPropertyTableColumns column, Object value) {
		String enteredString = (String) value;

		switch (column) {
		case KEY:
			return false;
		case VALUE:
			String propertyValue = StringHelper.avoidNull(property.getValue());
			if (!enteredString.equals(propertyValue)) {
				property.setValue(enteredString);

				modifySupport.fire();

				return true;
			}
		}
		return false;
	}

}

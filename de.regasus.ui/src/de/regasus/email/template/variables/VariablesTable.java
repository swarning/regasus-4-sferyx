package de.regasus.email.template.variables;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum VariablesTableColumns {
	VARIABLE, SAMPLE_VALUE
}

/**
 * A simple table that shows names and values of {@link VariableValuePair}s.
 * Variables can be copied to the clipboard by either the context menu or the keyboard shortcut.
 */
public class VariablesTable extends SimpleTable<VariableValuePair, VariablesTableColumns> {

	private ModifySupport modifySupport;


	public VariablesTable(Table table) {
		super(table, VariablesTableColumns.class, true, false);

		modifySupport = new ModifySupport(table);
	}


	@Override
	public String getColumnText(VariableValuePair pair, VariablesTableColumns column) {
		switch (column) {
		case VARIABLE:
			return pair.getVariable();
		case SAMPLE_VALUE:
			return StringHelper.avoidNull(pair.getSampleValue());
		}
		return null;
	}


	/**
	 * The table rows should appear in their original order first, but can be sorted later.
	 */
	@Override
	protected boolean shouldSortInitialTable() {
		return false;
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	public void removeModifyListener(ModifyListener listener) {
		modifySupport.removeListener(listener);
	}

}

package com.lambdalogic.util.rcp;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.custom.CCombo;

public class TableHelper {

	public static void prepareComboBoxCellEditor(ComboBoxCellEditor comboBoxCellEditor) {
		comboBoxCellEditor.setActivationStyle(0x0F); // Set the lower 4 bits for all activation styles
		
		CCombo ccombo = (CCombo) comboBoxCellEditor.getControl();
		ccombo.setVisibleItemCount(12);
	}
	
}

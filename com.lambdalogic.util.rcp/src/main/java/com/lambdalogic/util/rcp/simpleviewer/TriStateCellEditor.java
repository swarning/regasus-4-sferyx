package com.lambdalogic.util.rcp.simpleviewer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TriStateCellEditor extends CellEditor {

	/**
	 * The ternary value (TRUE, FALSE, null)
	 */
	private Boolean value = null;


	public TriStateCellEditor(Composite parent) {
		super(parent);
	}


	@Override
	public void activate() {
		value = getNextValue(value);
		fireApplyEditorValue();
	}


	@Override
	protected Control createControl(Composite parent) {
		return null;
	}


	@Override
	protected Object doGetValue() {
		return value;
	}


	@Override
	protected void doSetFocus() {
	}


	private Boolean getNextValue(Boolean value) {
		Boolean nextValue;
		if (Boolean.TRUE.equals(value)) {
			nextValue = Boolean.FALSE;
		}
		else if (Boolean.FALSE.equals(value)) {
			nextValue = null;
		}
		else {
			nextValue = Boolean.TRUE;
		}
		return nextValue;
	}


	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(value == null || value instanceof Boolean);
		if (value == null) {
			this.value = null;
		}
		else {
			this.value = (Boolean) value;
		}
	}

}

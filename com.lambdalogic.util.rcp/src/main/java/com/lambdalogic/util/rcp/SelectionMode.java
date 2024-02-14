package com.lambdalogic.util.rcp;

import org.eclipse.swt.SWT;

public enum SelectionMode {
	/**
	 * The user is not able to select a table row at all.
	 */
	NO_SELECTION,
	/**
	 * The user can and must select a single table row.
	 */
	SINGLE_SELECTION,
	/**
	 * The user can select one or multiple table rows, the selection of at least one is mandatory.
	 */
	MULTI_SELECTION,
	/**
	 * The user can select none, one or multiple table rows, but the selection is not mandatory.
	 * The user should also be able to de-select all rows.
	 */
	MULTI_OPTIONAL_SELECTION;
	
	
	public int getSwtStyle() {
		switch (this) {
		case NO_SELECTION:
			return SWT.HIDE_SELECTION;
		case SINGLE_SELECTION:
			return SWT.SINGLE | SWT.FULL_SELECTION;
		case MULTI_SELECTION:
		case MULTI_OPTIONAL_SELECTION:
			return SWT.MULTI | SWT.FULL_SELECTION;
		default:
			return SWT.HIDE_SELECTION;
		}
	}

}

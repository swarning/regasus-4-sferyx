package com.lambdalogic.util.rcp;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;

public class SelectionListenerAdapter implements ModifyListener {

	private SelectionListener selectionListener;
	
	public SelectionListenerAdapter(SelectionListener selectionListener) {
		this.selectionListener = selectionListener;
	}

	
	public void modifyText(ModifyEvent e) {
		if (selectionListener != null) {
			selectionListener.widgetSelected(null);
		}
	}

}

package com.lambdalogic.util.rcp;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;


/**
 * Class that can be used to implement a combination of a ModifyListener and a SelectionListener.
 */
public abstract class ModifySelectionAdapter implements ModifyListener, SelectionListener {

	/**
	 * Method that is called whenever a SelectionEvent or ModifyEvent occurs.
	 * @param event
	 */
	public abstract void handleEvent(TypedEvent event);
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		handleEvent(e);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		handleEvent(e);
	}

	@Override
	public void modifyText(ModifyEvent e) {
		handleEvent(e);
	}

}

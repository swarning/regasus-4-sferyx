package com.lambdalogic.util.rcp;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;

/**
 * Translates {@link SelectionEvent}s from SWT widgets and {@link SelectionChangedEvent}s from JFace 
 * viewers into {@link ModifyEvent}s and propagates them to one {@link ModifyListener}.
 */
public class ModifyListenerAdapter implements SelectionListener, ISelectionChangedListener {

	private ModifyListener modifyListener;
	
	public ModifyListenerAdapter(ModifyListener modifyListener) {
		this.modifyListener = modifyListener;
	}

	
	public static ModifyEvent createModifyEvent(SelectionEvent selectionEvent) {
		Event event = new Event();
		event.data = selectionEvent.data;
		event.display = selectionEvent.display;
		event.time = selectionEvent.time;
		event.widget = selectionEvent.widget;
		ModifyEvent modifyEvent = new ModifyEvent(event);
		return modifyEvent;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent selectionEvent) {
		if (modifyListener != null) {
			ModifyEvent modifyEvent = createModifyEvent(selectionEvent);
			modifyListener.modifyText(modifyEvent);
		}
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent selectionEvent) {
		if (modifyListener != null) {
			ModifyEvent modifyEvent = createModifyEvent(selectionEvent);
			modifyListener.modifyText(modifyEvent);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		if (modifyListener != null) {
			final Event event = new Event();
			
			Object source = e.getSource();
			
			if (source instanceof TableViewer) {
				event.widget = ((TableViewer) source).getTable();
			} 
			else if (source instanceof TreeViewer) {
				event.widget = ((TreeViewer) source).getTree();
			} 
			else if (source instanceof ListViewer) {
				event.widget = ((ListViewer) source).getList();
			}
			
			if (event.widget != null) {
				event.display = event.widget.getDisplay();
			}

			final ModifyEvent modifyEvent = new ModifyEvent(event);

			modifyListener.modifyText(modifyEvent);
		}
	}

}

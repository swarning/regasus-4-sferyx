package com.lambdalogic.util.rcp;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;

import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.error.ErrorHandler;


/**
 * Support for classes that are observing and can be observed.
 * These classes are in general {@link Composite} or {@link Group}. They are observing the
 * {@link Widgets}s that they contain as {@link ModifyListener} or {@link SelectionListener} and
 * they can be observed by other {@link ModifyListener}s.
 */
public class ModifySupport
implements Serializable, ModifyListener, SelectionListener, ISelectionChangedListener {

	private static final long serialVersionUID = 1L;

	private Collection<ModifyListener> beforeFireListeners = null;

	private Collection<ModifyListener> modifyListeners = new HashSet<ModifyListener>();

	private boolean enabled = true;

	private boolean suppress = false;
	private Set<ModifyEvent> supressedEvents;

	private Long ignoreUntil = null;


	/**
	 * Source of a fired {@link ModifyEvent}s.
	 */
	private Widget widget = null;


	public static ModifyEvent createModifyEvent(Widget widget) {
		if (widget == null) {
			throw new IllegalArgumentException("Parameter 'widget' must not be null.");
		}
		Event event = new Event();
		event.widget = widget;
		ModifyEvent modifyEvent = new ModifyEvent(event);
		return modifyEvent;
	}


	/**
	 * Determine if the the source of the given {@link TypedEvent} is a {@link Button} with the style
	 * {@link SWT#RADIO} that has been deselected.
	 *
	 * @param event
	 * @return {@code true} if the the source of the given {@link TypedEvent} is a {@link Button} with the style
	 *  {@link SWT#RADIO} that has been deselected, otherwise {@code false}
	 */
	public static final boolean isDeselectedRadioButton(TypedEvent event) {
		boolean isRadio = false;
		boolean isSelection = false;

		if (event != null) {
			if (event.getSource() instanceof Button) {
				Button button = (Button) event.getSource();
				isRadio = (button.getStyle() & SWT.RADIO) == SWT.RADIO;
				isSelection = button.getSelection();
			}
		}

		return isRadio && !isSelection;
	}


	public ModifySupport() {
	}


	/**
	 * Create a new {@link ModifySupport} with a widget that is source of all fired
	 * {@link ModifEvent}s.
	 *
	 * @param widget
	 */
	public ModifySupport(Widget widget) {
		this.widget = widget;
	}


	public Widget getWidget() {
		return widget;
	}


	public void setWidget(Widget widget) {
		this.widget = widget;
	}


	// *********************************************************************************************
	// * ModifyListener support
	// *

	/**
	 * Add the given {@link ModifyListener} as observer.
	 * {@link ModifyListener}s added by this method get informed before those that were added by
	 * {@link #addListener(ModifyListener)}.
	 *
	 * @param modifyListener
	 */
	public void addBeforeModifyListener(ModifyListener modifyListener) {
		if (beforeFireListeners == null) {
			beforeFireListeners = new HashSet<ModifyListener>();
		}

		beforeFireListeners.add(modifyListener);
	}


	/**
	 * Remove the given {@link ModifyListener} as observer.
	 *
	 * @param modifyListener
	 */
	public void removeBeforeModifyListener(ModifyListener modifyListener) {
		if (beforeFireListeners != null) {
			beforeFireListeners.remove(modifyListener);
		}
	}


	/**
	 * Add the given {@link ModifyListener} as observer.
	 * {@link ModifyListener}s added by this method get informed when this
	 * {@link ModifySupport} itself receives a {@link ModifyEvent} and delegates this
	 * {@link ModifyListener} to its own {@link ModifyListener}s.
	 * {@link ModifyListener}s added by this method get informed after those that were added by
	 * {@link #addBeforeModifyListener(ModifyListener)}.
	 *
	 * @param modifyListener
	 */
	public void addListener(ModifyListener modifyListener) {
		modifyListeners.add(modifyListener);
	}


	/**
	 * Remove the given {@link ModifyListener} as observer.
	 *
	 * @param modifyListener
	 */
	public void removeListener(ModifyListener modifyListener) {
		modifyListeners.remove(modifyListener);
	}


	/**
	 * Fire a {@link ModifyEvent} with {@link #widget} as source.
	 */
	public void fire(ModifyEvent event) {
		if (enabled) {
			if (suppress) {
				addSuppressedModifyEvent(event);
			}
			else if (ignoreUntil != null && System.currentTimeMillis() < ignoreUntil) {
				System.out.println("ModifySupport ignored");
			}
			else {
    			if (beforeFireListeners != null) {
    				for (ModifyListener modifyListener : beforeFireListeners) {
    					modifyListener.modifyText(event);
    				}
    			}

    			for (ModifyListener modifyListener : modifyListeners) {
    				modifyListener.modifyText(event);
    			}
			}
		}
	}


	/**
	 * Fire a {@link ModifyEvent} with {@link #widget} as source.
	 */
	public void fire(Widget widget) {
		if (enabled) {
			ModifyEvent event = createModifyEvent(widget);
			fire(event);
		}
	}


	/**
	 * Fire a {@link ModifyEvent} with {@link #widget} as source.
	 */
	public void fire() {
		fire(widget);
	}


	/**
	 * Add the given {@link ModifyEvent} to {@link #supressedEvents} if it does not contain a similar
	 * one.
	 * @param modifyEvent
	 */
	protected void addSuppressedModifyEvent(ModifyEvent modifyEvent) {
		boolean exist = false;
		for (ModifyEvent me : supressedEvents) {
			if (me.widget == modifyEvent.widget &&
				me.data == modifyEvent.data &&
				me.display == modifyEvent.display
			) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			supressedEvents.add(modifyEvent);
		}
	}

	// *
	// * ModifyListener support
	// *********************************************************************************************

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent event) {
		try {
			fire();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		try {
			fire();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent event) {
		try {
			/* Do not fire if the source is a radio button that has been deselected.
			 * In this case there will be another event for the button that has been selected.
			 * There is no "deselection" event without a corresponding "selection" event.
			 */
			if ( ! isDeselectedRadioButton(event)) {
				fire();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		try {
			fire();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Remove all listeners.
	 */
	public void clear() {
		modifyListeners.clear();
	}


	/**
	 * Show if this {@link ModifySupport} is enabled or disabled.
	 * Disabled {@link ModifySupport}s do not fire {@link ModifyEvent}s.
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}


	/**
	 * Enable/disable this {@link ModifySupport}.
	 * Disabled {@link ModifySupport}s do not fire {@link ModifyEvent}s.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	/**
	 * Start suppression of firing events and collect them instead.
	 * Similar {@link ModifyEvent}s are stored only once.
	 */
	public void suppress() {
		suppress = true;
		if (supressedEvents == null) {
			supressedEvents = CollectionsHelper.createHashSet();
		}
		else {
			supressedEvents.clear();
		}
	}


	/**
	 * Stop suppression of events and fire those events that have been collected while suppression
	 * was active.
	 */
	public void stopSuppressionAndFire() {
		suppress = false;
		if (supressedEvents != null && !supressedEvents.isEmpty()) {
			for (ModifyEvent event : supressedEvents) {
				fire(event);
			}
		}
	}


	public void ignoreMillis(long millis) {
		ignoreUntil = System.currentTimeMillis() + millis;
	}

}

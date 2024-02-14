package com.lambdalogic.util.rcp.i18n;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import com.lambdalogic.util.rcp.ModifySupport;


/**
 * Interface for classes that define and manage the widgets in an {@link I18NComposite}.
 */
public interface I18NWidgetController<Entity> {

	/**
	 * Create the widgets for the language in the widgetComposite and let the modifySupport observe them.
	 * @param widgetComposite
	 * @param modifySupport
	 * @param language
	 */
	void createWidgets(Composite widgetComposite, ModifySupport modifySupport, String language);

	void dispose();

	Entity getEntity();

	/**
	 * Set the entity and copy its data into the corresponding widgets.
	 * @param entity
	 */
	void setEntity(Entity entity);

	/**
	 * Copy the data from the widgets into the corresponding fields of the entity.
	 */
	void syncEntityToWidgets();

	/**
	 * Add the {@link FocusListener} to all widgets.
	 * @param listener
	 */
	void addFocusListener(FocusListener listener);

}

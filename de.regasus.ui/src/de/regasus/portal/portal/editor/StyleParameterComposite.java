package de.regasus.portal.portal.editor;

import java.util.Properties;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import de.regasus.portal.PageLayout;

/**
 * Interface for {@link Composite} classes that handle the parameters of a style sheet.
 * Implementing classes have to extend {@link Composite} (or one of its subclasses).
 *
 * Implementing classes read the {@link Properties} from {@link PageLayout#getStyleParameters()} and show the values
 * in their widgets. When {@link StyleParameterComposite#syncEntityToWidgets()} is called not only
 * {@link PageLayout#setStyleParameters(Properties)} has to be updated but also {@link PageLayout#setStyle(String)}.
 * The implementing class has to generate a CSS file according to the template and the parameters.
 */
public interface StyleParameterComposite {

	void setPageLayout(PageLayout pageLayout);

	void createWidgets() throws Exception;

	/**
	 * Update the values of {@link PageLayout#setStyleParameters(Properties)} and {@link PageLayout#setStyle(String)}.
	 */
	void syncEntityToWidgets();

	void addModifyListener(ModifyListener modifyListener);

	void removeModifyListener(ModifyListener modifyListener);

	void dispose();

}

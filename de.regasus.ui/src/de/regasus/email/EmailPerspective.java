package de.regasus.email;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.regasus.email.dispatchorder.view.EmailDispatchOrderView;
import de.regasus.email.dispatchorder.view.pref.EmailDispatchOrderViewPreference;
import de.regasus.email.template.search.view.EmailTemplateSearchView;
import de.regasus.email.template.search.view.pref.EmailTemplateSearchViewPreference;
import de.regasus.email.template.variables.view.EmailVariablesView;

public class EmailPerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.EmailPerspective";


	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		// Links oben die Variablen
		layout.addView(EmailVariablesView.ID,  IPageLayout.LEFT, 0.3f, editorArea);

		// Links unten die EmailVorlagen
		EmailTemplateSearchViewPreference.getInstance().initialize(); // delete previously saved preferences
		layout.addView(EmailTemplateSearchView.ID, IPageLayout.BOTTOM, 0.3f, EmailVariablesView.ID);

		// Unter dem Editor die Versandaufträge
		EmailDispatchOrderViewPreference.getInstance().initialize(); // delete previously saved preferences
		layout.addView(EmailDispatchOrderView.ID, IPageLayout.BOTTOM, 0.75f, editorArea);
	}

}

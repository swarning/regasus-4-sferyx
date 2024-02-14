package de.regasus.portal.pagelayout.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.LazyableComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.html.LazyHtmlEditor;
import com.lambdalogic.util.rcp.i18n.I18NWidgetController;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.PageLayout;

public class PageLayoutMenuI18NWidgetController implements I18NWidgetController<PageLayout>{
	// the entity
	private PageLayout pageLayout;

	// widget Maps
	private Map<String, LazyHtmlEditor> htmlEditorMap = MapHelper.createHashMap(10);


	public PageLayoutMenuI18NWidgetController() {
	}


	@Override
	public void createWidgets(Composite parent, ModifySupport modifySupport, String language) {
		parent.setLayout( new FillLayout() );


		LazyableComposite htmlComposite = new LazyableComposite(parent, SWT.NONE);
		htmlComposite.setLayout(new FillLayout());

		LazyHtmlEditor htmlEditor = new LazyHtmlEditor(htmlComposite, SWT.NONE);

		// add text widget to its Map
		htmlEditorMap.put(language, htmlEditor);


		htmlEditor.addModifyListener(modifySupport);
	}


	@Override
	public void dispose() {
	}


	@Override
	public PageLayout getEntity() {
		return pageLayout;
	}


	@Override
	public void setEntity(PageLayout pageLayout) {
		this.pageLayout = pageLayout;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (pageLayout != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						LanguageString menuText = pageLayout.getMenuText();
						if (menuText == null) {
							menuText = new LanguageString();
						}
						for (Map.Entry<String, LazyHtmlEditor> entry : htmlEditorMap.entrySet()) {
							entry.getValue().setHtml( avoidNull(menuText.getString(entry.getKey(), false)) );
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	@Override
	public void syncEntityToWidgets() {
		LanguageString menu = new LanguageString();
		for (Map.Entry<String, LazyHtmlEditor> entry : htmlEditorMap.entrySet()) {
			String lang = entry.getKey();
			LazyHtmlEditor widget = entry.getValue();
			menu.put(lang, widget.getHtml());
		}
		pageLayout.setMenuText(menu);
	}


	@Override
	public void addFocusListener(FocusListener listener) {
	}

}

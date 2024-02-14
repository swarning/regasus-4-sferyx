package com.lambdalogic.util.rcp.i18n;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.html.HtmlWidget;
import com.lambdalogic.util.rcp.widget.SWTHelper;

public class I18NHtmlWidgetController implements I18NWidgetController<LanguageString>{

	// the entity
	private LanguageString entity;

	// widget Maps
	private Map<String, HtmlWidget> htmlWidgetMap = new HashMap<>();


	@Override
	public void createWidgets(Composite widgetComposite, ModifySupport modifySupport, String language) {
		widgetComposite.setLayout( new FillLayout() );

		HtmlWidget htmlWidget = new HtmlWidget(widgetComposite, SWT.NONE);

		// add widget to its Map
		htmlWidgetMap.put(language, htmlWidget);

		htmlWidget.addModifyListener(modifySupport);
	}


	@Override
	public void dispose() {
	}


	public LanguageString getLanguageString() {
		return entity;
	}


	public void setLanguageString(LanguageString languageString) {
		setEntity(languageString);
	}


	@Override
	public LanguageString getEntity() {
		return entity;
	}


	@Override
	public void setEntity(LanguageString languageString) {
		this.entity = languageString;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (entity == null) {
						entity = new LanguageString();
					}

					for (Map.Entry<String, HtmlWidget> entry : htmlWidgetMap.entrySet()) {
						String lang = entry.getKey();
						HtmlWidget widget = entry.getValue();
						widget.setHtml( avoidNull(entity.getString(lang, false)) );
					}
				}
				catch (Exception e) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	@Override
	public void syncEntityToWidgets() {
		// build LanguageString from values in widget
		entity.clear();
		for (Map.Entry<String, HtmlWidget> entry : htmlWidgetMap.entrySet()) {
			String lang = entry.getKey();
			HtmlWidget widget = entry.getValue();
			entity.put(lang, widget.getHtml());
		}
	}


	@Override
	public void addFocusListener(FocusListener listener) {
		// not necessary
	}

}

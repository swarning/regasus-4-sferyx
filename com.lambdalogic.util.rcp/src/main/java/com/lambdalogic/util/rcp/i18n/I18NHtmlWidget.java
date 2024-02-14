package com.lambdalogic.util.rcp.i18n;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.rcp.html.HtmlWidget;

import de.regasus.common.Language;

/**
 * A widget to manage a {@link LanguageString} that contains HTML.
 *
 * The widget is a {@link I18NComposite} with an {@link HtmlWidget} on each tab.
 * Instead of an entity the {@link LanguageString} is set directly.
 */
public class I18NHtmlWidget extends I18NComposite<LanguageString> {

	public I18NHtmlWidget(
		Composite parent,
		int style,
		List<Language> languageList
	) {
		super(parent, style, languageList, new I18NHtmlWidgetController());
	}


	public LanguageString getLanguageString() {
		syncEntityToWidgets();
		return getEntity();
	}


	public void setLanguageString(LanguageString languageString) {
		setEntity(languageString);
	}

}

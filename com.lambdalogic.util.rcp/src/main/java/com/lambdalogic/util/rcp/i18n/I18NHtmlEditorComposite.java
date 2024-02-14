package com.lambdalogic.util.rcp.i18n;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.rcp.html.LazyHtmlEditor;

import de.regasus.common.Language;

/**
 * A widget to manage a {@link LanguageString} that contains HTML.
 *
 * The widget is a {@link I18NComposite} with an {@link LazyHtmlEditor} on each tab.
 * Instead of an entity the {@link LanguageString} is set directly.
 */
public class I18NHtmlEditorComposite extends I18NComposite<LanguageString> {

	public I18NHtmlEditorComposite(
		Composite parent,
		int style,
		List<Language> languageList
	) {
		super(parent, style, languageList, new I18NHtmlEditorWidgetController());
	}


	public LanguageString getLanguageString() {
		syncEntityToWidgets();
		return getEntity();
	}


	public void setLanguageString(LanguageString languageString) {
		setEntity(languageString);
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}

}

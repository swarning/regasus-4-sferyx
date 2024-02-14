package com.lambdalogic.util.rcp.i18n;

import java.util.List;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.error.ErrorHandler;

import de.regasus.common.Language;

public class I18NHtmlEditor extends Composite {

	private ModifySupport modifySupport = new ModifySupport(this);

	private List<Language> languageList;
	private LanguageString languageString;
	private boolean enabled = true;
	private I18NHtmlEditorComposite i18nHtmlEditorComposite;


	public I18NHtmlEditor(Composite parent, int style, List<Language> languageList) {
		super(parent, style);

		setLayout( new FillLayout() );

		this.languageList = languageList;

		/* Do not build widgets yet if the parent is a TabFolder, because the TabFolder will call setVisible(boolean)
		 * every time a tab bets selected or de-selected.
		 */
		if (! (parent instanceof TabFolder)) {
			setVisible(true);
		}
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			build();
		}
		else {
			destroy();
		}

		super.setVisible(visible);
	}


	private void build() {
		if (! isInitialized() ) {
			try {
				i18nHtmlEditorComposite = new I18NHtmlEditorComposite(this, getStyle(), languageList);

				i18nHtmlEditorComposite.setLanguageString(languageString);
				i18nHtmlEditorComposite.setEnabled(enabled);

				i18nHtmlEditorComposite.addModifyListener(modifySupport);

				layout();
			}
			catch (Exception e) {
				ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	private void destroy() {
		if ( isInitialized() ) {
			try {
				i18nHtmlEditorComposite.removeModifyListener(modifySupport);

				languageString = getLanguageString();
				enabled = i18nHtmlEditorComposite.getEnabled();

				i18nHtmlEditorComposite.dispose();
				i18nHtmlEditorComposite = null;
			}
			catch (Exception e) {
				ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	public boolean isInitialized() {
		return i18nHtmlEditorComposite != null;
	}



	@Override
	public void setEnabled(boolean enabled) {
		if (i18nHtmlEditorComposite != null) {
			i18nHtmlEditorComposite.setEnabled(enabled);
		}
		else {
			this.enabled = enabled;
		}
	}


	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public LanguageString getLanguageString() {
		if (i18nHtmlEditorComposite != null) {
			languageString = i18nHtmlEditorComposite.getLanguageString();
		}
		return languageString;
	}


	public void setLanguageString(LanguageString languageString) {
		if (i18nHtmlEditorComposite != null) {
			i18nHtmlEditorComposite.setLanguageString(languageString);
		}
		else {
			this.languageString = languageString;
		}
	}

}

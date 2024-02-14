package com.lambdalogic.util.rcp.i18n;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.lambdalogic.i18n.Language;


/**
 * A filter for StructuredViewers that show LanguageItems; when this filter is active 
 * it filters all but the common languages of our customers defined in {@link #commonLanguages}.
 *  
 * @author manfred
 *
 */
public class CommonLanguagesFilter extends ViewerFilter {

	/**
	 * Languages are filtered only when active is true
	 */
	private boolean active = true;
	
	public boolean isActive() {
		return active;
	}

	/**
	 * The common languages that are shown even if filter is active. 
	 */
	private static final String commonLanguages = "de,en,fr,it,pt,es";
	
	
	/**
	 * Sets whether the filter is active or not
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * When active, shows at most those languages contained in {@link #commonLanguages} 
	 */
	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		CheckboxTableViewer checkBoxTableViewer = (CheckboxTableViewer) viewer;
		
		
		if (active && element instanceof Language) {
			Language language = (Language) element;
			boolean isCommonLanguage = commonLanguages.contains(language.getLanguageCode());
			boolean isChecked = checkBoxTableViewer.getChecked(element);
			return isChecked || isCommonLanguage;
		}
		return true;
	}

}

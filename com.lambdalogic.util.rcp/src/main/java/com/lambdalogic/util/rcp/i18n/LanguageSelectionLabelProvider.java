package com.lambdalogic.util.rcp.i18n;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.Language;


/**
 * A label provider to be used for a Table which shows LanguageItems is used in CheckboxTableViewer. There, the first
 * column will show a checkbox, the second column shall show language code, and the last one the language name.
 * 
 * @author manfred
 * 
 */
public class LanguageSelectionLabelProvider extends BaseLabelProvider implements ITableLabelProvider {

	/**
	 * Show no images
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}


	/**
	 * The second column (index=1) shall show language code, and the third one (index=2) the language name.
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof Language) {
			Language language = (Language) element;
			switch (columnIndex) {
			case 1:
				return language.getLanguageCode();
			case 2:
				return language.getLanguageName().getString();
			}
		}
		return null;
	}

}

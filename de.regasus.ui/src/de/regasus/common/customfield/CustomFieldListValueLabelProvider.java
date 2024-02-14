package de.regasus.common.customfield;

import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.contact.CustomFieldListValue;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;

public class CustomFieldListValueLabelProvider extends LabelProvider {

	private List<String> languageCodesToShow;

	public void setLanguageCodesToShow(List<String> languageCodesToShow) {
		this.languageCodesToShow = languageCodesToShow;
	}

	
	@Override
	public String getText(Object element) {
		if (element instanceof CustomFieldListValue) {
			CustomFieldListValue value = (CustomFieldListValue) element;
			LanguageString languageString = value.getLabel();
			
			if (CollectionsHelper.empty(languageCodesToShow)) {
				return languageString.getString();
			}
			
			StringBuilder sb = new StringBuilder();
			
			// Concatenate all languages
			for (String language : languageCodesToShow) {
				
				String string = languageString.getString(language, false);
				if (StringHelper.isNotEmpty(string)) {
					sb.append(string);
				}
				else {
					sb.append("?");
				}
				sb.append(" (");
				sb.append(language);
				sb.append("), ");
			}
			
			// Remove trailing comma and whitespace
			int sl = sb.length();
			if (sl > 1) {
				sb.delete(sl - 2, sl - 1);
			}
			return sb.toString();
		}
		return super.getText(element);
	}


}

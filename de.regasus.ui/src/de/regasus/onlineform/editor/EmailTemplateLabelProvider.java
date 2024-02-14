package de.regasus.onlineform.editor;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.util.StringHelper;

public class EmailTemplateLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof EmailTemplate) {
			EmailTemplate emailTemplate = (EmailTemplate) element;
			
			StringBuilder sb = new StringBuilder();
			
			String name = emailTemplate.getName();
			sb.append(name);
			
			String language = emailTemplate.getLanguage();
			if (StringHelper.isNotEmpty(language)) {
				sb.append(" (").append(language).append(")");
			}
			
			return sb.toString();
		}
		
		return super.getText(element);
	}
	
}

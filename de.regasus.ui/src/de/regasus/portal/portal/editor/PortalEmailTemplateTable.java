package de.regasus.portal.portal.editor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.email.template.EmailTemplateSystemRoleHelper;


enum PortalEmailTemplateTableColumns {SYSTEM_ROLE_NAME, EMAIL_TEMPLATE_NAME, LANGUAGE};


class PortalEmailTemplateTable extends SimpleTable<EmailTemplate, PortalEmailTemplateTableColumns> {

	public PortalEmailTemplateTable(Table table) {
		super(table, PortalEmailTemplateTableColumns.class);
	}


	@Override
	public String getColumnText(EmailTemplate emailTemplate, PortalEmailTemplateTableColumns column) {
		String label = null;
		switch (column) {
    		case SYSTEM_ROLE_NAME:
    			label = emailTemplate.getSystemRole().getString();
    			break;
    		case EMAIL_TEMPLATE_NAME:
    			label = emailTemplate.getName();
    			break;
    		case LANGUAGE:
    			label = getLanguageName(emailTemplate.getLanguage());
    			break;
    		default:
    			label = "";
		}

		return label;
	}


	private String getLanguageName(String languageId) {
		String languageName = "";

		try {
			Language language = LanguageModel.getInstance().getLanguage(languageId);
			if (language != null) {
				languageName = language.getName().getString();
			}
		}
		catch (Exception e) {
			languageName = e.getMessage();
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return languageName;
	}


	@Override
	public Image getColumnImage(EmailTemplate emailTemplate, PortalEmailTemplateTableColumns column) {
		Image image = null;
		EmailTemplateSystemRole systemRole = emailTemplate.getSystemRole();
		if (systemRole != null && column == PortalEmailTemplateTableColumns.SYSTEM_ROLE_NAME) {
			image = EmailTemplateSystemRoleHelper.getImage(systemRole);
		}
		return image;
	}


	@Override
	protected Comparable<?> getColumnComparableValue(EmailTemplate emailTemplate, PortalEmailTemplateTableColumns column) {
		/* For values that are not of type String, return the original values (e.g., Date, Integer).
		 * Values of type String can be returned via super.getColumnComparableValue(emailTemplate, column).
		 */
		switch (column) {
    		case SYSTEM_ROLE_NAME:
    			return emailTemplate.getSystemRole().ordinal()
    				+ emailTemplate.getLanguage()
    				+ emailTemplate.getName();
    		case LANGUAGE:
    			return emailTemplate.getLanguage()
    				+ emailTemplate.getSystemRole().ordinal()
    				+ emailTemplate.getName();
    		default:
    			return super.getColumnComparableValue(emailTemplate, column);
		}
	}


	@Override
	protected PortalEmailTemplateTableColumns getDefaultSortColumn() {
		PortalEmailTemplateTableColumns defaultSortColumn = null;
		if (getCurrentSortColumn() != null) {
			defaultSortColumn = getCurrentSortColumn();
		}
		else {
			defaultSortColumn = PortalEmailTemplateTableColumns.SYSTEM_ROLE_NAME;
		}

		return defaultSortColumn;
	}

}

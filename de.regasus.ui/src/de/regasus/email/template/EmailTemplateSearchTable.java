package de.regasus.email.template;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;

enum EmailTemplateTableColumns {
	NAME, LANGUGAGE, WEBID
};

/**
 * A table to show the name, language and Web-ID (Registration Form Config) of {@@ink EmailTemplate}s.
 */
public class EmailTemplateSearchTable extends SimpleTable<EmailTemplate, EmailTemplateTableColumns> {

	private LanguageModel languageModel;
	private RegistrationFormConfigModel registrationFormConfigModel;


	public EmailTemplateSearchTable(Table table) {
		super(table, EmailTemplateTableColumns.class);

		languageModel = LanguageModel.getInstance();
		registrationFormConfigModel = RegistrationFormConfigModel.getInstance();
	}


	@Override
	public Image getColumnImage(EmailTemplate emailTemplate, EmailTemplateTableColumns column) {
		Image image = null;
		EmailTemplateSystemRole systemRole = emailTemplate.getSystemRole();
		if (systemRole != null && column == EmailTemplateTableColumns.NAME) {
			image = EmailTemplateSystemRoleHelper.getImage(systemRole);
		}
		return image;
	}


	@Override
	public String getColumnText(EmailTemplate emailTemplate, EmailTemplateTableColumns column) {
		switch (column) {

			case NAME:
				return emailTemplate.getName();

			case LANGUGAGE:
				String languageId = emailTemplate.getLanguage();
				if (languageId == null) {
					return "";
				}
				else {
					try {
						Language language = languageModel.getLanguage(languageId);
						// NPE happened here during logout
						if (language != null) {
							return language.getName().getString();
						}
					}
					catch (Throwable t) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
					}
					return languageId;
				}

			case WEBID:
				Long registrationFormConfigPK = emailTemplate.getRegistrationFormConfigPK();
				if (registrationFormConfigPK != null) {
					try {
						RegistrationFormConfig registrationFormConfig = registrationFormConfigModel.getRegistrationFormConfig(registrationFormConfigPK);
						return registrationFormConfig.getWebId();
					}
					catch (Exception e) {
						// TODO The form might have been deleted, the email might still have the old reference.
						// This should be improved by a) not being able to delete the config, or b) deleting
						// the reference when deleted
						return UtilI18N.Deleted;
					}
				}
				else if (emailTemplate.getSystemRole() != null && emailTemplate.getSystemRole().isEventSpecific()) {
					return UtilI18N.All;
				}
				else {
					return "";
				}

		}

		return null;
	}


	/**
	 * This is overridden from {@link SimpleTable} to preserve the (sorted) order in the list when we get it from the
	 * model; since the initial sort that takes place otherwise would compare the results from toString()ing the
	 * elements and thus shuffle the list in an undesired way.
	 *
	 * @return
	 */
	@Override
	protected boolean shouldSortInitialTable() {
		return false;
	}


	@Override
	protected EmailTemplateTableColumns getDefaultSortColumn() {
		return EmailTemplateTableColumns.NAME;
	}

}

package de.regasus.email.dispatch.dialog;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;

/**
 * This wizard is used in the EmailTemplateSearchView to set the standard SMTP settings
 * for the currently selected Event. 
 * 
 * @author manfred
 *
 */
public class SmtpSettingsWizard extends Wizard {

	private SmtpSettingsVO settings;
	private SmtpSettingsPage smtpSettingsPage;
	private EventVO eventVO;

	public SmtpSettingsWizard(SmtpSettingsVO settings, EventVO eventVO) {
		this.settings = settings;
		this.eventVO = eventVO;
	}

	@Override
	public void addPages() {
		smtpSettingsPage = new SmtpSettingsPage(settings, eventVO);
		
		addPage(smtpSettingsPage);
		
	}
	
	@Override
	public boolean performFinish() {
		smtpSettingsPage.syncEntityToWidgets();
		return true;
	}

}

package de.regasus.email.dispatch.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.email.EmailI18N;
import de.regasus.email.dispatch.DispatchCommandMode;
import de.regasus.email.dispatch.DispatchMode;
import de.regasus.email.dispatch.pref.EmailDispatchPreference;
import de.regasus.participant.dialog.ParticipantSelectionWizardPage;
import de.regasus.profile.dialog.ProfileSelectionWizardPage;

/**
 * This wizard is opened when the user starts the command to dispatch mail, either for
 * participants, for profiles, or for one email template.
 * The given {@link DispatchCommandMode} determines what pages have to be shown.
 */
public class EmailDispatchWizard extends Wizard {

	// *************************************************************************
	// * Pages
	// *

	private ProfileSelectionWizardPage profileSelectionWizardPage;

	private ParticipantSelectionWizardPage participantSelectionWizardPage;

	private EmailTemplateSelectionPage templateSelectionPage;

	private SmtpSettingsPage smtpSettingsPage;

	private DispatchModePage dispatchModePage;

	// *************************************************************************
	// * Other attributes
	// *

	private SmtpSettingsVO smtpSettings;

	private EventVO event;

	private EmailTemplate emailTemplateSearchData;

	private DispatchMode dispatchMode;

	private Date scheduledDate;

	private List<Long> abstractPersonIds = new ArrayList<>();

	private DispatchCommandMode mode;


	public EmailDispatchWizard(EventVO event, DispatchCommandMode mode, SmtpSettingsVO smtpSettings) throws Exception {
		this.event = event;
		this.mode = Objects.requireNonNull(mode);
		this.smtpSettings = Objects.requireNonNull(smtpSettings);
	}


	@Override
	public void addPages() {

		if (mode.isShowTemplateSearchPage()) {
			templateSelectionPage = new EmailTemplateSelectionPage(event != null ? event.getPK() : null);
			addPage(templateSelectionPage);
		}

		if (mode.isShowParticipantSearchPage()) {
			Long eventPK = event.getID();
			participantSelectionWizardPage = new ParticipantSelectionWizardPage(SelectionMode.MULTI_SELECTION, eventPK);
			participantSelectionWizardPage.setDescription(EmailLabel.SelectRecipients.getString());
			addPage(participantSelectionWizardPage);
		}

		if (mode.isShowProfileSearchPage()) {
			profileSelectionWizardPage = new ProfileSelectionWizardPage(SelectionMode.MULTI_SELECTION);
			profileSelectionWizardPage.setDescription(EmailLabel.SelectRecipients.getString());
			addPage(profileSelectionWizardPage);
		}

		smtpSettingsPage = new SmtpSettingsPage(smtpSettings, null);
		smtpSettingsPage.setDescription(EmailI18N.SmtpSettingsPage_ThisDispatch_Description);
		addPage(smtpSettingsPage);

		dispatchModePage = new DispatchModePage(smtpSettings);
		addPage(dispatchModePage);
	}


	/**
	 * All selected and entered data are retrieved from the pages, so this wizard can be asked for them after it has
	 * been closed (and the widgets have been disposed). The settings are not explicitly retrieved, since the original
	 * settings object was handed to the page and manipulated directly.
	 */
	@Override
	public boolean performFinish() {
		smtpSettingsPage.syncEntityToWidgets();
		if (templateSelectionPage != null) {
			emailTemplateSearchData = templateSelectionPage.getSelectedEmailTemplateSearchData();
		}
		if (participantSelectionWizardPage != null) {
			List<ParticipantSearchData> selectedParticipants = participantSelectionWizardPage.getSelectedParticipants();

			for (ParticipantSearchData psd : selectedParticipants) {
				abstractPersonIds.add(psd.getPK());
			}
		}
		if (profileSelectionWizardPage != null) {
			abstractPersonIds = profileSelectionWizardPage.getSelectedPKs();
		}
		dispatchMode = dispatchModePage.getDispatchMode();
		scheduledDate = dispatchModePage.getScheduledDate();

		// persist DispatchMode to preferences
		EmailDispatchPreference.getInstance().setDispatchMode(dispatchMode);
		EmailDispatchPreference.getInstance().save();

		return true;
	}

	// *************************************************************************
	// * Getter
	// *


	public List<Long> getAbstractPersonIds() {
		return abstractPersonIds;
	}


	public DispatchMode getDispatchMode() {
		return dispatchMode;
	}


	public EmailTemplate getEmailTemplateSearchData() {
		return emailTemplateSearchData;
	}


	public Date getScheduledDate() {
		return scheduledDate;
	}

}

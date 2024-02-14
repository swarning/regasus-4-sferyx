/**
 *
 */
package de.regasus.participant.dialog;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.I18N;
import de.regasus.common.AddressRole;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;


public class CreateAnonymousParticipantWizard extends Wizard implements IPageChangedListener {

	// wizard pages
	private AnonymousParticipantPage anonymousParticipantPage;
	private AddressPage addressPage;

	private Address groupManagerAddress;
	private Long groupManagerPK;

	/**
	 * Member is static so the WizardPages have access to it.
	 */
	private static Long eventPK;

	private int count;
	private Participant templateParticipant;



	public CreateAnonymousParticipantWizard(Participant groupManager) {
		this.groupManagerAddress = groupManager.getMainAddress();
		this.groupManagerPK = groupManager.getPK();
		eventPK = groupManager.getEventId();
	}


	@Override
	public void addPages() {
		try {
			setWindowTitle(I18N.CreateAnonymousParticipantWizard_Title);

			templateParticipant = ParticipantModel.getInitialParticipant();
			templateParticipant.setEventId(eventPK);

			anonymousParticipantPage = new AnonymousParticipantPage(templateParticipant);

			addPage(anonymousParticipantPage);

			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			String homeCountryPK = eventVO.getOrganisationOfficeCountryPK();

			addressPage = new AddressPage(
				"MAIN",										// addressType
				templateParticipant, 						// abstractPerson
				templateParticipant.getMainAddressNumber(),
				groupManagerAddress,
				homeCountryPK
			);
			addressPage.setTitle(AddressRole.MAIN.getString());
			addPage(addressPage);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public boolean performFinish() {
		try {
			count = anonymousParticipantPage.getCount();

			String lastName = anonymousParticipantPage.getLastName();
			Long participantTypePK = anonymousParticipantPage.getParticipantTypePK();
			String language = anonymousParticipantPage.getLanguage();

			addressPage.syncEntityToWidgets();

			templateParticipant.setEventId(eventPK);
			templateParticipant.setGroupManagerPK(groupManagerPK);
			templateParticipant.setLastName(lastName);
			templateParticipant.setParticipantTypePK(participantTypePK);
			templateParticipant.setLanguageCode(language);
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}

		return true;
	}


	@Override
	public boolean canFinish() {
		return
			addressPage.isPageComplete() &&
			anonymousParticipantPage.isPageComplete();
	}


	@Override
	public void pageChanged(PageChangedEvent event) {
		if (event.getSelectedPage() == addressPage) {
			addressPage.refreshDefaultAddressLabel();
		}
	}

	/**
	 * Member is static so the WizardPages have access to it.
	 */
	public static Long getEventPK() {
		return eventPK;
	}


	public int getCount() {
		return count;
	}


	public Participant getTemplateParticipant() {
		return templateParticipant;
	}

}

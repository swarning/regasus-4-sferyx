package de.regasus.participant.collectivechange.dialog;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.contact.CustomFieldUpdateParameter;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class CollectiveChangeParticipantCustomFieldsWizard extends Wizard {

	private List<CustomFieldUpdateParameter> parameters;
	private List<Long> participantPKs;
	private CollectiveChangeParticipantCustomFieldsPage collectiveChangeParticipantCustomFieldsPage;
	private Long eventID;


	// **************************************************************************
	// * Constructors
	// *

	public CollectiveChangeParticipantCustomFieldsWizard(Long eventPK, List<Long> participantPKs) {
		this.participantPKs = participantPKs;
		this.eventID = eventPK;
	}

	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {
		collectiveChangeParticipantCustomFieldsPage = new CollectiveChangeParticipantCustomFieldsPage(
			eventID,
			participantPKs.size()
		);
		addPage(collectiveChangeParticipantCustomFieldsPage);
	}


	@Override
	public String getWindowTitle() {
		return I18N.CollectiveChange;
	}


	@Override
	public boolean performFinish() {
		try {
			parameters = collectiveChangeParticipantCustomFieldsPage.getParameters();
			return true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return false;
		}
	}

	
	public List<CustomFieldUpdateParameter> getParameters() {
		return parameters;
	}

}

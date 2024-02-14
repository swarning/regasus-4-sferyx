package de.regasus.report.wizard.participant.list;

import de.regasus.report.wizard.common.AbstractSelectWizardPage;


public class ParticipantSelectWizardPage extends AbstractSelectWizardPage {

	public static final String ID = "de.regasus.report.wizard.participant.list.ParticipantSelectWizardPage"; 


	/**
	 * Create the wizard
	 */
	public ParticipantSelectWizardPage(boolean withGroups) {
		super(
			ID,	// pageName
			withGroups
		);
	}


	/**
	 * Create the wizard
	 */
	public ParticipantSelectWizardPage(boolean withGroups, boolean withSelection) {
		super(
			ID,	// pageName
			withGroups,
			withSelection
		);
	}

}

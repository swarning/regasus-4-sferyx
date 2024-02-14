package de.regasus.report.wizard.profile.list;

import de.regasus.report.wizard.common.AbstractSelectWizardPage;


public class ProfileSelectWizardPage extends AbstractSelectWizardPage {

	public static final String ID = "de.regasus.report.wizard.profile.list.ProfileSelectWizardPage"; 


	/**
	 * Create the wizard
	 */
	public ProfileSelectWizardPage(boolean withSelection) {
		super(
			ID,		// pageName
			false,	// withGroups
			withSelection
		);
	}

}

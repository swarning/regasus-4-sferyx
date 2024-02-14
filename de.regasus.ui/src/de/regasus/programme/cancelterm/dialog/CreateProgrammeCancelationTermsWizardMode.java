package de.regasus.programme.cancelterm.dialog;

import de.regasus.I18N;

public enum CreateProgrammeCancelationTermsWizardMode {
	
	EVENT(I18N.CreateProgrammeCancelationTermsForEvent), 
	
	PROGRAMME_POINT(I18N.CreateProgrammeCancelationTermsForProgrammePoint); 
	
	
	private String title;

	private CreateProgrammeCancelationTermsWizardMode(String title) {
		this.title = title;
	}

	
	public String getTitle() {
		return title;
	}

	
}

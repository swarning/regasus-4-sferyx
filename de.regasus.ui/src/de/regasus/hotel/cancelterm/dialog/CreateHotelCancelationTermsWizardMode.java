package de.regasus.hotel.cancelterm.dialog;

import de.regasus.I18N;

public enum CreateHotelCancelationTermsWizardMode {
	
	EVENT(I18N.CreateHotelCancelationTermsForEvent), 
	
	HOTEL(I18N.CreateHotelCancelationTermsForHotel), 
	
	CONTINGENT(I18N.CreateHotelCancelationTermsForContingent);
	
	private String title;

	private CreateHotelCancelationTermsWizardMode(String title) {
		this.title = title;
	}

	
	public String getTitle() {
		return title;
	}

	
}

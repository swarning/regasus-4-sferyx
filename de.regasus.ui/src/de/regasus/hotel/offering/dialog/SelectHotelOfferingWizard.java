package de.regasus.hotel.offering.dialog;

import java.util.Objects;

import org.eclipse.jface.wizard.Wizard;

// REFERENCE
class SelectHotelOfferingWizard extends Wizard {

	// wizard status
	private SelectHotelOfferingWizardStatus status;

	// wizard pages
	private HotelSelectionWizardPage hotelSelectionPage;
	private OfferingSelectionWizardPage offeringSelectionPage;


	public SelectHotelOfferingWizard(Long eventId) {
		Objects.requireNonNull(eventId);

		status = new SelectHotelOfferingWizardStatus(eventId);
	}


	@Override
	public void addPages() {
		hotelSelectionPage = new HotelSelectionWizardPage(status);
		addPage(hotelSelectionPage);

		offeringSelectionPage = new OfferingSelectionWizardPage(status);
		addPage(offeringSelectionPage);
	}


	@Override
	public boolean canFinish() {
		// enable finish button if (1.) that last page is the currently visible and (2.) the user has selected an offering
		return
			   status.getCurrentPage() == offeringSelectionPage
			&& getSelectedOfferingId() != null;
	}


	@Override
	public boolean performFinish() {
		return true;
	}


	/**
	 * Get the ID of the Hotel Offering the user selected.
	 * @return
	 */
	public Long getSelectedOfferingId() {
		return status.getOfferingId();
	}


	/**
	 * Get the ID of the Hotel the user selected.
	 * @return
	 */
	public Long getSelectedHotelId() {
		return status.getHotelId();
	}


	/**
	 * Define the Hotel Offering that shall be preselected.
	 * @param offeringId
	 */
	public void setInitialOfferingId(Long offeringId) {
		status.setInitialOfferingId(offeringId);
	}


	/**
	 * Define the Hotel that shall be preselected.
	 * @param hotelId
	 */
	public void setInitialHotelId(Long hotelId) {
		status.setInitialHotelId(hotelId);
	}


	/**
	 * Set a more specific description text on the page where the user selects the Hotel.
	 * @param text
	 */
	public void setHotelSelectionDescription(String text) {
		hotelSelectionPage.setDescription(text);
	}


	/**
	 * Set a more specific description text on the page where the user selects the Hotel Offering.
	 * @param text
	 */
	public void setOfferingSelectionDescription(String text) {
		offeringSelectionPage.setDescription(text);
	}

}

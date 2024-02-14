package de.regasus.hotel.offering.dialog;

import static com.lambdalogic.util.EqualsHelper.isEqual;

import java.util.Objects;

import org.eclipse.jface.wizard.WizardPage;

// REFERENCE
/**
 * Class that holds the status of the {@link SelectHotelOfferingWizard}.
 * An instance of this status is created by the Wizard and shared with all WizardPages.
 *
 * The implementation is specific to the business logic of the {@link SelectHotelOfferingWizard}.
 * It not only reflects the current status, but also contains business logic.
 * E.g. the method {@link #setHotelId(Long)} deleted the selected hotel offering when the selected hotel changes.
 *
 * The implementation implements the WizardStatus-interfaces of all WizardPages.
 */
class SelectHotelOfferingWizardStatus
	implements HotelSelectionWizardPage.WizardStatus, OfferingSelectionWizardPage.WizardStatus {

	/**
	 * The page that is currently visible.
	 * It's in the responsibility of every WizardPage to call {@link #setCurrentPage(WizardPage)} when they
	 * become visible. For this purpose they have to override <code>setVisible(boolean visible)</code> like
	 * in the following example:
	 *
	 * @Override
	 * public void setVisible(boolean visible) {
	 * 		if (visible) {
	 * 			status.setCurrentPage(this);
	 * 		}
	 * 		super.setVisible(visible);
	 * }
	 */
	private WizardPage currentPage;

	/**
	 * Mandatory parameter to restrict the displayed data to one event.
	 */
	private Long eventId;

	/**
	 * Optional parameter that determines the hotel that shall be preselected.
	 */
	private Long initialHotelId;

	/**
	 * ID of the selected hotel.
	 */
	private Long hotelId;

	/**
	 * Optional parameter that determines the hotel offering that shall be preselected.
	 */
	private Long initialOfferingId;

	/**
	 * ID of the selected hotel offering.
	 */
	private Long offeringId;


	public SelectHotelOfferingWizardStatus(Long eventId) {
		Objects.requireNonNull(eventId);

		this.eventId = eventId;
	}


	public WizardPage getCurrentPage() {
		return currentPage;
	}


	@Override
	public void setCurrentPage(WizardPage currentPage) {
		this.currentPage = currentPage;
	}


	@Override
	public Long getEventId() {
		return eventId;
	}


	@Override
	public Long getInitialHotelId() {
		return initialHotelId;
	}


	public void setInitialHotelId(Long initialHotelId) {
		this.initialHotelId = initialHotelId;
	}


	@Override
	public Long getHotelId() {
		return hotelId;
	}


	@Override
	public void setHotelId(Long hotelId) {
		// When the selected hotel changes, a possibly selected offering becomes invalid.
		if ( !isEqual(this.hotelId, hotelId) ) {
			this.offeringId = null;
		}

		this.hotelId = hotelId;
	}


	@Override
	public Long getInitialOfferingId() {
		return initialOfferingId;
	}


	public void setInitialOfferingId(Long initialOfferingId) {
		this.initialOfferingId = initialOfferingId;
	}


	@Override
	public Long getOfferingId() {
		return offeringId;
	}


	@Override
	public void setOfferingId(Long offeringId) {
		this.offeringId = offeringId;
	}

}

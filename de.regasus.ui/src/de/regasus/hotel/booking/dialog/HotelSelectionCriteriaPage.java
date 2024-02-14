package de.regasus.hotel.booking.dialog;

import java.math.BigDecimal;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.HotelOfferingParameter;
import com.lambdalogic.messeinfo.participant.data.HotelCostCoverage;
import com.lambdalogic.time.I18NDate;

import de.regasus.I18N;

/**
 * A page in the wizard to create hotel bookings where the user can select a desired hotel and category
 * and may specify arrival and departure days.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-84
 */

public class HotelSelectionCriteriaPage extends WizardPage {

	public static final String NAME = "HotelSelectionCriteriaPage";

	private HotelCostCoverage hotelCostCoverage;


	// widgets
	private HotelOfferingSearchComposite hotelOfferingSearchComposite;


	public HotelSelectionCriteriaPage() {
		super(NAME);

		setTitle(I18N.CreateHotelBooking_HotelSelectionCriteriaPage_Title);
		setMessage(I18N.CreateHotelBooking_HotelSelectionCriteriaPage_Message);
	}


	public void setHotelCostCoverage(HotelCostCoverage hotelCostCoverage) {
		this.hotelCostCoverage = hotelCostCoverage;
	}


	@Override
	public CreateHotelBookingWizard getWizard() {
		return (CreateHotelBookingWizard) super.getWizard();
	}


	@Override
	public void createControl(Composite parent) {
		hotelOfferingSearchComposite = new HotelOfferingSearchComposite(parent, hotelCostCoverage);
		hotelOfferingSearchComposite.setEventId( getWizard().getEventId() );

		// Never forget:
		setControl(hotelOfferingSearchComposite);
	}


	public Long getHotelPK() {
		Long hotelPK = hotelOfferingSearchComposite.getHotelPK();
		return hotelPK;
	}


	public String getCategory() {
		String category = hotelOfferingSearchComposite.getCategory();
		return category;
	}


	public I18NDate getArrival() {
		I18NDate arrvial = hotelOfferingSearchComposite.getArrival();
		return arrvial;
	}


	public I18NDate getDeparture() {
		I18NDate departure = hotelOfferingSearchComposite.getDeparture();
		return departure;
	}


	public Integer getGuestCount() {
		Integer guestCount = hotelOfferingSearchComposite.getGuestCount();
		return guestCount;
	}


	public Integer getMinRoomCount() {
		Integer minRoomCount = hotelOfferingSearchComposite.getMinRoomCount();
		return minRoomCount;
	}


	public String getCurrency() {
		String currency = hotelOfferingSearchComposite.getCurrency();
		return currency;
	}


	public BigDecimal getMinAmount() {
		BigDecimal minPrice = hotelOfferingSearchComposite.getMinAmount();
		return minPrice;
	}


	public BigDecimal getMaxAmount() {
		BigDecimal maxPrice = hotelOfferingSearchComposite.getMaxAmount();
		return maxPrice;
	}


	public HotelOfferingParameter getHotelOfferingParameter() {
		return hotelOfferingSearchComposite.getHotelOfferingParameter();
	}

}

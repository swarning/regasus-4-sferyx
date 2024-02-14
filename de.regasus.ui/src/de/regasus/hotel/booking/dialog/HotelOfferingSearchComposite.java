package de.regasus.hotel.booking.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.HotelOfferingParameter;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.data.EventCVO;
import com.lambdalogic.messeinfo.participant.data.HotelCostCoverage;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.finance.currency.combo.CurrencyCombo;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.hotel.combo.HotelCombo;
import de.regasus.hotel.offering.combo.HotelOfferingCategoryCombo;
import de.regasus.ui.Activator;


public class HotelOfferingSearchComposite extends Composite {

	private Long eventId;

	private Integer guestCount;
	private Long hotelContingentId;


	// widgets
	private HotelCombo hotelCombo;
	private HotelOfferingCategoryCombo categoryCombo;
	private DateComposite arrivalDate;
	private DateComposite departureDate;
	private NullableSpinner guestCountSpinner;
	private NullableSpinner minRoomCountSpinner;
	private CurrencyCombo currencyCombo;
	private Text minAmountText;
	private Text maxAmountText;

	private ModifySupport modifySupport = new ModifySupport(this);


	public HotelOfferingSearchComposite(Composite parent, HotelCostCoverage hotelCostCoverage) {
		super(parent, SWT.NONE);

		// ignore HotelCostCoverage if it cannot be used
		if (hotelCostCoverage != null && hotelCostCoverage.isDefined() && !hotelCostCoverage.isUsed()) {
			try {
    			// load Hotel Offering to determine guestCount and Hotel Contingent
    			Long offeringId = hotelCostCoverage.getOfferingId();
    			HotelOfferingVO offeringVO = HotelOfferingModel.getInstance().getHotelOfferingVO(offeringId);
    			guestCount = offeringVO.getBedCount();
    			hotelContingentId = offeringVO.getHotelContingentPK();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		createWidgets();
	}


	public HotelOfferingSearchComposite(Composite parent) {
		this(parent, null);
	}


	private void createWidgets() {

		this.setLayout(new GridLayout(1, false));

		Composite centerComposite = new Composite(this, SWT.NONE);

		GridData centerCompositeLayoutData = new GridData(SWT.CENTER, SWT.TOP, true, false);
		centerCompositeLayoutData.minimumWidth = 500;
		centerCompositeLayoutData.widthHint = 800;
		centerComposite.setLayoutData(centerCompositeLayoutData);

		centerComposite.setLayout(new GridLayout(4, false));

		try {
			// Hotel
			if (hotelContingentId == null) {
				createLabel(centerComposite, HotelLabel.Hotel);

				hotelCombo = new HotelCombo(centerComposite, SWT.NONE);
				hotelCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

				hotelCombo.addModifyListener(modifySupport);
			}

			// Category
			if (hotelContingentId == null) {
				createLabel(centerComposite, HotelLabel.Hotel_Category);

				categoryCombo = new HotelOfferingCategoryCombo(centerComposite, SWT.NONE);
				GridData categoryComboLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
				categoryComboLayoutData.widthHint = 100;
				categoryCombo.setLayoutData(categoryComboLayoutData);

				categoryCombo.addModifyListener(modifySupport);

				// placeholder
				Label placeholder = new Label(centerComposite, SWT.NONE);
				placeholder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			}

			{
				// Arrival
				createLabel(centerComposite, HotelLabel.HotelBooking_Arrival);

				arrivalDate = new DateComposite(centerComposite, SWT.BORDER);
				arrivalDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				arrivalDate.addModifyListener(modifySupport);

				// Departure
				createLabel(centerComposite, HotelLabel.HotelBooking_Departure);

				departureDate = new DateComposite(centerComposite, SWT.BORDER);
				departureDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

				departureDate.addModifyListener(modifySupport);
			}

			{
				// Guest count
				createLabel(centerComposite, HotelLabel.HotelBooking_GuestCount);

				guestCountSpinner = new NullableSpinner(centerComposite, SWT.NONE);
				guestCountSpinner.setMinimum(1);
				GridData spinnerLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
				spinnerLayoutData.widthHint = 100;
				guestCountSpinner.setLayoutData(spinnerLayoutData);

				guestCountSpinner.addModifyListener(modifySupport);

				guestCountSpinner.setValue(guestCount);


				// placeholder
				Label placeholder = new Label(centerComposite, SWT.NONE);
				placeholder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			}

			if (hotelContingentId == null) {
				// Minimum number of available rooms
				createLabel(centerComposite, HotelLabel.HotelBooking_MinRoomCount);

				minRoomCountSpinner = new NullableSpinner(centerComposite, SWT.NONE);
				minRoomCountSpinner.setMinimum(1);
				minRoomCountSpinner.setValue(1);
				minRoomCountSpinner.setNullable(false);
				GridData spinnerLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
				spinnerLayoutData.widthHint = 100;
				minRoomCountSpinner.setLayoutData(spinnerLayoutData);

				minRoomCountSpinner.addModifyListener(modifySupport);

				// placeholder
				Label placeholder = new Label(centerComposite, SWT.NONE);
				placeholder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			}

			if (hotelContingentId == null) {
				// currency
				createLabel(centerComposite, InvoiceLabel.Currency.getString());

				currencyCombo = new CurrencyCombo(centerComposite, SWT.NONE);
				GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
				currencyCombo.setLayoutData(layoutData);

				currencyCombo.addModifyListener(modifySupport);

				// placeholder
				Label placeholder = new Label(centerComposite, SWT.NONE);
				placeholder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
			}

			if (hotelContingentId == null) {
				// Minimum price
				minAmountText = createLabelAndText(centerComposite, HotelLabel.HotelBooking_MinAmount.getString());
				minAmountText.addModifyListener(modifySupport);

				// Maximum price
				maxAmountText = createLabelAndText(centerComposite, HotelLabel.HotelBooking_MaxAmount.getString());
				maxAmountText.addModifyListener(modifySupport);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public void setEventId(Long eventId) {
		Objects.requireNonNull(eventId);

		this.eventId = eventId;

		if (hotelCombo != null ) {
			hotelCombo.setEventPK(eventId);
		}

		if (categoryCombo != null) {
			categoryCombo.setEventPK(eventId);
		}

		/* Calculate default values for arrival and departure.
		 * The default for arrival is the begin of the event.
		 * The default for departure is the end of the event.
		 * If arrival and departure are equal, the latter is set to the following day.
		 */

		try {
    		EventCVO eventCVO = EventModel.getInstance().getEventCVO(eventId);

    		// initial arrival is the day when the event begins
    		I18NDate arrival = eventCVO.getBeginDate();

    		// initial departure is the day when the event ends ...
    		I18NDate departure = eventCVO.getEndDate();
    		// ... or one day after if the event begins and ends at the same day
    		if ( arrival.isEqual(departure) ) {
    			departure = departure.plusDays(1);
    		}

    		arrivalDate.setI18NDate(arrival);
    		departureDate.setI18NDate(departure);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	public Hotel getHotel() {
		if (hotelCombo != null) {
			return hotelCombo.getEntity();
		}
		return null;
	}


	public Long getHotelPK() {
		if (hotelCombo != null) {
			return hotelCombo.getHotelPK();
		}
		return null;
	}


	public void setHotelPK(Long hotelPK) {
		if (hotelCombo != null) {
			hotelCombo.setHotelPK(hotelPK);
		}
	}


	public String getCategory() {
		if (categoryCombo != null) {
			return categoryCombo.getCategory();
		}
		return null;
	}


	public void setCategory(String category) {
		if (categoryCombo != null) {
			categoryCombo.setCategory(category);
		}
	}


	public I18NDate getArrival() {
		I18NDate arrival = arrivalDate.getI18NDate();
		return arrival;
	}


	public void setArrival(I18NDate arrival) {
		arrivalDate.setI18NDate(arrival);
	}


	public I18NDate getDeparture() {
		I18NDate departure = departureDate.getI18NDate();
		return departure;
	}


	public void setDeparture(I18NDate departure) {
		departureDate.setI18NDate(departure);
	}


	public Integer getGuestCount() {
		Integer guestCount = guestCountSpinner.getValueAsInteger();
		return guestCount;
	}


	public void setGuestCount(Integer guestCount) {
		guestCountSpinner.setValue(guestCount);
	}


	public Integer getMinRoomCount() {
		if (minRoomCountSpinner != null) {
			return minRoomCountSpinner.getValueAsInteger();
		}
		return null;
	}


	public void setMinRoomCount(Integer minAvailableRoomCount) {
		if (minRoomCountSpinner != null) {
    		if (minAvailableRoomCount == null) {
    			minAvailableRoomCount = 1;
    		}
    		minRoomCountSpinner.setValue(minAvailableRoomCount);
		}
	}


	public String getCurrency() {
		if (currencyCombo != null) {
			return currencyCombo.getCurrencyCode();
		}
		return null;
	}


	public void setCurrency(String currency) {
		if (currencyCombo != null) {
			currencyCombo.setCurrencyCode(currency);
		}
	}


	public BigDecimal getMinAmount() {
		BigDecimal minPrice = null;

		if (   minAmountText != null
			&& minAmountText.getText() != null
			&& !minAmountText.getText().isEmpty()
		) {
			try {
				minPrice = TypeHelper.toBigDecimal(minAmountText.getText());
			}
			catch (ParseException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		return minPrice;
	}


	public void setMinAmount(BigDecimal minAmount) {
		if (minAmountText != null) {
			minAmountText.setText(StringHelper.toString(minAmount));
		}
	}


	public BigDecimal getMaxAmount() {
		BigDecimal maxPrice = null;

		if (   maxAmountText != null
			&& maxAmountText.getText() != null
			&& !maxAmountText.getText().isEmpty()
		) {
			try {
				maxPrice = TypeHelper.toBigDecimal(maxAmountText.getText());
			}
			catch (ParseException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return maxPrice;
	}


	public void setMaxAmount(BigDecimal maxAmount) {
		if (maxAmountText != null) {
			maxAmountText.setText(StringHelper.toString(maxAmount));
		}
	}


	public HotelOfferingParameter getHotelOfferingParameter() {
		HotelOfferingParameter parameter = new HotelOfferingParameter(eventId);
		parameter.setHotelContingentPK(hotelContingentId);

		parameter.setHotelPK( getHotelPK() );
		parameter.setCategory( getCategory() );
		parameter.setArrival( getArrival() );
		parameter.setDeparture( getDeparture() );
		parameter.setGuestCount( getGuestCount() );
		parameter.setMinRoomCount( getMinRoomCount() );
		parameter.setCurrency( getCurrency() );
		parameter.setMinAmount( getMinAmount() );
		parameter.setMaxAmount( getMaxAmount() );

		return parameter;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}

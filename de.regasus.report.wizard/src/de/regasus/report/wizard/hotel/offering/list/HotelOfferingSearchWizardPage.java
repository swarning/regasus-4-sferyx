package de.regasus.report.wizard.hotel.offering.list;

import java.math.BigDecimal;
import java.text.DateFormat;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.report.hotelList.IHotelOfferingSearchReportParameter;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.report.parameter.IEventReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;

import de.regasus.hotel.booking.dialog.HotelOfferingSearchComposite;
import de.regasus.report.dialog.IReportWizardPage;


public class HotelOfferingSearchWizardPage extends WizardPage implements IReportWizardPage, ModifyListener {

	public static final String ID = "HotelOfferingSearchWizardPage";

	// Parameter
	private IHotelOfferingSearchReportParameter hotelOfferingSearchReportParameter;

	// Widgets
	private HotelOfferingSearchComposite hotelOfferingSearchComposite = null;

	private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);


	public HotelOfferingSearchWizardPage() {
		super(ID);
		setTitle(HotelLabel.HotelOfferingSearchPage_Title.getString());
		setDescription(HotelLabel.HotelOfferingSearchPage_Description.getString());
	}


	@Override
	public void createControl(Composite parent) {
		hotelOfferingSearchComposite = new HotelOfferingSearchComposite(parent);

		// Never forget:
		setControl(hotelOfferingSearchComposite);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter != null ) {
			/*
			 * If the reportParameter are implementing IEventReportParameter and contain an eventPK,
			 * then hotels are limited to those that belong to this event.
			 */
			if (reportParameter instanceof IEventReportParameter){
				IEventReportParameter eventReportParameter = (IEventReportParameter) reportParameter;
				Long eventPK = eventReportParameter.getEventPK();

				hotelOfferingSearchComposite.setEventId(eventPK);
			}

			if (reportParameter instanceof IHotelOfferingSearchReportParameter){
				hotelOfferingSearchReportParameter = (IHotelOfferingSearchReportParameter) reportParameter;

				Long hotelPK = hotelOfferingSearchReportParameter.getHotelPK();
				if (hotelPK != null) {
					hotelOfferingSearchComposite.setHotelPK(hotelPK);
				}

				String category = hotelOfferingSearchReportParameter.getCategory();
				hotelOfferingSearchComposite.setCategory(category);

				I18NDate arrival = TypeHelper.toI18NDate( hotelOfferingSearchReportParameter.getArrival() );
				if (arrival != null) {
					// only set is not null, to avoid overwriting initial value which is the begin of the Event
					hotelOfferingSearchComposite.setArrival(arrival);
				}

				I18NDate departure = TypeHelper.toI18NDate( hotelOfferingSearchReportParameter.getDeparture() );
				if (departure != null) {
					// only set is not null, to avoid overwriting initial value which is the end of the Event
					hotelOfferingSearchComposite.setDeparture(departure);
				}

				Integer guestCount = hotelOfferingSearchReportParameter.getGuestCount();
				hotelOfferingSearchComposite.setGuestCount(guestCount);

				Integer minAvailableRoomCount = hotelOfferingSearchReportParameter.getMinimumRoomCount();
				hotelOfferingSearchComposite.setMinRoomCount(minAvailableRoomCount);

				String currency = hotelOfferingSearchReportParameter.getCurrency();
				hotelOfferingSearchComposite.setCurrency(currency);

				BigDecimal minPrice = hotelOfferingSearchReportParameter.getMinimumAmount();
				if (minPrice != null) {
					hotelOfferingSearchComposite.setMinAmount(minPrice);
				}

				BigDecimal maxPrice = hotelOfferingSearchReportParameter.getMaximumAmount();
				if (maxPrice != null) {
					hotelOfferingSearchComposite.setMaxAmount(maxPrice);
				}
			}
		}

		/* Start observing hotelOfferingSearchComposite AFTER setting its values to avoid
		 * calls of modifyText(ModifyEvent e) when settings its values that lead to side-effects.
		 */
		hotelOfferingSearchComposite.addModifyListener(this);
	}


	@Override
	public void modifyText(ModifyEvent e) {
		I18NDate arrival = hotelOfferingSearchComposite.getArrival();
		I18NDate departure = hotelOfferingSearchComposite.getDeparture();
		setPageComplete(arrival != null && departure != null);
	}


	@Override
	public void saveReportParameters() {
		if (hotelOfferingSearchReportParameter != null) {
			/* Read values from hotelOfferingSearchComposite and set them to report parameters
			 */

			I18NPattern desc = new I18NPattern();


			// hotel
			Long hotelPK = hotelOfferingSearchComposite.getHotelPK();
			hotelOfferingSearchReportParameter.setHotelPK(hotelPK);

			// hotel description
			Hotel hotel = hotelOfferingSearchComposite.getHotel();
			if (hotel != null) {
				desc.append(HotelLabel.Hotel);
				desc.append(": ");
				desc.append(StringHelper.avoidNull( hotel.getName1() ));
			}


			// category
			String category = hotelOfferingSearchComposite.getCategory();
			hotelOfferingSearchReportParameter.setCategory(category);

			// category description
			if (category != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
				desc.append(HotelLabel.Hotel_Category);
				desc.append(": ");
				desc.append(category);
			}


			// arrival
			I18NDate arrival = hotelOfferingSearchComposite.getArrival();
			hotelOfferingSearchReportParameter.setArrival( TypeHelper.toDate(arrival) );

			// arrival description
			if (arrival != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
				desc.append(HotelLabel.HotelBooking_Arrival);
				desc.append(": ");
				desc.append(dateFormat.format(arrival));
			}


			// departure
			I18NDate departure = hotelOfferingSearchComposite.getDeparture();
			hotelOfferingSearchReportParameter.setDeparture( TypeHelper.toDate(departure) );

			// departure description
			if (departure != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
				desc.append(HotelLabel.HotelBooking_Departure);
				desc.append(": ");
				desc.append(dateFormat.format(departure));
			}


			// guestCount
			Integer guestCount = hotelOfferingSearchComposite.getGuestCount();
			hotelOfferingSearchReportParameter.setGuestCount(guestCount);

			// guestCount description
			if (guestCount != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
				desc.append(HotelLabel.HotelBooking_GuestCount);
				desc.append(": ");
				desc.append(guestCount);
			}


    		// minAvailableRoomCount
    		Integer minAvailableRoomCount = hotelOfferingSearchComposite.getMinRoomCount();
    		hotelOfferingSearchReportParameter.setMinimumRoomCount(minAvailableRoomCount);

    		// minAvailableRoomCount description
    		if (minAvailableRoomCount != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
    			desc.append(HotelLabel.HotelBooking_MinRoomCount);
    			desc.append(": ");
    			desc.append(minAvailableRoomCount);
    		}


    		// currency
    		String currency = hotelOfferingSearchComposite.getCurrency();
    		hotelOfferingSearchReportParameter.setCurrency(currency);

    		// currency description
    		if (currency != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
    			desc.append(InvoiceLabel.Currency);
    			desc.append(": ");
    			desc.append(currency);
    		}

        	// minPrice
    		BigDecimal minAmount = hotelOfferingSearchComposite.getMinAmount();
    		hotelOfferingSearchReportParameter.setMinimumAmount(minAmount);

        	// minPrice description
        	if (minAmount != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
        		desc.append(HotelLabel.HotelBooking_MinAmount);
        		desc.append(": ");
        		desc.append(minAmount);
        	}


            // maxPrice
        	BigDecimal maxAmount = hotelOfferingSearchComposite.getMaxAmount();
        	hotelOfferingSearchReportParameter.setMaximumAmount(maxAmount);

        	// maxPrice description
            if (maxAmount != null) {
            	if (!desc.isEmpty()){
            		desc.append("\n");
            	}
            	desc.append(HotelLabel.HotelBooking_MaxAmount);
            	desc.append(": ");
            	desc.append(maxAmount);
            }


            hotelOfferingSearchReportParameter.setDescription(
            	IHotelOfferingSearchReportParameter.DESCRIPTION_ID,
            	desc.toString()
            );
		}
	}

}
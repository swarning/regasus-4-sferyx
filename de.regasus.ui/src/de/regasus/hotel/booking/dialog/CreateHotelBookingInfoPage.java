package de.regasus.hotel.booking.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.hotel.data.ArrivalInfo;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVO;
import com.lambdalogic.messeinfo.hotel.data.RoomType;
import com.lambdalogic.messeinfo.hotel.data.SmokerType;

import de.regasus.I18N;
import de.regasus.core.error.Activator;
import de.regasus.core.error.RegasusErrorHandler;

/**
 * A page in the wizard to create hotel bookings where arrival, smoker, bed and booking infos can be set.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-84
 */

public class CreateHotelBookingInfoPage extends WizardPage {

	public static final String NAME = "InfoPage";

	private Long eventPK;


	private HotelBookingInfoForHotelGroup infoForHotelGroup;
	private HotelBookingInfoForGuestGroup infoForGuestGroup;


	// *************************************************************************
	// * Constructor
	// *

	public CreateHotelBookingInfoPage(Long eventPK) {
		super(NAME);

		this.eventPK = eventPK;

		setTitle(I18N.CreateHotelBookingInfoPage_Title);
		setMessage(I18N.CreateHotelBookingInfoPage_Message);
	}


	@Override
	public CreateHotelBookingWizard getWizard() {
		return (CreateHotelBookingWizard) super.getWizard();
	}


	@Override
	public void createControl(Composite parent) {

		try {
			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout());


			// build HotelBookingInfoForHotelGroup
			infoForHotelGroup = new HotelBookingInfoForHotelGroup(mainComposite, SWT.NONE, false/*showEditTime*/);
			infoForHotelGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			adaptTwinRoomButtonToHotelBooking();

			// observe HotelOfferingsTablePage and react on changes of selected Hotel Offering
			getWizard().getHotelOfferingsTablePage().addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					adaptTwinRoomButtonToHotelBooking();
				}
			});


			// build HotelBookingInfoForGuestGroup
			infoForGuestGroup = new HotelBookingInfoForGuestGroup(mainComposite, SWT.NONE, false/*showEditTime*/, eventPK);


			// Never forget:
			setControl(mainComposite);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	private void adaptTwinRoomButtonToHotelBooking() {
		HotelOfferingCVO hoCVO = getWizard().getHotelOfferingsTablePage().getBookedHotelOfferingCVO();
		if (hoCVO != null) {
    		int bedCount = hoCVO.getVO().getBedCount().intValue();

    		RoomType roomType = hoCVO.getRoomDefinitionVO().getRoomType();

    		if (bedCount == 2) {
    			/* Select and disable twinRoomCheckBox if RoomType is TWIN.
    			 *
    			 * Otherwise, enable twinRoomCheckBox and deselect as default.
    			 * Even if the RoomType is DOUBLE the participant could request a twin room.
    			 * Often information about twin rooms is not available and all double rooms are
    			 * recorded with RoomType DOUBLE.
    			 */
    			if (roomType == RoomType.TWIN) {
    				infoForHotelGroup.setTwinRoom(true);
    				infoForHotelGroup.setTwinRoomEnabled(false);
    			}
    			else {
    				infoForHotelGroup.setTwinRoom(false);
    				infoForHotelGroup.setTwinRoomEnabled(true);
    			}
    		}
    		else {
    			infoForHotelGroup.setTwinRoom(false);
    			infoForHotelGroup.setTwinRoomEnabled(false);
    		}
		}
	}


	public ArrivalInfo getArrivalInfo() {
		return infoForHotelGroup.getArrivalInfo();
	}


	public String getArrivalNote() {
		return infoForHotelGroup.getArrivalNote();
	}


	public boolean isTwin() {
		return infoForHotelGroup.isTwinRoom();
	}


	public SmokerType getSmokerType() {
		return infoForHotelGroup.getSmokerType();
	}


	public String getHotelInfo() {
		return infoForHotelGroup.getHotelInfo();
	}


	public String getHotelPaymentInfo() {
		return infoForHotelGroup.getHotelPaymentInfo();
	}


	public String getAdditionalGuests() {
		return infoForHotelGroup.getAdditionalGuests();
	}


	public LanguageString getInfo() {
		return infoForGuestGroup.getGuestInfo();
	}

}

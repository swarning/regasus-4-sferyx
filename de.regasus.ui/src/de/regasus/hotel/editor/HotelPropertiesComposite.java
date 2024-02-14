package de.regasus.hotel.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.HotelProperties;
import com.lambdalogic.messeinfo.hotel.HotelProperty;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class HotelPropertiesComposite extends Composite {

	// the entity
	private HotelProperties hotelProperties;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// Widgets
	private Button frontDesk24HoursOpenButton;
	private Button expressCheckInCheckOutButton;
	private Button luggageStorageButton;
	private Button atmButton;
	private Button conciergeButton;
	private Button laundryButton;
	private Button newspapersButton;
	private Button poolButton;
	private Button fitnessButton;
	private Button parkingButton;
	private Button restaurantButton;
	private Button barButton;
	private Button nonSmokingButton;
	private Button roomsForAllergySufferersButton;
	private Button roomsForDisabledGuestsButton;
	private Button freeWiFiInPublicButton;
	private Button freeWiFiInRoomButton;
	private Button roomServiceButton;
	private Button businessCenterButton;
	private Button dailyHousekeepingButton;
	private Button hotBreakfastButton;
	private Button meetingRoomsButton;
	private Button saunaButton;
	private Button sustainabilityCertifiedButton;
	private Button smokingRoomsButton;
	private Button spaButton;


	public HotelPropertiesComposite(Composite parent, int style) {
		super(parent, style);

		try {
			// set layout for this Composite: 2 columns
			setLayout(new GridLayout(2, true));

			/* Buttons
			 * The order of the Buttons here does not correspond to the order in HotelProperties.
			 * The order in HotelProperties is chronological/technical. New fields are appended at the end.
			 * The order here is semantical. Properties that belong together (e.g. pool, fitness and saune) should
			 * appear near each other.
			 */

			GridDataFactory gridDataFactory = GridDataFactory.fillDefaults();
			gridDataFactory.align(SWT.FILL, SWT.FILL);
			gridDataFactory.grab(true,  false);

			int compositeStyle = SWT.BORDER;

			// reception service
			{
				Composite receptionServiceComposite = new Composite(this, compositeStyle);
				receptionServiceComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(receptionServiceComposite);

				frontDesk24HoursOpenButton = new Button(receptionServiceComposite, SWT.CHECK);
				frontDesk24HoursOpenButton.setText( HotelProperty.FRONT_DESK_24_HOURS_OPEN.getString() );
				frontDesk24HoursOpenButton.addSelectionListener(modifySupport);

				conciergeButton = new Button(receptionServiceComposite, SWT.CHECK);
				conciergeButton.setText( HotelProperty.CONCIERGE.getString() );
				conciergeButton.addSelectionListener(modifySupport);

				luggageStorageButton = new Button(receptionServiceComposite, SWT.CHECK);
				luggageStorageButton.setText( HotelProperty.LUGGAGE_STORAGE.getString() );
				luggageStorageButton.addSelectionListener(modifySupport);

				expressCheckInCheckOutButton = new Button(receptionServiceComposite, SWT.CHECK);
				expressCheckInCheckOutButton.setText( HotelProperty.EXPRESS_CHECK_IN_CHECK_OUT.getString() );
				expressCheckInCheckOutButton.addSelectionListener(modifySupport);

				parkingButton = new Button(receptionServiceComposite, SWT.CHECK);
				parkingButton.setText( HotelProperty.PARKING.getString() );
				parkingButton.addSelectionListener(modifySupport);

				sustainabilityCertifiedButton = new Button(receptionServiceComposite, SWT.CHECK);
				sustainabilityCertifiedButton.setText( HotelProperty.SUSTAINABILITY_CERTIFIED.getString() );
				sustainabilityCertifiedButton.addSelectionListener(modifySupport);
			}

			// room service
			{
				Composite roomServiceComposite = new Composite(this, compositeStyle);
				roomServiceComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(roomServiceComposite);

				roomServiceButton = new Button(roomServiceComposite, SWT.CHECK);
				roomServiceButton.setText( HotelProperty.ROOM_SERVICE.getString() );
				roomServiceButton.addSelectionListener(modifySupport);

				dailyHousekeepingButton = new Button(roomServiceComposite, SWT.CHECK);
				dailyHousekeepingButton.setText( HotelProperty.DAILY_HOUSEKEEPING.getString() );
				dailyHousekeepingButton.addSelectionListener(modifySupport);

				laundryButton = new Button(roomServiceComposite, SWT.CHECK);
				laundryButton.setText( HotelProperty.LAUNDRY.getString() );
				laundryButton.addSelectionListener(modifySupport);
			}

			// other service
			{
				Composite otherServiceComposite = new Composite(this, compositeStyle);
				otherServiceComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(otherServiceComposite);

				atmButton = new Button(otherServiceComposite, SWT.CHECK);
				atmButton.setText( HotelProperty.ATM.getString() );
				atmButton.addSelectionListener(modifySupport);

				newspapersButton = new Button(otherServiceComposite, SWT.CHECK);
				newspapersButton.setText( HotelProperty.NEWSPAPERS.getString() );
				newspapersButton.addSelectionListener(modifySupport);
			}

			// recreation
			{
				Composite recreationServiceComposite = new Composite(this, compositeStyle);
				recreationServiceComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(recreationServiceComposite);

				poolButton = new Button(recreationServiceComposite, SWT.CHECK);
				poolButton.setText( HotelProperty.POOL.getString() );
				poolButton.addSelectionListener(modifySupport);

				fitnessButton = new Button(recreationServiceComposite, SWT.CHECK);
				fitnessButton.setText( HotelProperty.FITNESS.getString() );
				fitnessButton.addSelectionListener(modifySupport);

				saunaButton = new Button(recreationServiceComposite, SWT.CHECK);
				saunaButton.setText( HotelProperty.SAUNA.getString() );
				saunaButton.addSelectionListener(modifySupport);

				spaButton = new Button(recreationServiceComposite, SWT.CHECK);
				spaButton.setText( HotelProperty.SPA.getString() );
				spaButton.addSelectionListener(modifySupport);
			}


			// food
			{
				Composite foodServiceComposite = new Composite(this, compositeStyle);
				foodServiceComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(foodServiceComposite);

				restaurantButton = new Button(foodServiceComposite, SWT.CHECK);
				restaurantButton.setText( HotelProperty.RESTAURANT.getString() );
				restaurantButton.addSelectionListener(modifySupport);

				barButton = new Button(foodServiceComposite, SWT.CHECK);
				barButton.setText( HotelProperty.BAR.getString() );
				barButton.addSelectionListener(modifySupport);

				hotBreakfastButton = new Button(foodServiceComposite, SWT.CHECK);
				hotBreakfastButton.setText( HotelProperty.HOT_BREAKFAST.getString() );
				hotBreakfastButton.addSelectionListener(modifySupport);
			}

			// health
			{
				Composite healthServiceComposite = new Composite(this, compositeStyle);
				healthServiceComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(healthServiceComposite);

				nonSmokingButton = new Button(healthServiceComposite, SWT.CHECK);
				nonSmokingButton.setText( HotelProperty.NON_SMOKING.getString() );
				nonSmokingButton.addSelectionListener(modifySupport);

				smokingRoomsButton = new Button(healthServiceComposite, SWT.CHECK);
				smokingRoomsButton.setText( HotelProperty.SMOKING_ROOMS.getString() );
				smokingRoomsButton.addSelectionListener(modifySupport);

				roomsForAllergySufferersButton = new Button(healthServiceComposite, SWT.CHECK);
				roomsForAllergySufferersButton.setText( HotelProperty.ALLERGY_FRIENDLY_ROOMS.getString() );
				roomsForAllergySufferersButton.addSelectionListener(modifySupport);

				roomsForDisabledGuestsButton = new Button(healthServiceComposite, SWT.CHECK);
				roomsForDisabledGuestsButton.setText( HotelProperty.ROOMS_FOR_DISABLED_GUESTS.getString() );
				roomsForDisabledGuestsButton.addSelectionListener(modifySupport);
			}

			// business
			{
				Composite businessServiceComposite = new Composite(this, compositeStyle);
				businessServiceComposite.setLayout(new GridLayout());
				gridDataFactory.applyTo(businessServiceComposite);

				businessCenterButton = new Button(businessServiceComposite, SWT.CHECK);
				businessCenterButton.setText( HotelProperty.BUSINESS_CENTER.getString() );
				businessCenterButton.addSelectionListener(modifySupport);

				meetingRoomsButton = new Button(businessServiceComposite, SWT.CHECK);
				meetingRoomsButton.setText( HotelProperty.MEETING_ROOMS.getString() );
				meetingRoomsButton.addSelectionListener(modifySupport);

				freeWiFiInPublicButton = new Button(businessServiceComposite, SWT.CHECK);
				freeWiFiInPublicButton.setText( HotelProperty.FREE_WIFI_IN_PUBLIC.getString() );
				freeWiFiInPublicButton.addSelectionListener(modifySupport);

				freeWiFiInRoomButton = new Button(businessServiceComposite, SWT.CHECK);
				freeWiFiInRoomButton.setText( HotelProperty.FREE_WIFI_IN_ROOM.getString() );
				freeWiFiInRoomButton.addSelectionListener(modifySupport);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}


	public void setHotelProperties(HotelProperties hotelProperties) {
		this.hotelProperties = hotelProperties;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (hotelProperties != null) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						final HotelProperties hp = hotelProperties;

						frontDesk24HoursOpenButton.setSelection(	hp.isFrontDesk24HoursOpen());
						expressCheckInCheckOutButton.setSelection(	hp.isExpressCheckInCheckOut());
						luggageStorageButton.setSelection(			hp.isLuggageStorage());
						atmButton.setSelection(						hp.isATM());
						conciergeButton.setSelection(				hp.isConcierge());
						laundryButton.setSelection(					hp.isLaundry());
						newspapersButton.setSelection(				hp.isNewspapers());
						poolButton.setSelection(					hp.isPool());
						fitnessButton.setSelection(					hp.isFitness());
						parkingButton.setSelection(					hp.isParking());
						restaurantButton.setSelection(				hp.isRestaurant());
						barButton.setSelection(						hp.isBar());
						nonSmokingButton.setSelection(				hp.isNonSmoking());
						roomsForAllergySufferersButton.setSelection(hp.isAllergyFriendlyRooms());
						roomsForDisabledGuestsButton.setSelection(	hp.isRoomsForDisabledGuests());
						freeWiFiInPublicButton.setSelection(		hp.isFreeWiFiInPublic());
						freeWiFiInRoomButton.setSelection(			hp.isFreeWiFiInRoom());
						roomServiceButton.setSelection(				hp.isRoomService());
						businessCenterButton.setSelection(			hp.isBusinessCenter());
						dailyHousekeepingButton.setSelection(		hp.isDailyHousekeeping());
						hotBreakfastButton.setSelection(			hp.isHotBreakfast());
						meetingRoomsButton.setSelection(			hp.isMeetingRooms());
						saunaButton.setSelection(					hp.isSauna());
						sustainabilityCertifiedButton.setSelection(	hp.isSustainabilityCertified());
						smokingRoomsButton.setSelection(			hp.isSmokingRooms());
						spaButton.setSelection(						hp.isSpa());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public void syncEntityToWidgets() {
		if (hotelProperties != null) {
			final HotelProperties hp = hotelProperties;

			hp.setFrontDesk24HoursOpen(frontDesk24HoursOpenButton.getSelection());
			hp.setExpressCheckInCheckOut(expressCheckInCheckOutButton.getSelection());
			hp.setLuggageStorage(luggageStorageButton.getSelection());
			hp.setATM(atmButton.getSelection());
			hp.setConcierge(conciergeButton.getSelection());
			hp.setLaundry(laundryButton.getSelection());
			hp.setNewspapers(newspapersButton.getSelection());
			hp.setPool(poolButton.getSelection());
			hp.setFitness(fitnessButton.getSelection());
			hp.setParking(parkingButton.getSelection());
			hp.setRestaurant(restaurantButton.getSelection());
			hp.setBar(barButton.getSelection());
			hp.setNonSmoking(nonSmokingButton.getSelection());
			hp.setRoomsForAllergySufferers(roomsForAllergySufferersButton.getSelection());
			hp.setRoomsForDisabledGuests(roomsForDisabledGuestsButton.getSelection());
			hp.setFreeWiFiInPublic(freeWiFiInPublicButton.getSelection());
			hp.setFreeWiFiInRoom(freeWiFiInRoomButton.getSelection());
			hp.setRoomService(roomServiceButton.getSelection());
			hp.setBusinessCenter(businessCenterButton.getSelection());
			hp.setDailyHousekeeping(dailyHousekeepingButton.getSelection());
			hp.setHotBreakfast(hotBreakfastButton.getSelection());
			hp.setMeetingRooms(meetingRoomsButton.getSelection());
			hp.setSauna(saunaButton.getSelection());
			hp.setSustainabilityCertified(sustainabilityCertifiedButton.getSelection());
			hp.setSmokingRooms(smokingRoomsButton.getSelection());
			hp.setSpa(spaButton.getSelection());
		}
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

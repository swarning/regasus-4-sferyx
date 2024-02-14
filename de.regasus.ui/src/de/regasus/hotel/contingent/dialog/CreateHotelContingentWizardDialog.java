package de.regasus.hotel.contingent.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.dialog.HotelAdditionalInformationWizardPage;
import de.regasus.hotel.dialog.HotelRoomDefinitionWizardPage;
import de.regasus.ui.Activator;

/**
 * See requirement description in {@link CreateHotelContingentWizard}
 */
public class CreateHotelContingentWizardDialog extends WizardDialog {

	// IDs to discriminate custom added buttons
	private static final int ADD_HOTEL_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;
	private static final int ADD_ROOM_DEFINITION_BUTTON_ID = IDialogConstants.CLIENT_ID + 2;

	// custom added buttons
	private Button addHotelButton;
	private Button addRoomDefinitionButton;

	// The wizard contained in this dialog
	private CreateHotelContingentWizard createHotelContingentWizard;


	// **************************************************************************
	// * Constructors
	// *

	public CreateHotelContingentWizardDialog(Shell parentShell, CreateHotelContingentWizard createHotelContingentWizard) {
		super(parentShell, createHotelContingentWizard);

		this.createHotelContingentWizard = createHotelContingentWizard;
	}


	// **************************************************************************
	// * Overriden Methods
	// *

	/**
	 * The navigation area shows besides "Next" two additional buttons, to create a Hotel and an
	 * additional room definition.
	 * <p>
	 * To realize this requirement, we hook into the moment before the finish button will be created.
	 *
	 * The common way to add Buttons is overriding createButtonsForButtonBar(Composite parent).
	 * But this would add the additional Buttons to the left of the Back-Button.
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.FINISH_ID) {
			addHotelButton = super.createButton(
				parent,
				ADD_HOTEL_BUTTON_ID,
				I18N.CreateHotel,
				false
			);
			addHotelButton.setVisible(true);

			addRoomDefinitionButton = super.createButton(
				parent,
				ADD_ROOM_DEFINITION_BUTTON_ID,
				I18N.CreateHotel_AdditionalRoomDefinition,
				false
			);
			addRoomDefinitionButton.setVisible(false);
		}

		return super.createButton(parent, id, label, defaultButton);
	}


	/**
	 * Pressing one of the special buttons, we tell the wizard to create new pages
	 * and also navigate to them.
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		try {
			if (buttonId == ADD_HOTEL_BUTTON_ID) {
				createHotelContingentWizard.createHotelDefinitionPages();
				addHotelButton.setEnabled(false);
				addHotelButton.setVisible(false);
				nextPressed();
			}
			else if (buttonId == ADD_ROOM_DEFINITION_BUTTON_ID) {
				createHotelContingentWizard.createRoomDefinitionPages();
				nextPressed();
			}
			else {
				super.buttonPressed(buttonId);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void nextPressed() {
		// If next page is to show room definitions and we do not use an existing hotel,
		// we have to collect all previously defined room definitions (which are not yet in database)
		// and set them in the page.

		// Also we set artificial dummy PKs so that we and the roomDefinitionTable can distinguish them

		IWizardPage currentPage = getCurrentPage();
		IWizardPage nextPage = currentPage.getNextPage();
		if (nextPage instanceof HotelContingentNameRoomDefinitionsWizardPage) {
			createHotelContingentWizard.copyHotelNameToHotelContingentCVO();

			if (createHotelContingentWizard.isCreatingHotel()) {
				HotelContingentNameRoomDefinitionsWizardPage hotelContingentNameWizardPage = (HotelContingentNameRoomDefinitionsWizardPage) nextPage;

				List<RoomDefinitionVO> roomDefinitionVOs = new ArrayList<RoomDefinitionVO>();
				long dummyId = -1;
				for (HotelRoomDefinitionWizardPage page : createHotelContingentWizard.getHotelRoomDefinitionWizardPages()) {
					RoomDefinitionVO roomDefinitionVO = page.getRoomDefinitionVO();
					roomDefinitionVO.setID(dummyId);
					roomDefinitionVOs.add(roomDefinitionVO);
					dummyId--;
				}
				hotelContingentNameWizardPage.setRoomDefinitionVOs(roomDefinitionVOs);
			}

			else {
				createHotelContingentWizard.useSearchedHotelForContingent();
			}
		}

		super.nextPressed();
	}


	/**
	 * The special button to create a hotel is only visible on the first page, and only
	 * if no hotel has yet been created.
	 * <p>
	 * The special button to create a room definition is only visible on the last
	 * of the hotel-related pages
	 * <p>
	 * We don't allow to go back to the hotel search page if the user has decided to create a new hotel.
	 * <p>
	 * We don't allow to go back to any hotel page if the hotel has been created.
	 */
	@Override
	public void updateButtons() {
		super.updateButtons();

		IWizardPage currentPage = getCurrentPage();
		IWizardPage previousPage = currentPage.getPreviousPage();
		IWizardPage nextPage = currentPage.getNextPage();

		// The special button to create a hotel is only visible on the first page, and only
		// if no hotel has yet been created.
		addHotelButton.setVisible(null == previousPage && ! createHotelContingentWizard.isCreatingHotel());

		// The special button to create a room definition is only visible on the last
		// of the hotel-related pages
		boolean onPossiblyLastHotelPagePage =
			(currentPage instanceof HotelRoomDefinitionWizardPage
			||
			currentPage instanceof HotelAdditionalInformationWizardPage);

		boolean nextIsContingentNamePage = (nextPage instanceof HotelContingentNameRoomDefinitionsWizardPage);
		addRoomDefinitionButton.setVisible(onPossiblyLastHotelPagePage && nextIsContingentNamePage);

		// We don't allow to go back to the hotel search page if the user has decided to create a new hotel.
		// This might get too confusing.
		boolean canGoBack = previousPage != null;

		if (createHotelContingentWizard.isCreatingHotel()
			&&
			previousPage instanceof HotelSearchWizardPage) {

			canGoBack = false;
//			System.out.println("Not going back to page for searching hotel, because decisision was to create a new hotel");
		}

		/* We don't allow to go back to any room definition page if that one has already been created.
		 * This may happen if an error occurs during saving room definitions, because this is not
		 * done in a transaction, but one by one.
		 */
		if (createHotelContingentWizard.isHasCreatedHotel()
			&&
			previousPage instanceof HotelAdditionalInformationWizardPage
		) {
			canGoBack = false;
//			System.out.println("Not allowing to go back to pages for already created hotel");
		}


		/* We don't allow to go back to any room definition page if that one has already been created.
		 * This may happen if an error occurs during saving room definitions, because this is not
		 * done in a transaction, but one by one.
		 */
		if (createHotelContingentWizard.isHasCreatedHotel()
			&&
			previousPage instanceof HotelRoomDefinitionWizardPage
		) {
			HotelRoomDefinitionWizardPage roomDefinitionWizardPage = (HotelRoomDefinitionWizardPage) previousPage;
			RoomDefinitionVO roomDefinitionVO = roomDefinitionWizardPage.getRoomDefinitionVO();
			boolean alredyCreated = roomDefinitionVO.getID() != null;

			if (alredyCreated) {
//				System.out.println("Not allowing to go back to page for already created room definition " + roomDefinitionVO.getName());
				canGoBack = false;
			}
		}

		getButton(IDialogConstants.BACK_ID).setEnabled(canGoBack);
	}

}

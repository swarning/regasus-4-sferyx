package de.regasus.hotel.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.CollectionsHelper;

import de.regasus.I18N;
import de.regasus.common.CountryCity;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.hotel.editor.HotelEditor;
import de.regasus.hotel.editor.HotelEditorInput;
import de.regasus.ui.Activator;

/**
 * This wizard serves to create a new hotel including room definitions and shall ease the creation
 * of hotel master data. It's initiating issue is MIRCP-1535.
 * <p>
 * First the wizard collects the required data. Upon finishing, the hotel and room definitions are
 * created within a transaction. Should an error occur, the error gets shown and the wizard remains
 * open, otherweise the corresponding hotel editor is opened.
 * <p>
 * The assistent can be started by
 * <ul>
 * <li>a button in the toolbar of the hotel view
 * <li>a menu item that shows in all nodes of the hotel tree view
 * </ul>
 * <p>
 * The pages of the wizard are
 * <ol>
 * <li>Names, containing the fields name 1 to 4</li>
 * <li>Address, containing the fields of the main address, corresponding to the address tab of the hotel editor</li>
 * <li>Communication, containing the fields for the communication, corresponding to the tab of the hotel editor</li>
 * <li>Additional information, containing the fields note and guest info</li>
 * </ol>
 * <p>
 * On this page, the navigation area shows besides "Next" an additional special button reading
 * "Additional room definition". With it, the user obtains a pair of pages 5 and 6. The button "Next"
 * is inactive here. On page 6 (and again on each last page) this button re-appears to offer the
 * creation of a new pair of pages (7 and 8, 9 and 10, and so on). Those pages offer:
 * <ol>
 * <li>Selection of a (first) default room type. A selection is required, for otherwise one cannot
 * generate a corresponding VO.</li>
 * <li>(First) room definition, showing the fields of a room definition. Depending on the selection
 * in the previous page, the fields are prefilled. The name and description are given in german,
 * english and french.<br>
 * </li>
 * </ol>
 * <p>
 * The wizard can be finished as soon as name1, country and city are given.
 */
public class CreateHotelWizard extends Wizard {

	// **************************************************************************
	// * Wizard Pages and other Attributes
	// *

	private HotelNamesWizardPage hotelNamesWizardPage;
	private HotelAddressWizardPage hotelAddressWizardPage;
	private HotelCommunicationWizardPage hotelCommunicationWizardPage;
	private HotelAdditionalInformationWizardPage hotelAdditionalInformationWizardPage;
	private List<HotelRoomDefinitionWizardPage> hotelRoomDefinitionWizardPages = CollectionsHelper.createArrayList();

	private Hotel hotel;

	// **************************************************************************
	// * Constructors
	// *

	public CreateHotelWizard(CountryCity countryCity) throws Exception {
		this.hotel = HotelModel.getInitialHotel();
		if (countryCity != null) {
			Address address = hotel.getMainAddress();
			address.setCity(countryCity.getCity());
			address.setCountryPK(countryCity.getCountryCode());
		}
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {

		// Page 1: Names
		hotelNamesWizardPage = new HotelNamesWizardPage(hotel);
		addPage(hotelNamesWizardPage);

		// Page 2: Address
		hotelAddressWizardPage = new HotelAddressWizardPage(hotel);
		addPage(hotelAddressWizardPage);

		// Page 3: Communication
		hotelCommunicationWizardPage = new HotelCommunicationWizardPage(hotel);
		addPage(hotelCommunicationWizardPage);

		// Page 4: Additional Information
		hotelAdditionalInformationWizardPage = new HotelAdditionalInformationWizardPage(hotel);
		addPage(hotelAdditionalInformationWizardPage);

		// More pages are created dynamically upon pressing the special button
	}


	/**
	 * The wizard can be finished as soon as name1, country and city are given.
	 * <p>
	 * However, if hotel room definitions were requested by the special button, those must be completed,
	 * which currently means they must have a room type and a name. However, we don't need to know
	 * precisely, because we rely on the validation logic.
	 *
	 */
	@Override
	public boolean canFinish() {
		boolean allRoomDefinitionsValid = true;
		for (HotelRoomDefinitionWizardPage page : hotelRoomDefinitionWizardPages) {
			try {
				RoomDefinitionVO roomDefinitionVO = page.getRoomDefinitionVO();
				/* Set a temporary dummy value for hotelPK, so that we can use the validation logic
				 * of the VO to check whether everything needed has been selected and entered.
				 */
				roomDefinitionVO.setHotelPK(0L);
				roomDefinitionVO.validate();
				roomDefinitionVO.setHotelPK(null);
			}
			catch (InvalidValuesException e) {
				allRoomDefinitionsValid = false;
				break;
			}
		}

		boolean result =
			allRoomDefinitionsValid &&
			hotelNamesWizardPage.isPageComplete() &&
			hotelAddressWizardPage.isPageComplete();

		return result;
	}


	/**
	 * Upon finishing, the hotel and room definitions are created. Should an error occur, the error
	 * gets shown and the wizard remains open, otherweise the hotel is shown in the hotel view.
	 */
	@Override
	public boolean performFinish() {

		StringBuilder summaryMessage = new StringBuilder();

		try {
			// Page 1: Names
			hotelNamesWizardPage.syncEntityToWidgets();

			// Page 2: Address
			hotelAddressWizardPage.syncEntityToWidgets();

			// Page 3: Communication
			hotelCommunicationWizardPage.syncEntityToWidgets();

			// Page 4: Additional Information
			hotelAdditionalInformationWizardPage.syncEntityToWidgets();

			hotel = HotelModel.getInstance().create(hotel);

			// open HotelEditor
			openHotelEditor();

			String message = buildCreateEntitySuccessMessage(HotelLabel.Hotel, hotel.getName());
			summaryMessage.append(message);
		}
		catch (Exception e) {
			String message = buildCreateEntityErrorMessage(HotelLabel.Hotel, hotel.getName());
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, message);
			return false;
		}


		// Page 5: Room Type is stored in same roomDefinitionVO object which we obtain
		// filled with additional data from next page

		// Page 6: Room Definitions
		for (HotelRoomDefinitionWizardPage  hotelRoomDefinitionWizardPage : hotelRoomDefinitionWizardPages) {
			RoomDefinitionVO roomDefinitionVO = hotelRoomDefinitionWizardPage.getRoomDefinitionVO();
			roomDefinitionVO.setHotelPK(hotel.getID());

			try {
				RoomDefinitionModel.getInstance().create(roomDefinitionVO);

				String message = buildCreateEntitySuccessMessage(HotelLabel.RoomDefinition, roomDefinitionVO.getName().getString());
				summaryMessage.append(message);
			}
			catch (Exception e) {
				String message = buildCreateEntityErrorMessage(HotelLabel.RoomDefinition, roomDefinitionVO.getName().getString());
				summaryMessage.append(message);

				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, summaryMessage.toString());
				return false;
			}
		}

		MessageDialog.openInformation(getShell(), UtilI18N.Success, summaryMessage.toString());

		return true;
	}


	/**
	 * Open editor when hotel has been successully created
	 */
	public void openHotelEditor() {
		if (isHasCreatedHotel()) {
			HotelEditorInput editorInput = new HotelEditorInput(hotel.getID());
			try {
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				activePage.openEditor(editorInput, HotelEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public String getWindowTitle() {
		return I18N.CreateHotel;
	}


	// **************************************************************************
	// * Helper Methods
	// *

	/**
	 * Creates a pair of pages used for one room definition. Is called from the
	 * wizard in case the special button "Add room definition" is pressed.
	 */
	void createRoomDefinitionPages() {
		int index = hotelRoomDefinitionWizardPages.size();
		RoomDefinitionVO roomDefinitionVO = new RoomDefinitionVO();

		// Page 5+2*index: Room Type
		HotelRoomTypeSelectionWizardPage hotelRoomTypeSelectionWizardPage = new HotelRoomTypeSelectionWizardPage(roomDefinitionVO, index);
		addPage(hotelRoomTypeSelectionWizardPage);

		// Page 6+2*index: Room Definition
		HotelRoomDefinitionWizardPage hotelRoomDefinitionWizardPage = new HotelRoomDefinitionWizardPage(roomDefinitionVO, index);
		addPage(hotelRoomDefinitionWizardPage);
		hotelRoomDefinitionWizardPages.add(hotelRoomDefinitionWizardPage);
	}

	private String buildCreateEntityErrorMessage(I18NString entity, String name) {
		String message = I18N.CreateEntityErrorMessage;
		message = message.replaceFirst("<entity>", entity.getString());
		message = message.replaceFirst("<name>", name);
		return message;
	}

	private String buildCreateEntitySuccessMessage(I18NString entity, String name) {
		String message = I18N.CreateEntitySuccessMessage;
		message = message.replaceFirst("<entity>", entity.getString());
		message = message.replaceFirst("<name>", name);
		return message;
	}


	public boolean isHasCreatedHotel() {
		return hotel.getID() != null;
	}

}

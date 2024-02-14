package de.regasus.hotel.contingent.dialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.common.CountryCity;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.hotel.contingent.editor.HotelContingentEditor;
import de.regasus.hotel.contingent.editor.HotelContingentEditorInput;
import de.regasus.hotel.dialog.CreateHotelWizard;
import de.regasus.hotel.dialog.HotelAdditionalInformationWizardPage;
import de.regasus.hotel.dialog.HotelAddressWizardPage;
import de.regasus.hotel.dialog.HotelCommunicationWizardPage;
import de.regasus.hotel.dialog.HotelNamesWizardPage;
import de.regasus.hotel.dialog.HotelRoomDefinitionWizardPage;
import de.regasus.hotel.dialog.HotelRoomTypeSelectionWizardPage;
import de.regasus.ui.Activator;

/**
 *
 * This assistent helps to create a new hotel contingent. It's initiating issue is MIRCP-1536.
 * <p>
 * The creation may happen for an existing hotel, but a new hotel including room definitions may be created as well.
 * <p>
 * First the wizard collects the required data. Upon finishing, the hotel and room definitions are created (if required)
 * and afterwards the hotel contingent. Should any of the creation of the hotel or the hotel contingent fail, an error
 * is shown and the wizard remains open. Should the hotel have been created, the according pages are to be * removed.
 * <p>
 * The wizard replaces the old way of creating a hotel contingent and is started via the earlier used menu items of the
 * event tree view.
 * <p>
 * The pages are:
 * <ol>
 * <li>Hotel selection <br>
 * This page corresponds to the hotel search from MIRCP-1527, with search fields "Country", "City" and "Name 1" already
 * filled in. Either the country is taken from the event or, if a hotel or contingent is selected, together with city
 * and name 1 from that hotel. <br>
 * Opening the page executes the search with the pre-filled values. The button "Next" is only active when a hotel is selected. There is also a special button to create a new hotel
 * alternatively. Pressing this button opens pages used for the assistent for a new hotel (MIRCP-1535). On its page for
 * room definitions, the "Next" button leads to the second page of this assistent.
 *
 * <li>Name <br>
 * Contains the fields for name, (used) room definitions (a table), and provision. The name is prefilled with the
 * hotel's name.
 *
 * <li>Capacity <br>
 * Contains the fields for first and last night as well as the capacity table
 *
 * <li>Additional information. <br>
 * Contains the fields for hotel info, guest info and note.
 * </ol>
 * The positions is one above the highest position of the other contingents of the same hotel in the same event.
 *
 */
public class CreateHotelContingentWizard extends Wizard {

	// **************************************************************************
	// * Wizard Pages
	// *

	private HotelNamesWizardPage hotelNamesWizardPage;
	private HotelAddressWizardPage hotelAddressWizardPage;
	private HotelCommunicationWizardPage hotelCommunicationWizardPage;
	private HotelAdditionalInformationWizardPage hotelAdditionalInformationWizardPage;
	private ArrayList<HotelRoomDefinitionWizardPage> hotelRoomDefinitionWizardPages = new ArrayList<>();

	/**
	 * The hotel selected on the first page, or the created hotel
	 */
	private Hotel hotel;

	private boolean creatingHotel = false;

	private CountryCity countryCity;
	private String name1;


	private HotelContingentCVO hotelContingentCVO;
	private HotelSearchWizardPage hotelSearchWizardPage;
	private HotelContingentNameRoomDefinitionsWizardPage hotelContingentNameWizardPage;
	private HotelContingentCapacityWizardPage hotelContingentCapacityWizardPage;

	private HotelContingentInfoWizardPage hotelContingentInfoWizardPage;


	// **************************************************************************
	// * Constructors
	// *

	public CreateHotelContingentWizard(CountryCity countryCity, String name1, EventVO eventVO) {
		this.countryCity = countryCity;
		this.name1 = name1;

		HotelContingentVO hotelContingentVO = new HotelContingentVO();
		hotelContingentVO.setEventPK(eventVO.getPK());

		hotelContingentCVO = new HotelContingentCVO();
		hotelContingentCVO.setHotelContingentVO(hotelContingentVO);
		hotelContingentCVO.createVolumesForEvent(eventVO);
		hotelContingentCVO.setRoomDefinitionPKs(new ArrayList<Long>());
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {

		// Page 1: Auswahl eines Hotels
		hotelSearchWizardPage = new HotelSearchWizardPage(countryCity, name1);
		addPage(hotelSearchWizardPage);

		hotelContingentNameWizardPage = new HotelContingentNameRoomDefinitionsWizardPage(hotelContingentCVO);
		addPage(hotelContingentNameWizardPage);

		// Page 3: Capacity, enthält die Felder für die erst und letzte Nacht sowie die Tabelle Umfang.
		hotelContingentCapacityWizardPage = new HotelContingentCapacityWizardPage(hotelContingentCVO);
		addPage(hotelContingentCapacityWizardPage);

		// Page 4: Additional Information, enthält die Felder Hotelinfo, Gastinfo und Notiz.
		hotelContingentInfoWizardPage = new HotelContingentInfoWizardPage(hotelContingentCVO);
		addPage(hotelContingentInfoWizardPage);
	}


	@Override
	public boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();

		return
			currentPage == hotelContingentCapacityWizardPage ||
			currentPage == hotelContingentInfoWizardPage;
	}



	@Override
	public boolean performFinish() {

		StringBuilder summaryMessage = new StringBuilder();

		hotelContingentNameWizardPage.syncEntityToWidgets();
		hotelContingentCapacityWizardPage.syncEntityToWidgets();
		hotelContingentInfoWizardPage.syncEntityToWidgets();

		try {
			// Create the hotel if needed, but if it fails, do not close the wizard.
			if (hotel.getID() == null) {

				// Page 1: Names
				hotelNamesWizardPage.syncEntityToWidgets();

				// Page 2: Address
				hotelAddressWizardPage.syncEntityToWidgets();

				// Page 3: Communication
				hotelCommunicationWizardPage.syncEntityToWidgets();

				// Page 4: Additional Information
				hotelAdditionalInformationWizardPage.syncEntityToWidgets();

				hotel = HotelModel.getInstance().create(hotel);

				System.out.println("Created hotel: " + hotel.getName());

				String message = buildCreateEntitySuccessMessage(HotelLabel.Hotel, hotel.getName());
				summaryMessage.append(message);
			}
		}

		catch (Exception e) {
			String message = buildCreateEntityErrorMessage(HotelLabel.Hotel, hotel.getName());
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, message);
			return false;
		}




		// In any case, we have now a (created or existing) hotel

		// Create only those that have not yet been created
		for (HotelRoomDefinitionWizardPage  hotelRoomDefinitionWizardPage : hotelRoomDefinitionWizardPages) {
			RoomDefinitionVO roomDefinitionVO = hotelRoomDefinitionWizardPage.getRoomDefinitionVO();

			// Attention, difficult problem ahead:
			// When we want to create the contingent later on, it already has references to some of
			// these room definitions which it want to use. There, they have only a negative dummy PK
			// We have to replace each used room definition's PK in the contingent by the respective
			// PK of the created room definition in data base.
			Long roomDefinitionPK = roomDefinitionVO.getPK();
			Collection<Long> contingentRoomDefinitionPKs = hotelContingentCVO.getRoomDefinitionPKs();
			List<RoomDefinitionVO> allRoomDefinitionVOs = hotelContingentNameWizardPage.getAllRoomDefinitionVOs();

			// Negative value means dummy, not yet in DB, have to create and replace in the contingent
			if (roomDefinitionPK.longValue() < 0) {

				int indexOfRoomDefinitionInTableOfAllRoomDefinitions = allRoomDefinitionVOs.indexOf(roomDefinitionVO);

				roomDefinitionVO.setHotelPK(hotel.getID());
				roomDefinitionVO.setID(null);

				try {
					roomDefinitionVO = RoomDefinitionModel.getInstance().create(roomDefinitionVO);

					String message = buildCreateEntitySuccessMessage(HotelLabel.RoomDefinition, roomDefinitionVO.getName().getString());
					summaryMessage.append(message);
				}
				catch (Exception e) {
					String message = buildCreateEntityErrorMessage(HotelLabel.RoomDefinition, roomDefinitionVO.getName().getString());
					summaryMessage.append(message);

					RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, summaryMessage.toString());
					return false;
				}


				System.out.println("Replacing roomDefinitinPK " + roomDefinitionPK + " by " + roomDefinitionVO.getPK());
				if (contingentRoomDefinitionPKs.contains(roomDefinitionPK)) {
					contingentRoomDefinitionPKs.remove(roomDefinitionPK);
					contingentRoomDefinitionPKs.add(roomDefinitionVO.getPK());
				}

				if (indexOfRoomDefinitionInTableOfAllRoomDefinitions > -1) {
					allRoomDefinitionVOs.set(indexOfRoomDefinitionInTableOfAllRoomDefinitions, roomDefinitionVO);
				}

				hotelRoomDefinitionWizardPage.setRoomDefinitionVO(roomDefinitionVO);
			}
		}

		// Create the contingent (with room definitions having IDs!) and open its editor
		try {
			hotelContingentCVO.getVO().setHotelPK(hotel.getID());
			hotelContingentCVO = HotelContingentModel.getInstance().create(hotelContingentCVO);
			String message = buildCreateEntitySuccessMessage(HotelLabel.HotelContingent, hotelContingentCVO.getHcName());
			summaryMessage.append(message);
		}
		catch (Exception e) {
			String message = buildCreateEntityErrorMessage(HotelLabel.HotelContingent, hotelContingentCVO.getHcName());
			summaryMessage.append(message);

			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, summaryMessage.toString());
			return false;
		}

		MessageDialog.openInformation(getShell(), UtilI18N.Success, summaryMessage.toString());

		HotelContingentEditorInput editorInput = new HotelContingentEditorInput(hotelContingentCVO.getPK());
		try {
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			activePage.openEditor(editorInput, HotelContingentEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return true;
	}


	@Override
	public String getWindowTitle() {
		return I18N.CreateHotelContingent;
	}


	// **************************************************************************
	// * Helper Methods
	// *

	public ArrayList<HotelRoomDefinitionWizardPage> getHotelRoomDefinitionWizardPages() {
		return hotelRoomDefinitionWizardPages;
	}


	/**
	 * The super class {@link Wizard} contains a List of pages as private attribute (which is invisible to us),
	 * and only offers the method addPage() for adding additional pages at the end. Which is sufficient for
	 * eg the {@link CreateHotelWizard}.
	 * <p>
	 * But the requirement for this wizard is to add additional pages not only at the end,
	 * but also inbetween, when a hotel is to be created before the contingent.
	 * <p>
	 * So here comes a little bit of black magic: Using reflection to access the superclass' field, making
	 * it accessible and then call it's method.
	 */
	void addPage(int index, IWizardPage page) {
		try {
			// Using reflection to access the superclass' field and making it accessible
			Field field = Wizard.class.getDeclaredField("pages");
			field.setAccessible(true);
			ArrayList<IWizardPage> pages = (ArrayList<IWizardPage>) field.get(this);

			// Voilà, we have the field, now the adapted logic of the addPage() method.
			pages.add(index, page);
			page.setWizard(this);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	/**
	 * Creates a set of pages used for a hotel definition. Is called from the
	 * wizard in case the first special button "Create hotel" is pressed.
	 * <p>
	 * If this is not done, the wizard will take the selected hotel on
	 * the HotelSearchWizardPage
	 * @throws Exception
	 */
	void createHotelDefinitionPages() throws Exception {
		hotel = HotelModel.getInitialHotel();
		if (countryCity != null) {
			Address address = hotel.getMainAddress();
			address.setCity(countryCity.getCity());
			address.setCountryPK(countryCity.getCountryCode());
		}

		// Hotel Page 1: Names
		hotelNamesWizardPage = new HotelNamesWizardPage(hotel);
		addPage(1, hotelNamesWizardPage);

		// Hotel Page 2: Address
		hotelAddressWizardPage = new HotelAddressWizardPage(hotel);
		addPage(2, hotelAddressWizardPage);

		// Hotel Page 3: Communication
		hotelCommunicationWizardPage = new HotelCommunicationWizardPage(hotel);
		addPage(3, hotelCommunicationWizardPage);

		// Hotel Page 4: Additional Information
		hotelAdditionalInformationWizardPage = new HotelAdditionalInformationWizardPage(hotel);
		addPage(4, hotelAdditionalInformationWizardPage);

		// More pages are created dynamically upon pressing the second special button

		creatingHotel = true;
	}


	/**
	 * Creates a pair of pages used for one room definition. Is called from the
	 * wizard in case the second special button "Add room definition" is pressed.
	 */
	void createRoomDefinitionPages() {
		int index = hotelRoomDefinitionWizardPages.size();
		RoomDefinitionVO roomDefinitionVO = new RoomDefinitionVO();

		// Hotel Page 5+2*index: Room Type
		HotelRoomTypeSelectionWizardPage hotelRoomTypeSelectionWizardPage = new HotelRoomTypeSelectionWizardPage(roomDefinitionVO, index);
		addPage(5 + 2*index, hotelRoomTypeSelectionWizardPage);

		// Hotel Page 6+2*index: Room Definition
		HotelRoomDefinitionWizardPage hotelRoomDefinitionWizardPage = new HotelRoomDefinitionWizardPage(roomDefinitionVO, index);
		addPage(6 + 2*index, hotelRoomDefinitionWizardPage);

		hotelRoomDefinitionWizardPages.add(hotelRoomDefinitionWizardPage);
	}


	boolean isCreatingHotel() {
		return creatingHotel;
	}

	boolean isHasCreatedHotel() {
		return creatingHotel && hotel != null && hotel.getID() != null;
	}

	/**
	 * The initially suggested name of a hotel contingent is that of default name of
	 */
	void copyHotelNameToHotelContingentCVO() {
		hotelContingentCVO.getVO().setName(hotel.getName1());
	}


	/**
	 * Called by search wizard page when a hotel is selected by the user, or when the search
	 * produces the initial result
	 */
	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}


	/**
	 * Called by the managing dialog when the user has selected a hotel and switches
	 * to the page to enter name and select room definitions
	 */
	public void useSearchedHotelForContingent() {
		hotelContingentCVO.getVO().setHotelPK(hotel.getID());
		hotelContingentCVO.getVO().setName(hotel.getName1());
		hotelContingentNameWizardPage.setRoomDefinitionVOs(null);
	}


	private String buildCreateEntityErrorMessage(I18NString entity, String name) {
		String message = I18N.CreateEntityErrorMessage;
		message = message.replaceFirst("<entity>", entity.getString());
		message = message.replaceFirst("<name>", StringHelper.avoidNull(name));
		return message;
	}

	private String buildCreateEntitySuccessMessage(I18NString entity, String name) {
		String message = I18N.CreateEntitySuccessMessage;
		message = message.replaceFirst("<entity>", entity.getString());
		message = message.replaceFirst("<name>", StringHelper.avoidNull(name));
		return message;
	}

}

package de.regasus.hotel.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;

import de.regasus.I18N;

/**
 * See requirement description in {@link CreateHotelWizard}
 */
public class CreateHotelWizardDialog extends WizardDialog {
	
	// IDs to discriminate custom added button
	private static final int ADD_ROOM_DEFINITION_BUTTON_ID = IDialogConstants.CLIENT_ID + 1;

	// custom added button
	private Button addRoomDefinitionButton;
	
	// The wizard contained in this dialog
	private CreateHotelWizard createHotelWizard;
	
	
	// **************************************************************************
	// * Constructors
	// *

	public CreateHotelWizardDialog(Shell parentShell, CreateHotelWizard createHotelWizard) {
		super(parentShell, createHotelWizard);
		
		this.createHotelWizard = createHotelWizard;
	}

	
	// **************************************************************************
	// * Overriden Methods
	// *
	
	/** 
	 * The navigation area shows besides "Next" an additional button reading "Additional room definition". 
	 * <p>
	 * To realize this requirement, we hook into the moment before the finish button will be created.
	 *  
	 * The common way to add Buttons is overriding createButtonsForButtonBar(Composite parent).
	 * But this would add the additional Buttons to the left of the Back-Button. 
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.FINISH_ID) {
			addRoomDefinitionButton = super.createButton(
				parent, 
				ADD_ROOM_DEFINITION_BUTTON_ID, 
				I18N.CreateHotel_AdditionalRoomDefinition, 
				false
			);
			addRoomDefinitionButton.setVisible(false);
		}

		// create the actual FINISH button
		return super.createButton(parent, id, label, defaultButton);
	}

	
	/**
	 * Pressing the special button, we tell the wizard to create a new pair of pages 5 and 6, 
	 * and also navigate to them.
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == ADD_ROOM_DEFINITION_BUTTON_ID) {
			createHotelWizard.createRoomDefinitionPages();
			nextPressed();
		}
		else {
			super.buttonPressed(buttonId);
		}
	}


	/**
	 * The special button to create a new pair of pages 5 and 6 is only visible on the last page.
	 */
	@Override
	public void updateButtons() {
		super.updateButtons();
		
		// make addRoomDefinitionButton visible if the current page is the last one
		IWizardPage nextPage = createHotelWizard.getNextPage(getCurrentPage());
		addRoomDefinitionButton.setVisible(null == nextPage);
			
		IWizardPage currentPage = getCurrentPage();
		IWizardPage previousPage = currentPage.getPreviousPage();
	
		boolean canGoBack = true;
		
		/* We don't allow to go back to any hotel page if the hotel has been created.
		 * This may happen if an error occurs during saving room definitions, because this is not
		 * done in a transaction, but one by one.
		 */
		if (createHotelWizard.isHasCreatedHotel()
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
		if (createHotelWizard.isHasCreatedHotel() 
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

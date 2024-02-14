package de.regasus.hotel.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.Communication;
import com.lambdalogic.messeinfo.hotel.Hotel;

import de.regasus.common.composite.CommunicationGroup;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class HotelCommunicationWizardPage extends WizardPage {

	// **************************************************************************
	// * Attributes and Widgets
	// *

	private CommunicationGroup communicationGroup;
	private Communication communication;


	// **************************************************************************
	// * Constructors
	// *

	public HotelCommunicationWizardPage(Hotel hotel) {
		super(HotelCommunicationWizardPage.class.getName());

		communication = hotel.getCommunication();
	}

	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void createControl(Composite parent) {
		try {
    		communicationGroup = new CommunicationGroup(parent,	SWT.NONE);
    		communicationGroup.setCommunication(communication);

    		setTitle(Hotel.COMMUNICATION.getString());
    		setControl(communicationGroup);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	public void syncEntityToWidgets() {
		communicationGroup.syncEntityToWidgets();
	}

}

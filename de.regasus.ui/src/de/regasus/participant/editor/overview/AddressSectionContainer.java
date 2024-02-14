package de.regasus.participant.editor.overview;

import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.EntityNotFoundException;

import de.regasus.common.AddressRole;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class AddressSectionContainer
extends AbstractSectionContainer 
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;
	
	private ParticipantModel participantModel;
	
	private AddressConfigParameterSet adrConfigParameterSet;
	
	private boolean ignoreCacheModelEvents = false;
	
		
	public AddressSectionContainer(
		FormToolkit formToolkit, 
		Composite body, 
		Long participantID,
		AddressConfigParameterSet addressConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);
		
		this.participantID = participantID;
		this.adrConfigParameterSet = addressConfigParameterSet;

		addDisposeListener(this);
		
		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this, participantID);
		
		refreshSection();
	}

	
	@Override
	protected String getTitle() {
		return ContactLabel.addresses.getString();
	}
	
	
	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

    		Participant participant = participantModel.getParticipant(participantID);
    
    		
    		// set 2nd person before generating address labels
			Long secondPersonID = participant.getSecondPersonID();
			Participant secondPerson = null;
			if (secondPersonID != null) {
				try {
					secondPerson = ParticipantModel.getInstance().getParticipant(secondPersonID);
				}
				catch (EntityNotFoundException e) {
					// ignore
				}
			}
			participant.setSecondPerson(secondPerson);

			
    		ArrayList<Address> addressList = participant.getAddressList();
    		
    		// set visible if at least 1 address exists that is not empty
    		boolean anyAddressNotEmpty = false;
    		for (Address address : addressList) {
    			if ( ! address.isEmpty()) {
    				anyAddressNotEmpty = true;
    				break;
    			}
    		}
    		
    		boolean visible =
    			(adrConfigParameterSet == null || adrConfigParameterSet.isVisible()) &&
    			anyAddressNotEmpty;
    	
    		
    		setVisible(visible);
    
    		if (visible) {
    			for (int addressNumber = 1; addressNumber <= Participant.ADDRESS_COUNT ; addressNumber++) {
        			if (adrConfigParameterSet != null && adrConfigParameterSet.getAddress(addressNumber).isVisible()) {
        				Address address = participant.getAddress(addressNumber);
        				if ( ! address.isEmpty()) {
            				boolean isMainAddress = addressNumber == participant.getMainAddressNumber();
        					boolean isInvoiceAddress = addressNumber == participant.getInvoiceAddressNumber();
        					
        					String label = AddressRole.getAddressRoleName(
            					addressNumber, 
            					isMainAddress, 
            					isInvoiceAddress
            				);
            
            				addIfNotEmpty(label, participant.getAddressLabel(addressNumber));
        				}
        			}
    			}			
    		}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}
	

	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (participantModel != null && participantID != null) {
			try {
				participantModel.removeListener(this, participantID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}


/**
 * AddressSectionContainer.java
 * created on 06.08.2013 09:13:32
 */
package de.regasus.profile.editor.overview;

import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.common.AddressRole;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

public class AddressSectionContainer 
extends AbstractSectionContainer 
implements CacheModelListener<Long>, DisposeListener {
	
	private Long profileID;
	
	private ProfileModel profileModel;
	
	private AddressConfigParameterSet adrConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;
	
	
	public AddressSectionContainer(
		FormToolkit formToolkit, 
		Composite body,
		Long profileID,
		AddressConfigParameterSet addressConfigParameterSet
	) 
	throws Exception {
		super(formToolkit, body);
		
		this.profileID = profileID;
		this.adrConfigParameterSet = addressConfigParameterSet;
		
		addDisposeListener(this);

		profileModel = ProfileModel.getInstance();
		profileModel.addListener(this, profileID);
		
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

    		Profile profile = profileModel.getProfile(profileID);

    		
    		// set 2nd person before generating address labels
			Long secondPersonID = profile.getSecondPersonID();
			Profile secondPerson = null;
			if (secondPersonID != null) {
				secondPerson = ProfileModel.getInstance().getProfile(secondPersonID);
			}
			profile.setSecondPerson(secondPerson);
    		
    		
    		ArrayList<Address> addressList = profile.getAddressList();
    		
    		// set visible if at least 1 address exists that is not empty
    		boolean anyAddressNotEmpty = false;
    		for (Address address : addressList) {
    			if (!address.isEmpty()) {
    				anyAddressNotEmpty = true;
    				break;
    			}
    		}
    		
    		boolean visible =
    			(adrConfigParameterSet == null || adrConfigParameterSet.isVisible()) &&
    			anyAddressNotEmpty;
    		
    		setVisible(visible);
    		
    		if (visible) {
    			for (int addressNumber = 1; addressNumber <= Profile.ADDRESS_COUNT; addressNumber++) {
    				if (adrConfigParameterSet != null && adrConfigParameterSet.getAddress(addressNumber).isVisible()) {
    					Address address = profile.getAddress(addressNumber);
    					if (!address.isEmpty()) {
    						boolean isMainAddress = addressNumber == profile.getMainAddressNumber();
    						boolean isInvoiceAddress = addressNumber == profile.getInvoiceAddressNumber();
    						
    						String label = AddressRole.getAddressRoleName(
    							addressNumber, 
    							isMainAddress, 
    							isInvoiceAddress
    						);
    						
    						addIfNotEmpty(label, profile.getAddressLabel(addressNumber));
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
		if (profileModel != null && profileID != null) {
			try {
				profileModel.removeListener(this, profileID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}	
	
}

package de.regasus.hotel.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import de.regasus.common.Country;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.util.StringHelper;

import de.regasus.common.composite.AddressGroup;
import de.regasus.common.composite.AddressGroupsComposite;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class HotelAddressWizardPage extends WizardPage {

	// **************************************************************************
	// * Attributes and Widgets
	// *

	private AddressGroup addressGroup;
	private Address address;
	private Hotel hotel;


	// **************************************************************************
	// * Constructors
	// *

	public HotelAddressWizardPage(Hotel hotel) {
		super(HotelAddressWizardPage.class.getName());

		this.address = hotel.getMainAddress();
		this.hotel = hotel;
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void createControl(Composite parent) {

		try {
			addressGroup = new AddressGroup(
				parent,
				SWT.NONE,
				(AddressConfigParameterSet) null, // TODO: Use as soon as exist for hotels
				1,
				(AddressGroupsComposite) null, // only in editor
				(Address) null	// groupManagerAddress, here not relevant
			);

			addressGroup.setHomeCountryPK( PropertyModel.getInstance().getDefaultCountry() );
			addressGroup.setAddress(address);
			addressGroup.setAbstractPerson(hotel);
			addressGroup.setCountryBold(true);
			addressGroup.setCityBold(true);
			addressGroup.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					setPageComplete(isPageComplete());
					addressGroup.refreshDefaultAddressLabel();
				}
			});

			setTitle(ContactLabel.Address.getString());
			setControl(addressGroup);
		}
		catch (Exception e) {
			// The ONLY exception that can take place is that of the CountryCombo that
			// wants upon creation sync itself with. Maybe this might be suppressed?
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			throw new RuntimeException(e);
		}
	}


	@Override
	public boolean isPageComplete() {
		String city = addressGroup.getCity();
		Country country = addressGroup.getCountry();
		return country != null && StringHelper.isNotEmpty(city);
	}


	public void syncEntityToWidgets() {
		addressGroup.syncEntityToWidgets();
	}

}

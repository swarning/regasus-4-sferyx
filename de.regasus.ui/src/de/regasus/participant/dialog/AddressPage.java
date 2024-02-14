package de.regasus.participant.dialog;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.AddressConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.AbstractPerson;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

import de.regasus.common.composite.AddressGroup;
import de.regasus.core.ConfigParameterSetModel;


public class AddressPage extends WizardPage {

	public static final String NAME = "AddressPage";
	private AddressGroup addressGroup;
	private Address groupManagerAddress;
	private AbstractPerson abstractPerson;
	private int addressNumber;
	private String homeCountryPK;


	public AddressPage(
		String addressType,
		AbstractPerson abstractPerson,
		int addressNumber,
		Address groupManagerAddress,
		String homeCountryPK
	) {
		super(NAME + "_" + addressType);

		this.abstractPerson = abstractPerson;
		this.addressNumber = addressNumber;
		this.groupManagerAddress = groupManagerAddress;
		this.homeCountryPK = homeCountryPK;
	}


	@Override
	public void createControl(Composite parent) {
		try {
			AddressConfigParameterSet parameter = null;

			if (abstractPerson instanceof IParticipant) {
				IParticipant participant = (IParticipant) abstractPerson;
				ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();
				Long eventPK = participant.getEventId();
				ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
				ParticipantConfigParameterSet participantConfigParameterSet = configParameterSet.getEvent().getParticipant();
				parameter = participantConfigParameterSet.getAddress();
			}
			else {
				// Extend once the Address Page is used for non-participants and configs
			}


			addressGroup = new AddressGroup(parent, SWT.NONE, parameter, addressNumber, null, groupManagerAddress);
			addressGroup.setHomeCountryPK(homeCountryPK);
			addressGroup.setAbstractPerson(abstractPerson);
			addressGroup.setText(getTitle());
			setControl(addressGroup);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public AddressGroup getAddressGroup() {
		return addressGroup;
	}


	public void syncEntityToWidgets() {
		addressGroup.syncEntityToWidgets();
	}


	public void refreshDefaultAddressLabel() {
		addressGroup.refreshDefaultAddressLabel();
	}

}

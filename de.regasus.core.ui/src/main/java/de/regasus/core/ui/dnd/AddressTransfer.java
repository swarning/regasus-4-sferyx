package de.regasus.core.ui.dnd;

import com.lambdalogic.messeinfo.contact.Address;

public class AddressTransfer extends GenericTransfer<Address> {

	private static final AddressTransfer INSTANCE = new AddressTransfer();

	public static AddressTransfer getInstance() {
		return INSTANCE;
	}

}

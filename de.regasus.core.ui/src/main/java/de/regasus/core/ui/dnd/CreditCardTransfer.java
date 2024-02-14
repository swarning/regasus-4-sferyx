package de.regasus.core.ui.dnd;

import com.lambdalogic.messeinfo.contact.CreditCard;

public class CreditCardTransfer extends GenericTransfer<CreditCard> {

	private static final CreditCardTransfer INSTANCE = new CreditCardTransfer();

	public static CreditCardTransfer getInstance() {
		return INSTANCE;
	}

}

package de.regasus.portal.page.editor.dnd;

import de.regasus.core.ui.dnd.GenericTransfer;
import de.regasus.portal.IdProvider;


public class IdProviderTransfer extends GenericTransfer<IdProvider> {

	private static final IdProviderTransfer INSTANCE = new IdProviderTransfer();

	public static IdProviderTransfer getInstance() {
		return INSTANCE;
	}

}

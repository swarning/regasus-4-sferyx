package de.regasus.document;

import de.regasus.common.GlobalFile;

public class GlobalImprintDocumentModel extends DocumentModel {

	private static GlobalImprintDocumentModel singleton = null;


	private GlobalImprintDocumentModel() {
		super(GlobalFile.IMPRINT);
	}


	public static GlobalImprintDocumentModel getInstance() {
		if (singleton == null) {
			singleton = new GlobalImprintDocumentModel();
		}
		return singleton;
	}

}

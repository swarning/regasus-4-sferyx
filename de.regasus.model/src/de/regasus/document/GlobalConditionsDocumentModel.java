package de.regasus.document;

import de.regasus.common.GlobalFile;

public class GlobalConditionsDocumentModel extends DocumentModel {

	private static GlobalConditionsDocumentModel singleton = null;


	private GlobalConditionsDocumentModel() {
		super(GlobalFile.CONDITIONS);
	}


	public static GlobalConditionsDocumentModel getInstance() {
		if (singleton == null) {
			singleton = new GlobalConditionsDocumentModel();
		}
		return singleton;
	}

}

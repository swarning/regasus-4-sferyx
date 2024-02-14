package de.regasus.document;

import de.regasus.common.GlobalFile;

public class GlobalPrivacyPolicyDocumentModel extends DocumentModel {

	private static GlobalPrivacyPolicyDocumentModel singleton = null;


	private GlobalPrivacyPolicyDocumentModel() {
		super(GlobalFile.PRIVACY_POLICY);
	}


	public static GlobalPrivacyPolicyDocumentModel getInstance() {
		if (singleton == null) {
			singleton = new GlobalPrivacyPolicyDocumentModel();
		}
		return singleton;
	}

}

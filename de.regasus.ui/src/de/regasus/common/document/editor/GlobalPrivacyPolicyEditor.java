package de.regasus.common.document.editor;

import de.regasus.document.GlobalPrivacyPolicyDocumentModel;

/**
 * Editor used to manage global privacy policy documents.
 * The document which is opened by this editor will be stored as File with the
 * internal path <b>/global/privacyPolicy/...</b>
 */
public class GlobalPrivacyPolicyEditor extends DocumentEditor {

	public static final String ID = "GlobalPrivacyPolicyEditor";


	public GlobalPrivacyPolicyEditor() {
		super( GlobalPrivacyPolicyDocumentModel.getInstance() );
	}

}

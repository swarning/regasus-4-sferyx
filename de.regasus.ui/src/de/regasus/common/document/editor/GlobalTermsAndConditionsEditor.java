package de.regasus.common.document.editor;

import de.regasus.document.GlobalConditionsDocumentModel;

/**
 * Editor used to manage global terms And conditions documents.
 * The document which is opened by this editor will be stored as File with the
 * internal path <b>/global/termsAndConditions/...</b>
 */
public class GlobalTermsAndConditionsEditor extends DocumentEditor {

	public static final String ID = "GlobalTermsAndConditionsEditor";


	public GlobalTermsAndConditionsEditor() {
		super( GlobalConditionsDocumentModel.getInstance() );
	}

}

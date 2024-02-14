package de.regasus.common.document.editor;

import de.regasus.document.GlobalImprintDocumentModel;

/**
 * Editor used to manage global imprint documents.
 * The document which is opened by this editor will be stored as File with the
 * internal path <b>/global/imprint/...</b>
 */
public class GlobalImprintEditor extends DocumentEditor {

	public static final String ID = "GlobalImprintEditor";


	public GlobalImprintEditor() {
		super( GlobalImprintDocumentModel.getInstance() );
	}

}

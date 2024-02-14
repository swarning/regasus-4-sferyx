package de.regasus.common.document.editor;

import de.regasus.I18N;
import de.regasus.core.ui.editor.SimpleEditorInput;

public class GlobalTermsAndConditionsEditorInput extends SimpleEditorInput {

	public GlobalTermsAndConditionsEditorInput() {
		super(
			null, // imageDescriptor,
			I18N.GlobalTermsAndConditionsEditor_Text,
			I18N.GlobalTermsAndConditionsEditor_ToolTip
		);
	}

}

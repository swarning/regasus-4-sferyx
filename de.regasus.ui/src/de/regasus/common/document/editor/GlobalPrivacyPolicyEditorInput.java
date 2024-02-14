package de.regasus.common.document.editor;

import de.regasus.I18N;
import de.regasus.core.ui.editor.SimpleEditorInput;

public class GlobalPrivacyPolicyEditorInput extends SimpleEditorInput {

	public GlobalPrivacyPolicyEditorInput() {
		super(
			null, // imageDescriptor,
			I18N.GlobalPrivacyPolicyEditor_Text,
			I18N.GlobalPrivacyPolicyEditor_ToolTip
		);
	}

}

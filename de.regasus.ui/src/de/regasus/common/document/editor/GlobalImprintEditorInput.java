package de.regasus.common.document.editor;

import de.regasus.I18N;
import de.regasus.core.ui.editor.SimpleEditorInput;

public class GlobalImprintEditorInput extends SimpleEditorInput {

	public GlobalImprintEditorInput() {
		super(
			null, // imageDescriptor,
			I18N.GlobalImprintEditor_Text,
			I18N.GlobalImprintEditor_ToolTip
		);
	}

}

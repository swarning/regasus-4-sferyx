package de.regasus.push.editor;

import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.editor.SimpleEditorInput;

public class EditPushSettingsEditorInput extends SimpleEditorInput {

	public EditPushSettingsEditorInput() {
		super(
			null,	// Activator.getImageDescriptor(IImageKeys.?),
			CoreI18N.EditPushSettings_Text,
			CoreI18N.EditPushSettings_ToolTip
		);
	}

}

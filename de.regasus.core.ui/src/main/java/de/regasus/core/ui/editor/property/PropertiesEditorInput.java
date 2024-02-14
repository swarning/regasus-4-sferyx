package de.regasus.core.ui.editor.property;

import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.editor.SimpleEditorInput;

public class PropertiesEditorInput extends SimpleEditorInput {

	public PropertiesEditorInput() {
		super(
			Activator.getImageDescriptor(IImageKeys.EditPropertiesAction),
			CoreI18N.EditPropertiesAction_Text,
			CoreI18N.PropertiesEditor_ToolTip
		);
	}

}

package de.regasus.programme.programmepointtype.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class ProgrammePointTypeEditorInput extends AbstractEditorInput<Long> {

	public ProgrammePointTypeEditorInput() {
	}
	
	
	public ProgrammePointTypeEditorInput(Long programmePointTypePK) {
		key = programmePointTypePK;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, null);
	}

}

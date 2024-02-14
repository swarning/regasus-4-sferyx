package de.regasus.finance.costunit.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class CostUnitEditorInput extends AbstractEditorInput<Integer> {

	public CostUnitEditorInput() {
	}
	
	
	public CostUnitEditorInput(Integer countryCode) {
		key = countryCode;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/salesaccount2.png");
	}

}

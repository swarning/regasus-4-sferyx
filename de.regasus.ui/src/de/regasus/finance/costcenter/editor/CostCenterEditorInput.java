package de.regasus.finance.costcenter.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class CostCenterEditorInput extends AbstractEditorInput<Integer> {

	public CostCenterEditorInput() {
	}
	
	
	public CostCenterEditorInput(Integer countryCode) {
		key = countryCode;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/salesaccount2.png");
	}

}

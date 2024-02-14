package de.regasus.finance.customeraccount.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class CustomerAccountEditorInput extends AbstractEditorInput<String> {

	public CustomerAccountEditorInput() {
	}
	
	
	public CustomerAccountEditorInput(String customerAccountNo) {
		key = customerAccountNo;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/salesaccount2.png");
	}

}

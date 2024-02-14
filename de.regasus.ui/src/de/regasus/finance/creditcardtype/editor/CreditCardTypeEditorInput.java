package de.regasus.finance.creditcardtype.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class CreditCardTypeEditorInput extends AbstractEditorInput<Long> {

	public CreditCardTypeEditorInput() {
	}
	
	
	public CreditCardTypeEditorInput(Long countryCode) {
		key = countryCode;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/salesaccount2.png");
	}

}

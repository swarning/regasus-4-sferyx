package de.regasus.finance.currency.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import de.regasus.core.ui.editor.AbstractEditorInput;

public class CurrencyEditorInput extends AbstractEditorInput<String> {

	public CurrencyEditorInput() {
	}
	
	
	public CurrencyEditorInput(String currencyID) {
		key = currencyID;
	}

	
	public ImageDescriptor getImageDescriptor() {
		// TODO
		// return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/currency.png");
		return null;
	}

}

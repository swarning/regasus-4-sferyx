package de.regasus.common.country.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.IImageKeys;
import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class CountryEditorInput extends AbstractEditorInput<String> {

	public CountryEditorInput() {
	}
	
	
	public CountryEditorInput(String countryCode) {
		key = countryCode;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.COUNTRIES);
	}

}

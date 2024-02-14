package de.regasus.common.language.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.IImageKeys;
import de.regasus.core.model.Activator;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class LanguageEditorInput extends AbstractEditorInput<String> {

	public LanguageEditorInput() {
	}
	
	
	public LanguageEditorInput(String languageCode) {
		key = languageCode;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.LANGUAGES);
	}

}

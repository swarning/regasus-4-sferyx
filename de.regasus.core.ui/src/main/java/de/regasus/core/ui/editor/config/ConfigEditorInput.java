package de.regasus.core.ui.editor.config;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.ConfigParameter;

import de.regasus.core.ConfigIdentifier;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class ConfigEditorInput extends AbstractEditorInput<ConfigIdentifier> {

	private ConfigEditorInput() {
	}
	
	
	/**
	 * Return instance of a new ConfigEditorInput.
	 * 
	 * @param key PK of the Config entity for which the Editor is needed
	 * @param scope {@link ConfigScope} only needed if the Config does not exist and has to be created
	 * @return
	 */
	public static ConfigEditorInput getInstance(ConfigScope scope, String key) {
		ConfigEditorInput configEditorInput = new ConfigEditorInput();
		
		ConfigIdentifier configIdentifier = new ConfigIdentifier(scope, key);
		configEditorInput.key = configIdentifier;
		return configEditorInput;
	}
	
	
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(IImageKeys.EditConfiguration);
	}


	public Class<?> getEntityType() {
		return ConfigParameter.class;
	}
	
}

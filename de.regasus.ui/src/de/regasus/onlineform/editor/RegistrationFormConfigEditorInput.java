package de.regasus.onlineform.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;

import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.ui.Activator;

import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

public class RegistrationFormConfigEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	private Long eventPK;
	private RegistrationFormConfig registrationFormConfig;
	

	public RegistrationFormConfigEditorInput(Long eventPK) {
		this.eventPK = eventPK;
		this.toolTipText = OnlineFormI18N.WebsiteConfiguration;
		
	}
	
	public RegistrationFormConfigEditorInput(RegistrationFormConfig registrationFormConfig) {
		this(registrationFormConfig.getEventPK());
		
		this.registrationFormConfig = registrationFormConfig;
		this.key = registrationFormConfig.getId();
	}


	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			"icons/world.png");
	}

	
	public Long getEventPK() {
		return eventPK;
	}
	
	
	/**
	 * Returning Mnemonic instead of names makes the editor's tab 
	 * incription shorter and consistent to the event editor 
	 */
	@Override
	public String getName() {
		if (registrationFormConfig != null) {
			String webId = registrationFormConfig.getWebId();
			if (webId != null) {
				return webId;
			}
		}
		return OnlineFormI18N.Regasus_Editor_NewName;
	}

	

	public void setConfig(RegistrationFormConfig registrationFormConfig) {
		this.registrationFormConfig = registrationFormConfig;
		this.key = registrationFormConfig.getId();
		
	}
	
	public RegistrationFormConfig getConfig() {
		return registrationFormConfig;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((registrationFormConfig == null) ? 0 : registrationFormConfig.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegistrationFormConfigEditorInput other = (RegistrationFormConfigEditorInput) obj;
		if (registrationFormConfig == null) {
			if (other.registrationFormConfig != null)
				return false;
		}
		else if (!registrationFormConfig.equals(other.registrationFormConfig))
			return false;
		return true;
	}


	@Override
	public Class<?> getEntityType() {
		return RegistrationFormConfig.class;
	}

	
}

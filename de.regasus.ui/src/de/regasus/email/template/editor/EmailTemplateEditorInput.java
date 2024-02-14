package de.regasus.email.template.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.email.EmailLabel;

import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.email.IImageKeys;
import de.regasus.ui.Activator;

public class EmailTemplateEditorInput extends AbstractEditorInput<Long> {

	private Long eventPK;


	/**
	 * To be used to open an editor for an existing Entity
	 */
	public EmailTemplateEditorInput(Long id) {
		this(id, null);
	}


	/**
	 * To be used to open an editor for a new entity that might belong to a certain event (in which case the id
	 * <i>must</i> be null) or to open an editor for an existing Entity (in which case the eventPK <i>may</i> be null).
	 */
	public EmailTemplateEditorInput(Long id, Long eventPK) {
		this.key = id;
		this.eventPK = eventPK;
	}


	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.EMAIL);
	}


	public String getName() {
		return EmailLabel.EmailTemplate.getString();
	}
	
	public Long getEventPK() {
		return eventPK;
	}


}

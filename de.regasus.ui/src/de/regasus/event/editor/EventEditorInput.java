package de.regasus.event.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;

public class EventEditorInput
extends AbstractEditorInput<Long>
implements ILinkableEditorInput, EventIdProvider {


	private Long eventGroupId;


	private EventEditorInput() {
	}


	public static EventEditorInput getEditInstance(Long eventPK) {
		EventEditorInput eventEditorInput = new EventEditorInput();
		eventEditorInput.key = eventPK;
		return eventEditorInput;
	}


	public static EventEditorInput getCreateInstance(Long eventGroupId) {
		EventEditorInput eventEditorInput = new EventEditorInput();
		eventEditorInput.eventGroupId = eventGroupId;
		return eventEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.EVENT);
	}


	@Override
	public Class<?> getEntityType() {
		return EventVO.class;
	}


	/**
	 * @return the eventPK
	 */
	@Override
	public Long getEventId() {
		return key;
	}


	public Long getEventGroupId() {
		return eventGroupId;
	}

}

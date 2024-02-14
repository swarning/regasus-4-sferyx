/**
 * LocationEditorInput.java
 * created on 23.09.2013 10:31:46
 */
package de.regasus.event.location.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;

public class LocationEditorInput
extends AbstractEditorInput<Long>
implements ILinkableEditorInput, EventIdProvider {

	private Long eventPK = null;


	private LocationEditorInput() {
	}


	public static LocationEditorInput getEditInstance(Long locationPK, Long eventPK) {
		LocationEditorInput locationEditorInput = new LocationEditorInput();
		locationEditorInput.key = locationPK;
		locationEditorInput.eventPK = eventPK;
		return locationEditorInput;
	}


	public static LocationEditorInput getCreateInstance(Long eventPK) {
		LocationEditorInput locationEditorInput = new LocationEditorInput();
		locationEditorInput.eventPK = eventPK;
		return locationEditorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.LOCATION);
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}


	@Override
	public Class<?> getEntityType() {
		return LocationVO.class;
	}

}

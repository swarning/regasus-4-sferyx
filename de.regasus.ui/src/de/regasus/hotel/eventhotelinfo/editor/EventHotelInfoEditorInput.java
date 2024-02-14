package de.regasus.hotel.eventhotelinfo.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.hotel.data.EventHotelInfoVO;
import com.lambdalogic.messeinfo.hotel.data.EventHotelKey;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;
import de.regasus.ui.Activator;


/**
 * An {@link IEditorInput} that uses as key a combination of PKs of event and hotel, to identify a hotel within an
 * event.
 */
public class EventHotelInfoEditorInput
extends AbstractEditorInput<EventHotelKey>
implements ILinkableEditorInput, EventIdProvider {

	boolean showReminderTab = false;


	public EventHotelInfoEditorInput(Long eventID, Long hotelID) {
		this(new EventHotelKey(eventID, hotelID));
	}


	public EventHotelInfoEditorInput(EventHotelKey eventHotelKey) {
		this.key = eventHotelKey;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.HOTEL_EVENT);
	}


	@Override
	public Class<?> getEntityType() {
		return EventHotelInfoVO.class;
	}


	@Override
	public Long getEventId() {
		return key.getEventPK();
	}


	public Long getHotelPK() {
		return key.getHotelPK();
	}


	public boolean isShowReminderTab() {
		return showReminderTab;
	}


	public void setShowReminderTab(boolean showReminderTab) {
		this.showReminderTab = showReminderTab;
	}

}

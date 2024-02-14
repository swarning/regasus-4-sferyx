package de.regasus.hotel.contingent.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;

import com.lambdalogic.messeinfo.hotel.data.EventHotelKey;
import com.lambdalogic.messeinfo.hotel.data.HotelVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.event.EventIdProvider;


/**
 * An {@link IEditorInput} that uses as key a combination of PKs of Event and Hotel.
 * If the PK of the Hotel is null, the key identifies only an Event, otherwise a Event-Hotel-Info.
 */
public class OptionalHotelBookingOverviewEditorInput
extends AbstractEditorInput<EventHotelKey>
implements ILinkableEditorInput, EventIdProvider {

	public OptionalHotelBookingOverviewEditorInput(Long eventID) {
		this(new EventHotelKey(eventID, null));
	}


	public OptionalHotelBookingOverviewEditorInput(Long eventID, Long hotelID) {
		this(new EventHotelKey(eventID, hotelID));
	}


	public OptionalHotelBookingOverviewEditorInput(EventHotelKey eventHotelKey) {
		this.key = eventHotelKey;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
		// TODO: image for OptionalHotelBookingOverview
//		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.HOTEL_EVENT);
	}


	@Override
	public Class<?> getEntityType() {
		if (key.getHotelPK() == null) {
			return EventVO.class;
		}
		else {
			return HotelVO.class;
		}
	}


	@Override
	public Long getEventId() {
		return key.getEventPK();
	}


	public Long getHotelPK() {
		return key.getHotelPK();
	}

}

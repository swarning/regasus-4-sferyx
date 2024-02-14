package de.regasus.event;

import com.lambdalogic.messeinfo.participant.IEventProvider;
import com.lambdalogic.messeinfo.participant.data.EventVO;

public class ClientEventProvider implements IEventProvider {

	private static ClientEventProvider eventProvider;
	

	public static ClientEventProvider getInstance() {
		if (eventProvider == null) {
			eventProvider = new ClientEventProvider();
		}
		return eventProvider;
	}
	
	
	private ClientEventProvider() {
	}

	
	@Override
	public EventVO getEventVO(Long eventPK) throws Exception {
		return EventModel.getInstance().getEventVO(eventPK);
	}

}

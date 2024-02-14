package de.regasus.participant;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import de.regasus.event.ParticipantType;

public class ParticipantTypeSearchValuesProvider implements SearchValuesProvider {

	private Long eventPK;


	public ParticipantTypeSearchValuesProvider(Long eventPK) {
		this.eventPK = eventPK;
	}


	@Override
	public LinkedHashMap<Long, I18NString> getValues() throws Exception {
		Collection<ParticipantType> participantTypes;
		if (eventPK != null) {
			participantTypes = ParticipantTypeModel.getInstance().getParticipantTypesByEvent(eventPK);
		}
		else {
			participantTypes = ParticipantTypeModel.getInstance().getAllUndeletedParticipantTypes();
		}

		LinkedHashMap<Long, I18NString> values = new LinkedHashMap<>(participantTypes.size());
		for (ParticipantType participantType : participantTypes) {
			values.put(participantType.getId(), participantType.getName());
		}

		return values;
	}


	public Long getEventPK() {
		return eventPK;
	}


	public void setEventPK(Long eventPK) {
		this.eventPK = eventPK;
	}

}

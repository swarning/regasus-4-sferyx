package de.regasus.participant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.lambdalogic.messeinfo.kernel.sql.SearchValuesProvider;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.ParticipantStateComparator;


public class ParticipantStateSearchValuesProvider implements SearchValuesProvider {

	@Override
	public LinkedHashMap getValues() throws Exception {
		ParticipantStateModel psModel = ParticipantStateModel.getInstance();
		List<ParticipantState> participantStates = new ArrayList<>( psModel.getParticipantStates() );

		Collections.sort(participantStates, ParticipantStateComparator.getInstance());

		LinkedHashMap valueMap = ParticipantState.getEntityLinkedHashMap(participantStates);

		return valueMap;
	}

}

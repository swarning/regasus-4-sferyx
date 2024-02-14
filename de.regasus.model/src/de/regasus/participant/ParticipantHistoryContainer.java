package de.regasus.participant;

import java.util.List;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValue;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;

public class ParticipantHistoryContainer {
	private Long participantPK;
	private List<ParticipantVO> participantHistoryVOs;
	private List<ParticipantCustomFieldValue> customFieldValueHistoryList;
	
	public ParticipantHistoryContainer(
		Long participantPK, 
		List<ParticipantVO> participantHistoryVOs,
		List<ParticipantCustomFieldValue> customFieldValueHistoryList
	) {
		this.participantPK = participantPK;
		this.participantHistoryVOs = participantHistoryVOs;
		this.customFieldValueHistoryList = customFieldValueHistoryList;
	}
	
	public Long getParticipantPK() {
		return participantPK;
	}

	public List<ParticipantVO> getParticipantHistoryVOs() {
		return participantHistoryVOs;
	}
	
	public List<ParticipantCustomFieldValue> getCustomFieldValueHistoryList() {
		return customFieldValueHistoryList;
	}

	public void setCustomFieldValueHistoryList(List<ParticipantCustomFieldValue> valueList) {
		this.customFieldValueHistoryList = valueList;
	}
}
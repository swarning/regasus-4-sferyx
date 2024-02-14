package de.regasus.onlineform.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;

import de.regasus.event.ParticipantType;

public class ParticipantTypePKLabelProvider extends LabelProvider {

	private Map<Long, String> pk2String = new LinkedHashMap<>();

	public ParticipantTypePKLabelProvider(List<ParticipantType> participantTypes) {
		for (ParticipantType type : participantTypes) {
			pk2String.put(type.getId(), type.getName().getString());
		}
	}


	@Override
	public String getText(Object element) {
		if (element instanceof Long) {
			Long pk = (Long) element;
			return pk2String.get(pk);
		}
		return super.getText(element);
	}

}

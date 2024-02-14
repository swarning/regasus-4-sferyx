package de.regasus.onlineform.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.participant.ParticipantCustomField;

public class ParticipantCustomFieldPKLabelProvider extends LabelProvider {

	private Map<Long, String> id2String = new LinkedHashMap<Long, String>();

	public ParticipantCustomFieldPKLabelProvider(List<ParticipantCustomField> pcFields) {
		for (ParticipantCustomField pcField : pcFields) {
			id2String.put(pcField.getID(), pcField.getName());
		}
	}
	
	
	@Override
	public String getText(Object element) {
		if (element instanceof Long) {
			Long id = (Long) element;
			return id2String.get(id);
		}
		return super.getText(element);
	}
}

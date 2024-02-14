package de.regasus.onlineform.provider;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.participant.data.EventVO;

public class CustomFieldLabelProvider extends LabelProvider {

	private EventVO eventVO;

	public CustomFieldLabelProvider(EventVO eventVO) {
		super();
		this.eventVO = eventVO;
	}
	
	
	@Override
	public String getText(Object element) {
		if (element instanceof Integer) {
			Integer integer = (Integer) element;
			return eventVO.getCustomFieldName(integer) + " (" + integer + ")";
		}
		return super.getText(element);
	}
}

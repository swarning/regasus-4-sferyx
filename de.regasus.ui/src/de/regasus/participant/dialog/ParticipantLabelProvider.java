package de.regasus.participant.dialog;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.participant.Participant;

public class ParticipantLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element) {
		if (element instanceof Participant) {
			Participant participant = (Participant) element;
			return participant.getName(true);
		}
		return super.getText(element);
	}

}

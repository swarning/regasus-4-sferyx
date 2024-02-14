package de.regasus.participant.state;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.participant.ParticipantState;

public class ParticipantStateLabelProvider extends BaseLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		return null;
	}


	@Override
	public String getText(Object element) {
		ParticipantState participantState = (ParticipantState) element;
		if (participantState != null) {
			LanguageString name = participantState.getName();
			if (name != null) {
				return name.getString();
			}
		}
		return "";
	}

}

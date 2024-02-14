package de.regasus.participant.type;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.i18n.LanguageString;

import de.regasus.event.ParticipantType;

public class ParticipantTypeLabelProvider extends BaseLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		return null;
	}


	@Override
	public String getText(Object element) {
		ParticipantType participantType = (ParticipantType) element;
		if (participantType != null) {
			LanguageString name = participantType.getName();
			if (name != null) {
				return name.getString();
			}
		}
		return "";
	}

}

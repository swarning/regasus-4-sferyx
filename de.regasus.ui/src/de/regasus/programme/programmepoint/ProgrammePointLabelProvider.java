package de.regasus.programme.programmepoint;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;

public class ProgrammePointLabelProvider extends LabelProvider {

	public ProgrammePointLabelProvider() {
	}


	@Override
	public String getText(Object element) {
		String text = null;

		if (element != null) {
    		if (element instanceof ProgrammePointCVO) {
    			text = ((ProgrammePointCVO) element).getProgrammePointVO().getName().getString();
    		}
    		else if (element instanceof ProgrammePointVO) {
    			text = ((ProgrammePointVO) element).getName().getString();
    		}
    		else {
    			text = element.toString();
    		}
		}

		if (text == null) {
			text = "";
		}

		return text;
	}

}

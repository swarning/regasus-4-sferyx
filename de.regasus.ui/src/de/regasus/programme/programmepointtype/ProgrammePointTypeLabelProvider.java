package de.regasus.programme.programmepointtype;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;

public class ProgrammePointTypeLabelProvider extends LabelProvider {

	public ProgrammePointTypeLabelProvider() {
	}


	@Override
	public String getText(Object element) {
		String text = null;

		if (element != null) {
    		if (element instanceof ProgrammePointTypeVO) {
    			text = ((ProgrammePointTypeVO) element).getName().getString();
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

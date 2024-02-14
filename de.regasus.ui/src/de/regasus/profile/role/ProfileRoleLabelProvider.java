package de.regasus.profile.role;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.profile.ProfileRole;


public class ProfileRoleLabelProvider extends LabelProvider {

	public ProfileRoleLabelProvider() {
	}
	
	
	@Override
	public String getText(Object element) {
		String text = null;
		
		if (element != null) {
    		if (element instanceof ProfileRole) {
    			text = ((ProfileRole) element).getName();
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

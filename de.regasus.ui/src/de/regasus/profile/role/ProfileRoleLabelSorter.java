/**
 * ProfileRoleLabelSorter.java
 * created on 25.11.2013 16:31:15
 */
package de.regasus.profile.role;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import com.lambdalogic.messeinfo.profile.ProfileRole;

public class ProfileRoleLabelSorter extends ViewerSorter {

	private static final Collator collator = Collator.getInstance();
	
	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		if (o1 != null && o1 instanceof ProfileRole && 
			o2 != null && o2 instanceof ProfileRole
		) {
			ProfileRole role1 = (ProfileRole) o1;
			ProfileRole role2 = (ProfileRole) o2;
			String name1 = role1.getName();
			String name2 = role2.getName();
			return collator.compare(name1, name2);
		}
		else {
			return super.compare(viewer, o1, o2);
		}
	}
	
}

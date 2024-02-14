package de.regasus.users.common;

import java.util.HashSet;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.AccessControlEntryCVO;

public class AccessControlRightTypeViewerFilter extends ViewerFilter {

	private HashSet<String> nameSet = new HashSet<String>();

	boolean active;


	public void setActive(boolean active) {
		this.active = active;
	}

	public void setCheckedStrings(String[] strings) {
		nameSet.clear();
		if (strings != null && strings.length > 0) {
			setActive(true);
			for (String string : strings) {
				nameSet.add(string);
			}
		}
	}


	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		AccessControlEntryCVO aceCVO = ((AccessControlEntryCVO) element);
		
		String type = aceCVO.getVO().getACLObject().object;
		String label = AccountLabel.valueOf(type).getString();
		
		if (active && !nameSet.contains(label)) {
			return false;
		}
		return true;
	}
}

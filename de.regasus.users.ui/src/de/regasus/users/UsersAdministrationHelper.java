package de.regasus.users;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;

import de.regasus.auth.api.ACLObject;
import de.regasus.auth.api.ACLObjectDefinitions;

public class UsersAdministrationHelper {

	public static String[] selectUserGroup(
		Shell shell,
		String title,
		String message,
		Collection<UserGroupVO> userGroupVOs) throws Exception {
		ElementListSelectionDialog listDialog = new ElementListSelectionDialog(shell, new LabelProvider());

		listDialog.setMultipleSelection(true);
		listDialog.setTitle(title);
		listDialog.setMessage(message);
		List<String> userGroupPKs = AbstractVO.getPKs(userGroupVOs);
		listDialog.setElements(userGroupPKs.toArray(new String[0]));

		String[] resultIDs = new String[0];
		int code = listDialog.open();
		if (code == Window.OK) {
			Object[] result = listDialog.getResult();
			resultIDs = new String[result.length];
			for (int i = 0; i < result.length; i++) {
				resultIDs[i] = (String) result[i];
			}
		}
		return resultIDs;
	}


	public static String[] openGroupSelectionDialog(Shell shell, String title, String message) throws Exception {
		Collection<UserGroupVO> userGroupVOs = UserGroupModel.getInstance().getAllUserGroupVOs();
		return selectUserGroup(shell, title, message, userGroupVOs);
	}


	public static String getLabelForConstraintType(String constraintType) {
		if (ACLObjectDefinitions.CONSTRAINT_TYPE_EVENT.equals(constraintType)) {
			return AccountLabel.Event.getString();
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL.equals(constraintType)) {
			return AccountLabel.Hotel.getString();
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_HOTEL_CONTINGENT.equals(constraintType)) {
			return AccountLabel.HotelContingent.getString();
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_PROGRAMME_POINT.equals(constraintType)) {
			return AccountLabel.ProgrammePoint.getString();
		}
		else if (ACLObjectDefinitions.CONSTRAINT_TYPE_REPORT.equals(constraintType)) {
			return AccountLabel.Report.getString();
		}
		else return "";
	}


	public static List<ACLObject> getACLObjectsSortedByCurrentLocale() {
		ArrayList<ACLObject> aclObjects = new ArrayList<>(Arrays.asList(ACLObjectDefinitions.ACL_OBJECTS));

		Collections.sort(aclObjects, ACLObjectComparator.getInstance());

		return aclObjects;
	}


	public static List<String> getAllTypesLabels() {
		List<String> allTypes = new ArrayList<>();
		for (ACLObject aclObject : ACLObjectDefinitions.ACL_OBJECTS) {
			String label = AccountLabel.valueOf(aclObject.object).getString();
			allTypes.add(label);
		}
		Collections.sort(allTypes, Collator.getInstance());
		return allTypes;
	}


	public static Image getImageForRight(Boolean value) {
		if (Boolean.TRUE.equals(value)) {
			return IconRegistry.getImage("icons/add.gif");
		}
		else if (Boolean.FALSE.equals(value)) {
			return IconRegistry.getImage("icons/forbidden.gif");
		}
		else {
			return null;
		}
	}

}

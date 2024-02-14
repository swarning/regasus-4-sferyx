package de.regasus.users.user.dialog;

import org.eclipse.jface.viewers.LabelProvider;

import com.lambdalogic.messeinfo.account.AccountLabel;

import de.regasus.auth.api.ACLObject;

public class ACLObjectLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof ACLObject) {
			ACLObject aclObject = (ACLObject) element;
			return AccountLabel.valueOf(aclObject.object).getString();
		} else {
			return super.getText(element);
		}
	}

}

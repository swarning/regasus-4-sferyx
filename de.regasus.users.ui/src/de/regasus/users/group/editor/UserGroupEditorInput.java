package de.regasus.users.group.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.core.ui.editor.AbstractEditorInput;

public class UserGroupEditorInput extends AbstractEditorInput<String> implements ILinkableEditorInput {

	private UserGroupEditorInput() {
	}


	public static UserGroupEditorInput getEditInstance(String userGroupID) {
		UserGroupEditorInput invoiceNoRangeEditorInput = new UserGroupEditorInput();
		invoiceNoRangeEditorInput.key = userGroupID;
		return invoiceNoRangeEditorInput;
	}


	public static UserGroupEditorInput getCreateInstance() {
		return new UserGroupEditorInput();
	}


	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.users.ui.Activator.PLUGIN_ID,
			"icons/group.png");
	}


	public Class<?> getEntityType() {
		return UserGroupVO.class;
	}

}

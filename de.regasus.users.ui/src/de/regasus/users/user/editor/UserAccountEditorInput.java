package de.regasus.users.user.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.account.data.UserAccountCVO;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.core.ui.editor.AbstractEditorInput;

public class UserAccountEditorInput extends AbstractEditorInput<Long> implements ILinkableEditorInput {

	private UserAccountEditorInput() {
	}


	public static UserAccountEditorInput getEditInstance(Long userAccountPK) {
		UserAccountEditorInput invoiceNoRangeEditorInput = new UserAccountEditorInput();
		invoiceNoRangeEditorInput.key = userAccountPK;
		return invoiceNoRangeEditorInput;
	}


	public static UserAccountEditorInput getCreateInstance() {
		return new UserAccountEditorInput();
	}


	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.users.ui.Activator.PLUGIN_ID,
			"icons/user.png");
	}


	public Class<?> getEntityType() {
		return UserAccountCVO.class;
	}

}

package de.regasus.report.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.regasus.core.model.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;

public class UserReportEditorInput extends AbstractEditorInput<Long> {
	private Long userReportDirPK = null;
	

	private UserReportEditorInput() {
	}

	
	public static UserReportEditorInput getEditInstance(Long userReportPK) {
		UserReportEditorInput userReportEditorInput = new UserReportEditorInput();
		userReportEditorInput.key = userReportPK;
		return userReportEditorInput;
	}
	
	
	public static UserReportEditorInput getCreateInstance(Long userReportDirPK) {
		UserReportEditorInput userReportEditorInput = new UserReportEditorInput();
		userReportEditorInput.userReportDirPK = userReportDirPK;
		return userReportEditorInput;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.USER_REPORT);
	}

	
	public Long getUserReportDirPK() {
		return userReportDirPK;
	}
	
	
	public void setUserReportDirPK(Long userReportDirPK) {
		this.userReportDirPK = userReportDirPK;
	}
	
}

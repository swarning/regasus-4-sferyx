package de.regasus.profile.customfield.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.ui.Activator;

public class ProfileCustomFieldEditorInput 
extends AbstractEditorInput<Long>
implements ILinkableEditorInput {
	
	private Long customFieldGroupID = null;

	
	private ProfileCustomFieldEditorInput() {
	}
	
	
	public static ProfileCustomFieldEditorInput getEditInstance(Long profileCustomFieldID) {
		ProfileCustomFieldEditorInput editorInput = new ProfileCustomFieldEditorInput();
		editorInput.key = profileCustomFieldID;
		return editorInput;
	}
	
	
	public static ProfileCustomFieldEditorInput getCreateInstance(Long customFieldGroupID) {
		ProfileCustomFieldEditorInput editorInput = new ProfileCustomFieldEditorInput();
		editorInput.customFieldGroupID = customFieldGroupID;
		return editorInput;
	}

	
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(IImageKeys.CUSTOM_FIELD);
	}

	
	public Class<?> getEntityType() {
		return ProfileCustomField.class;
	}

	
	public Long getCustomFieldGroupPK() {
		return customFieldGroupID;
	}
	
	@Override
	public String getToolTipText() {
		return I18N.ProfileCustomFieldEditor_DefaultToolTip;
	}

}

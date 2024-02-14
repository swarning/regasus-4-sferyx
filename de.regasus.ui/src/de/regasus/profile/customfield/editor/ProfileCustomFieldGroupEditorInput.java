package de.regasus.profile.customfield.editor;

import org.eclipse.jface.resource.ImageDescriptor;

import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroupLocation;
import com.lambdalogic.util.rcp.tree.ILinkableEditorInput;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.core.ui.editor.AbstractEditorInput;
import de.regasus.ui.Activator;

public class ProfileCustomFieldGroupEditorInput extends AbstractEditorInput<Long>
	implements ILinkableEditorInput {

	private ProfileCustomFieldGroupLocation location;


	private ProfileCustomFieldGroupEditorInput() {
	}


	public static ProfileCustomFieldGroupEditorInput getEditInstance(Long profileCustomFieldGroupID) {
		ProfileCustomFieldGroupEditorInput editorInput = new ProfileCustomFieldGroupEditorInput();
		editorInput.key = profileCustomFieldGroupID;
		return editorInput;
	}


	public static ProfileCustomFieldGroupEditorInput getCreateInstance(ProfileCustomFieldGroupLocation location) {
		ProfileCustomFieldGroupEditorInput editorInput = new ProfileCustomFieldGroupEditorInput();
		editorInput.location = location;
		return editorInput;
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(IImageKeys.CUSTOM_FIELD_GROUP);
	}


	@Override
	public Class<?> getEntityType() {
		return ProfileCustomFieldGroup.class;
	}


	@Override
	public String getToolTipText() {
		return I18N.ProfileCustomFieldGroupEditor_DefaultToolTip;
	}


	public ProfileCustomFieldGroupLocation getLocation() {
		return location;
	}

}

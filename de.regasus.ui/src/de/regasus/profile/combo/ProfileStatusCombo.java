package de.regasus.profile.combo;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.profile.ProfileStatus;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;


public class ProfileStatusCombo extends AbstractComboComposite<ProfileStatus> {

	public ProfileStatusCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected Object getEmptyEntity() {
		return null;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				ProfileStatus profileStatus = (ProfileStatus) element;
				return profileStatus.getString();
			}
		};
	}


	@Override
	protected Collection<ProfileStatus> getModelData() {
		return Arrays.asList(ProfileStatus.values());
	}


	@Override
	protected void initModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected void disposeModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		// return null to keep the original order as in the enum
		return null;
	}


	public ProfileStatus getProfileStatus() {
		return entity;
	}


	public void setProfileStatus(ProfileStatus profileStatus) {
		setEntity(profileStatus);
	}

}

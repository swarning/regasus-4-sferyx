package de.regasus.profile.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.combo.ProfileStatusCombo;
import de.regasus.ui.Activator;

public class ProfileGroup extends Group {

	// the entity
	private Profile profile;

	private ModifySupport modifySupport = new ModifySupport(this);


	// Widgets
	private ProfileStatusCombo profileStatusCombo;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @param configParameterSet
	 * @throws Exception
	 */
	public ProfileGroup(
		Composite parent,
		int style,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(parent, style);

		ProfileConfigParameterSet profileConfigParameterSet = null;
		if (configParameterSet != null) {
			profileConfigParameterSet = configParameterSet.getProfile();
		}
		else {
			profileConfigParameterSet = new ProfileConfigParameterSet();
		}

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);
		setText(ProfileLabel.Profile.getString());

		if (profileConfigParameterSet.getProfileState().isVisible()) {
			Label profileStatusLabel = new Label(this, SWT.NONE);
			profileStatusLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			profileStatusLabel.setText( Profile.PROFILE_STATUS.getString() );
			profileStatusLabel.setToolTipText( Profile.PROFILE_STATUS.getDescription() );

			profileStatusCombo = new ProfileStatusCombo(this, SWT.BORDER);
			profileStatusCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			profileStatusCombo.addModifyListener(modifySupport);

			new Label(this, SWT.NONE);
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

	private void syncWidgetsToEntity() {
		if (profile != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						modifySupport.setEnabled(false);

						if (profileStatusCombo != null) {
							profileStatusCombo.setProfileStatus( profile.getProfileStatus() );
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (profile != null) {
			if (profileStatusCombo != null) {
				profile.setProfileStatus(profileStatusCombo.getProfileStatus());
			}
		}
	}


	public void setProfile(Profile profile) {
		this.profile = profile;
		syncWidgetsToEntity();
	}


	@Override
	public void setEnabled (boolean enabled) {
		if (profileStatusCombo != null) {
			profileStatusCombo.setEnabled(enabled);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

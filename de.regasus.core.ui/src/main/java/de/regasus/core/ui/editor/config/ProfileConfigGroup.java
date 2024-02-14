/**
 * ProfileConfigGroup.java
 * created on 31.05.2013 10:22:42
 */
package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.ProfileConfigParameter;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class ProfileConfigGroup extends Group {

	// the entity
	private ProfileConfigParameter configParameter;

	// filter
	private ProfileConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets profileStateWidgets;
	private FieldConfigWidgets secondPersonWidgets;
	private FieldConfigWidgets correspondenceWidgets;
	private FieldConfigWidgets documentWidgets;


	/** Array with all widgets to make some methods shorter*/
	private FieldConfigWidgets[] fieldConfigWidgetsList;


	public ProfileConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(ProfileLabel.ProfileData.getString());

		// Widgets
		profileStateWidgets = new FieldConfigWidgets(this, Profile.PROFILE_STATUS.getString());

		secondPersonWidgets = new FieldConfigWidgets(this, ContactLabel.secondPerson.getString());
		correspondenceWidgets = new FieldConfigWidgets(this, ContactLabel.Correspondence.getString());
		documentWidgets = new FieldConfigWidgets(this, ContactLabel.Files.getString());

		fieldConfigWidgetsList = new FieldConfigWidgets[] {
			profileStateWidgets,
			secondPersonWidgets,
			correspondenceWidgets,
			documentWidgets
		};
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(ProfileConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of widgets depends on
		 * - enable-state of the Group
		 * - the setting of corresponding Admin-Config
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();

		profileStateWidgets.setEnabled(		enabled && adminConfigParameter.getProfileStateConfigParameter().isVisible());
		secondPersonWidgets.setEnabled(		enabled && adminConfigParameter.getSecondPersonConfigParameter().isVisible());
		correspondenceWidgets.setEnabled(	enabled && adminConfigParameter.getCorrespondenceConfigParameter().isVisible());
		documentWidgets.setEnabled(			enabled && adminConfigParameter.getDocumentConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		for (FieldConfigWidgets fieldConfigWidgets : fieldConfigWidgetsList) {
			fieldConfigWidgets.addModifyListener(modifyListener);
		}
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {

				@Override
				public void run() {
					try {
						for (FieldConfigWidgets fcw : fieldConfigWidgetsList) {
							fcw.syncWidgetsToEntity();
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (configParameter != null) {
			for (FieldConfigWidgets fieldConfigWidgets : fieldConfigWidgetsList) {
				fieldConfigWidgets.syncEntityToWidgets();
			}
		}
	}


	public void setProfileConfigParameter(ProfileConfigParameter configParameter) {
		this.configParameter = configParameter;

		profileStateWidgets.setFieldConfigParameter(configParameter.getProfileStateConfigParameter());
		secondPersonWidgets.setFieldConfigParameter(configParameter.getSecondPersonConfigParameter());
		correspondenceWidgets.setFieldConfigParameter(configParameter.getCorrespondenceConfigParameter());
		documentWidgets.setFieldConfigParameter(configParameter.getDocumentConfigParameter());
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

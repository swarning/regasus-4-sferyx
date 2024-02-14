/**
 * ProfileCustomFieldsConfigGroup.java
 * created on 31.05.2013 14:40:37
 */
package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.ProfileConfigParameter;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class ProfileCustomFieldsConfigGroup extends Group {

	// the entity
	private ProfileConfigParameter configParameter;

	// filter
	private ProfileConfigParameter adminConfigParameter;

	// widgets
	private FieldConfigWidgets customFieldWidgets;
	private FieldConfigWidgets allTypesWidgets;


	public ProfileCustomFieldsConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(ContactLabel.CustomFields.getString());

		customFieldWidgets = new FieldConfigWidgets(this, CoreI18N.Config_TypedCustomFields);
		customFieldWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});

		allTypesWidgets = new FieldConfigWidgets(this, CoreI18N.Config_AllTypes);
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

		boolean customFieldVisible = adminConfigParameter.getCustomFieldConfigParameter().isVisible();
		boolean addTypesVisible = adminConfigParameter.getCustomFieldConfigParameter().getAllTypesConfigParameter().isVisible();

		customFieldWidgets.setEnabled(enabled && customFieldVisible);

		// visibility of all other widgets depends further on the current value of customFieldWidgets ...
		// and on the setting of the current field in Admin-Config
		allTypesWidgets.setEnabled(enabled && customFieldWidgets.getVisible() && addTypesVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		customFieldWidgets.addModifyListener(modifyListener);
		allTypesWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {

				@Override
				public void run() {
					try {
						customFieldWidgets.syncWidgetsToEntity();
						allTypesWidgets.syncWidgetsToEntity();

						updateEnabledStatus();
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
			customFieldWidgets.syncEntityToWidgets();
			allTypesWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(ProfileConfigParameter profileConfigParameter) {
		this.configParameter = profileConfigParameter;

		customFieldWidgets.setFieldConfigParameter(profileConfigParameter.getCustomFieldConfigParameter());
		allTypesWidgets.setFieldConfigParameter(profileConfigParameter.getCustomFieldConfigParameter().getAllTypesConfigParameter());
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

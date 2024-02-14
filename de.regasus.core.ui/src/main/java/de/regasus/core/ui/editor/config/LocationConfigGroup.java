package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.ConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.FieldConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class LocationConfigGroup extends Group {

	// the entity
	private ConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private ConfigParameter adminConfigParameter;

	// widgets
	private FieldConfigWidgets globalGateDeviceWidgets;
	private FieldConfigWidgets locationWidgets;


	private boolean globalScope;


	public LocationConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(CoreI18N.Config_Location);

		globalScope = (scope == ConfigScope.GLOBAL_ADMIN || scope == ConfigScope.GLOBAL_CUSTOMER);

		if (globalScope) {
			globalGateDeviceWidgets = new FieldConfigWidgets(
				this,
				CoreI18N.Config_GlobalGateDevice,
				CoreI18N.Config_GlobalGateDevice_toolTip
			);
		}

		locationWidgets = new FieldConfigWidgets(this, CoreI18N.Config_Location);
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(ConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of widgets depends on
		 * - enable-state of the Group
		 * - the setting of globalAdminConfig
		 */

		/* Use getEnabled() instead of isEnabled(), because isEnabled() returns only true if the
		 * Control and all its parent controls are enabled, whereas the result of getEnabled()
		 * relates only to the Control itself.
		 * For some reason, isEnbaled() returns false.
		 */
		boolean enabled = getEnabled();
		boolean gateVisible =  adminConfigParameter.getGateDeviceConfigParameter().isVisible();
		boolean locationVisible =  adminConfigParameter.getEventConfigParameter().getLocationConfigParameter().isVisible();

		if (globalScope) {
			globalGateDeviceWidgets.setEnabled(enabled && gateVisible);
		}

		locationWidgets.setEnabled(enabled && locationVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		if (globalScope) {
			globalGateDeviceWidgets.addModifyListener(modifyListener);
		}
		locationWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (globalScope) {
							globalGateDeviceWidgets.syncWidgetsToEntity();
						}
						locationWidgets.syncWidgetsToEntity();
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
			if (globalScope) {
				globalGateDeviceWidgets.syncEntityToWidgets();
			}
			locationWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(ConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		if (globalScope) {
			globalGateDeviceWidgets.setFieldConfigParameter(configParameter.getGateDeviceConfigParameter());
		}
		FieldConfigParameter locationConfigParameter = configParameter.getEventConfigParameter().getLocationConfigParameter();
		locationWidgets.setFieldConfigParameter(locationConfigParameter);

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

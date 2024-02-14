package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.EventConfigParameter;
import com.lambdalogic.messeinfo.config.parameter.FieldConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class PushServiceSettingsConfigGroup extends Group {

	// the entity
	private EventConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private EventConfigParameter adminConfigParameter;

	// widgets
	private FieldConfigWidgets pushServiceSettingsWidgets;


	public PushServiceSettingsConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(CoreI18N.Config_PushServiceSettings);

		pushServiceSettingsWidgets = new FieldConfigWidgets(this, CoreI18N.Config_PushServiceSettings);
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(EventConfigParameter adminConfigParameter) {
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
		boolean pushServiceSettingsVisible = adminConfigParameter.getPushServiceSettingsConfigParameter().isVisible();

		pushServiceSettingsWidgets.setEnabled(enabled && pushServiceSettingsVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		pushServiceSettingsWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						pushServiceSettingsWidgets.syncWidgetsToEntity();
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
			pushServiceSettingsWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(EventConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites

		FieldConfigParameter pushServiceSettingsConfigParameter = configParameter.getPushServiceSettingsConfigParameter();
		pushServiceSettingsWidgets.setFieldConfigParameter(pushServiceSettingsConfigParameter);

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

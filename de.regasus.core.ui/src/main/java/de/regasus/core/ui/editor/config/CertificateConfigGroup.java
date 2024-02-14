package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.CertificateConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class CertificateConfigGroup extends Group {

	// the entity
	private CertificateConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private CertificateConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets certificateWidgets;
	private FieldConfigWidgets emailWidgets;


	public CertificateConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(CoreI18N.Config_Certificate);

		certificateWidgets = new FieldConfigWidgets(this, CoreI18N.Config_Certificate);
		certificateWidgets.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateEnabledStatus();
			}
		});
		emailWidgets = new FieldConfigWidgets(this, CoreI18N.Config_CertificateEmail);
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(CertificateConfigParameter adminConfigParameter) {
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
		boolean certificateVisible = adminConfigParameter.isVisible();
		boolean certificateEmailVisible = adminConfigParameter.getEmailConfigParameter().isVisible();

		// visibility of portalWidgets depends on the setting of globalAdminConfig
		certificateWidgets.setEnabled(enabled && certificateVisible);
		emailWidgets.setEnabled(enabled && certificateWidgets.getVisible() && certificateEmailVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		certificateWidgets.addModifyListener(modifyListener);
		emailWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						certificateWidgets.syncWidgetsToEntity();
						emailWidgets.syncWidgetsToEntity();

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
			certificateWidgets.syncEntityToWidgets();
			emailWidgets.syncEntityToWidgets();

			if ( !configParameter.isVisible() ) {
				configParameter.getEmailConfigParameter().setVisible(false);
			}
		}
	}


	public void setConfigParameter(CertificateConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		certificateWidgets.setFieldConfigParameter(configParameter);
		emailWidgets.setFieldConfigParameter(configParameter.getEmailConfigParameter());

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

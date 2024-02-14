/**
 * LocationConfigGroup.java
 * created on 24.09.2013 15:13:37
 */
package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.TerminalConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

/**
 * Introduced for "MIRCP-1628 VA-Konfiguration für Sichtbarkeit von Self-Check-in und Zertifikatsdruck".
 */
public class TerminalConfigGroup extends Group {

	// the entity
	private TerminalConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private TerminalConfigParameter adminConfigParameter;

	// widgets
	private FieldConfigWidgets selfCheckinWidgets;
	private FieldConfigWidgets certificatePrintWidgets;
	private FieldConfigWidgets gateWidgets;


	public TerminalConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(CoreI18N.Config_Terminal);

		selfCheckinWidgets = new FieldConfigWidgets(this, CoreI18N.Config_SelfCheckin);
		certificatePrintWidgets = new FieldConfigWidgets(this, CoreI18N.Config_CertificatePrint);
		gateWidgets = new FieldConfigWidgets(this, CoreI18N.Config_Gate);
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(TerminalConfigParameter adminConfigParameter) {
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
		boolean selfCheckinVisible = adminConfigParameter.getSelfCheckinConfigParameter().isVisible();
		boolean certificatePrintVisible = adminConfigParameter.getCertificatePrintConfigParameter().isVisible();
		boolean gateVisible = adminConfigParameter.getGateConfigParameter().isVisible();

		selfCheckinWidgets.setEnabled(enabled && selfCheckinVisible);
		certificatePrintWidgets.setEnabled(enabled && certificatePrintVisible);
		gateWidgets.setEnabled(enabled && gateVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		selfCheckinWidgets.addModifyListener(modifyListener);
		certificatePrintWidgets.addModifyListener(modifyListener);
		gateWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						selfCheckinWidgets.syncWidgetsToEntity();
						certificatePrintWidgets.syncWidgetsToEntity();
						gateWidgets.syncWidgetsToEntity();
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
			selfCheckinWidgets.syncEntityToWidgets();
			certificatePrintWidgets.syncEntityToWidgets();
			gateWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(TerminalConfigParameter configParameter) {
		this.configParameter = configParameter;

		selfCheckinWidgets.setFieldConfigParameter( configParameter.getSelfCheckinConfigParameter() );
		certificatePrintWidgets.setFieldConfigParameter( configParameter.getCertificatePrintConfigParameter() );
		gateWidgets.setFieldConfigParameter( configParameter.getGateConfigParameter() );

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

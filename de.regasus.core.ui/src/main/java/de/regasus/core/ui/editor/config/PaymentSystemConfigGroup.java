package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.InvoiceConfigParameter;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.finance.PaymentSystem;

public class PaymentSystemConfigGroup extends Group {

	// the entity
	private InvoiceConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private InvoiceConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets payEngineWidgets;
	private FieldConfigWidgets easyCheckoutWidgets;


	public PaymentSystemConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText( InvoiceLabel.PaymentSystems.getString() );

		payEngineWidgets = new FieldConfigWidgets(this, PaymentSystem.PAYENGINE.getString());
		easyCheckoutWidgets = new FieldConfigWidgets(this, PaymentSystem.EASY_CHECKOUT.getString());
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(InvoiceConfigParameter adminConfigParameter) {
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
		boolean payEngineVisible = adminConfigParameter.getPayEngineConfigParameter().isVisible();
		boolean easyCheckoutVisible = adminConfigParameter.getEasyCheckoutConfigParameter().isVisible();

		payEngineWidgets.setEnabled(enabled && payEngineVisible);
		easyCheckoutWidgets.setEnabled(enabled && easyCheckoutVisible);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		payEngineWidgets.addModifyListener(modifyListener);
		easyCheckoutWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {

				@Override
				public void run() {
					try {
						payEngineWidgets.syncWidgetsToEntity();
						easyCheckoutWidgets.syncWidgetsToEntity();
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
			payEngineWidgets.syncEntityToWidgets();
			easyCheckoutWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(InvoiceConfigParameter configParameter) {
		this.configParameter = configParameter;

		payEngineWidgets.setFieldConfigParameter( configParameter.getPayEngineConfigParameter() );
		easyCheckoutWidgets.setFieldConfigParameter( configParameter.getEasyCheckoutConfigParameter() );
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

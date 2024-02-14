package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.InvoiceConfigParameter;
import com.lambdalogic.messeinfo.contact.CreditCard;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class GlobalInvoiceConfigGroup extends Group {

	// the entity
	private InvoiceConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private InvoiceConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets invoiceSearchWidgets;
	private FieldConfigWidgets costCenter1Widgets;
	private FieldConfigWidgets costCenter2Widgets;
	private FieldConfigWidgets impersonalAccountWidgets;
	private FieldConfigWidgets customerAccountWidgets;
	private FieldConfigWidgets creditCardTypeWidgets;


	public GlobalInvoiceConfigGroup(Composite parent, int style, ConfigScope scope) {
		super(parent, style);

		setText(CoreI18N.Config_Accountancy);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );

		invoiceSearchWidgets = new FieldConfigWidgets(this, CoreI18N.Config_InvoiceSearch);
		costCenter1Widgets = new FieldConfigWidgets(this, InvoiceLabel.CostCenter.getString());
		costCenter2Widgets = new FieldConfigWidgets(this, InvoiceLabel.CostUnit.getString());
		impersonalAccountWidgets = new FieldConfigWidgets(this, InvoiceLabel.ImpersonalAccount.getString());
		customerAccountWidgets = new FieldConfigWidgets(this, InvoiceLabel.CustomerAccount.getString());
		creditCardTypeWidgets = new FieldConfigWidgets(this, CreditCard.CREDIT_CARD_TYPE.getString());
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

		// visibility of invoiceWidgets depends on the setting of globalAdminConfig
		invoiceSearchWidgets.setEnabled(enabled && adminConfigParameter.getInvoiceSearchConfigParameter().isVisible());
		costCenter1Widgets.setEnabled(enabled && adminConfigParameter.getCostCenter1ConfigParameter().isVisible());
		costCenter2Widgets.setEnabled(enabled && adminConfigParameter.getCostCenter2ConfigParameter().isVisible());
		impersonalAccountWidgets.setEnabled(enabled && adminConfigParameter.getImpersonalAccountConfigParameter().isVisible());
		customerAccountWidgets.setEnabled(enabled && adminConfigParameter.getCustomerAccountConfigParameter().isVisible());
		creditCardTypeWidgets.setEnabled(enabled && adminConfigParameter.getCreditCardTypeConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		invoiceSearchWidgets.addModifyListener(modifyListener);
		costCenter1Widgets.addModifyListener(modifyListener);
		costCenter2Widgets.addModifyListener(modifyListener);
		impersonalAccountWidgets.addModifyListener(modifyListener);
		customerAccountWidgets.addModifyListener(modifyListener);
		creditCardTypeWidgets.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						invoiceSearchWidgets.syncWidgetsToEntity();
						costCenter1Widgets.syncWidgetsToEntity();
						costCenter2Widgets.syncWidgetsToEntity();
						impersonalAccountWidgets.syncWidgetsToEntity();
						customerAccountWidgets.syncWidgetsToEntity();
						creditCardTypeWidgets.syncWidgetsToEntity();
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
			invoiceSearchWidgets.syncEntityToWidgets();
			costCenter1Widgets.syncEntityToWidgets();
			costCenter2Widgets.syncEntityToWidgets();
			impersonalAccountWidgets.syncEntityToWidgets();
			customerAccountWidgets.syncEntityToWidgets();
			creditCardTypeWidgets.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(InvoiceConfigParameter configParameter) {
		this.configParameter = configParameter;

		invoiceSearchWidgets.setFieldConfigParameter( configParameter.getInvoiceSearchConfigParameter() );
		costCenter1Widgets.setFieldConfigParameter( configParameter.getCostCenter1ConfigParameter() );
		costCenter2Widgets.setFieldConfigParameter( configParameter.getCostCenter2ConfigParameter() );
		impersonalAccountWidgets.setFieldConfigParameter( configParameter.getImpersonalAccountConfigParameter() );
		customerAccountWidgets.setFieldConfigParameter( configParameter.getCustomerAccountConfigParameter() );
		creditCardTypeWidgets.setFieldConfigParameter( configParameter.getCreditCardTypeConfigParameter() );
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

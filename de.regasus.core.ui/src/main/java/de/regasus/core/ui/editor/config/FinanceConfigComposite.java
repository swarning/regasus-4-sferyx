package de.regasus.core.ui.editor.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.ConfigScope;
import com.lambdalogic.messeinfo.config.parameter.InvoiceConfigParameter;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class FinanceConfigComposite extends Composite {

	// the entity
	private InvoiceConfigParameter configParameter;

	// corresponding admin Config that controls which settings are enabled
	private InvoiceConfigParameter adminConfigParameter;

	// Widgets
	private GlobalInvoiceConfigGroup globalInvoiceConfigGroup;
	private PaymentSystemConfigGroup paymentSystemGroup;


	public FinanceConfigComposite(
		Composite parent,
		int style,
		ConfigScope scope
	) {
		super(parent, style);

		setLayout( new GridLayout() );

		globalInvoiceConfigGroup = new GlobalInvoiceConfigGroup(this, style, scope);
		globalInvoiceConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		paymentSystemGroup = new PaymentSystemConfigGroup(this, style);
		paymentSystemGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(InvoiceConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		globalInvoiceConfigGroup.setAdminConfigParameter(adminConfigParameter);
		paymentSystemGroup.setAdminConfigParameter(adminConfigParameter);

		updateEnabledStatus();
	}


	protected void updateEnabledStatus() {
		boolean enabled = getEnabled();

		globalInvoiceConfigGroup.setEnabled(enabled);
		paymentSystemGroup.setEnabled(enabled);
	}


	public void addModifyListener(ModifyListener modifyListener) {
		globalInvoiceConfigGroup.addModifyListener(modifyListener);
		paymentSystemGroup.addModifyListener(modifyListener);
	}


	public void syncWidgetsToEntity() {
		if (configParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						globalInvoiceConfigGroup.syncWidgetsToEntity();
						paymentSystemGroup.syncWidgetsToEntity();

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
			globalInvoiceConfigGroup.syncEntityToWidgets();
			paymentSystemGroup.syncEntityToWidgets();
		}
	}


	public void setConfigParameter(InvoiceConfigParameter configParameter) {
		this.configParameter = configParameter;

		// set entity to other composites
		globalInvoiceConfigGroup.setConfigParameter(configParameter);
		paymentSystemGroup.setConfigParameter(configParameter);

		// syncEntityToWidgets() is called from outside
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

package de.regasus.finance.invoicenumberrange.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class GeneralTabComposite extends Composite {

	// the entity
	private InvoiceNoRangeVO invoiceNoRangeVO;

	private ModifySupport modifySupport = new ModifySupport(this);

	private ConfigParameterSet configParameterSet;


	// **************************************************************************
	// * Widgets
	// *

	private GeneralGroup generalGroup;
	private NumberGroup numberGroup;
	private DaysBetweenRemindersGroup daysBetweenRemindersGroup;
	private PaymentTermsGroup paymentTermsGroup;

	// *
	// * Widgets
	// **************************************************************************


	public GeneralTabComposite(Composite parent, ConfigParameterSet configParameterSet)
	throws Exception {
		super(parent, SWT.NONE);

		this.configParameterSet = configParameterSet;

		createWidgets();
		addModifyListenerToWidgets();
	}


	private void createWidgets() throws Exception {
		setLayout(new GridLayout(2, false));

		generalGroup = new GeneralGroup(this, SWT.NONE, configParameterSet);
		generalGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		numberGroup = new NumberGroup(this, SWT.NONE);
		numberGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		daysBetweenRemindersGroup = new DaysBetweenRemindersGroup(this, SWT.NONE);
		daysBetweenRemindersGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		paymentTermsGroup = new PaymentTermsGroup(this, SWT.NONE);
		paymentTermsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	}


	private void addModifyListenerToWidgets() {
		generalGroup.addModifyListener(modifySupport);
		numberGroup.addModifyListener(modifySupport);
		daysBetweenRemindersGroup.addModifyListener(modifySupport);
		paymentTermsGroup.addModifyListener(modifySupport);
	}


	public void setInvoiceNoRange(InvoiceNoRangeVO invoiceNoRangeVO) {
		this.invoiceNoRangeVO = invoiceNoRangeVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (invoiceNoRangeVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						generalGroup.setInvoiceNoRange(invoiceNoRangeVO);
						numberGroup.setInvoiceNoRange(invoiceNoRangeVO);
						daysBetweenRemindersGroup.setInvoiceNoRange(invoiceNoRangeVO);
						paymentTermsGroup.setInvoiceNoRange(invoiceNoRangeVO);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (invoiceNoRangeVO != null) {
			generalGroup.syncEntityToWidgets();
			numberGroup.syncEntityToWidgets();
			daysBetweenRemindersGroup.syncEntityToWidgets();
			paymentTermsGroup.syncEntityToWidgets();
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

}

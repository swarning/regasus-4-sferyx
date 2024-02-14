package de.regasus.finance;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.InvoiceConfigParameterSet;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.finance.costcenter.view.CostCenterView;
import de.regasus.finance.creditcardtype.view.CreditCardTypeView;
import de.regasus.finance.currency.view.CurrenciesView;
import de.regasus.finance.customeraccount.view.CustomerAccountView;
import de.regasus.finance.datatrans.dialog.CostUnitView;
import de.regasus.finance.impersonalaccount.view.ImpersonalAccountView;
import de.regasus.finance.invoice.view.InvoiceSearchView;
import de.regasus.finance.invoice.view.pref.InvoiceSearchViewPreference;
import de.regasus.finance.paymentsystem.view.PaymentSystemSetupView;
import de.regasus.ui.Activator;

public class AccountancyPerspective implements IPerspectiveFactory {

	public static final String ID = "com.lambdalogic.mi.invoice.ui.AccountancyPerspective";


	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);

		IFolderLayout folder = layout.createFolder("folder", IPageLayout.LEFT, 0.6f, editorArea);


		try {
			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet();
			InvoiceConfigParameterSet invoiceConfigParameterSet = null;
			if (configParameterSet != null) {
				invoiceConfigParameterSet = configParameterSet.getInvoiceDetails();
			}

			if (invoiceConfigParameterSet == null || invoiceConfigParameterSet.getInvoiceSearch().isVisible()) {
    			// This is a bit unfortunate: We cannot make Invoice UI depending on Event UI
    			// (because of circular dependencies) and thus not make sure that the class is present.
    			// I tried with extending this AccountancyPerspective from the eventUI via plugin.xml,
    			// which worked in principle but put the InvoiceSearchView on the last tab
				InvoiceSearchViewPreference.getInstance().initialize(); // delete previously saved preferences
    			folder.addView(InvoiceSearchView.ID);
			}

			if (invoiceConfigParameterSet == null || invoiceConfigParameterSet.getCostCenter1().isVisible()) {
				folder.addView(CostCenterView.ID);
			}
			if (invoiceConfigParameterSet == null || invoiceConfigParameterSet.getCostCenter2().isVisible()) {
				folder.addView(CostUnitView.ID);
			}
			if (invoiceConfigParameterSet == null || invoiceConfigParameterSet.getImpersonalAccount().isVisible()) {
				folder.addView(ImpersonalAccountView.ID);
			}
			if (invoiceConfigParameterSet == null || invoiceConfigParameterSet.getCustomerAccount().isVisible()) {
				folder.addView(CustomerAccountView.ID);
			}
			if (invoiceConfigParameterSet == null || invoiceConfigParameterSet.getCreditCardType().isVisible()) {
				folder.addView(CreditCardTypeView.ID);
			}
			if (invoiceConfigParameterSet == null || invoiceConfigParameterSet.getPayEngine().isVisible()) {
				folder.addView(PaymentSystemSetupView.ID);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		folder.addView(CurrenciesView.ID);

	}

}

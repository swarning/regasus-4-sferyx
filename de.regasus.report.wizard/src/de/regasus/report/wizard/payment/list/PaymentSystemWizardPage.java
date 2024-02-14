package de.regasus.report.wizard.payment.list;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.report.paymentList.PaymentListReportParameter;
import com.lambdalogic.report.parameter.ILanguageReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.finance.PaymentSystem;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class PaymentSystemWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "PaymentSystemWizardPage";

	private ListViewer listViewer;
	private PaymentListReportParameter parameter;


	public PaymentSystemWizardPage() {
		super(ID);
		setTitle(InvoiceLabel.PaymentSystems.getString());
		setDescription(ReportWizardI18N.PaymentSystemWizardPage_Description);
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());

		setControl(container);

		listViewer = new ListViewer(container);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				PaymentSystem paymentSystem = (PaymentSystem) element;
				return paymentSystem.getString();
			}
		});

		listViewer.setInput(PaymentSystem.values());
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ILanguageReportParameter) {
			parameter = (PaymentListReportParameter) reportParameter;

			if (listViewer != null) {
				List<PaymentSystem> paymentSystems = parameter.getPaymentSystems();
				if (paymentSystems == null || paymentSystems.isEmpty()) {
					listViewer.setSelection(new StructuredSelection());
				}
				else {
					listViewer.setSelection(new StructuredSelection(paymentSystems));
				}
			}
		}
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void saveReportParameters() {
		if (parameter != null) {
			List<PaymentSystem> paymentSystems = SelectionHelper.toList(listViewer.getSelection());
			String description = buildDescription(paymentSystems);

			parameter.setPaymentSystems(paymentSystems);
			parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_PAYMENT_SYSTEMS, description);
		}
	}


	private String buildDescription(List<PaymentSystem> paymentSystems) {
		String description = null;
		if ( !paymentSystems.isEmpty() ) {
			I18NPattern i18nPattern = new I18NPattern();
			i18nPattern.append( InvoiceLabel.PaymentSystems.getString() );
			i18nPattern.append(": ");
			int i = 0;
			for (PaymentSystem paymentSystem : paymentSystems) {
				if (i++ > 0) {
					i18nPattern.append(", ");
				}
				i18nPattern.append( paymentSystem.getString() );
			}

			description = i18nPattern.getString();
		}
		return description;
	}

}

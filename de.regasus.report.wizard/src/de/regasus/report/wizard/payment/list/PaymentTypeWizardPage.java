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

import de.regasus.finance.PaymentType;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class PaymentTypeWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "PaymentTypeWizardPage";

	private ListViewer listViewer;
	private PaymentListReportParameter parameter;


	public PaymentTypeWizardPage() {
		super(ID);
		setTitle(InvoiceLabel.PaymentTypes.getString());
		setDescription(ReportWizardI18N.PaymentTypeWizardPage_Description);
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		//
		setControl(container);

		listViewer = new ListViewer(container);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				PaymentType paymentType = (PaymentType) element;
				return paymentType.getString();
			}
		});

		// init models
		listViewer.setInput(PaymentType.values());
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ILanguageReportParameter) {
			parameter = (PaymentListReportParameter) reportParameter;

			if (listViewer != null) {
				List<PaymentType> paymentTypes = parameter.getPaymentTypes();
				if (paymentTypes == null || paymentTypes.isEmpty()) {
					listViewer.setSelection(new StructuredSelection());
				}
				else {
					listViewer.setSelection(new StructuredSelection(paymentTypes));
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
			List<PaymentType> paymentTypes = SelectionHelper.toList(listViewer.getSelection());
			String description = buildDescription(paymentTypes);

			parameter.setPaymentTypes(paymentTypes);
			parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_PAYMENT_TYPES, description);
		}
	}


	private String buildDescription(List<PaymentType> paymentTypes) {
		String description = null;
		if ( !paymentTypes.isEmpty() ) {
			I18NPattern i18nPattern = new I18NPattern();
			i18nPattern.append( InvoiceLabel.PaymentTypes.getString() );
			i18nPattern.append(": ");
			int i = 0;
			for (PaymentType paymentType : paymentTypes) {
				if (i++ > 0) {
					i18nPattern.append(", ");
				}
				i18nPattern.append( paymentType.getString() );
			}

			description = i18nPattern.getString();
		}
		return description;
	}

}

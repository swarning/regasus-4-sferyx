package de.regasus.report.wizard.payment.list;

import com.lambdalogic.messeinfo.invoice.report.paymentList.PaymentListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class PaymentListWizard extends DefaultReportWizard implements IReportWizard {
	private PaymentListReportParameter paymentListReportParameter;

	public PaymentListWizard() {
		super();
	}

	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			paymentListReportParameter = new PaymentListReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(paymentListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new PaymentListOptionsWizardPage());
		addPage(new PaymentTypeWizardPage());
		addPage(new PaymentSystemWizardPage());
	}

}

package de.regasus.report.wizard.invoice.list;

import com.lambdalogic.messeinfo.invoice.report.invoiceList.InvoiceListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.InvoiceNoRangeWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class InvoiceListWizard extends DefaultReportWizard implements IReportWizard {
	private InvoiceListReportParameter invoiceListReportParameter;
	
	public InvoiceListWizard() {
		super();
	}

	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        invoiceListReportParameter = new InvoiceListReportParameter(xmlRequest);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(invoiceListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new InvoiceNoRangeWizardPage());
		addPage(new InvoiceListOptionsWizardPage());
	}

}

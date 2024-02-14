package de.regasus.report.wizard.invoice.position.list;

import com.lambdalogic.messeinfo.invoice.report.invoicePositionList.InvoicePositionListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.InvoiceNoRangeWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class InvoicePositionListWizard extends DefaultReportWizard implements IReportWizard {
	private InvoicePositionListReportParameter invoicePositionListReportParameter;
	
	public InvoicePositionListWizard() {
		super();
	}

	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        invoicePositionListReportParameter = new InvoicePositionListReportParameter(xmlRequest);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(invoicePositionListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new InvoiceNoRangeWizardPage());
		addPage(new InvoicePositionListOptionsWizardPage());
	}

}

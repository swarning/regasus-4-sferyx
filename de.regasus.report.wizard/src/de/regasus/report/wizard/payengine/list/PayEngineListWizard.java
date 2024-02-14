package de.regasus.report.wizard.payengine.list;

import com.lambdalogic.messeinfo.invoice.report.payEngineList.PayEngineListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class PayEngineListWizard extends DefaultReportWizard implements IReportWizard {
	private PayEngineListReportParameter payEngineListReportParameter;

	public PayEngineListWizard() {
		super();
	}

	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			payEngineListReportParameter = new PayEngineListReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(payEngineListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new PayEngineListOptionsWizardPage());
	}

}

package de.regasus.report.wizard.openposition.list;

import com.lambdalogic.messeinfo.invoice.report.openPositionList.OpenPositionListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class OpenPositionListWizard extends DefaultReportWizard implements IReportWizard {
	private OpenPositionListReportParameter openPositionListReportParameter;

	public OpenPositionListWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
	        openPositionListReportParameter = new OpenPositionListReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(openPositionListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new OpenPositionListOptionsWizardPage());
	}

}

package de.regasus.report.wizard.generic;

import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.kernel.report.generic.GenericReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.ui.Activator;


public class GenericReportWizard extends DefaultReportWizard implements IReportWizard {
	private GenericReportParameter genericReportParameter;

	public GenericReportWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			genericReportParameter = new GenericReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(genericReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new SqlWizardPage());
	}


	@Override
	public Point getPreferredSize() {
		return new Point(800, 400);
	}

}

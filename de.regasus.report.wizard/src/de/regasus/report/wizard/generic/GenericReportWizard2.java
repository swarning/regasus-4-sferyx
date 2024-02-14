package de.regasus.report.wizard.generic;

import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.kernel.report.generic.GenericReportParameter2;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.ui.Activator;


public class GenericReportWizard2 extends DefaultReportWizard implements IReportWizard {
	private GenericReportParameter2 genericReportParameter2;

	public GenericReportWizard2() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			genericReportParameter2 = new GenericReportParameter2(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(genericReportParameter2);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
	}


	@Override
	public Point getPreferredSize() {
		return new Point(800, 400);
	}

}

package de.regasus.report.wizard.country.statistics2;

import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.participant.report.countryStatistics.CountryStatistics2ReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ParticipantStateWizardPage;
import de.regasus.report.wizard.common.ParticipantTypeWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class CountryStatistics2Wizard extends DefaultReportWizard implements IReportWizard {
	private CountryStatistics2ReportParameter reportParameter;
	
	
	public CountryStatistics2Wizard() {
		super();
	}
	
	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        reportParameter = new CountryStatistics2ReportParameter(xmlRequest);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(reportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	
	@Override
	protected void doAddPages() {
		addPage(new EventWizardPage());
		addPage(new ParticipantStateWizardPage());
		addPage(new ParticipantTypeWizardPage());
		addPage(new CountryStatistics2OptionsWizardPage());
	}

	
	@Override
	public Point getPreferredSize() {
		return new Point(800, 400);
	}

}

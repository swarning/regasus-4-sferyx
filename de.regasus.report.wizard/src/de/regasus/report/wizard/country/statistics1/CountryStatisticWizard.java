package de.regasus.report.wizard.country.statistics1;

import org.eclipse.swt.graphics.Point;

import com.lambdalogic.messeinfo.participant.report.countryStatistics.CountryStatisticsReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.common.ParticipantStateWizardPage;
import de.regasus.report.wizard.common.ParticipantTypeWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class CountryStatisticWizard extends DefaultReportWizard implements IReportWizard {
	private CountryStatisticsReportParameter countryStatisticsReportParameter;
	
	public CountryStatisticWizard() {
		super();
	}
	
	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        countryStatisticsReportParameter = new CountryStatisticsReportParameter(xmlRequest);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(countryStatisticsReportParameter);
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
		addPage(new CountryStatisticsOptionsWizardPage());
	}

	
	@Override
	public Point getPreferredSize() {
		return new Point(800, 400);
	}

}

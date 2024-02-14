package de.regasus.report.wizard.profile.list;

import java.util.Arrays;
import java.util.List;

import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.profile.report.profileAtEventsPerCity.ProfileAtEventsPerCityReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventListWizardPage;
import de.regasus.report.wizard.common.ParticipantStateWizardPage;
import de.regasus.report.wizard.ui.Activator;

/**
 * This wizard is used by both ProfileAtEventsPerCityReportGenerator and ProfileAtEventsReportGenerator
 */
public class ProfileAtEventsPerCityWizard extends DefaultReportWizard implements IReportWizard {

	private ProfileAtEventsPerCityReportParameter profileAtEventsPerCityReportParameter;


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlContainer) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
			profileAtEventsPerCityReportParameter = new ProfileAtEventsPerCityReportParameter(xmlContainer);

			// Make REGISTRATION and ONLINE default states
			if (profileAtEventsPerCityReportParameter.getParticipantStatePKs() == null) {
				List<Long> defaultStates = Arrays.asList(ParticipantState.REGISTRATION, ParticipantState.ONLINE);
				profileAtEventsPerCityReportParameter.setParticipantStatePKs(defaultStates);
			}

			// ReportParameter der Superklasse bekannt machen
			setReportParameter(profileAtEventsPerCityReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage(new EventListWizardPage());
		addPage(new ParticipantStateWizardPage());
	}

}

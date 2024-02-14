package de.regasus.report.wizard.participant.label;

import com.lambdalogic.messeinfo.participant.report.participantLabel.ParticipantLabelReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.participant.list.ParticipantSelectionReportWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ParticipantLabelWizard extends DefaultReportWizard implements IReportWizard {

	public ParticipantLabelWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter of required type
			ParticipantLabelReportParameter participantLabelReportParameter = new ParticipantLabelReportParameter(xmlRequest);

	        // provide ReportParameter to super class
	        setReportParameter(participantLabelReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage( new EventWizardPage() );
		addPage( new ParticipantSelectionReportWizardPage(SelectionMode.MULTI_SELECTION) );
		addPage( new LabelNumberWizardPage() );
	}

}


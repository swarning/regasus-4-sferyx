package de.regasus.report.wizard.participant.list;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.lang.invoke.MethodHandles;
import java.util.List;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.IEventReportParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.IParticipantPKsReportParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.participant.sql.ParticipantSearch;
import com.lambdalogic.messeinfo.report.IWhereClauseReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.participant.dialog.ParticipantSelectionWizardPage;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;


public class ParticipantSelectionReportWizardPage extends ParticipantSelectionWizardPage implements IReportWizardPage {

	public static final String NAME = MethodHandles.lookup().lookupClass().getName();

	private IWhereClauseReportParameter whereClauseReportParameter;
	private IParticipantPKsReportParameter participantPKsReportParameter;


	public ParticipantSelectionReportWizardPage(SelectionMode selectionMode) {
		super(selectionMode, null /*eventPK*/);

		setTitle(ReportWizardI18N.ParticipantWhereWizardPage_Title);
		setDescription(ReportWizardI18N.ParticipantWhereWizardPage_Description);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter != null) {
			Long eventPK = null;
			if (reportParameter instanceof IEventReportParameter) {
				eventPK = ((IEventReportParameter) reportParameter).getEventPK();
			}
			participantSearchComposite.setEventPK(eventPK);

			whereClauseReportParameter = (IWhereClauseReportParameter) reportParameter;
			List<WhereField> whereFields = whereClauseReportParameter.getWhereFields();
			if (notEmpty(whereFields)) {
				participantSearchComposite.setWhereFields(whereFields);
				participantSearchComposite.doSearch();
			}

			if (reportParameter instanceof IParticipantPKsReportParameter) {
				participantPKsReportParameter = (IParticipantPKsReportParameter) reportParameter;
				List<Long> participantPKs = participantPKsReportParameter.getParticipantPKs();
				if (!participantPKs.isEmpty()) {
					participantSearchComposite.setSelection(participantPKs);
				}
			}
		}
	}


	@Override
	public ParticipantSearch getParticipantSearch() {
		return participantSearchComposite.getParticipantSearch();
	}


	@Override
	public void saveReportParameters() {
		if (whereClauseReportParameter != null) {
			List<SQLParameter> sqlParameterList = participantSearchComposite.getSQLParameters();
			whereClauseReportParameter.setSQLParameters(sqlParameterList);

			I18NPattern desc = new I18NPattern();
			desc.append(ReportWizardI18N.AbstractWhereWizardPage_SearchCriteria);
			desc.append(": ");
			desc.append(participantSearchComposite.getDescription());

			whereClauseReportParameter.setDescription(
				IWhereClauseReportParameter.DESCRIPTION_ID,
				desc.getString()
			);
		}

		if (participantPKsReportParameter != null) {
			List<Long> participantPKs = getSelectedPKs();
			participantPKsReportParameter.setParticipantPKs(participantPKs);

			I18NPattern desc = new I18NPattern();
			desc.append(ReportWizardI18N.ParticipantWhereWizardPage_SelectedParticipantsNumber);
			desc.append(": ");
			desc.append(participantPKs.size());

			participantPKsReportParameter.setDescription(
				IParticipantPKsReportParameter.DESCRIPTION_ID,
				desc.getString()
			);
		}
	}

}

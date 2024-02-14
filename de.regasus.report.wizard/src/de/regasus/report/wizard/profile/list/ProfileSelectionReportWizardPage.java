package de.regasus.report.wizard.profile.list;

import static com.lambdalogic.util.CollectionsHelper.notEmpty;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.WhereField;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.messeinfo.profile.report.profileList.IProfilePKsReportParameter;
import com.lambdalogic.messeinfo.profile.sql.ProfileSearch;
import com.lambdalogic.messeinfo.report.IWhereClauseReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionMode;

import de.regasus.profile.dialog.ProfileSelectionWizardPage;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class ProfileSelectionReportWizardPage extends ProfileSelectionWizardPage implements IReportWizardPage {

	public static final String NAME = MethodHandles.lookup().lookupClass().getName();

	private IWhereClauseReportParameter whereClauseReportParameter;
	private IProfilePKsReportParameter profilePKsReportParameter;


	/**
	 * Create the wizard
	 */
	public ProfileSelectionReportWizardPage(SelectionMode selectionMode) {
		super(selectionMode);

		setTitle(ReportWizardI18N.ProfileWhereWizardPage_Title);
		setDescription(ReportWizardI18N.ProfileWhereWizardPage_Description);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter != null) {
			whereClauseReportParameter = (IWhereClauseReportParameter) reportParameter;
			List<WhereField> whereFields = whereClauseReportParameter.getWhereFields();
			if (notEmpty(whereFields)) {
				profileSearchComposite.setWhereFields(whereFields);
				profileSearchComposite.doSearch();
			}

			if (reportParameter instanceof IProfilePKsReportParameter) {
				profilePKsReportParameter = (IProfilePKsReportParameter) reportParameter;
				List<Long> profilePKs = profilePKsReportParameter.getProfilePKs();
				if (!profilePKs.isEmpty()) {
					profileSearchComposite.setSelection(profilePKs);
				}
			}
		}
	}


	@Override
	public ProfileSearch getProfileSearch() {
		return profileSearchComposite.getProfileSearch();
	}


	@Override
	public void saveReportParameters() {
		if (whereClauseReportParameter != null) {
			List<SQLParameter> sqlParameterList = profileSearchComposite.getSQLParameters();
			whereClauseReportParameter.setSQLParameters(sqlParameterList);

			I18NPattern desc = new I18NPattern();
			desc.append(ReportWizardI18N.AbstractWhereWizardPage_SearchCriteria);
			desc.append(": ");
			desc.append(profileSearchComposite.getDescription());

			whereClauseReportParameter.setDescription(
				IWhereClauseReportParameter.DESCRIPTION_ID,
				desc.getString()
			);
		}

		if (profilePKsReportParameter != null) {
			IStructuredSelection selection = (IStructuredSelection) profileSearchComposite.getTableViewer().getSelection();
			List<Long> profilePKs = new ArrayList<>(selection.size());
			if (selection.size() > 0) {
				for (Iterator<Profile> it = selection.iterator(); it.hasNext();) {
					Profile profile = it.next();
					profilePKs.add(profile.getPrimaryKey());
				}
			}
			profilePKsReportParameter.setProfilePKs(profilePKs);

			I18NPattern desc = new I18NPattern();
			desc.append(ReportWizardI18N.ProfileWhereWizardPage_SelectedProfileNumber);
			desc.append(": ");
			desc.append(selection.size());

			profilePKsReportParameter.setDescription(
				IProfilePKsReportParameter.DESCRIPTION_ID,
				desc.getString()
			);
		}
	}

}

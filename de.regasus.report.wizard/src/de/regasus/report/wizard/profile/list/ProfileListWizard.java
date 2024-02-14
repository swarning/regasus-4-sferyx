package de.regasus.report.wizard.profile.list;

import org.eclipse.jface.wizard.IWizardPage;

import com.lambdalogic.messeinfo.profile.report.profileList.ProfileListReportParameter;
import com.lambdalogic.messeinfo.profile.sql.ProfileSearch;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.SelectionMode;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class ProfileListWizard extends DefaultReportWizard implements IReportWizard {
	private ProfileListReportParameter profileListReportParameter;

	private ProfileSelectionReportWizardPage profileWhereWizardPage;
	private ProfileSelectWizardPage profileSelectWizardPage;


	public ProfileListWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter instance of the specific type
			profileListReportParameter = new ProfileListReportParameter(xmlRequest);

			// propagate ReportParameter to the super class
	        setReportParameter(profileListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		profileWhereWizardPage = new ProfileSelectionReportWizardPage(SelectionMode.NO_SELECTION);
		addPage(profileWhereWizardPage);

		profileSelectWizardPage = new ProfileSelectWizardPage(true);
		addPage(profileSelectWizardPage);
	}


	@Override
	protected void doNextPressed(IReportWizardPage currentPage) {
		super.doNextPressed(currentPage);

		// detect and initialize the next page
		IWizardPage nextWizardPage =  super.getNextPage(currentPage);
		if (nextWizardPage == profileSelectWizardPage) {
			ProfileSearch profileSearch = profileWhereWizardPage.getProfileSearch();
			profileSelectWizardPage.setAbstractSearch(profileSearch);
		}
	}


	@Override
	public IReportParameter getReportParameter() {
		return profileListReportParameter;
	}

}

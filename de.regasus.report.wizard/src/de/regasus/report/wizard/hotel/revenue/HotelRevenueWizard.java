package de.regasus.report.wizard.hotel.revenue;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;

import java.util.Collection;

import org.eclipse.jface.wizard.IWizardPage;

import com.lambdalogic.messeinfo.hotel.report.revenue.HotelRevenueReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.CurrencyWizardPage;
import de.regasus.report.wizard.common.EventOrTimePeriodWizardPage;
import de.regasus.report.wizard.ui.Activator;


public class HotelRevenueWizard extends DefaultReportWizard implements IReportWizard {

	private HotelRevenueReportParameter reportParameter;


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// create ReportParameter of demanded type
			reportParameter = new HotelRevenueReportParameter(xmlRequest);

	        // publish ReportParameter to super class
	        setReportParameter(reportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		addPage( new EventOrTimePeriodWizardPage() );
		addPage( new CurrencyWizardPage() );
		addPage( new HotelRevenueOptionsWizardPage() );
	}


	@Override
	protected Collection<IWizardPage> getFinishablePages() {
		// all pages
		return createArrayList( getPages() );
	}

}

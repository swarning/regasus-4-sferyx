package de.regasus.report.wizard.hotel.offering.list;

import com.lambdalogic.messeinfo.hotel.report.hotelOfferingList.HotelOfferingListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class HotelOfferingListWizard extends DefaultReportWizard implements IReportWizard {	
	private HotelOfferingListReportParameter hotelOfferingListReportParameter;
	
	
	// *************************************************************************
	// * Pages
	// *

	// page 1
	private EventWizardPage eventWizardPage;
	
	// page 2
	private HotelOfferingSearchWizardPage hotelOfferingSearchPage;

	// page 3
	private HotelOfferingSelectionWizardPage hotelOfferingSelectionWizardPage;
	
	
	public HotelOfferingListWizard() {
		super();
	}

	
	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);
			
			// ReportParameter des benötigten Typs erzeugen
	        hotelOfferingListReportParameter = new HotelOfferingListReportParameter(xmlRequest);
	        
	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(hotelOfferingListReportParameter);	        	       						
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		eventWizardPage = new EventWizardPage();
		addPage(eventWizardPage);
				
		hotelOfferingSearchPage = new HotelOfferingSearchWizardPage();
		addPage(hotelOfferingSearchPage);
		
		hotelOfferingSelectionWizardPage = new HotelOfferingSelectionWizardPage();
		addPage(hotelOfferingSelectionWizardPage);
	}

}

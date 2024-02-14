package de.regasus.report.wizard.hotel.list;

import java.util.Collection;

import org.eclipse.jface.wizard.IWizardPage;

import com.lambdalogic.messeinfo.hotel.report.hotelList.HotelListReportParameter;
import com.lambdalogic.messeinfo.report.data.BaseReportVO;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.DefaultReportWizard;
import de.regasus.report.dialog.IReportWizard;
import de.regasus.report.wizard.common.EventListWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class HotelListWizard extends DefaultReportWizard implements IReportWizard {
	private HotelListReportParameter hotelListReportParameter;

	private CitiesWizardPage citiesWizardPage;


	public HotelListWizard() {
		super();
	}


	@Override
	public void initialize(BaseReportVO baseReportVO, String reportName, XMLContainer xmlRequest) {
		try {
			setReportName(reportName);

			// ReportParameter des benötigten Typs erzeugen
	        hotelListReportParameter = new HotelListReportParameter(xmlRequest);

	        // ReportParameter der Superklasse bekannt machen
	        setReportParameter(hotelListReportParameter);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected void doAddPages() {
		EventListWizardPage eventListWizardPage = new EventListWizardPage();
		eventListWizardPage.setDescription(ReportWizardI18N.EventListWizardPage_Description_Hotel);
		addPage(eventListWizardPage);

		addPage(new CountriesWizardPage());
		citiesWizardPage = new CitiesWizardPage();
		addPage(citiesWizardPage);
	}


	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		// skip CitiesWizardPage if no countries are selected
		IWizardPage nextPage = super.getNextPage(page);
		if (nextPage instanceof CitiesWizardPage) {
			Collection<String> countryCodes = hotelListReportParameter.getCountryCodes();
			if (countryCodes == null || countryCodes.isEmpty()) {
				nextPage = super.getNextPage(nextPage);
			}
		}
		return nextPage;
	}


	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		// skip CitiesWizardPage if no countries are selected
		IWizardPage prevPage = super.getPreviousPage(page);
		if (prevPage instanceof CitiesWizardPage) {
			Collection<String> countryCodes = hotelListReportParameter.getCountryCodes();
			if (countryCodes == null || countryCodes.isEmpty()) {
				prevPage = super.getPreviousPage(prevPage);
			}
		}
		return prevPage;
	}

}

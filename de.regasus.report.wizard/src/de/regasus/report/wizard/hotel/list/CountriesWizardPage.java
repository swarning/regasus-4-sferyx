package de.regasus.report.wizard.hotel.list;

import static de.regasus.LookupService.getHotelMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import de.regasus.common.Country;
import com.lambdalogic.messeinfo.hotel.report.parameter.ICountriesReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.CountryModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class CountriesWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "CountriesWizardPage";

	private CountryModel countryModel;
	private ListViewer listViewer;
	private ICountriesReportParameter countriesReportParameter;
	private String language;


	public CountriesWizardPage() {
		super(ID);
		setTitle(ContactLabel.countries.getString());
		setDescription(ReportWizardI18N.CountriesWizardPage_Description);
		countryModel = CountryModel.getInstance();
		language = Locale.getDefault().getLanguage();
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new FillLayout());
		//
		setControl(container);

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Country) element).getName().getString(language);
			}
		});
		listViewer.setSorter(new ViewerSorter());
	}



	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ICountriesReportParameter) {
			countriesReportParameter = (ICountriesReportParameter) reportParameter;

			// init models
			try {
				java.util.List<String> hotelCountryCodes = getHotelMgr().getHotelCountryPKs();
				List<Country> hotelCountryList = new ArrayList<>( hotelCountryCodes.size() );
				for (String countryCode : hotelCountryCodes) {
					Country country = countryModel.getCountry(countryCode);
					hotelCountryList.add(country);
				}
				listViewer.setInput(hotelCountryList);

				// select the parameters values
				Collection<String> countryCodes = countriesReportParameter.getCountryCodes();
				if (countryCodes == null || countryCodes.isEmpty()) {
					listViewer.setSelection(new StructuredSelection());
				}
				else {
					List<Country> countryList = new ArrayList<>( countryCodes.size() );
					for (String countryCode : countryCodes) {
						Country country = countryModel.getCountry(countryCode);
						countryList.add(country);
					}

					listViewer.setSelection(new StructuredSelection(countryList), true);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	public void saveReportParameters() {
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		Collection<Country> selectedCountryCol = new ArrayList<>( selection.size() );

		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Country country = (Country) it.next();
			selectedCountryCol.add(country);
		}

		if (countriesReportParameter != null) {
			List<String> countryCodes = null;
			I18NPattern description = new I18NPattern();

			if ( !selectedCountryCol.isEmpty() ) {
				// create lists of country codes and a String with all names
				countryCodes = new ArrayList<>( selectedCountryCol.size() );
				StringBuilder countryNames = new StringBuilder(selectedCountryCol.size() * 50);
				for (Country country : selectedCountryCol) {
					countryCodes.add(country.getId());

					if (countryNames.length() > 0) {
						countryNames.append(", ");
					}
					countryNames.append(country.getName().getString(language));
				}

				description.append(ContactLabel.countries.getString());
				description.append(": ");
				description.append(countryNames.toString());
			}

			countriesReportParameter.setCountryCodes(countryCodes);
			countriesReportParameter.setDescription(
				ICountriesReportParameter.DESCRIPTION_ID,
				description.toString()
			);
		}
	}

}

package de.regasus.report.wizard.hotel.list;

import static de.regasus.LookupService.getHotelMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import com.lambdalogic.messeinfo.hotel.report.hotelList.IHotelListReportParameter;
import com.lambdalogic.messeinfo.hotel.report.parameter.ICitiesReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class CitiesWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "hotellist.CitiesWizardPage";

	private ListViewer listViewer;
	private IHotelListReportParameter hotelListReportParameter;


	public CitiesWizardPage() {
		super(ID);
		setTitle(ContactLabel.cities.getString());
		setDescription(ReportWizardI18N.CitiesWizardPage_Description);
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		//
		setControl(container);

		listViewer = new ListViewer(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});
		listViewer.setSorter(new ViewerSorter());
	}



	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IHotelListReportParameter) {
			hotelListReportParameter = (IHotelListReportParameter) reportParameter;

			/* Daten des ListViewers ermitteln und setzen.
			 * Der ListViewer kann erst nach dem Setzen der ReportParameter mit Daten gefüllt werden,
			 * da die Cities von den dort gesetzten Countries abhängen.
			 */
			Collection<String> countryCodes = hotelListReportParameter.getCountryCodes();
			if (countryCodes != null &&  ! countryCodes.isEmpty()) {
				List<String> allCities = new ArrayList<>();
				try {
					for (String countryCode : countryCodes) {
						java.util.List<String> countryCities = getHotelMgr().getHotelCities(countryCode);
						allCities.addAll(countryCities);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}

				listViewer.setInput(allCities);
			}

			// select the parameters values
			java.util.List<String> cities = hotelListReportParameter.getCities();
			if (cities == null || cities.isEmpty()) {
				listViewer.setSelection(new StructuredSelection());
			}
			else {
				listViewer.setSelection(new StructuredSelection(cities), true);
			}
		}
	}


	@Override
	public void saveReportParameters() {
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		ArrayList<String> selectedCities = new ArrayList<>( selection.size() );

		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			selectedCities.add((String) it.next());
		}

		if (hotelListReportParameter != null) {
			I18NPattern description = new I18NPattern();

			if ( !selectedCities.isEmpty() ) {
				// create lists of country codes and a String with all names
				StringBuilder cityNames = new StringBuilder(selectedCities.size() * 50);
				for (String city : selectedCities) {
					if (cityNames.length() > 0) {
						cityNames.append(", ");
					}
					cityNames.append(city);
				}


				description.append(ContactLabel.cities.getString());
				description.append(": ");
				description.append(cityNames.toString());
			}

			hotelListReportParameter.setCities(selectedCities);
			hotelListReportParameter.setDescription(
				ICitiesReportParameter.DESCRIPTION_ID,
				description.toString()
			);
		}
	}

}

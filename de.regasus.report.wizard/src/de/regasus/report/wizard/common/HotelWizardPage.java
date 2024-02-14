package de.regasus.report.wizard.common;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static de.regasus.LookupService.getHotelMgr;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelCVOSettings;
import com.lambdalogic.messeinfo.hotel.report.parameter.IHotelReportParameter;
import com.lambdalogic.messeinfo.participant.report.parameter.IEventReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelTable;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;


public class HotelWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "HotelWizardPage";

	private IHotelReportParameter hotelReportParameter;

	// Widgets
	private TableViewer tableViewer;


	public HotelWizardPage() {
		super(ID);
		setTitle(HotelLabel.Hotel.getString());
		setDescription(ReportWizardI18N.HotelWizardPage_Description);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());

		setControl(container);

		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tableColumn = null;

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(250);
		tableColumn.setText(HotelLabel.Hotel.getString());

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(200);
		tableColumn.setText( Address.CITY.getString() );

		HotelTable hotelTable = new HotelTable(table);
		tableViewer = hotelTable.getViewer();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IHotelReportParameter) {
			hotelReportParameter = (IHotelReportParameter) reportParameter;


			/*
			 * If the reportParameter are implementing IEventReportParameter and contain an eventPK,
			 * then hotels are limited to those that belong to this event.
			 */
			List<Long> eventPKs = null;
			if (reportParameter instanceof IEventReportParameter) {
				IEventReportParameter eventReportParameter = (IEventReportParameter) reportParameter;
				Long eventPK = eventReportParameter.getEventPK();
				eventPKs = createArrayList(eventPK);
			}


			// load and show Hotels
			HotelCVOSettings settings = new HotelCVOSettings();
			List<HotelCVO> hotelCVOs = null;
			try {
				hotelCVOs = getHotelMgr().getHotelCVOs(
					settings,
					eventPKs,
					null,		// countryPKs
					null,		// cityNames
					true		// withDeleted
				);

				tableViewer.setInput(hotelCVOs);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}


			// get hotelPK from report parameter
			Long hotelPK = hotelReportParameter.getHotelPK();

			// find hotelCVO with hotelPK
			HotelCVO hotelCVO = null;
			if (hotelPK != null) {
				for (HotelCVO hCVO : hotelCVOs) {
					if (hotelPK.equals(hCVO.getPK())) {
						hotelCVO = hCVO;
						break;
					}
				}
			}

			StructuredSelection selection = null;
			if (hotelCVO != null) {
				selection = new StructuredSelection(hotelCVO);
			}
			else {
				selection = new StructuredSelection();
			}

			tableViewer.setSelection(selection, true);
		}
	}


	@Override
	public void saveReportParameters() {
		HotelCVO hotelCVO = null;
		Long hotelPK = null;
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		if (selection.size() == 1) {
			hotelCVO = (HotelCVO) selection.getFirstElement();
			hotelPK = hotelCVO.getPK();
		}

		if (hotelReportParameter != null) {
			// set parameter
			hotelReportParameter.setHotelPK(hotelPK);

			// set description
			String description = null;
			if (hotelCVO != null) {
    			I18NPattern i18nPattern = new I18NPattern();
    			i18nPattern.append(HotelLabel.Hotel.getString());
    			i18nPattern.append(": ");
    			i18nPattern.append(hotelCVO.getName());

    			description = i18nPattern.getString();
			}

			hotelReportParameter.setDescription(
				IHotelReportParameter.DESCRIPTION_ID,
				description
			);
		}
	}

}

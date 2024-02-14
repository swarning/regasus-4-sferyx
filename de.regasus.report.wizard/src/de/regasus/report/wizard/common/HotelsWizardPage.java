package de.regasus.report.wizard.common;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static de.regasus.LookupService.getHotelMgr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.lambdalogic.messeinfo.hotel.report.parameter.IHotelsReportParameter;
import com.lambdalogic.messeinfo.kernel.data.AbstractCVO;
import com.lambdalogic.messeinfo.participant.report.parameter.IEventReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelTable;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class HotelsWizardPage
extends WizardPage
implements IReportWizardPage {
	public static final String ID = "HotelsWizardPage";

	private IHotelsReportParameter hotelsReportParameter;

	// Widgets
	private TableViewer tableViewer;


	public HotelsWizardPage() {
		super(ID);
		setTitle(HotelLabel.Hotels.getString());
		setDescription(ReportWizardI18N.HotelsWizardPage_Description);
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

		final Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tableColumn = null;

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(250);
		tableColumn.setText(HotelLabel.Hotel.getString());

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(200);
		tableColumn.setText( Address.CITY.getString() );

		final HotelTable hotelTable = new HotelTable(table);
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
		if (reportParameter instanceof IHotelsReportParameter) {
			hotelsReportParameter = (IHotelsReportParameter) reportParameter;


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


			// In den Parametern gesetzte Hotels ermitteln und in Tabelle selektieren
			Map<Long, HotelCVO> hotelMap = AbstractCVO.abstractCVOs2Map(hotelCVOs);
			List<Long> hotelPKs = hotelsReportParameter.getHotelPKs();
			List<HotelCVO> selectedHotelCVOs = null;
			StructuredSelection selection = null;
			if (hotelPKs != null && ! hotelPKs.isEmpty()) {
				selectedHotelCVOs = new ArrayList<>(hotelPKs.size());
				for (Long hotelPK : hotelPKs) {
					HotelCVO hotelCVO = hotelMap.get(hotelPK);
					if (hotelCVO != null) {
						selectedHotelCVOs.add(hotelCVO);
					}
				}
				selection = new StructuredSelection(selectedHotelCVOs);
			}
			else {
				selection = new StructuredSelection();
			}
			tableViewer.setSelection(selection, true);
		}
	}


	@Override
	public void saveReportParameters() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		List<HotelCVO> hotelCVOs = new ArrayList<>( selection.size() );
		List<Long> hotelPKs = new ArrayList<>( selection.size() );

		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			HotelCVO hotelCVO = (HotelCVO) it.next();
			hotelCVOs.add(hotelCVO);
			hotelPKs.add( hotelCVO.getPK() );
		}

		if (hotelsReportParameter != null) {
			hotelsReportParameter.setHotelPKs(hotelPKs);

			String description = null;
			if ( !hotelCVOs.isEmpty() ) {
				I18NPattern i18nPattern = new I18NPattern();
				i18nPattern.append(HotelLabel.Hotels.getString());
				i18nPattern.append(": ");

				int i = 0;
				for (HotelCVO hotelCVO : hotelCVOs) {
					if (i++ > 0) {
						i18nPattern.append(", ");
					}
					i18nPattern.append(hotelCVO.getName());
				}

				description = i18nPattern.getString();
			}

			hotelsReportParameter.setDescription(
				IHotelsReportParameter.DESCRIPTION_ID,
				description
			);
		}
	}

}

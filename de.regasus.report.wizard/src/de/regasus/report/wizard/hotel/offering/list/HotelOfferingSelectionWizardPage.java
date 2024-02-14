package de.regasus.report.wizard.hotel.offering.list;

import static de.regasus.LookupService.getHotelOfferingMgr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.HotelOfferingParameter;
import com.lambdalogic.messeinfo.hotel.data.HotelCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.VolumeVO;
import com.lambdalogic.messeinfo.hotel.report.hotelOfferingList.HotelOfferingListReportParameter;
import com.lambdalogic.messeinfo.hotel.report.parameter.IHotelOfferingIDsReportParameter;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.booking.dialog.HotelOfferingsTable;
import de.regasus.hotel.booking.dialog.HotelOfferingsTableColumns;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ui.Activator;

public class HotelOfferingSelectionWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "HotelOfferingSelectionPage";

	// Widgets
	private HotelOfferingsTable hotelOfferingsTable;
	private TableViewer tableViewer;

	private TableColumnLayout tableColumnLayout;
	private TableColumn[] nightTableColumns =
		new TableColumn[HotelOfferingsTableColumns.values().length - HotelOfferingsTableColumns.NIGHT00.ordinal()];

	// ConfigParameterSet
	private HotelConfigParameterSet configParameterSet;

	// Data
	private List<HotelOfferingCVO> hotelOfferingCVOs;
	private HotelOfferingListReportParameter hotelOfferingListReportParameter;
	private IHotelOfferingIDsReportParameter hotelOfferingIDsReportParameter;

	/**
	 * For each HotelOfferingCVO, this map stores the possibly edited prices
	 */
	private TreeMap<Long, PriceVO> depositMap = new TreeMap<>();


	protected static final TimeFormatter TIME_FORMATTER = TimeFormatter.getInstance("d.M");


	public HotelOfferingSelectionWizardPage() {
		super(ID);

		setTitle(HotelLabel.HotelOfferingSelectionPage_Title.getString());
		setDescription(HotelLabel.HotelOfferingSelectionPage_Description.getString());

		this.configParameterSet = new HotelConfigParameterSet();
	}


	@Override
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(1, false));


		// create Composite for the SWT Table and set a TableColumnLayout
		Composite tableComposite = new Composite(controlComposite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);


		// create SWT Table
		final Table table = new Table(tableComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Book (invisible)
		TableColumn bookTableColumn = new TableColumn(table, SWT.NONE);
		bookTableColumn.setText("");
		bookTableColumn.setWidth(0);
		tableColumnLayout.setColumnData(bookTableColumn, new ColumnWeightData(0, 0, false));

		// Count (invisible)
		TableColumn countTableColumn = new TableColumn(table, SWT.RIGHT);
		countTableColumn.setText("");
		countTableColumn.setWidth(0);
		tableColumnLayout.setColumnData(countTableColumn, new ColumnWeightData(0, 0, false));

		// Hotel
		final TableColumn hotelTableColumn = new TableColumn(table, SWT.NONE);
		hotelTableColumn.setText(HotelLabel.Hotel.getString());
		// descTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(hotelTableColumn, new ColumnWeightData(130));

		// Contingent
		final TableColumn contingentTableColumn = new TableColumn(table, SWT.NONE);
		contingentTableColumn.setText(HotelLabel.Contingent.getString());
		tableColumnLayout.setColumnData(contingentTableColumn, new ColumnWeightData(130));

		// Guest Count
		TableColumn guestCountTableColumn = new TableColumn(table, SWT.RIGHT);
		guestCountTableColumn.setText(HotelLabel.HotelBooking_GuestCount.getString());
		guestCountTableColumn.setWidth(80);
		tableColumnLayout.setColumnData(guestCountTableColumn, new ColumnWeightData(0));

		// Description
		TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
		descriptionTableColumn.setText(UtilI18N.Description);
		//guestCountTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(descriptionTableColumn, new ColumnWeightData(150));

		// Category
		TableColumn categoryTableColumn = new TableColumn(table, SWT.RIGHT);
		categoryTableColumn.setText(HotelLabel.Hotel_Category.getString());
		//categoryTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(categoryTableColumn, new ColumnWeightData(70));

		// Deposit
		TableColumn depositTableColumn = new TableColumn(table, SWT.RIGHT);
		depositTableColumn.setText(HotelLabel.HotelBooking_Deposit.getString());
		//depositTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(depositTableColumn, new ColumnWeightData(80));

		// Lodging-Price
		TableColumn lodgePriceTableColumn = new TableColumn(table, SWT.RIGHT);
		lodgePriceTableColumn.setText(HotelLabel.Lodge.getString());
		//lodgePriceTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(lodgePriceTableColumn, new ColumnWeightData(100));


		// Columns for additional prices: Breakfast Price, Add Price 1, Add Price 2, Total Price
		TableColumn bfPriceTableColumn = new TableColumn(table, SWT.RIGHT);
		TableColumn addPrice1Column = new TableColumn(table, SWT.RIGHT);
		TableColumn addPrice2Column = new TableColumn(table, SWT.RIGHT);
		TableColumn totalPriceColumn = new TableColumn(table, SWT.RIGHT);

		// ColumnWeightData for all additional price columns
		ColumnWeightData addPriceColumnWeightData = new ColumnWeightData(100);

		if (configParameterSet.getAdditionalPrice().isVisible()) {
			// if additional columns are visible: set column names
			bfPriceTableColumn.setText(HotelLabel.Breakfast.getString());
			addPrice1Column.setText(InvoiceLabel.Add1Price.getString());
			addPrice2Column.setText(InvoiceLabel.Add2Price.getString());
			totalPriceColumn.setText(InvoiceLabel.TotalPrice.getString());
		}
		else {
			// if additional columns are not visible: make columns as small as possible
			addPriceColumnWeightData.weight = 0;
			addPriceColumnWeightData.minimumWidth = 0;
			addPriceColumnWeightData.resizable = false;

			bfPriceTableColumn.setResizable(false);
			addPrice1Column.setResizable(false);
			addPrice2Column.setResizable(false);
			totalPriceColumn.setResizable(false);
		}

		tableColumnLayout.setColumnData(bfPriceTableColumn, addPriceColumnWeightData);
		tableColumnLayout.setColumnData(addPrice1Column, addPriceColumnWeightData);
		tableColumnLayout.setColumnData(addPrice2Column, addPriceColumnWeightData);
		tableColumnLayout.setColumnData(totalPriceColumn, addPriceColumnWeightData);


		// create one TableColumn for each night
		for (int nightIndex = 0; nightIndex < nightTableColumns.length; nightIndex++) {
			TableColumn nightTableColumn = new TableColumn(table, SWT.RIGHT);
			tableColumnLayout.setColumnData(nightTableColumn, new ColumnWeightData(0));
			nightTableColumns[nightIndex] = nightTableColumn;
		}


		hotelOfferingsTable = new HotelOfferingsTable(table, depositMap);
		hotelOfferingsTable.setBookColumnCheckBox(false);

		tableViewer = hotelOfferingsTable.getViewer();

		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				setPageComplete( !event.getSelection().isEmpty() );
			}
		});

		setControl(controlComposite);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof HotelOfferingListReportParameter) {
			hotelOfferingListReportParameter = (HotelOfferingListReportParameter) reportParameter;
			updateOfferingTable();

			if (tableViewer != null && reportParameter instanceof HotelOfferingListReportParameter) {
				hotelOfferingIDsReportParameter = (IHotelOfferingIDsReportParameter) reportParameter;
				List<HotelOfferingCVO> selectedHotelOfferingCVOs = null;
				Collection<Long> hotelOfferingPKs = hotelOfferingIDsReportParameter.getHotelOfferingPKs();

				if (hotelOfferingPKs != null) {
					selectedHotelOfferingCVOs = CollectionsHelper.createArrayList(hotelOfferingPKs.size());
					for (HotelOfferingCVO hotelOfferingCVO : hotelOfferingCVOs){
						if (hotelOfferingPKs.contains(hotelOfferingCVO.getPK())){
							selectedHotelOfferingCVOs.add(hotelOfferingCVO);
						}
					}
				}

				StructuredSelection selection = null;
				if (selectedHotelOfferingCVOs != null) {
					selection = new StructuredSelection(selectedHotelOfferingCVOs);
				}
				else {
					selection = new StructuredSelection();
				}
				tableViewer.setSelection(selection, true);
			}

		}
	}


	public void setPageComplete() {
		setPageComplete(!tableViewer.getSelection().isEmpty());
	}


	private void updateOfferingTable() {
		Long eventPK = hotelOfferingListReportParameter.getEventPK();
		Long hotelPK = hotelOfferingListReportParameter.getHotelPK();
		String category = hotelOfferingListReportParameter.getCategory();
		I18NDate firstNight = TypeHelper.toI18NDate( hotelOfferingListReportParameter.getArrival() );
		I18NDate departure = TypeHelper.toI18NDate( hotelOfferingListReportParameter.getDeparture() );
		Integer bedCount = hotelOfferingListReportParameter.getGuestCount();
		Integer minRoomCount = hotelOfferingListReportParameter.getMinimumRoomCount();
		String currency = hotelOfferingListReportParameter.getCurrency();
		BigDecimal minAmount = hotelOfferingListReportParameter.getMinimumAmount();
		BigDecimal maxAmount = hotelOfferingListReportParameter.getMaximumAmount();

		HotelContingentCVOSettings hotelContingentCVOSettings = new HotelContingentCVOSettings();
		hotelContingentCVOSettings.withRoomDefinitionVOs = true;
		hotelContingentCVOSettings.withVolumeVOs = true;
		hotelContingentCVOSettings.hotelCVOSettings = new HotelCVOSettings();

		HotelOfferingCVOSettings hotelOfferingCVOSettings = new HotelOfferingCVOSettings(hotelContingentCVOSettings);
		hotelOfferingCVOSettings.withRoomDefinitionVO = true;

		try {
			HotelOfferingParameter hotelOfferingParameter = new HotelOfferingParameter(eventPK);
			hotelOfferingParameter.setHotelPK(hotelPK);
			hotelOfferingParameter.setArrival(firstNight);
			hotelOfferingParameter.setDeparture(departure);
			hotelOfferingParameter.setCategory(category);
			hotelOfferingParameter.setGuestCount(bedCount);
			hotelOfferingParameter.setMinRoomCount(minRoomCount);
			hotelOfferingParameter.setCurrency(currency);
			hotelOfferingParameter.setMinAmount(minAmount);
			hotelOfferingParameter.setMaxAmount(maxAmount);

			hotelOfferingCVOs = getHotelOfferingMgr().getHotelOfferingCVOs(
				hotelOfferingParameter,
				hotelOfferingCVOSettings
			);


			I18NDate minVolumeNight = null;
			I18NDate maxVolumeNight = null;

			for (HotelOfferingCVO hotelOfferingCVO : hotelOfferingCVOs) {
				PriceVO lodgePrice = hotelOfferingCVO.getOfferingVO().getLodgePriceVO().clone();
				PriceVO deposit = lodgePrice.clone();
				deposit.setAmount(hotelOfferingCVO.getHotelOfferingVO().getDeposit());

				if (! depositMap.containsKey(hotelOfferingCVO.getPK())) {
					depositMap.put(hotelOfferingCVO.getPK(), deposit);
				}

				// set minVolumeNight ans maxVolumeNight
				List<VolumeVO> volumeVOs = hotelOfferingCVO.getHotelContingentCVO().getVolumes();
				if (volumeVOs != null && !volumeVOs.isEmpty()) {
					VolumeVO firstVolumeVO = volumeVOs.get(0);
					VolumeVO lastVolumeVO = volumeVOs.get(volumeVOs.size() - 1);

					if (minVolumeNight == null || firstVolumeVO.getDay().isBefore(minVolumeNight)) {
						minVolumeNight = firstVolumeVO.getDay();
					}
					if (maxVolumeNight == null || lastVolumeVO.getDay().isAfter(maxVolumeNight)) {
						maxVolumeNight = lastVolumeVO.getDay();
					}
				}
			}

			hotelOfferingsTable.setFirstNight(minVolumeNight);

			I18NDate[] nights = DateHelper.getDays(minVolumeNight, maxVolumeNight);
			for (int nightIndex = 0; nightIndex < nightTableColumns.length; nightIndex++) {
				TableColumn nightTableColumn = nightTableColumns[nightIndex];
				if (nightIndex < nights.length) {
					String nightText = TIME_FORMATTER.format(nights[nightIndex]);
					nightTableColumn.setText(nightText);
					nightTableColumn.setWidth(35);
				}
				else {
					nightTableColumn.setWidth(0);
					tableColumnLayout.setColumnData(nightTableColumn, new ColumnWeightData(0, 0, false));
				}
			}

			hotelOfferingsTable.setInput(hotelOfferingCVOs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void saveReportParameters() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

		List<HotelOfferingCVO> hotelOfferingCVOs = new ArrayList<>( selection.size() );
		List<Long> hotelOfferingPKs = new ArrayList<>( selection.size() );
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			HotelOfferingCVO hotelOfferingCVO = (HotelOfferingCVO) it.next();
			hotelOfferingCVOs.add(hotelOfferingCVO);
			hotelOfferingPKs.add( hotelOfferingCVO.getPK() );
		}

		if (hotelOfferingIDsReportParameter != null) {
			StringBuilder desc = new StringBuilder(hotelOfferingCVOs.size() * 100);

			if (!hotelOfferingCVOs.isEmpty()) {
    			desc.append(HotelLabel.HotelOffering);
    			desc.append(": ");

    			int i = 0;
    			for (HotelOfferingCVO hotelOfferingCVO : hotelOfferingCVOs){
    				if (i++ > 0) {
    					desc.append(", ");
    				}
    				desc.append(hotelOfferingCVO.getShortLabel());
    			}
			}


			hotelOfferingIDsReportParameter.setHotelOfferingPKs(hotelOfferingPKs);
			hotelOfferingIDsReportParameter.setDescription(
				IHotelOfferingIDsReportParameter.DESCRIPTION_ID,
				desc.toString()
			);
		}
	}

}
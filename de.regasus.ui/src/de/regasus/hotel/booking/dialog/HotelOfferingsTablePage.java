package de.regasus.hotel.booking.dialog;

import static de.regasus.LookupService.getHotelOfferingMgr;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.HotelOfferingParameter;
import com.lambdalogic.messeinfo.hotel.data.HotelCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingCVOSettings;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingHelper;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.hotel.data.VolumeVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.time.TimeFormatter;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.simpleviewer.ITableEditListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


/**
 * A page in the wizard to create hotel bookings where the user can check one of several possible
 * offerings and possibly also alter the price and deposit.
 * <p>
 * See https://mi2.lambdalogic.de/jira/browse/MIRCP-84
 */
public class HotelOfferingsTablePage extends WizardPage {

	public static final String NAME = "HotelOfferingsTablePage";

	private static final HotelOfferingCVOSettings CVO_SETTINGS = new HotelOfferingCVOSettings();
	static {
		CVO_SETTINGS.withRoomDefinitionVO = true;

		CVO_SETTINGS.hotelContingentCVOSettings = new HotelContingentCVOSettings();
		CVO_SETTINGS.hotelContingentCVOSettings.withRoomDefinitionVOs = true;
		CVO_SETTINGS.hotelContingentCVOSettings.withVolumeVOs = true;
		CVO_SETTINGS.hotelContingentCVOSettings.hotelCVOSettings = new HotelCVOSettings();
	}

	protected static final TimeFormatter TIME_FORMATTER = TimeFormatter.getInstance("d.M");


	private Long filterHotelContingentId;

	private ModifySupport modifySupport = new ModifySupport();

	private TreeMap<Long, PriceVO> depositMap = new TreeMap<>();

	private HotelOfferingsTable hotelOfferingsTable;

	private List<HotelOfferingCVO> hotelOfferingCVOs;

	// TableColumns that hide or show dynamically dependent on the data
	private TableColumnLayout tableColumnLayout;
	private TableColumn lodgePriceTableColumn;
	private TableColumn bfPriceTableColumn;
	private TableColumn addPrice1TableColumn;
	private TableColumn addPrice2TableColumn;
	private TableColumn[] nightTableColumns = new TableColumn[HotelOfferingsTableColumns.values().length - HotelOfferingsTableColumns.NIGHT00.ordinal()];


	public HotelOfferingsTablePage() {
		super(NAME);

		setTitle(I18N.CreateHotelBooking_HotelOfferingsTablePage_Title);
		setMessage(I18N.CreateHotelBooking_HotelOfferingsTablePage_Message);
	}


	public void setHotelContingentFilter(Long hotelContingentId) {
		this.filterHotelContingentId = hotelContingentId;
	}


	@Override
	public CreateHotelBookingWizard getWizard() {
		return (CreateHotelBookingWizard) super.getWizard();
	}


	public void addModifyListener(ModifyListener listener) {
		modifySupport.addListener(listener);
	}


	@Override
	public void createControl(Composite parent) {
		// create Composite for the SWT Table and set a TableColumnLayout
		Composite tableComposite = new Composite(parent, SWT.NONE);
		setControl(tableComposite);

		tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		// create SWT Table
		final Table table = new Table(tableComposite, SWT.BORDER /* SWT.SINGLE | SWT.FULL_SELECTION */);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Book
		final TableColumn bookTableColumn = new TableColumn(table, SWT.NONE);
		tableColumnLayout.setColumnData(bookTableColumn, new ColumnWeightData(0, 25, true));

		// Count
		final TableColumn countTableColumn = new TableColumn(table, SWT.RIGHT);
		countTableColumn.setText(UtilI18N.Count);
		tableColumnLayout.setColumnData(countTableColumn, new ColumnWeightData(0, 40, true));

		// Hotel
		final TableColumn hotelTableColumn = new TableColumn(table, SWT.NONE);
		hotelTableColumn.setText(HotelLabel.Hotel.getString());
		// descTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(hotelTableColumn, new ColumnWeightData(130));

		// Contingent
		final TableColumn contingentTableColumn = new TableColumn(table, SWT.NONE);
		contingentTableColumn.setText( HotelLabel.Contingent.getString() );
		tableColumnLayout.setColumnData(contingentTableColumn, new ColumnWeightData(130));

		// Guest Count
		final TableColumn guestCountTableColumn = new TableColumn(table, SWT.RIGHT);
		guestCountTableColumn.setText(HotelLabel.HotelBooking_GuestCount.getString());
		tableColumnLayout.setColumnData(guestCountTableColumn, new ColumnWeightData(0, 40, true));

		// Description
		final TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
		descriptionTableColumn.setText(UtilI18N.Description);
		//guestCountTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(descriptionTableColumn, new ColumnWeightData(150));

		// Category
		final TableColumn categoryTableColumn = new TableColumn(table, SWT.RIGHT);
		categoryTableColumn.setText(HotelLabel.Hotel_Category.getString());
		//categoryTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(categoryTableColumn, new ColumnWeightData(70));

		// Deposit
		final TableColumn depositTableColumn = new TableColumn(table, SWT.RIGHT);
		depositTableColumn.setText(HotelLabel.HotelBooking_Deposit.getString());
		//depositTableColumn.setWidth(...);
		tableColumnLayout.setColumnData(depositTableColumn, new ColumnWeightData(80));

		// Lodging Price
		lodgePriceTableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamic setting is done too late
		tableColumnLayout.setColumnData(lodgePriceTableColumn, new ColumnWeightData(100));

		// Breakfast Price (hide/show dynamically)
		bfPriceTableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamic setting is done too late
		tableColumnLayout.setColumnData(bfPriceTableColumn, new ColumnWeightData(100));

		// Additional Price 1 (hide/show dynamically)
		addPrice1TableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamic setting is done too late
		tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(100));

		// Additional Price 2 (hide/show dynamically)
		addPrice2TableColumn = new TableColumn(table, SWT.RIGHT);
		// setting default ColumnData to avoid Exception, because dynamic setting is done too late
		tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(100));

		// Total Price
		final TableColumn totalPriceTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(totalPriceTableColumn, new ColumnWeightData(100));
		totalPriceTableColumn.setText(InvoiceLabel.TotalPrice.getString());


		// create TableColumn for each night
		for (int nightIndex = 0; nightIndex < nightTableColumns.length; nightIndex++) {
			TableColumn nightTableColumn = new TableColumn(table, SWT.RIGHT);
			tableColumnLayout.setColumnData(nightTableColumn, new ColumnWeightData(0));
			nightTableColumns[nightIndex] = nightTableColumn;
		}



		hotelOfferingsTable = new HotelOfferingsTable(table, depositMap);

		hotelOfferingsTable.addEditListener(new ITableEditListener() {
			@Override
			public void tableCellChanged() {
				boolean anythingBooked = hotelOfferingsTable.isAnythingBooked();
				setPageComplete(anythingBooked);

				if (anythingBooked) {
					modifySupport.fire(table);
				}
			}
		});

		setPageComplete(false);
	}


	/**
	 * Whenever this page is made visible, it tries to get the bookingParamters from the wizard to show them in the
	 * table.
	 */
	@Override
	public void setVisible(boolean visible) {
		// Don't(!) do anything if this page is made invisible!
		if (visible) {
			updateOfferingTable();
		}
		super.setVisible(visible);
	}


	private void updateOfferingTable() {
		try {
			loadOfferings();
			List<HotelOfferingVO> hotelOfferingVOs = HotelOfferingCVO.getVOs(hotelOfferingCVOs);

			String lang = Locale.getDefault().getLanguage();

			// *************************************************************************************
			// * hide/show column for breakfast price
			// *

			boolean showBfPrice = false;
			{
    			// determine if at least one offering has a breakfast price
    			for (HotelOfferingVO hotelOfferingVO : hotelOfferingVOs) {
    				if (hotelOfferingVO.isWithBfPrice()) {
    					showBfPrice = true;
    					break;
    				}
    			}

    			// show or hide column of breakfast price
    			if (showBfPrice) {
        			// show column
    				tableColumnLayout.setColumnData(bfPriceTableColumn, new ColumnWeightData(100));
    				bfPriceTableColumn.setText( HotelLabel.Breakfast.getString() );
    			}
    			else {
    				// hide column
    				bfPriceTableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(bfPriceTableColumn, new ColumnWeightData(0, 0, false));
    				bfPriceTableColumn.setText("");
    			}

    			bfPriceTableColumn.setResizable(showBfPrice);
			}

			// *
			// * hide/show column for breakfast price
			// *************************************************************************************

			// *************************************************************************************
			// * hide/show column for additional price 1
			// *

			boolean showAdd1Price = false;
			{
    			// determine if at least one offering has an additional price 1
    			for (HotelOfferingVO hotelOfferingVO : hotelOfferingVOs) {
    				if (hotelOfferingVO.isWithAdd1Price()) {
    					showAdd1Price = true;
    					break;
    				}
    			}

    			// show or hide column of additional price 1
    			if (showAdd1Price) {
    				/* Determine column name for column of additional price 1:
    				 * If all Offerings that have an additional price 1 share the same name, use it as
    				 * column name. Otherwise use the default name.
    				 */
        			String columnName = HotelOfferingHelper.getAddPrice1Name(hotelOfferingVOs, lang);

        			// show column
    				tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(100));
    				addPrice1TableColumn.setText(columnName);
    			}
    			else {
    				// hide column
    				addPrice1TableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(addPrice1TableColumn, new ColumnWeightData(0, 0, false));
    				addPrice1TableColumn.setText("");
    			}

    			addPrice1TableColumn.setResizable(showAdd1Price);
			}

			// *
			// * hide/show column for additional price 1
			// *************************************************************************************

			// *************************************************************************************
			// * hide/show column for additional price 2
			// *

			boolean showAdd2Price = false;
			{
    			// determine if at least one offering has an additional price 2
    			for (HotelOfferingVO hotelOfferingVO : hotelOfferingVOs) {
    				if (hotelOfferingVO.isWithAdd1Price()) {
    					showAdd2Price = true;
    					break;
    				}
    			}

    			// show or hide column of additional price 2
    			if (showAdd2Price) {
    				/* Determine column name for column of additional price 2:
    				 * If all Offerings that have an additional price 1 share the same name, use it as
    				 * column name. Otherwise use the default name.
    				 */
        			String columnName = HotelOfferingHelper.getAddPrice2Name(hotelOfferingVOs, lang);

        			// show column
    				tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(100));
    				addPrice2TableColumn.setText(columnName);
    			}
    			else {
    				// hide column
    				addPrice2TableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(addPrice2TableColumn, new ColumnWeightData(0, 0, false));
    				addPrice2TableColumn.setText("");
    			}

    			addPrice2TableColumn.setResizable(showAdd2Price);
			}

			// *
			// * hide/show column for additional price 2
			// *************************************************************************************

			// *************************************************************************************
			// * hide/show column for lodge price
			// *

			{
				/* The column for lodge price is visible if either the column for
				 * breakfast price, additional price 1 or additional price 2 is visible.
				 * Otherwise the lodge price is equal to total price and should not be
				 * visible.
				 */
				//boolean showLodgePrice = showBfPrice || showAdd1Price || showAdd2Price;
				boolean showLodgePrice = true;

				String columnName = HotelLabel.Lodge.getString();

    			// show or hide column of lodge price
    			if (showLodgePrice) {
        			// show column
    				tableColumnLayout.setColumnData(lodgePriceTableColumn, new ColumnWeightData(100));
    				lodgePriceTableColumn.setText(columnName);
    			}
    			else {
    				// hide column
    				lodgePriceTableColumn.setWidth(0);
    				tableColumnLayout.setColumnData(lodgePriceTableColumn, new ColumnWeightData(0, 0, false));
    				lodgePriceTableColumn.setText("");
    			}

    			lodgePriceTableColumn.setResizable(showLodgePrice);
			}

			// *
			// * hide/show column for lodge price
			// *************************************************************************************

			// *************************************************************************************
			// * hide/show column for nights
			// *

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
					// show column
					String nightText = TIME_FORMATTER.format(nights[nightIndex]);
					nightTableColumn.setText(nightText);
					nightTableColumn.setWidth(35);
				}
				else {
					// hide column
					nightTableColumn.setWidth(0);
					tableColumnLayout.setColumnData(nightTableColumn, new ColumnWeightData(0, 0, false));
				}
			}

			// *
			// * hide/show column for nights
			// *************************************************************************************

			hotelOfferingsTable.setInput(hotelOfferingCVOs);

			// refresh table layout to hide/show dynamic columns
			SWTHelper.deferredLayout(300, hotelOfferingsTable.getViewer().getTable().getParent());
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void loadOfferings() throws Exception {
		HotelOfferingParameter parameter = getWizard().getHotelSelectionCriteriaPage().getHotelOfferingParameter();

		I18NDate firstNight = parameter.getArrival();
		I18NDate departure = parameter.getDeparture();
		I18NDate lastNight = departure.minusDays(1);

		int participantCount = getWizard().getParticipantList().size();

		hotelOfferingCVOs = getHotelOfferingMgr().getHotelOfferingCVOs(parameter, CVO_SETTINGS);


		// remove Hotel Offerings which do not belong to the same Hotel Contingent as the Offering of HoteCostCoverage
		if (filterHotelContingentId != null) {
			for (Iterator<HotelOfferingCVO> it = hotelOfferingCVOs.listIterator(); it.hasNext();) {
				HotelOfferingCVO hotelOfferingCVO = it.next();
				if ( ! hotelOfferingCVO.getHotelOfferingVO().getHotelContingentPK().equals(filterHotelContingentId) ) {
					it.remove();
				}
			}
		}


		// remove Hotel Offerings which do not have enough free rooms for every for all the participants
		for (Iterator<HotelOfferingCVO> it = hotelOfferingCVOs.listIterator(); it.hasNext();) {
			HotelOfferingCVO hotelOfferingCVO = it.next();

			boolean volumesOK = checkVolumes(hotelOfferingCVO, firstNight, lastNight, participantCount);
			if ( ! volumesOK) {
				it.remove();
			}
		}
	}


	private boolean checkVolumes(HotelOfferingCVO hotelOfferingCVO, I18NDate firstNight, I18NDate lastNight, int count) {
		List<VolumeVO> volumeList = hotelOfferingCVO.getHotelContingentCVO().getVolumes();
		for (VolumeVO volumeVO : volumeList) {
			boolean afterOrEqualFirstDay = !volumeVO.getDay().isBefore(firstNight);
			boolean beforeOrEqualLastDay = !volumeVO.getDay().isAfter(lastNight);

			boolean isRelevant = afterOrEqualFirstDay && beforeOrEqualLastDay;

			if (isRelevant) {
				if (volumeVO.getBookFree() < count) {
					return false;
				}
			}
		}

		return true;
	}


	public HotelOfferingCVO getBookedHotelOfferingCVO() {
		return hotelOfferingsTable.getBookedOffering();
	}


	public Integer getBookedCount() {
		return hotelOfferingsTable.getBookedCount();
	}


	public PriceVO getDeposit() {
		return hotelOfferingsTable.getDeposit();
	}

}

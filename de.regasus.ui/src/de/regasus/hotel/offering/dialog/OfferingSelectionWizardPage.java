package de.regasus.hotel.offering.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelOfferingVO;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelOfferingModel;
import de.regasus.ui.Activator;


// REFERENCE
public class OfferingSelectionWizardPage extends WizardPage {
	public static final String ID = OfferingSelectionWizardPage.class.getSimpleName();


	/* This interface defines the methods of the status object that are required by this WizardPage.
	 * Defining an interface makes the WizardPage independent from a specific status class
	 * (e.g. SelectHotelOfferingWizardStatus).
	 */
	public static interface WizardStatus {
		void setCurrentPage(WizardPage currentPage);

		Long getEventId();
		Long getHotelId();
		Long getInitialOfferingId();
		Long getOfferingId();
		void setOfferingId(Long hotelId);
	}


	private WizardStatus status;


	// Widgets
	private TableViewer tableViewer;


	public OfferingSelectionWizardPage(WizardStatus status) {
		super(ID);
		setTitle( HotelLabel.HotelOffering.getString() );

		// default description that might be replaced by a more specific one
		setDescription(I18N.HotelOfferingSelectionWizardPage_Description);

		this.status = Objects.requireNonNull(status);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		try {
    		Composite tableComposite = new Composite(parent, SWT.NONE);
    		setControl(tableComposite);

    		TableColumnLayout tableColumnLayout = new TableColumnLayout();
    		tableComposite.setLayout(tableColumnLayout);

    		// create SWT Table
    		final Table table = new Table(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
    		table.setHeaderVisible(true);
    		table.setLinesVisible(true);

    		// HOTEL_NAME
    		{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        		tableColumn.setText( HotelLabel.Hotel.getString() );
        		tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(
        			20,		// weight
        			100,	// minimumWidth
        			true	// resizable
        		));
    		}

    		// CONTINGENT_NAME
    		{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    			tableColumn.setText( HotelLabel.Contingent.getString() );
        		tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(
        			20,		// weight
        			100,	// minimumWidth
        			true	// resizable
        		));
    		}

    		// OFFERING_DESCRIPTION
    		{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        		tableColumn.setText( UtilI18N.Description );
        		tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(
        			20,		// weight
        			100,	// minimumWidth
        			true	// resizable
        		));
    		}

    		// GUEST_COUNT
    		{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        		tableColumn.setText( HotelLabel.HotelBooking_GuestCount.getString() );
        		tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(
        			0,		// weight
        			40,		// minimumWidth
        			true	// resizable
        		));
    		}

    		// PRICE
    		{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        		tableColumn.setText( InvoiceLabel.TotalPrice.getString() );
        		tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(
        			10,		// weight
        			80,		// minimumWidth
        			true	// resizable
        		));
    		}

    		HotelOfferingTable hotelOfferingTable = new HotelOfferingTable(table);
    		tableViewer = hotelOfferingTable.getViewer();

    		tableViewer.addSelectionChangedListener(selectionListener);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			// process selection
			IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();

			Long selectedOfferingId = null;
			if (selection.size() == 1) {
				HotelOfferingVO offeringVO = (HotelOfferingVO) selection.getFirstElement();
				selectedOfferingId = offeringVO.getId();
			}
			status.setOfferingId(selectedOfferingId);

			setPageComplete( isPageComplete() );
		}
	};


	@Override
	public boolean isPageComplete() {
		return status.getOfferingId() != null;
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			// signal that this is he WizardPage which is currently visible
			status.setCurrentPage(this);

			init();
		}

		super.setVisible(visible);
	}


	/**
	 * Initialize the table.
	 *
	 * - load table data
	 * - select the previously selected element or the element that shall be preselected
	 * - if the element could be selected (it exists in the data) set it as the selected element
	 *   (this assures that the initial id is only set if it really exists)
	 */
	private void init() {
		try {
			// load data and set to table
			List<HotelOfferingVO> offeringVOs = getHotelOfferings();
			tableViewer.setInput(offeringVOs);

			// handle selection
			Long offeringId = status.getOfferingId();
			if (offeringId == null) {
				offeringId = status.getInitialOfferingId();
			}

			if (offeringId != null) {
				HotelOfferingVO offeringVO = find(offeringVOs, offeringId);

				StructuredSelection selection;
				if (offeringVO == null) {
					selection = new StructuredSelection();
					offeringId = null;
				}
				else {
					selection = new StructuredSelection(offeringVO);
				}

				/* It is important to call setSelection() even if nothing is selected, because this triggers
				 * the selectionListener which calls  setPageComplete( isPageComplete() )
				 */
				tableViewer.setSelection(selection, true);

				status.setOfferingId(offeringId);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Determine all Hotel Offerings of the Event and Hotel.
	 * @return
	 * @throws Exception
	 */
	private List<HotelOfferingVO> getHotelOfferings() throws Exception {
		Long eventId = status.getEventId();
		Objects.requireNonNull(eventId);

		Long hotelId = status.getHotelId();
		Objects.requireNonNull(hotelId);

		List<HotelOfferingVO> offeringVOs = new ArrayList<>(100);

		HotelOfferingModel hoModel = HotelOfferingModel.getInstance();
		HotelContingentModel hcModel = HotelContingentModel.getInstance();

		List<HotelContingentCVO> hcCVOs = hcModel.getHotelContingentCVOsByEventAndHotel(eventId, hotelId);
		List<Long> contingentIds = HotelContingentCVO.getPKs(hcCVOs);

		hoModel.loadOfferingsOfEvent(eventId);
		for (Long contingentId : contingentIds) {
			List<HotelOfferingVO> hoVOs = hoModel.getHotelOfferingVOsByHotelContingentPK(contingentId);
			offeringVOs.addAll(hoVOs);
		}

		return offeringVOs;
	}


	private HotelOfferingVO find(Collection<HotelOfferingVO> offeringVOs, Long offeringId) {
		Objects.requireNonNull(offeringVOs);
		Objects.requireNonNull(offeringId);

		for (HotelOfferingVO offeringVO : offeringVOs) {
			if ( offeringId.equals(offeringVO.getId()) ) {
				return offeringVO;
			}
		}
		return null;
	}

}

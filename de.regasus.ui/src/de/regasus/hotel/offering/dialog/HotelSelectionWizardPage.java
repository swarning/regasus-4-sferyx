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

import com.lambdalogic.messeinfo.contact.Address;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.HotelTable;
import de.regasus.ui.Activator;


// REFERENCE
public class HotelSelectionWizardPage extends WizardPage {
	public static final String ID = HotelSelectionWizardPage.class.getSimpleName();


	/* This interface defines the methods of the status object that are required by this WizardPage.
	 * Defining an interface makes the WizardPage independent from a specific status class
	 * (e.g. SelectHotelOfferingWizardStatus).
	 */
	public static interface WizardStatus {
		void setCurrentPage(WizardPage currentPage);

		Long getEventId();
		Long getInitialHotelId();
		Long getHotelId();
		void setHotelId(Long hotelId);
	}


	private WizardStatus status;


	// Widgets
	private TableViewer tableViewer;


	public HotelSelectionWizardPage(WizardStatus status) {
		super(ID);
		setTitle( HotelLabel.Hotel.getString() );

		// default description that might be replaced by a more specific one
		setDescription(I18N.HotelSelectionWizardPage_Description);

		this.status = Objects.requireNonNull(status);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		try {
    		Composite tableComposite = new Composite(parent, SWT.NULL);
    		setControl(tableComposite);

    		TableColumnLayout tableColumnLayout = new TableColumnLayout();
    		tableComposite.setLayout(tableColumnLayout);


    		final Table table = new Table(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
    		table.setHeaderVisible(true);
    		table.setLinesVisible(true);

    		// Hotel name
    		{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        		tableColumn.setText( HotelLabel.Hotel.getString() );
        		tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(
        			20,		// weight
        			200,	// minimumWidth
        			true	// resizable
        		));
    		}

    		// Hotel city
    		{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    			tableColumn.setText( Address.CITY.getString() );
        		tableColumnLayout.setColumnData(tableColumn, new ColumnWeightData(
        			10,		// weight
        			100,	// minimumWidth
        			true	// resizable
        		));
    		}

    		HotelTable hotelTable = new HotelTable(table);
    		tableViewer = hotelTable.getViewer();

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

			Long selectedHotelId = null;
			if (selection.size() == 1) {
				HotelCVO hotelCVO = (HotelCVO) selection.getFirstElement();
				selectedHotelId = hotelCVO.getId();
			}
			status.setHotelId(selectedHotelId);

			setPageComplete( isPageComplete() );
		}
	};


	@Override
	public boolean isPageComplete() {
		return status.getHotelId() != null;
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
			List<HotelCVO> hotelCVOs = getHotels();
			tableViewer.setInput(hotelCVOs);

			// handle selection
			Long hotelId = status.getHotelId();
			if (hotelId == null) {
				hotelId = status.getInitialHotelId();
			}

			if (hotelId != null) {
				HotelCVO hotelCVO = find(hotelCVOs, hotelId);

				StructuredSelection selection;
				if (hotelCVO == null) {
					selection = new StructuredSelection();
					hotelId = null;
				}
				else {
					selection = new StructuredSelection(hotelCVO);
				}

				/* It is important to call setSelection() even if nothing is selected, because this triggers
				 * the selectionListener which calls  setPageComplete( isPageComplete() )
				 */
				tableViewer.setSelection(selection, true);

				status.setHotelId(hotelId);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private List<HotelCVO> getHotels() throws Exception {
		Collection<Long> hotelPKs = HotelContingentModel.getInstance().getHotelPKsByEventPK( status.getEventId() );
		List<Hotel> hotels = HotelModel.getInstance().getHotels(hotelPKs);

		List<HotelCVO> hotelCVOs = new ArrayList<>( hotels.size() );
		for (Hotel hotel : hotels) {
			HotelVO hotelVO = new HotelVO(hotel);
			HotelCVO hotelCVO = new HotelCVO(hotelVO);
			hotelCVOs.add(hotelCVO);
		}
		return hotelCVOs;
	}


	private HotelCVO find(Collection<HotelCVO> hotelCVOs, Long hotelId) {
		Objects.requireNonNull(hotelCVOs);
		Objects.requireNonNull(hotelId);

		for (HotelCVO hotelCVO : hotelCVOs) {
			if ( hotelId.equals(hotelCVO.getId()) ) {
				return hotelCVO;
			}
		}
		return null;
	}

}

package de.regasus.hotel.contingent.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.buildMenuItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.EventHotelKey;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.OptionalHotelBookingVO;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventCVO;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.CloneHelper;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;


public class OptionalHotelBookingOverviewEditor
extends AbstractEditor<OptionalHotelBookingOverviewEditorInput>
implements IRefreshableEditorPart, EventIdProvider {

	public static final String ID = "OptionalHotelBookingOverviewEditor";

	// **************************************************************************
	// * Models
	// *

	private EventModel eventModel = EventModel.getInstance();
	private HotelModel hotelModel = HotelModel.getInstance();
	private HotelContingentModel hotelContingentModel = HotelContingentModel.getInstance();


	// keys
	private EventHotelKey eventHotelKey;
	private Long eventID;
	private Long hotelID;


	// Entities
	private ArrayList<OptionalHotelBookingVO> optionalHotelBookingVOs;
	private EventCVO eventCVO;
	private Hotel hotel;


	// Widgets
	private Label eventNameLabel;
	private Table table;
	private TableViewer tableViewer;
	private OptionalHotelBookingTable optionalHotelBookingTable;

	private MenuItem openHotelContingentEditorsMenuItem;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		eventHotelKey = editorInput.getKey();

		if (eventHotelKey != null) {
			eventID = eventHotelKey.getEventPK();
			hotelID = eventHotelKey.getHotelPK();


			// loadOptionalHotelBookings
			optionalHotelBookingVOs = getOptionalHotelBookings(eventID, hotelID);

			// get event
			eventCVO = eventModel.getEventCVO(eventID);

			// get hotel
			if (hotelID != null) {
				hotel = hotelModel.getHotel(hotelID);
			}


			// register at model
			hotelContingentModel.addForeignKeyListener(hotelContingentModelListener, eventID);
			eventModel.addListener(eventModelListener, eventID);

			if (hotelID != null) {
				hotelModel.addListener(hotelModelListener, hotelID);
			}
		}
	}


	private ArrayList<OptionalHotelBookingVO> getOptionalHotelBookings(Long eventID, Long hotelID)
	throws Exception {
		// get HotelContingentCVOs (incl. OptionalHotelBookingVOs) of eventID and hotelPK
		List<HotelContingentCVO> hotelContingentCVOs = hotelContingentModel.getHotelContingentCVOsByEventAndHotel(
			eventID,
			hotelID
		);

		ArrayList<OptionalHotelBookingVO> optionalHotelBookingVOs = CollectionsHelper.createArrayList();

		// create list of all OptionalHotelBookingVO in hotelContingentCVOs
		I18NDate today = I18NDate.now();
		for (HotelContingentCVO hotelContingentCVO : hotelContingentCVOs) {
			List<OptionalHotelBookingVO> list = hotelContingentCVO.getOptionalHotelBookingVOs();

			for (OptionalHotelBookingVO optionalHotelBookingVO : list) {
				boolean expirationEnabled = optionalHotelBookingVO.isExpirationEnabled();
				I18NDate expiration = optionalHotelBookingVO.getExpiration();
				if ( ! expirationEnabled || expiration.isAfter(today)) {
					optionalHotelBookingVOs.add(optionalHotelBookingVO);
				}
			}
		}

		return optionalHotelBookingVOs;
	}


	@Override
	public void dispose() {
		if (hotelContingentModel != null && eventID != null) {
			try {
				hotelContingentModel.removeForeignKeyListener(hotelContingentModelListener, eventID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}


		if (eventModel != null && eventID != null) {
			try {
				eventModel.removeListener(eventModelListener, eventID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}


		if (hotelModel != null && hotelID != null) {
			try {
				hotelModel.removeListener(hotelModelListener, hotelID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ArrayList<OptionalHotelBookingVO> optionalHotelBookingVOs) throws Exception {
		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
		this.optionalHotelBookingVOs = CloneHelper.deepCloneArrayList(optionalHotelBookingVOs);

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return HotelLabel.OptionalHotelBookings.getString();
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param parent
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite widgetComposite = new Composite(parent, SWT.NONE);
			widgetComposite.setLayout(new GridLayout(2, false));


			// row 1

			Label eventLabel = new Label(widgetComposite, SWT.NONE);
			eventLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			eventLabel.setText(ParticipantLabel.Event.getString() + ":");

			eventNameLabel = new Label(widgetComposite, SWT.NONE);
			eventNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			//eventNameLabel.setText(...);


			// row 2
			Composite tableComposite = buildTableComposite(widgetComposite);
			GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(tableComposite);


			// set (and synchronize) the entity
			setEntity(optionalHotelBookingVOs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private Composite buildTableComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		composite.setLayout(tableColumnLayout);

		table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// HOTEL
		TableColumn hotelTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(hotelTableColumn, new ColumnWeightData(20));
		hotelTableColumn.setText(HotelLabel.Hotel.getString());

		// NAME
		TableColumn nameTableColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(nameTableColumn, new ColumnWeightData(20));
		nameTableColumn.setText(UtilI18N.Name);

		// ARRIVAL
		TableColumn arrivalTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(arrivalTableColumn, new ColumnWeightData(20));
		arrivalTableColumn.setText(HotelLabel.OptionalHotelBooking_Arrival.getString());

		// DEPARTURE
		TableColumn departureTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(departureTableColumn, new ColumnWeightData(20));
		departureTableColumn.setText(HotelLabel.OptionalHotelBooking_Departure.getString());

		// COUNT
		TableColumn countTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(countTableColumn, new ColumnWeightData(20));
		countTableColumn.setText(HotelLabel.OptionalHotelBooking_Count.getString());

		// EXPIRATION_TIME
		TableColumn expirationTimeTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(expirationTimeTableColumn, new ColumnWeightData(20));
		expirationTimeTableColumn.setText(HotelLabel.OptionalHotelBooking_Expiration.getString());

		// EXPIRATION_ENABLED
		TableColumn expirationEnabledTableColumn = new TableColumn(table, SWT.RIGHT);
		tableColumnLayout.setColumnData(expirationEnabledTableColumn, new ColumnWeightData(20));
		expirationEnabledTableColumn.setText(HotelLabel.OptionalHotelBooking_Enable_Expiration.getString());


		optionalHotelBookingTable = new OptionalHotelBookingTable(table);

		tableViewer = optionalHotelBookingTable.getViewer();

		// make the Table the SelectionProvider
		getSite().setSelectionProvider(tableViewer);

		// make that Ctl+C copies table contents to clipboard
		optionalHotelBookingTable.registerCopyAction( getEditorSite().getActionBars() );

		table.addSelectionListener(tableSelectionListener);

		buildContextMenu();
		tableViewer.addDoubleClickListener(e -> openHotelContingentEditors());

		return composite;
	}


	private SelectionListener tableSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateButtonStates();
		}
	};


	private void buildContextMenu() {
		Menu menu = new Menu (getEditorSite().getShell(), SWT.POP_UP);

		openHotelContingentEditorsMenuItem = buildMenuItem(menu, I18N.EditHotelContingent, e -> openHotelContingentEditors());

		optionalHotelBookingTable.getViewer().getTable().setMenu(menu);
	}


	private List<OptionalHotelBookingVO> getSelection() {
		List<OptionalHotelBookingVO> selectedOptionalHotelBookingVOs = SelectionHelper.getSelection(
			tableViewer,
			OptionalHotelBookingVO.class
		);
		return selectedOptionalHotelBookingVOs;
	}


	private void openHotelContingentEditors() {
		try {
			// determine Hotel Contingent PKs of selected Optional Hotel Bookings
			Set<Long> hotelContingentPKs = new HashSet<>();
			List<OptionalHotelBookingVO> optionalHotelBookingVOs = getSelection();
			for (OptionalHotelBookingVO optionalHotelBookingVO : optionalHotelBookingVOs) {
				hotelContingentPKs.add( optionalHotelBookingVO.getHotelContingentPK() );
			}


			// Open editors for Hotel Contingents
			try {
				for (Long hotelContingentPK : hotelContingentPKs) {
					HotelContingentEditorInput input = new HotelContingentEditorInput(hotelContingentPK);
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
						input,
						HotelContingentEditor.ID
					);
				}
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void updateButtonStates() {
		boolean somethingIsSelected = !getSelection().isEmpty();

		openHotelContingentEditorsMenuItem.setEnabled(somethingIsSelected);
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		String[] labels;
		if (hotel == null) {
			labels = new String[] {
				ParticipantLabel.Event.getString(),
				ParticipantLabel.EventID.getString()
			};
		}
		else {
			labels = new String[] {
				ParticipantLabel.Event.getString(),
				ParticipantLabel.EventID.getString(),
				HotelLabel.Hotel.getString(),
				HotelLabel.HotelID.getString()
			};
		}

		// the values of the info dialog
		String[] values;
		if (hotel == null) {
			values = new String[] {
				eventCVO.getMnemonic(),
				StringHelper.avoidNull(eventID)
			};
		}
		else {
			values = new String[] {
				eventCVO.getMnemonic(),
				StringHelper.avoidNull(eventID),
				hotel.getName(),
				StringHelper.avoidNull(hotelID)
			};
		}

		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.OptionalHotelBookings.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		// nothing to do, because data shown in this editoris read only
	}


	private void syncWidgetsToEntity() {
		if (optionalHotelBookingVOs != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						eventNameLabel.setText(eventCVO.getLabel(Locale.getDefault()));

						tableViewer.setInput(optionalHotelBookingVOs);

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						//setDirty(false);

						updateButtonStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (event.getOperation() == CacheModelOperation.DELETE) {
				closeBecauseDeletion();
			}
			else {
				eventCVO = eventModel.getEventCVO(eventID);
				if (eventCVO == null) {
					closeBecauseDeletion();
				}
				else {
					syncWidgetsToEntity();
				}
			}
		}
	};


	private CacheModelListener<Long> hotelModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (event.getOperation() == CacheModelOperation.DELETE) {
				closeBecauseDeletion();
			}
			else {
				hotel = hotelModel.getHotel(hotelID);
				if (hotel == null) {
					closeBecauseDeletion();
				}
				else {
					syncWidgetsToEntity();
				}
			}
		}
	};


	private CacheModelListener<Long> hotelContingentModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			optionalHotelBookingVOs = getOptionalHotelBookings(eventID, hotelID);
			syncWidgetsToEntity();
		}
	};


	@Override
	public void refresh() throws Exception {
		if (eventID != null) {
			eventModel.refresh(eventID);
			hotelContingentModel.refreshForeignKey(eventID);
		}

		if (hotelID != null) {
			hotelModel.refresh(hotelID);
		}
	}


	@Override
	protected String getName() {
		return HotelLabel.OptionalHotelBookings.getString();
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			StringBuilder toolTipText = new StringBuilder();
			toolTipText.append(HotelLabel.OptionalHotelBookings);

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.Event.getString());
			toolTipText.append(": ");
			toolTipText.append(eventCVO.getMnemonic());

			if (hotel != null) {
				toolTipText.append('\n');
				toolTipText.append(HotelLabel.Hotel.getString());
				toolTipText.append(": ");
				toolTipText.append(hotel.getName1());
			}

			toolTip = toolTipText.toString();
		}
		catch (Exception e) {
			// This shouldn't happen
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			toolTip = "";
		}
		return toolTip;
	}


	@Override
	public boolean isNew() {
		return false;
	}


	@Override
	public Long getEventId() {
		return eventID;
	}

}

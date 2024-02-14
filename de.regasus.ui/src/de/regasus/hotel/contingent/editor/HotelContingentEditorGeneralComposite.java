package de.regasus.hotel.contingent.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.simpleviewer.ITableEditListener;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.hotel.contingent.combo.HotelContingentTypeCombo;
import de.regasus.ui.Activator;

public class HotelContingentEditorGeneralComposite
extends Composite
implements CacheModelListener<Long>, DisposeListener {

	// the entity
	private HotelContingentCVO hotelContingentCVO;
	private Long hotelPK;

	// additional data
	private List<RoomDefinitionVO> allRoomDefinitionVOs;

	// models
	private HotelModel hotelModel = HotelModel.getInstance();
	private RoomDefinitionModel roomDefinitionModel = RoomDefinitionModel.getInstance();


	protected ModifySupport modifySupport = new ModifySupport(this);

	private Text nameText;
	private Label hotelText;
	private HotelContingentTypeCombo hotelContingentTypeCombo;
	private DecimalNumberText provisionNumberText;
	private NullableSpinner positionSpinner;
	private Button webCheckButton;
	private Text noteText;
	private RoomDefinitionTable roomDefinitionsTable;
	private TableViewer roomDefinitionsTableViewer;



	public HotelContingentEditorGeneralComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));

		addDisposeListener(this);


		try {
			// create Widgets
			createLabel(this, HotelLabel.Hotel.getString());
			{
				hotelText = new Label(this, SWT.LEFT);
				GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
				hotelText.setLayoutData(layoutData);
			}

			nameText = createLabelAndText(this, UtilI18N.Name, true);

			nameText.addModifyListener(modifySupport);

			// HotelContingentType
			{
				Label label = new Label(this, SWT.RIGHT);
				GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
				label.setLayoutData(gridData);
				label.setText(HotelLabel.HotelContingent_Type.getString());
			}
			hotelContingentTypeCombo = new HotelContingentTypeCombo(this, SWT.NONE);
			hotelContingentTypeCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			hotelContingentTypeCombo.addModifyListener(modifySupport);

			{
				createLabel(this, HotelLabel.HotelContingent_Provision.getString());

				provisionNumberText = new DecimalNumberText(this, SWT.BORDER);
				provisionNumberText.setFractionDigits(2);
				provisionNumberText.setNullAllowed(false);
				provisionNumberText.setShowPercent(true);
				provisionNumberText.setMaxValue(100);
				provisionNumberText.setMinValue(0);
				provisionNumberText.setValue(0.0);
				GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				layoutData.widthHint = 100;
				provisionNumberText.setLayoutData(layoutData);

				provisionNumberText.addModifyListener(modifySupport);
			}

			{
				createLabel(this, HotelLabel.HotelContingent_Position.getString());

				positionSpinner = new NullableSpinner(this, SWT.NONE);
				positionSpinner.setMinimum(HotelContingentVO.MIN_POSITION);
				positionSpinner.setMaximum(HotelContingentVO.MAX_POSITION);
				GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				positionSpinner.setLayoutData(layoutData);
				WidgetSizer.setWidth(positionSpinner);

				positionSpinner.addModifyListener(modifySupport);
			}

			// Web
			{
	    		createLabel(this, "");
	    		webCheckButton = new Button(this, SWT.CHECK);
	    		webCheckButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
	    		webCheckButton.setText(HotelLabel.HotelContingent_Web.getString());

	    		webCheckButton.addSelectionListener(modifySupport);
			}

			noteText = SWTHelper.createLabelAndMultiText(this, Hotel.NOTE.getLabel(), false);
			// TODO: set tooltip to Label
			{
    			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    			layoutData.heightHint = 100;
    			noteText.setLayoutData(layoutData);
			}
			noteText.addModifyListener(modifySupport);


			// The table of room definitions
			createTopLabel(this, HotelLabel.RoomDefinition.getString());

			Composite roomDefTableComposite = new Composite(this, SWT.BORDER);
			{
    			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    			layoutData.heightHint = 100;
    			roomDefTableComposite.setLayoutData(layoutData);
			}

			TableColumnLayout roomDefTableColumnLayout = new TableColumnLayout();
			roomDefTableComposite.setLayout(roomDefTableColumnLayout);
			Table roomDefSWTTable = new Table(roomDefTableComposite, SWT.MULTI | SWT.FULL_SELECTION);
			roomDefSWTTable.setHeaderVisible(true);
			roomDefSWTTable.setLinesVisible(true);

			// Used
			final TableColumn usedTableColumn = new TableColumn(roomDefSWTTable, SWT.LEFT);
			roomDefTableColumnLayout.setColumnData(usedTableColumn, new ColumnWeightData(20));
			usedTableColumn.setText(UtilI18N.Used);

			// Name
			final TableColumn nameTableColumn = new TableColumn(roomDefSWTTable, SWT.LEFT);
			roomDefTableColumnLayout.setColumnData(nameTableColumn, new ColumnWeightData(20));
			nameTableColumn.setText(UtilI18N.Name);

			// Guest Count
			final TableColumn guestCountTableColumn = new TableColumn(roomDefSWTTable, SWT.RIGHT);
			roomDefTableColumnLayout.setColumnData(guestCountTableColumn, new ColumnWeightData(20));
			guestCountTableColumn.setText(HotelLabel.HotelBooking_GuestCount.getString());

			roomDefinitionsTable = new RoomDefinitionTable(roomDefSWTTable);


			roomDefinitionsTableViewer = roomDefinitionsTable.getViewer();

			roomDefinitionsTable.addEditListener(new ITableEditListener() {
				@Override
				public void tableCellChanged() {
					// fire ModifyEvent via modifySupport
					modifySupport.fire();
				}
			});
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			throw new RuntimeException(e);
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	public void setEntity(HotelContingentCVO hotelContingentCVO) {
		this.hotelContingentCVO = hotelContingentCVO;

		if (hotelPK == null) {
    		hotelPK = hotelContingentCVO.getVO().getHotelPK();

    		hotelModel.addListener(this, hotelPK);
    		roomDefinitionModel.addForeignKeyListener(this, hotelPK);
		}
		else if (!hotelPK.equals(hotelContingentCVO.getVO().getHotelPK())) {
			throw new IllegalArgumentException("The value of 'hotelPK' in the HotelContingentCVO must not change.");
		}

		// load additional data
		try {
			loadRoomDefinitionVOs();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}


		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (hotelContingentCVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						// set values of entity to widgets
						HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();

						nameText.setText(StringHelper.avoidNull(hotelContingentVO.getName()));

						syncHotelNameToEntity();
						syncRoomDefinitionTableToEntity();

						hotelContingentTypeCombo.setHotelContingentType(hotelContingentVO.getType());
						provisionNumberText.setValue(hotelContingentVO.getProvision());
						webCheckButton.setSelection(hotelContingentVO.isWeb());
						positionSpinner.setValue(hotelContingentVO.getPosition());
						noteText.setText(StringHelper.avoidNull(hotelContingentVO.getNote()));
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncHotelNameToEntity() {
		if (hotelContingentCVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						String hotelName = null;
						Hotel hotel = hotelModel.getHotel(hotelPK);
						if (hotel != null) {
							hotelName = hotel.getName();
							if (hotelName != null) {
								hotelName = hotelName.replace("\n", ", ");
							}
						}

						if (hotelName == null) {
							hotelName = "";
						}

						hotelText.setText(hotelName);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	private void syncRoomDefinitionTableToEntity() {
		if (hotelContingentCVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						/* set usedRoomDefinitionPKs before setting the input, because
						 * setUsedRoomDefinitionPKs(Collection) just sets the data but does not
						 * update the table, whereas setInput(Object) causes a refresh of the table
						 * based on the new input and the previously set usedRoomDefinitionPKs.
						 */

						Collection<Long> usedRoomDefinitionPKs = hotelContingentCVO.getRoomDefinitionPKs();
						roomDefinitionsTable.setUsedRoomDefinitionPKs(usedRoomDefinitionPKs);

						roomDefinitionsTableViewer.setInput(allRoomDefinitionVOs);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (hotelContingentCVO != null) {
			HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();

			hotelContingentVO.setName(nameText.getText());
			// hotel name may not be changed here, is read only
			// first and last days are derived values, are not to be set in any attribute
			hotelContingentVO.setType(hotelContingentTypeCombo.getHotelContingentType());
			hotelContingentVO.setProvision(provisionNumberText.getValue());
			hotelContingentVO.setPosition(positionSpinner.getValueAsInteger());
			hotelContingentVO.setWeb(webCheckButton.getSelection());
			hotelContingentVO.setNote(noteText.getText());

			hotelContingentCVO.setRoomDefinitionPKs(roomDefinitionsTable.getUsedRoomDefinitionPKs());
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == hotelModel) {
				if (event.getOperation() == CacheModelOperation.REFRESH ||
					event.getOperation() == CacheModelOperation.UPDATE
				) {
					syncHotelNameToEntity();
				}
			}
			else if (event.getSource() == roomDefinitionModel) {
				loadRoomDefinitionVOs();
				syncRoomDefinitionTableToEntity();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void loadRoomDefinitionVOs() throws Exception {
		// get all (undeleted) RoomDefinitions of the Hotel
		allRoomDefinitionVOs = roomDefinitionModel.getRoomDefinitionVOsByHotelPK(hotelPK);

		// copy the list because it is unmodifiable
		allRoomDefinitionVOs = new ArrayList<RoomDefinitionVO>(allRoomDefinitionVOs);

		Collection<Long> referencedRoomDefinitionPKs = hotelContingentCVO.getRoomDefinitionPKs();

		// remove deleted Room Definitions which are not referenced by the Hotel Contingent
		for (Iterator<RoomDefinitionVO> it = allRoomDefinitionVOs.iterator(); it.hasNext();) {
			RoomDefinitionVO roomDefinitionVO = it.next();
			if (roomDefinitionVO.isDeleted() && ! referencedRoomDefinitionPKs.contains(roomDefinitionVO.getID())) {
				it.remove();
			}
		}

		// determine those RoomDefinitions which are referenced by the HotelContingent but are deleted
		List<Long> deletedRoomDefinitionPKs = new ArrayList<Long>();
		for (Long roomDefinitionPK : hotelContingentCVO.getRoomDefinitionPKs()) {
			boolean isDeleted = true;
			for (RoomDefinitionVO roomDefinitionVO : allRoomDefinitionVOs) {
				if (roomDefinitionPK.equals(roomDefinitionVO.getID())) {
					isDeleted = false;
					break;
				}
			}
			if (isDeleted) {
				deletedRoomDefinitionPKs.add(roomDefinitionPK);
			}
		}
		List<RoomDefinitionVO> deletedRoomDefinitionVOs = roomDefinitionModel.getRoomDefinitionVOs(deletedRoomDefinitionPKs);
		allRoomDefinitionVOs.addAll(deletedRoomDefinitionVOs);
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		try {
			hotelModel.removeListener(this, hotelPK);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			roomDefinitionModel.removeForeignKeyListener(this, hotelPK);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

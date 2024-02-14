package de.regasus.hotel.contingent.dialog;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;

import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.hotel.contingent.editor.RoomDefinitionTable;

public class HotelContingentNameRoomDefinitionsWizardPage extends WizardPage {

	// **************************************************************************
	// * Attributes and Widgets
	// *

	private HotelContingentCVO hotelContingentCVO;

	private DecimalNumberText provisionNumberText;

	private Text nameText;

	private RoomDefinitionTable roomDefinitionsTable;

	private TableViewer roomDefinitionsTableViewer;

	private List<RoomDefinitionVO> allRoomDefinitionVOs;


	protected HotelContingentNameRoomDefinitionsWizardPage(HotelContingentCVO hotelContingentCVO) {
		super(HotelContingentNameRoomDefinitionsWizardPage.class.getName());

		this.hotelContingentCVO = hotelContingentCVO;

		setTitle(HotelLabel.HotelContingent.getString());
	}



	/**
	 * Enthält die Felder Name, Zimmerbezeichnung und Provision.
	 */
	@Override
	public void createControl(Composite parent) {

		Composite contentComposite = new Composite(parent, SWT.NONE);
		contentComposite.setLayout(new GridLayout(2, false));

		// create Widgets

		nameText = createLabelAndText(contentComposite, UtilI18N.Name, true);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setPageComplete( isPageComplete() );
			}
		});

		{
			createLabel(contentComposite, HotelLabel.HotelContingent_Provision.getString());

			provisionNumberText = new DecimalNumberText(contentComposite, SWT.BORDER);
			provisionNumberText.setFractionDigits(2);
			provisionNumberText.setNullAllowed(false);
			provisionNumberText.setShowPercent(true);
			provisionNumberText.setMaxValue(100);
			provisionNumberText.setMinValue(0);
			provisionNumberText.setValue(0.0);
			GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			layoutData.widthHint = 100;
			provisionNumberText.setLayoutData(layoutData);

		}

		createTopLabel(contentComposite, HotelLabel.RoomDefinition.getString());

		Composite roomDefTableComposite = new Composite(contentComposite, SWT.BORDER);
		{
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.heightHint = 80;
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
		roomDefinitionsTable.setUsedRoomDefinitionPKs(new ArrayList<Long>());
		roomDefinitionsTableViewer = roomDefinitionsTable.getViewer();

		setControl(contentComposite);
	}


	@Override
	public boolean isPageComplete() {
		return StringHelper.isNotEmpty( nameText.getText() );
	}


	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		if (visible) {
			nameText.setText( hotelContingentCVO.getVO().getName() );

			try {
				if (allRoomDefinitionVOs == null) {
					// This logic is used when the contingent is to be created for an already existing hotel
					// otherwise an empty list of room definitions will have been set
					Long hotelPK = hotelContingentCVO.getHotelPK();
					allRoomDefinitionVOs = RoomDefinitionModel.getInstance().getRoomDefinitionVOsByHotelPK(hotelPK);

					// copy the list because it is unmodifiable
					allRoomDefinitionVOs = new ArrayList<>(allRoomDefinitionVOs);

					// remove deleted Room Definitions
					for (Iterator<RoomDefinitionVO> it = allRoomDefinitionVOs.iterator(); it.hasNext();) {
						RoomDefinitionVO roomDefinitionVO = it.next();
						if (roomDefinitionVO.isDeleted()) {
							it.remove();
						}
					}
				}
				roomDefinitionsTableViewer.setInput(allRoomDefinitionVOs);
			}
			catch (Exception e) {
				setErrorMessage(e.getMessage());
			}

		}
	}


	public void syncEntityToWidgets() {
		hotelContingentCVO.getVO().setName(nameText.getText());
		hotelContingentCVO.getVO().setProvision(provisionNumberText.getValue());
		hotelContingentCVO.setRoomDefinitionPKs(roomDefinitionsTable.getUsedRoomDefinitionPKs());
	}


	public void setRoomDefinitionVOs(List<RoomDefinitionVO> roomDefinitionVOs) {
		allRoomDefinitionVOs = roomDefinitionVOs;
	}


	public List<RoomDefinitionVO> getAllRoomDefinitionVOs() {
		return allRoomDefinitionVOs;
	}

}

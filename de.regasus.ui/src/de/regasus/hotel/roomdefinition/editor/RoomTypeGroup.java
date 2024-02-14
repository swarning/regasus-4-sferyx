package de.regasus.hotel.roomdefinition.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.hotel.data.RoomType;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class RoomTypeGroup extends Group {

	// the entity
	private RoomDefinitionVO roomDefinitionVO;

	// RoomTypes in defined order / layout
	private static final RoomType[] ROOM_TYPES = new RoomType[] {
		RoomType.SINGLE,			RoomType.DOUBLE_AS_SINGLE,
		RoomType.DOUBLE,			RoomType.TWIN,
		RoomType.MULTI_BED_ROOM,	RoomType.SUITE
	};

	private ModifySupport modifySupport = new ModifySupport(this);


	// Widgets
	private NullableSpinner guestCountSpinner;
	
	/**
	 * Array with 1 Radio Button for each RoomType in ROOM_TYPES.
	 */
	private Button[] roomTypeButtons;



	public RoomTypeGroup(Composite parent, int style) {
		super(parent, style);

		/* Column 1: Label for Guest Count
		 * Column 2: Guest Count Spinner
		 * Column 3: Label to make a gap
		 * Column 4 and 5: Buttons
		 */
		setLayout(new GridLayout(5, false));

		setText(HotelLabel.RoomType.getString());

		// Guest Count
		{
    		Label label = new Label(this, SWT.RIGHT);
    		label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 3));
    		label.setText(HotelLabel.HotelBooking_GuestCount.getString());
   			SWTHelper.makeBold(label);

   			guestCountSpinner = new NullableSpinner(this, SWT.BORDER);
   			guestCountSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 3));
   			guestCountSpinner.setMinimum(RoomDefinitionVO.MIN_GUEST_QUANTITY);
   			guestCountSpinner.setMaximum(RoomDefinitionVO.MAX_GUEST_QUANTITY);
   			guestCountSpinner.addModifyListener(new ModifyListener() {
   				@Override
   				public void modifyText(ModifyEvent event) {
   					try {
						adaptRoomTypeToGuestCount();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
   				}
   			});

   			guestCountSpinner.addModifyListener(modifySupport);
		}


		// label for realizing a gap between the guestCountSpinner and the Buttons
		{
    		Label label = new Label(this, SWT.RIGHT);
    		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 3);
    		layoutData.widthHint = 20;
    		label.setLayoutData(layoutData);
		}


		// create Radio Buttons for RoomTypes
		roomTypeButtons = new Button[ROOM_TYPES.length];
		
		SelectionListener roomTypeSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!ModifySupport.isDeselectedRadioButton(event)) {
					try {
						adaptGuestCountToRoomType();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			}
		};
		
		for (int i = 0; i < ROOM_TYPES.length; i++) {
			RoomType roomType = ROOM_TYPES[i];

			roomTypeButtons[i] = new Button(this, SWT.RADIO);
			roomTypeButtons[i].setText(roomType.getString());
			roomTypeButtons[i].setData(roomType);

			roomTypeButtons[i].addSelectionListener(roomTypeSelectionListener);
			roomTypeButtons[i].addSelectionListener(modifySupport);
		}
	}


	public boolean isComplete() {
		return
			guestCountSpinner.getValue() != null &&
			getRoomType() != null;
	}


	private RoomType getRoomType() {
		RoomType roomType = null;

		for (int i = 0; i < roomTypeButtons.length; i++) {
			Button roomTypeButton = roomTypeButtons[i];
			if (roomTypeButton.getSelection()) {
				roomType = ROOM_TYPES[i];
				break;
			}
		}

		return roomType;
	}


	private void setRoomType(RoomType roomType) {
		for (int i = 0; i < roomTypeButtons.length; i++) {
			Button roomTypeButton = roomTypeButtons[i];
			RoomType roomTypeButtonValue = ROOM_TYPES[i];

			roomTypeButton.setSelection(roomType == roomTypeButtonValue);
		}
	}


	/**
	 * Set selected RoomType according guestCount.
	 */
	protected void adaptRoomTypeToGuestCount() {
		Integer guestCount = guestCountSpinner.getValueAsInteger();

		if (guestCount != null) {
			int guestCountInt = guestCount.intValue();

			RoomType roomType = getRoomType();

			if (guestCountInt == 1 &&
				(
					roomType == null ||
					roomType == RoomType.DOUBLE ||
					roomType == RoomType.TWIN ||
					roomType == RoomType.MULTI_BED_ROOM
				)
			) {
				setRoomType(RoomType.SINGLE);
			}
			else if (guestCountInt == 2 &&
				(
					roomType == null ||
					roomType == RoomType.SINGLE ||
					roomType == RoomType.DOUBLE_AS_SINGLE ||
					roomType == RoomType.MULTI_BED_ROOM
				)
			) {
				setRoomType(RoomType.DOUBLE);
			}
			else if (guestCountInt >= 3 &&
				(
					roomType == null ||
					roomType == RoomType.SINGLE ||
					roomType == RoomType.DOUBLE_AS_SINGLE ||
					roomType == RoomType.DOUBLE ||
					roomType == RoomType.TWIN
				)
			) {
				setRoomType(RoomType.MULTI_BED_ROOM);
			}
		}
	}


	/**
	 * Set guestCountSpinner according to RoomType.
	 */
	protected void adaptGuestCountToRoomType() {
		RoomType roomType = getRoomType();

		if (roomType != null) {
			switch (roomType) {
				case SINGLE:
				case DOUBLE_AS_SINGLE:
					guestCountSpinner.setValue(1);
					break;

				case DOUBLE:
				case TWIN:
					guestCountSpinner.setValue(2);
					break;

				case SUITE:
					if (guestCountSpinner.getValueAsInteger() == null ||
						guestCountSpinner.getValueAsInteger() == 0
					) {
						guestCountSpinner.setValue(2);
					}
					break;

				case MULTI_BED_ROOM:
					if (guestCountSpinner.getValueAsInteger() == null ||
						guestCountSpinner.getValueAsInteger() <= 2
					) {
						guestCountSpinner.setValue(3);
					}
					break;
			}
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

	private void syncWidgetsToEntity() {
		if (roomDefinitionVO != null ) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						Integer guestQuantity = roomDefinitionVO.getGuestQuantity();
						if (guestQuantity != null) {
							guestCountSpinner.setValue(guestQuantity);
						}

						setRoomType(roomDefinitionVO.getRoomType());


						if (roomDefinitionVO.isDeleted()) {
							for (int i = 0; i < roomTypeButtons.length; i++) {
								roomTypeButtons[i].setEnabled(false);
							}
							guestCountSpinner.setEnabled(false);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (roomDefinitionVO != null) {
			roomDefinitionVO.setGuestQuantity(guestCountSpinner.getValueAsInteger());
			roomDefinitionVO.setRoomType(getRoomType());
		}
	}


	public RoomDefinitionVO getRoomDefinitionVO() {
		return roomDefinitionVO;
	}


	public void setRoomDefinitionVO(RoomDefinitionVO roomDefinitionVO) {
		this.roomDefinitionVO = roomDefinitionVO;
		syncWidgetsToEntity();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

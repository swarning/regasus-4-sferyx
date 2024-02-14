package de.regasus.portal.page.editor.hotel;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.util.rcp.EntityComposite;

import de.regasus.I18N;
import de.regasus.portal.component.hotel.HotelBookingTableComponent;

public class HotelBookingTableComponentAvailableItemsComposite extends EntityComposite<HotelBookingTableComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	// available table columns
	private Button showArrivalButton;
	private Button showDepartureButton;
	private Button showHotelButton;

	// available buttons
	private Button showAddBookingButton;
	private Button showEditBookingButton;

	// *
	// * Widgets
	// **************************************************************************


	public HotelBookingTableComponentAvailableItemsComposite(Composite parent, int style)
	throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(2, false) );

		GridDataFactory groupGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		// available table columns
		{
			Group group = new Group(parent, SWT.NONE);
			groupGridDataFactory.applyTo(group);
			group.setText(I18N.GroupMemberTable_GroupText_AvailableTableColumns);
			group.setLayout( new GridLayout(1, false) );

    		showArrivalButton = createButton(group, HotelBookingTableComponent.FIELD_SHOW_ARRIVAL.getString() );
    		showDepartureButton = createButton(group, HotelBookingTableComponent.FIELD_SHOW_DEPARTURE.getString() );
    		showHotelButton = createButton(group, HotelBookingTableComponent.FIELD_SHOW_HOTEL.getString() );
		}

		// available buttons
		{
			Group group = new Group(parent, SWT.NONE);
			groupGridDataFactory.applyTo(group);
			group.setText(I18N.GroupMemberTable_GroupText_AvailableButtons);
			group.setLayout( new GridLayout(1, false) );

			showAddBookingButton = createButton(group, HotelBookingTableComponent.FIELD_SHOW_ADD_BOOKING_BUTTON.getString() );
			showEditBookingButton = createButton(group, HotelBookingTableComponent.FIELD_SHOW_EDIT_BOOKING_BUTTON.getString() );
		}
	}


	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(button);
		button.addSelectionListener(modifySupport);
		return button;
	}


	@Override
	protected void syncWidgetsToEntity() {
		showArrivalButton.setSelection( entity.isShowArrival() );
		showDepartureButton.setSelection( entity.isShowDeparture() );
		showHotelButton.setSelection( entity.isShowHotel() );

		showAddBookingButton.setSelection( entity.isShowAddBookingButton() );
		showEditBookingButton.setSelection( entity.isShowEditBookingButton() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setShowArrival( showArrivalButton.getSelection() );
			entity.setShowDeparture( showDepartureButton.getSelection() );
			entity.setShowHotel( showHotelButton.getSelection() );

			entity.setShowAddBookingButton( showAddBookingButton.getSelection() );
			entity.setShowEditBookingButton( showEditBookingButton.getSelection() );
		}
	}

}

package de.regasus.portal.page.editor.group;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.util.rcp.EntityComposite;

import de.regasus.I18N;
import de.regasus.portal.component.group.GroupMemberTableComponent;

public class GroupMemberTableComponentAvailableItemsComposite extends EntityComposite<GroupMemberTableComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	// available table columns
	private Button showFirstNameButton;
	private Button showEmail1Button;
	private Button showParticipantTypeButton;
	private Button showParticipantStateButton;
	private Button showCountry1Button;
	private Button showHasBookingsButton;

	// available buttons
	private Button showNewGroupMemberButton;
	private Button showEditGroupMemberPersonalDataButton;
	private Button showEditGroupMemberProgrammeBookingButton;
	private Button showCancelGroupMemberButton;
	private Button showSendConfirmationEmailButton;

	// *
	// * Widgets
	// **************************************************************************


	public GroupMemberTableComponentAvailableItemsComposite(Composite parent, int style)
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

    		showFirstNameButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_FIRST_NAME.getString() );
    		showEmail1Button = createButton(group, GroupMemberTableComponent.FIELD_SHOW_EMAIL_1.getString() );
    		showParticipantTypeButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_PARTICIPANT_TYPE.getString() );
    		showParticipantStateButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_PARTICIPANT_STATE.getString() );
    		showCountry1Button = createButton(group, GroupMemberTableComponent.FIELD_SHOW_COUNTRY_1.getString() );
    		showHasBookingsButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_HAS_BOOKINGS.getString() );
		}

		// available buttons
		{
			Group group = new Group(parent, SWT.NONE);
			groupGridDataFactory.applyTo(group);
			group.setText(I18N.GroupMemberTable_GroupText_AvailableButtons);
			group.setLayout( new GridLayout(1, false) );

			showNewGroupMemberButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_NEW_GROUP_MEMBER_BUTTON.getString() );
			showEditGroupMemberPersonalDataButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_EDIT_GROUP_MEMBER_PERSONAL_DATA_BUTTON.getString() );
			showEditGroupMemberProgrammeBookingButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_EDIT_GROUP_MEMBER_PROGRAMME_BOOKING_BUTTON.getString() );
			showCancelGroupMemberButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_CANCEL_GROUP_MEMBER_BUTTON.getString() );
			showSendConfirmationEmailButton = createButton(group, GroupMemberTableComponent.FIELD_SHOW_SEND_CONFIRMATION_EMAIL_BUTTON.getString() );
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
		showFirstNameButton.setSelection( entity.isShowFirstName() );
		showEmail1Button.setSelection( entity.isShowEmail1() );
		showParticipantTypeButton.setSelection( entity.isShowParticipantType() );
		showParticipantStateButton.setSelection( entity.isShowParticipantState() );
		showCountry1Button.setSelection( entity.isShowCountry1() );
		showHasBookingsButton.setSelection( entity.isShowHasBookings() );

		showNewGroupMemberButton.setSelection( entity.isShowNewGroupMemberButton() );
		showEditGroupMemberPersonalDataButton.setSelection( entity.isShowEditGroupMemberPersonalDataButton() );
		showEditGroupMemberProgrammeBookingButton.setSelection( entity.isShowEditGroupMemberProgrammeBookingButton() );
		showCancelGroupMemberButton.setSelection( entity.isShowCancelGroupMemberButton() );
		showSendConfirmationEmailButton.setSelection( entity.isShowSendConfirmationEmailButton() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setShowFirstName( showFirstNameButton.getSelection() );
			entity.setShowEmail1( showEmail1Button.getSelection() );
			entity.setShowParticipantType( showParticipantTypeButton.getSelection() );
			entity.setShowParticipantState( showParticipantStateButton.getSelection() );
			entity.setShowCountry1( showCountry1Button.getSelection() );
			entity.setShowHasBookings( showHasBookingsButton.getSelection() );

			entity.setShowNewGroupMemberButton( showNewGroupMemberButton.getSelection() );
			entity.setShowEditGroupMemberPersonalDataButton( showEditGroupMemberPersonalDataButton.getSelection() );
			entity.setShowEditGroupMemberProgrammeBookingButton( showEditGroupMemberProgrammeBookingButton.getSelection() );
			entity.setShowCancelGroupMemberButton( showCancelGroupMemberButton.getSelection() );
			entity.setShowSendConfirmationEmailButton( showSendConfirmationEmailButton.getSelection() );
		}
	}

}

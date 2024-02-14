package de.regasus.portal.type.standard.registration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

public class VisibilitySettingsGroup extends EntityGroup<StandardRegistrationPortalConfig> {

	private final int COL_COUNT = 1;


	// **************************************************************************
	// * Widgets
	// *

	private Button showStartPageButton;
	private Button startPageRequiresPasswordButton;
	private Button showPersonalPage2Button;
	private Button showStepBarButton;
	private Button withProgrammeBookingsButton;
	private Button withProgrammeBookings2Button;
	private Button withStreamsButton;
	private Button withHotelButton;
	private Button withHotel2Button;


	// *
	// * Widgets
	// **************************************************************************


	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public VisibilitySettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardRegistrationPortalI18N.VisibilityGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		showStartPageButton = new Button(parent, SWT.CHECK);
		showStartPageButton.setText( StandardRegistrationPortalConfig.SHOW_START_PAGE.getLabel() );
		showStartPageButton.addSelectionListener(modifySupport);
		showStartPageButton.addListener(SWT.Selection, e -> refreshState());


		startPageRequiresPasswordButton = new Button(parent, SWT.CHECK);
		startPageRequiresPasswordButton.setText( StandardRegistrationPortalConfig.START_PAGE_REQUIRES_PASSWORD.getLabel() );
		startPageRequiresPasswordButton.addSelectionListener(modifySupport);


		showPersonalPage2Button = new Button(parent, SWT.CHECK);
		showPersonalPage2Button.setText( StandardRegistrationPortalConfig.SHOW_PERSONAL_PAGE_2.getLabel() );
		showPersonalPage2Button.addSelectionListener(modifySupport);

		showStepBarButton = new Button(parent, SWT.CHECK);
		showStepBarButton.setText( StandardRegistrationPortalConfig.SHOW_STEP_BAR.getLabel() );
		showStepBarButton.addSelectionListener(modifySupport);

		withProgrammeBookingsButton = new Button(parent, SWT.CHECK);
		withProgrammeBookingsButton.setText( StandardRegistrationPortalConfig.SHOW_PROGRAMME_BOOKING_PAGE.getLabel() );
		withProgrammeBookingsButton.addSelectionListener(modifySupport);
		withProgrammeBookingsButton.addListener(SWT.Selection, e -> refreshState());

		withProgrammeBookings2Button = new Button(parent, SWT.CHECK);
		withProgrammeBookings2Button.setText( StandardRegistrationPortalConfig.SHOW_PROGRAMME_BOOKING_PAGE_2.getLabel() );
		withProgrammeBookings2Button.addSelectionListener(modifySupport);

		withStreamsButton = new Button(parent, SWT.CHECK);
		withStreamsButton.setText( StandardRegistrationPortalConfig.SHOW_STREAMS_PAGE.getLabel() );
		withStreamsButton.addSelectionListener(modifySupport);

		withHotelButton = new Button(parent, SWT.CHECK);
		withHotelButton.setText( StandardRegistrationPortalConfig.SHOW_HOTEL_PAGE.getLabel() );
		withHotelButton.addSelectionListener(modifySupport);
		withHotelButton.addListener(SWT.Selection, e -> refreshState());

		withHotel2Button = new Button(parent, SWT.CHECK);
		withHotel2Button.setText( StandardRegistrationPortalConfig.SHOW_HOTEL_PAGE_2.getLabel() );
		withHotel2Button.addSelectionListener(modifySupport);
	}


	private void refreshState() {
		startPageRequiresPasswordButton.setEnabled( showStartPageButton.getSelection() );

		withProgrammeBookings2Button.setEnabled( withProgrammeBookingsButton.getSelection() );
		if ( !withProgrammeBookings2Button.getEnabled() ) {
			withProgrammeBookings2Button.setSelection(false);
		}

		withHotel2Button.setEnabled( withHotelButton.getSelection() );
		if ( !withHotel2Button.getEnabled() ) {
			withHotel2Button.setSelection(false);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		showStartPageButton.setSelection( entity.isShowStartPage() );
		startPageRequiresPasswordButton.setSelection( entity.isStartPageRequiresPassword() );
		showPersonalPage2Button.setSelection( entity.isShowPersonalPage2() );
		showStepBarButton.setSelection( entity.isShowStepBar() );
		withProgrammeBookingsButton.setSelection( entity.isShowProgrammeBookingPage() );
		withProgrammeBookings2Button.setSelection( entity.isShowProgrammeBookingPage2() );
		withStreamsButton.setSelection( entity.isShowStreamsPage() );
		withHotelButton.setSelection( entity.isShowHotelPage() );
		withHotel2Button.setSelection( entity.isShowHotelPage2() );

		refreshState();
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
    		entity.setShowStartPage( showStartPageButton.getSelection() );
    		entity.setStartPageRequiresPassword( startPageRequiresPasswordButton.getSelection() );
    		entity.setShowPersonalPage2( showPersonalPage2Button.getSelection() );
    		entity.setShowStepBar( showStepBarButton.getSelection() );
			entity.setShowProgrammeBookingPage( withProgrammeBookingsButton.getSelection() );
			entity.setShowProgrammeBookingPage2( withProgrammeBookings2Button.getSelection() );
			entity.setShowStreamsPage( withStreamsButton.getSelection() );
			entity.setShowHotelPage( withHotelButton.getSelection() );
			entity.setShowHotelPage2( withHotel2Button.getSelection() );
		}
	}

}

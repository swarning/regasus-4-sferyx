package de.regasus.portal.type.dsgv.registration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.type.standard.registration.StandardRegistrationPortalConfig;

public class ProgrammeBookingSettingsGroup extends EntityGroup<DsgvRegistrationPortalConfig> {

	private final int COL_COUNT = 1;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button avoidOverlappingRegistrationBookingsButton;
	private Button avoidOverlappingNonRegistrationBookingsButton;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammeBookingSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(DsgvRegistrationPortalI18N.BookingGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		avoidOverlappingRegistrationBookingsButton = new Button(parent, SWT.CHECK);
		avoidOverlappingRegistrationBookingsButton.setText( StandardRegistrationPortalConfig.AVOID_OVERLAPPING_REGISTRATION_BOOKINGS.getLabel() );
		avoidOverlappingRegistrationBookingsButton.addSelectionListener(modifySupport);

		avoidOverlappingNonRegistrationBookingsButton = new Button(parent, SWT.CHECK);
		avoidOverlappingNonRegistrationBookingsButton.setText( StandardRegistrationPortalConfig.AVOID_OVERLAPPING_NON_REGISTRATION_BOOKINGS.getLabel() );
		avoidOverlappingNonRegistrationBookingsButton.addSelectionListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		avoidOverlappingRegistrationBookingsButton.setSelection( entity.isAvoidOverlappingRegistrationBookings() );
		avoidOverlappingNonRegistrationBookingsButton.setSelection( entity.isAvoidOverlappingNonRegistrationBookings() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setAvoidOverlappingRegistrationBookings( avoidOverlappingRegistrationBookingsButton.getSelection() );
			entity.setAvoidOverlappingNonRegistrationBookings( avoidOverlappingNonRegistrationBookingsButton.getSelection() );
		}
	}

}

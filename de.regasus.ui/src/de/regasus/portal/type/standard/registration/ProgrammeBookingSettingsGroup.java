package de.regasus.portal.type.standard.registration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

public class ProgrammeBookingSettingsGroup extends EntityGroup<StandardRegistrationPortalConfig> {

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
	private Button closeInvoicesButton;

	// *
	// * Widgets
	// **************************************************************************

	public ProgrammeBookingSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardRegistrationPortalI18N.BookingGroup);
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

		closeInvoicesButton = new Button(parent, SWT.CHECK);
		closeInvoicesButton.setText( StandardRegistrationPortalConfig.CLOSE_INVOICES.getLabel() );
		closeInvoicesButton.setToolTipText( StandardRegistrationPortalConfig.CLOSE_INVOICES.getDescription() );
		closeInvoicesButton.addSelectionListener(modifySupport);
	}


	private void refreshState() {
	}


	@Override
	protected void syncWidgetsToEntity() {
		avoidOverlappingRegistrationBookingsButton.setSelection( entity.isAvoidOverlappingRegistrationBookings() );
		avoidOverlappingNonRegistrationBookingsButton.setSelection( entity.isAvoidOverlappingNonRegistrationBookings() );
		closeInvoicesButton.setSelection( entity.isCloseInvoices() );

		refreshState();
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setAvoidOverlappingRegistrationBookings( avoidOverlappingRegistrationBookingsButton.getSelection() );
			entity.setAvoidOverlappingNonRegistrationBookings( avoidOverlappingNonRegistrationBookingsButton.getSelection() );
			entity.setCloseInvoices( closeInvoicesButton.getSelection() );
		}
	}

}

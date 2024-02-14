package de.regasus.portal.type.react.registration;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.widget.DecimalNumberText;

import de.regasus.portal.type.standard.registration.StandardRegistrationPortalConfig;
import de.regasus.portal.type.standard.registration.StandardRegistrationPortalI18N;

public class ProgrammeBookingSettingsGroup extends EntityGroup<ReactRegistrationPortalConfig> {

	private final int COL_COUNT = 2;

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

	private DecimalNumberText creditCardLimitNumberText;

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

		GridDataFactory labelGridDataFactory = GridDataFactory.swtDefaults()
				.align(SWT.RIGHT, SWT.CENTER);

			GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
				.align(SWT.FILL, SWT.CENTER)
				.grab(true, false);

		/* Row 1 */
		{
			new Label(parent, SWT.NONE);
			avoidOverlappingRegistrationBookingsButton = new Button(parent, SWT.CHECK);
			avoidOverlappingRegistrationBookingsButton.setText( StandardRegistrationPortalConfig.AVOID_OVERLAPPING_REGISTRATION_BOOKINGS.getString() );
			avoidOverlappingRegistrationBookingsButton.addSelectionListener(modifySupport);
		}

		/* Row 2 */
		{
			new Label(parent, SWT.NONE);
			avoidOverlappingNonRegistrationBookingsButton = new Button(parent, SWT.CHECK);
			avoidOverlappingNonRegistrationBookingsButton.setText( StandardRegistrationPortalConfig.AVOID_OVERLAPPING_NON_REGISTRATION_BOOKINGS.getString() );
			avoidOverlappingNonRegistrationBookingsButton.addSelectionListener(modifySupport);
		}

		/* Row 3 */
		{
			new Label(parent, SWT.NONE);
			closeInvoicesButton = new Button(parent, SWT.CHECK);
			closeInvoicesButton.setText( StandardRegistrationPortalConfig.CLOSE_INVOICES.getString() );
			closeInvoicesButton.setToolTipText( StandardRegistrationPortalConfig.CLOSE_INVOICES.getDescription() );
			closeInvoicesButton.addSelectionListener(modifySupport);
		}

		/* Row 4 */
		{
			Label creditCardLimitLabel = new Label(this, SWT.NONE);
			creditCardLimitLabel.setText(ReactRegistrationPortalConfig.LIMIT_FOR_PAYMENT_WITH_CREDIT_CARD.getString());
			labelGridDataFactory.applyTo(creditCardLimitLabel);

			creditCardLimitNumberText = new DecimalNumberText(this, SWT.BORDER);
			creditCardLimitNumberText.setFractionDigits(2);
			creditCardLimitNumberText.setNullAllowed(true);
			creditCardLimitNumberText.addModifyListener(modifySupport);
			widgetGridDataFactory.applyTo(creditCardLimitNumberText);
		}
	}


	private void refreshState() {
	}


	@Override
	protected void syncWidgetsToEntity() {
		avoidOverlappingRegistrationBookingsButton.setSelection( entity.isAvoidOverlappingRegistrationBookings() );
		avoidOverlappingNonRegistrationBookingsButton.setSelection( entity.isAvoidOverlappingNonRegistrationBookings() );
		closeInvoicesButton.setSelection( entity.isCloseInvoices() );
		creditCardLimitNumberText.setValue( entity.getLimitForPaymentWithCreditCard() );

		refreshState();
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setAvoidOverlappingRegistrationBookings( avoidOverlappingRegistrationBookingsButton.getSelection() );
			entity.setAvoidOverlappingNonRegistrationBookings( avoidOverlappingNonRegistrationBookingsButton.getSelection() );
			entity.setCloseInvoices( closeInvoicesButton.getSelection() );
			entity.setLimitForPaymentWithCreditCard( creditCardLimitNumberText.getValue() );
		}
	}

}

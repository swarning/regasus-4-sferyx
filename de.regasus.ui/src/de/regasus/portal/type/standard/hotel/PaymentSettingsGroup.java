package de.regasus.portal.type.standard.hotel;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.type.standard.registration.PayAmountCalculation;

public class PaymentSettingsGroup extends EntityGroup<StandardHotelPortalConfig> {

	private final int COL_COUNT = 2;


	// **************************************************************************
	// * Widgets
	// *

	private Button currentSessionButton;
	private Button portalBookingsButton;

	private Button closeInvoicesButton;

	// *
	// * Widgets
	// **************************************************************************

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */


	public PaymentSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(StandardHotelPortalI18N.PaymentGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		buildCloseInvoicesButton(parent);

		buildPayAmountCalculation(parent);
	}


	protected void buildPayAmountCalculation(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(COL_COUNT, 1).applyTo(label);
		label.setText( StandardHotelPortalConfig.PAY_AMOUNT_CALCULATION.getLabel() );


		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).span(COL_COUNT, 1).applyTo(buttonComposite);
		buttonComposite.setLayout( new GridLayout() );

		currentSessionButton = new Button(buttonComposite, SWT.RADIO);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(currentSessionButton);
		currentSessionButton.setText( PayAmountCalculation.CURRENT_SESSION.getString() );
		currentSessionButton.addSelectionListener(modifySupport);

		portalBookingsButton = new Button(buttonComposite, SWT.RADIO);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(portalBookingsButton);
		portalBookingsButton.setText( PayAmountCalculation.PORTAL_BOOKINGS.getString() );
		portalBookingsButton.addSelectionListener(modifySupport);
	}


	protected void buildCloseInvoicesButton(Composite parent) {
		closeInvoicesButton = new Button(parent, SWT.CHECK);
		closeInvoicesButton.setText( StandardHotelPortalConfig.CLOSE_INVOICES.getString() );
		closeInvoicesButton.setToolTipText( StandardHotelPortalConfig.CLOSE_INVOICES.getDescription() );
		closeInvoicesButton.addSelectionListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		currentSessionButton.setSelection( entity.getPayAmountCalculation() == PayAmountCalculation.CURRENT_SESSION );
		portalBookingsButton.setSelection( entity.getPayAmountCalculation() == PayAmountCalculation.PORTAL_BOOKINGS );

		closeInvoicesButton.setSelection( entity.isCloseInvoices() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			if ( currentSessionButton.getSelection() ) {
				entity.setPayAmountCalculation(PayAmountCalculation.CURRENT_SESSION);
			}
			else if ( portalBookingsButton.getSelection() ) {
				entity.setPayAmountCalculation(PayAmountCalculation.PORTAL_BOOKINGS);
			}

			entity.setCloseInvoices( closeInvoicesButton.getSelection() );
		}
	}

}

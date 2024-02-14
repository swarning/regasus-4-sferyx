package de.regasus.portal.type.standard.registration;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityGroup;

public class PaymentSettingsGroup extends EntityGroup<StandardRegistrationPortalConfig> {

	private final int COL_COUNT = 2;


	// **************************************************************************
	// * Widgets
	// *

	private Button currentSessionButton;
	private Button portalBookingsButton;

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

		setText(StandardRegistrationPortalI18N.PaymentGroup);
	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout(COL_COUNT, false) );

		buildPayAmountCalculation(parent);
	}


	protected void buildPayAmountCalculation(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(COL_COUNT, 1).applyTo(label);
		label.setText( StandardRegistrationPortalConfig.PAY_AMOUNT_CALCULATION.getLabel() );


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


	@Override
	protected void syncWidgetsToEntity() {
		currentSessionButton.setSelection( entity.getPayAmountCalculation() == PayAmountCalculation.CURRENT_SESSION );
		portalBookingsButton.setSelection( entity.getPayAmountCalculation() == PayAmountCalculation.PORTAL_BOOKINGS );
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
		}
	}

}

package de.regasus.portal.page.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityComposite;

import de.regasus.portal.component.ProgrammeBookingComponent;

public class ProgrammeBookingComponentVisibleFieldsComposite extends EntityComposite<ProgrammeBookingComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button showDetailColumnButton;
	private Button showAvailableSeatsColumnButton;
	private Button showNetAmountColumnButton;
	private Button showTaxAmountColumnButton;
	private Button showTaxRateColumnButton;
	private Button showGrossAmountColumnButton;
	private Button showSubtotalColumnButton;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammeBookingComponentVisibleFieldsComposite(Composite parent, int style)
	throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		final int COL_COUNT = 3;
		setLayout( new GridLayout(COL_COUNT, false) );

		// row 1
		showDetailColumnButton = createButton(parent, ProgrammeBookingComponent.FIELD_SHOW_DETAIL_COLUMN.getString() );
		showAvailableSeatsColumnButton = createButton(parent, ProgrammeBookingComponent.FIELD_SHOW_AVAILABLE_SEATS_COLUMN.getString() );
		new Label(this, SWT.NONE);

		// row 2
		showNetAmountColumnButton = createButton(parent, ProgrammeBookingComponent.FIELD_SHOW_NET_AMOUNT_COLUMN.getString() );
		showTaxAmountColumnButton = createButton(parent, ProgrammeBookingComponent.FIELD_SHOW_TAX_AMOUNT_COLUMN.getString() );
		showTaxRateColumnButton = createButton(parent, ProgrammeBookingComponent.FIELD_SHOW_TAX_RATE_COLUMN.getString() );

		// row 3
		showGrossAmountColumnButton = createButton(parent, ProgrammeBookingComponent.FIELD_SHOW_GROSS_AMOUNT_COLUMN.getString() );
		showSubtotalColumnButton = createButton(parent, ProgrammeBookingComponent.FIELD_SHOW_SUBTOTAL_COLUMN.getString() );
		new Label(this, SWT.NONE);
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
		showDetailColumnButton.setSelection( entity.isShowDetailColumn() );
		showAvailableSeatsColumnButton.setSelection( entity.isShowAvailableSeatsColumn() );
		showNetAmountColumnButton.setSelection( entity.isShowNetAmountColumn() );
		showTaxAmountColumnButton.setSelection( entity.isShowTaxAmountColumn() );
		showTaxRateColumnButton.setSelection( entity.isShowTaxRateColumn() );
		showGrossAmountColumnButton.setSelection( entity.isShowGrossAmountColumn() );
		showSubtotalColumnButton.setSelection( entity.isShowSubtotalColumn() );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setShowDetailColumn( showDetailColumnButton.getSelection() );
			entity.setShowAvailableSeatsColumn( showAvailableSeatsColumnButton.getSelection() );
			entity.setShowNetAmountColumn( showNetAmountColumnButton.getSelection() );
			entity.setShowTaxAmountColumn( showTaxAmountColumnButton.getSelection() );
			entity.setShowTaxRateColumn( showTaxRateColumnButton.getSelection() );
			entity.setShowGrossAmountColumn( showGrossAmountColumnButton.getSelection() );
			entity.setShowSubtotalColumn( showSubtotalColumnButton.getSelection() );
		}
	}

}

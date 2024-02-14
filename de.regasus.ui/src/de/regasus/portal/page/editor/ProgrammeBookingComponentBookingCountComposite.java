package de.regasus.portal.page.editor;

import static com.lambdalogic.util.StringHelper.avoidNull;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.util.rcp.EntityComposite;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.portal.component.ProgrammeBookingComponent;


public class ProgrammeBookingComponentBookingCountComposite extends EntityComposite<ProgrammeBookingComponent> {

	private final int COL_COUNT = 2;

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// **************************************************************************
	// * Widgets
	// *

	private Button enableCancelUninvoicedButton;
	private Button enableCancelInvoicedButton;
	private Button showFullyBookedProgrammePointsButton;
	private NullableSpinner minBookCountSpinner;
	private NullableSpinner maxBookCountSpinner;
	private NullableSpinner maxBookPerPpCountSpinner;
	private MultiLineText bookingRulesDescriptionText;

	private GridDataFactory multiLineLabelGridDataFactory;
	private GridDataFactory multiLineTextGridDataFactory;

	// *
	// * Widgets
	// **************************************************************************


	public ProgrammeBookingComponentBookingCountComposite(
		Composite parent,
		int style,
		Long portalPK
	)
	throws Exception {
		super(
			parent,
			style,
			Objects.requireNonNull(portalPK)
		);

	}


	@Override
	protected void initialize(Object[] initValues) throws Exception {
		// determine Portal languages
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		multiLineLabelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.TOP)
			.indent(SWT.DEFAULT, SWTConstants.VERTICAL_INDENT);

		multiLineTextGridDataFactory = GridDataFactory
			.fillDefaults()
			.grab(true, false);

		setLayout( new GridLayout(COL_COUNT, false) );

		buildEnableCancelUninvoiced(parent);
		buildEnableCancelInvoiced(parent);
		buildShowFullyBookedProgrammePoints(parent);
		buildMinBookCount(parent);
		buildMaxBookCount(parent);
		buildMaxBookPerPpCount(parent);
		buildBookingRulesDescription(parent);
	}


	private void buildEnableCancelUninvoiced(Composite parent) {
		new Label(parent, SWT.NONE); // placeholder

		enableCancelUninvoicedButton = new Button(parent, SWT.CHECK);
		enableCancelUninvoicedButton.setText( ProgrammeBookingComponent.FIELD_ENABLE_CANCEL_OF_UNINVOICED_BOOKIGNS.getString() );
		enableCancelUninvoicedButton.setToolTipText( ProgrammeBookingComponent.FIELD_ENABLE_CANCEL_OF_UNINVOICED_BOOKIGNS.getDescription() );
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(enableCancelUninvoicedButton);
		enableCancelUninvoicedButton.addSelectionListener(modifySupport);
	}


	private void buildEnableCancelInvoiced(Composite parent) {
		new Label(parent, SWT.NONE); // placeholder

		enableCancelInvoicedButton = new Button(parent, SWT.CHECK);
		enableCancelInvoicedButton.setText( ProgrammeBookingComponent.FIELD_ENABLE_CANCEL_OF_INVOICED_BOOKIGNS.getString() );
		enableCancelInvoicedButton.setToolTipText( ProgrammeBookingComponent.FIELD_ENABLE_CANCEL_OF_INVOICED_BOOKIGNS.getDescription() );
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(enableCancelInvoicedButton);
		enableCancelInvoicedButton.addSelectionListener(modifySupport);
	}


	private void buildShowFullyBookedProgrammePoints(Composite parent) {
		new Label(parent, SWT.NONE); // placeholder

		showFullyBookedProgrammePointsButton = new Button(parent, SWT.CHECK);
		showFullyBookedProgrammePointsButton.setText( ProgrammeBookingComponent.FIELD_SHOW_FULLY_BOOKED_PROGRAMME_POINTS.getString() );
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(showFullyBookedProgrammePointsButton);
		showFullyBookedProgrammePointsButton.addSelectionListener(modifySupport);
	}


	private void buildMinBookCount(Composite parent) {
		SWTHelper.createLabel(parent, ProgrammeBookingComponent.FIELD_MIN_BOOK_COUNT.getString());

		minBookCountSpinner = new NullableSpinner(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(minBookCountSpinner);
		minBookCountSpinner.setMinimum(0);
		minBookCountSpinner.setMaximum(999);
		WidgetSizer.setWidth(minBookCountSpinner);
		minBookCountSpinner.addModifyListener(modifySupport);
	}


	private void buildMaxBookCount(Composite parent) {
		SWTHelper.createLabel(parent, ProgrammeBookingComponent.FIELD_MAX_BOOK_COUNT.getString());

		maxBookCountSpinner = new NullableSpinner(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(maxBookCountSpinner);
		maxBookCountSpinner.setMinimum(1);
		maxBookCountSpinner.setMaximum(999);
		WidgetSizer.setWidth(maxBookCountSpinner);
		maxBookCountSpinner.addModifyListener(modifySupport);
	}


	private void buildMaxBookPerPpCount(Composite parent) {
		SWTHelper.createLabel(parent, ProgrammeBookingComponent.FIELD_MAX_BOOK_PER_PP_COUNT.getString());

		maxBookPerPpCountSpinner = new NullableSpinner(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER).applyTo(maxBookPerPpCountSpinner);
		maxBookPerPpCountSpinner.setMinimum(1);
		maxBookPerPpCountSpinner.setMaximum(999);
		WidgetSizer.setWidth(maxBookPerPpCountSpinner);
		maxBookPerPpCountSpinner.addModifyListener(modifySupport);
	}


	private void buildBookingRulesDescription(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ProgrammeBookingComponent.FIELD_BOOKING_RULES_DESCRIPTION.getString());
		multiLineLabelGridDataFactory.applyTo(label);

		bookingRulesDescriptionText = new MultiLineText(parent, SWT.BORDER);
		multiLineTextGridDataFactory.applyTo(bookingRulesDescriptionText);
		bookingRulesDescriptionText.setTextLimit( ProgrammeBookingComponent.FIELD_BOOKING_RULES_DESCRIPTION.getMaxLength() );
		SWTHelper.enableTextWidget(bookingRulesDescriptionText, false);
		bookingRulesDescriptionText.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		enableCancelUninvoicedButton.setSelection( entity.isEnableCancelUninvoiced() );
		enableCancelInvoicedButton.setSelection( entity.isEnableCancelInvoiced() );
		showFullyBookedProgrammePointsButton.setSelection( entity.isShowFullyBookedProgrammePoints() );
		minBookCountSpinner.setValue( entity.getMinBookCount() );
		maxBookCountSpinner.setValue( entity.getMaxBookCount() );
		maxBookPerPpCountSpinner.setValue( entity.getMaxBookPerPpCount() );
		bookingRulesDescriptionText.setText( avoidNull(entity.getBookingRulesDescription()) );
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setEnableCancelUninvoiced( enableCancelUninvoicedButton.getSelection() );
			entity.setEnableCancelInvoiced( enableCancelInvoicedButton.getSelection() );
			entity.setShowFullyBookedProgrammePoints( showFullyBookedProgrammePointsButton.getSelection() );
			entity.setMinBookCount( minBookCountSpinner.getValueAsInteger() );
			entity.setMaxBookCount( maxBookCountSpinner.getValueAsInteger() );
			entity.setMaxBookPerPpCount( maxBookPerPpCountSpinner.getValueAsInteger() );
			entity.setBookingRulesDescription( bookingRulesDescriptionText.getText() );
		}
	}

}

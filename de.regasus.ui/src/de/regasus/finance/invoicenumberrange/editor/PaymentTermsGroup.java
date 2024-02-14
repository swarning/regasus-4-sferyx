package de.regasus.finance.invoicenumberrange.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


public class PaymentTermsGroup extends Group {

	// the entity
	private InvoiceNoRangeVO invoiceNoRangeVO;

	protected ModifySupport modifySupport = new ModifySupport(this);

	// **************************************************************************
	// * Widgets
	// *

	private NullableSpinner paymentPercentage1Spinner;
	private NullableSpinner paymentPercentage2Spinner;
	private NullableSpinner paymentPercentage3Spinner;

	private NullableSpinner paymentDays1Spinner;
	private NullableSpinner paymentDays2Spinner;
	private NullableSpinner paymentDays3Spinner;

	private DateComposite paymentDate1DateComposite;
	private DateComposite paymentDate2DateComposite;
	private DateComposite paymentDate3DateComposite;

	// *
	// * Widgets
	// **************************************************************************


	public PaymentTermsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		createWidgets();
		addModifyListenerToWidgets();
	}


	private void createWidgets() throws Exception {
		setText(I18N.InvoiceNoRangeEditor_PaymentTerms);

		setLayout(new GridLayout(6, false));


		// header labels
		{
			GridDataFactory gridDataFactory = GridDataFactory.swtDefaults().align(SWT.LEFT,  SWT.CENTER);

			new Label(this, SWT.NONE);

			Label payTimePercentLabel = new Label(this, SWT.NONE);
			gridDataFactory.applyTo(payTimePercentLabel);
			payTimePercentLabel.setText(I18N.InvoiceNoRangeEditor_PayTimePercentLabel);
			payTimePercentLabel.setToolTipText(I18N.InvoiceNoRangeEditor_PayTimePercentToolTip);


			gridDataFactory.indent(10, SWT.DEFAULT);

			new Label(this, SWT.NONE);

			Label payTimeDayLabel = new Label(this, SWT.NONE);
			gridDataFactory.applyTo(payTimeDayLabel);
			payTimeDayLabel.setText(I18N.InvoiceNoRangeEditor_PayTimeDaysLabel);
			payTimeDayLabel.setToolTipText(I18N.InvoiceNoRangeEditor_PayTimeDaysToolTip);


			new Label(this, SWT.NONE);

			Label payTimeDateLabel = new Label(this, SWT.NONE);
			gridDataFactory.applyTo(payTimeDateLabel);
			payTimeDateLabel.setText(I18N.InvoiceNoRangeEditor_PayTimeDateLabel);
			payTimeDateLabel.setToolTipText(I18N.InvoiceNoRangeEditor_PayTimeDateToolTip);
		}


		GridDataFactory rightAlignLabelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.RIGHT, SWT.CENTER);

		GridDataFactory leftAlignLabelGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.LEFT, SWT.CENTER);

		GridDataFactory percentageGridDataFactory = GridDataFactory
			.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		GridDataFactory daysGridDataFactory = percentageGridDataFactory.copy()
			.indent(10, SWT.DEFAULT);

		GridDataFactory dateGridDataFactory = daysGridDataFactory.copy()
			.grab(false, false);

		// 1
		{
    		Label paymentLabel = new Label(this, SWT.NONE);
    		rightAlignLabelGridDataFactory.applyTo(paymentLabel);
    		paymentLabel.setText(I18N.InvoiceNoRangeEditor_Payment1);

    		paymentPercentage1Spinner = new NullableSpinner(this, SWT.NONE);
    		percentageGridDataFactory.applyTo(paymentPercentage1Spinner);
    		paymentPercentage1Spinner.setMinimum(InvoiceNoRangeVO.MIN_PAYTIME_PERCENT);
    		paymentPercentage1Spinner.setMaximum(InvoiceNoRangeVO.MAX_PAYTIME_PERCENT);
    		paymentPercentage1Spinner.setNullable(InvoiceNoRangeVO.NULL_ALLOWED_PAYTIME_PERCENT);


    		Label paymentPercentLabel = new Label(this, SWT.NONE);
    		leftAlignLabelGridDataFactory.applyTo(paymentPercentLabel);
    		paymentPercentLabel.setText(UtilI18N.PercentSign);

    		paymentDays1Spinner = new NullableSpinner(this, SWT.NONE);
    		daysGridDataFactory.applyTo(paymentDays1Spinner);
    		paymentDays1Spinner.setMinimum(InvoiceNoRangeVO.MIN_PAYTIME_DAYS);
    		paymentDays1Spinner.setMaximum(InvoiceNoRangeVO.MAX_PAYTIME_DAYS);
    		paymentDays1Spinner.setNullable(InvoiceNoRangeVO.NULL_ALLOWED_PAYTIME_DAYS);


    		Label daysLabel = new Label(this, SWT.NONE);
    		leftAlignLabelGridDataFactory.applyTo(daysLabel);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);

    		paymentDate1DateComposite = new DateComposite(this, SWT.BORDER);
    		dateGridDataFactory.applyTo(paymentDate1DateComposite);
    		paymentDate1DateComposite.setLocalDate(null);
		}


		// 2
		{
    		Label paymentLabel = new Label(this, SWT.NONE);
    		rightAlignLabelGridDataFactory.applyTo(paymentLabel);
    		paymentLabel.setText(I18N.InvoiceNoRangeEditor_Payment2);

    		paymentPercentage2Spinner = new NullableSpinner(this, SWT.NONE);
    		percentageGridDataFactory.applyTo(paymentPercentage2Spinner);
    		paymentPercentage2Spinner.setMinimum(InvoiceNoRangeVO.MIN_PAYTIME_PERCENT);
    		paymentPercentage2Spinner.setMaximum(InvoiceNoRangeVO.MAX_PAYTIME_PERCENT);
    		paymentPercentage2Spinner.setNullable(InvoiceNoRangeVO.NULL_ALLOWED_PAYTIME_PERCENT);


    		Label paymentPercentLabel = new Label(this, SWT.NONE);
    		leftAlignLabelGridDataFactory.applyTo(paymentPercentLabel);
    		paymentPercentLabel.setText(UtilI18N.PercentSign);

    		paymentDays2Spinner = new NullableSpinner(this, SWT.NONE);
    		daysGridDataFactory.applyTo(paymentDays2Spinner);
    		paymentDays2Spinner.setMinimum(InvoiceNoRangeVO.MIN_PAYTIME_DAYS);
    		paymentDays2Spinner.setMaximum(InvoiceNoRangeVO.MAX_PAYTIME_DAYS);
    		paymentDays2Spinner.setNullable(InvoiceNoRangeVO.NULL_ALLOWED_PAYTIME_DAYS);


    		Label daysLabel = new Label(this, SWT.NONE);
    		leftAlignLabelGridDataFactory.applyTo(daysLabel);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);

    		paymentDate2DateComposite = new DateComposite(this, SWT.BORDER);
    		dateGridDataFactory.applyTo(paymentDate2DateComposite);
    		paymentDate2DateComposite.setLocalDate(null);
		}


		// 3
		{
    		Label paymentLabel = new Label(this, SWT.NONE);
    		rightAlignLabelGridDataFactory.applyTo(paymentLabel);
    		paymentLabel.setText(I18N.InvoiceNoRangeEditor_Payment3);

    		paymentPercentage3Spinner = new NullableSpinner(this, SWT.NONE);
    		percentageGridDataFactory.applyTo(paymentPercentage3Spinner);
    		paymentPercentage3Spinner.setMinimum(InvoiceNoRangeVO.MIN_PAYTIME_PERCENT);
    		paymentPercentage3Spinner.setMaximum(InvoiceNoRangeVO.MAX_PAYTIME_PERCENT);
    		paymentPercentage3Spinner.setNullable(InvoiceNoRangeVO.NULL_ALLOWED_PAYTIME_PERCENT);


    		Label paymentPercentLabel = new Label(this, SWT.NONE);
    		leftAlignLabelGridDataFactory.applyTo(paymentPercentLabel);
    		paymentPercentLabel.setText(UtilI18N.PercentSign);

    		paymentDays3Spinner = new NullableSpinner(this, SWT.NONE);
    		daysGridDataFactory.applyTo(paymentDays3Spinner);
    		paymentDays3Spinner.setMinimum(InvoiceNoRangeVO.MIN_PAYTIME_DAYS);
    		paymentDays3Spinner.setMaximum(InvoiceNoRangeVO.MAX_PAYTIME_DAYS);
    		paymentDays3Spinner.setNullable(InvoiceNoRangeVO.NULL_ALLOWED_PAYTIME_DAYS);


    		Label daysLabel = new Label(this, SWT.NONE);
    		leftAlignLabelGridDataFactory.applyTo(daysLabel);
    		daysLabel.setText(I18N.InvoiceNoRangeEditor_Days);

    		paymentDate3DateComposite = new DateComposite(this, SWT.BORDER);
    		dateGridDataFactory.applyTo(paymentDate3DateComposite);
    		paymentDate3DateComposite.setLocalDate(null);
		}
	}


	private void addModifyListenerToWidgets() {
		paymentPercentage1Spinner.addModifyListener(modifySupport);
		paymentPercentage2Spinner.addModifyListener(modifySupport);
		paymentPercentage3Spinner.addModifyListener(modifySupport);

		paymentDays1Spinner.addModifyListener(modifySupport);
		paymentDays2Spinner.addModifyListener(modifySupport);
		paymentDays3Spinner.addModifyListener(modifySupport);

		paymentDate1DateComposite.addModifyListener(modifySupport);
		paymentDate2DateComposite.addModifyListener(modifySupport);
		paymentDate3DateComposite.addModifyListener(modifySupport);
	}


	public void setInvoiceNoRange(InvoiceNoRangeVO invoiceNoRangeVO) {
		this.invoiceNoRangeVO = invoiceNoRangeVO;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (invoiceNoRangeVO != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						paymentDate1DateComposite.setDate( invoiceNoRangeVO.getPayTimeDate1() );
						paymentDate2DateComposite.setDate( invoiceNoRangeVO.getPayTimeDate2() );
						paymentDate3DateComposite.setDate( invoiceNoRangeVO.getPayTimeDate3() );

						paymentDays1Spinner.setValue( invoiceNoRangeVO.getPayTimeDays1() );
						paymentDays2Spinner.setValue( invoiceNoRangeVO.getPayTimeDays2() );
						paymentDays3Spinner.setValue( invoiceNoRangeVO.getPayTimeDays3() );

						paymentPercentage1Spinner.setValue( invoiceNoRangeVO.getPayTimePercent1() );
						paymentPercentage2Spinner.setValue( invoiceNoRangeVO.getPayTimePercent2() );
						paymentPercentage3Spinner.setValue( invoiceNoRangeVO.getPayTimePercent3() );
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (invoiceNoRangeVO != null) {
			invoiceNoRangeVO.setPayTimeDate1( paymentDate1DateComposite.getDate() );
			invoiceNoRangeVO.setPayTimeDate2( paymentDate2DateComposite.getDate() );
			invoiceNoRangeVO.setPayTimeDate3( paymentDate3DateComposite.getDate() );

			invoiceNoRangeVO.setPayTimeDays1( paymentDays1Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setPayTimeDays2( paymentDays2Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setPayTimeDays3( paymentDays3Spinner.getValueAsInteger() );

			invoiceNoRangeVO.setPayTimePercent1( paymentPercentage1Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setPayTimePercent2( paymentPercentage2Spinner.getValueAsInteger() );
			invoiceNoRangeVO.setPayTimePercent3( paymentPercentage3Spinner.getValueAsInteger() );
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

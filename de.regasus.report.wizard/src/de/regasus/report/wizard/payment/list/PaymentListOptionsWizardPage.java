package de.regasus.report.wizard.payment.list;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.report.paymentList.PaymentListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class PaymentListOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "PaymentListOptionsWizardPage";

	private PaymentListReportParameter parameter;

	private DateFormat dateFormat;

	// Widgets
	private Button withPaymentsButton;
	private Button withCancelationsButton;
	private Button orderByNewTimeButton;
	private Button orderByCancelationDateButton;
	private DateTimeComposite beginTime;
	private DateTimeComposite endTime;
	private Button useNewTimeButton;
	private Button useBookingDateButton;


	public PaymentListOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.PaymentListOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.PaymentListOptionsWizardPage_Description);
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout());
		//
		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);

		Group dataOptionsGroup = new Group(composite, SWT.NONE);
		dataOptionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		dataOptionsGroup.setText(ReportWizardI18N.PaymentListOptionsWizardPage_DataOptionsGroup);
		dataOptionsGroup.setToolTipText(ReportWizardI18N.PaymentListOptionsWizardPage_DataOptionsGroup_ToolTip);
		dataOptionsGroup.setLayout(new GridLayout());

		withPaymentsButton = new Button(dataOptionsGroup, SWT.CHECK);
		withPaymentsButton.setText(ReportWizardI18N.PaymentListOptionsWizardPage_WithNonCanceledPayments);
		withPaymentsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// get value
				boolean withPayments = withPaymentsButton.getSelection();

				// set parameter
				parameter.setWithPayments(withPayments);

				// set description
				String desc = null;
				if (withPayments) {
					desc = ReportWizardI18N.PaymentListOptionsWizardPage_WithNonCanceledPayments;
				}
				else {
					desc = ReportWizardI18N.PaymentListOptionsWizardPage_WithoutNonCanceledPayments;
				}
				parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_WITH_PAYMENTS, desc);

			}
		});

		withCancelationsButton = new Button(dataOptionsGroup, SWT.CHECK);
		withCancelationsButton.setText(ReportWizardI18N.PaymentListOptionsWizardPage_WithCanceledPayments);
		withCancelationsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// get value
				boolean withCancelations = withCancelationsButton.getSelection();

				// set parameter
				parameter.setWithCancelations(withCancelations);

				// set description
				String desc = null;
				if (withCancelations) {
					desc = ReportWizardI18N.PaymentListOptionsWizardPage_WithCanceledPayments;
				}
				else {
					desc = ReportWizardI18N.PaymentListOptionsWizardPage_WithoutCanceledPayments;
				}
				parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_WITH_CANCELATIONS, desc);

			}
		});

		Group orderingGroup = new Group(composite, SWT.NONE);
		orderingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		orderingGroup.setText(UtilI18N.Sorting);
		orderingGroup.setLayout(new GridLayout());

		orderByNewTimeButton = new Button(orderingGroup, SWT.RADIO);
		orderByNewTimeButton.setText(ReportWizardI18N.PaymentListOptionsWizardPage_ByNewTime);

		orderByCancelationDateButton = new Button(orderingGroup, SWT.RADIO);
		orderByCancelationDateButton.setText(ReportWizardI18N.PaymentListOptionsWizardPage_ByCancelationDate);


		SelectionAdapter orderBySelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					if (!ModifySupport.isDeselectedRadioButton(event)) {
						// get value
						boolean orderByCancelationDate = orderByCancelationDateButton.getSelection();

						// set parameter
						parameter.setOrderByCancelations(orderByCancelationDate);

						// set description
						String desc = null;
						if (orderByCancelationDate) {
							desc = ReportWizardI18N.PaymentListOptionsWizardPage_ByNewTime;
						}
						else {
							desc = ReportWizardI18N.PaymentListOptionsWizardPage_ByCancelationDate;
						}
						parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_ORDER_BY_CANCELATIONS, desc);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		};
		orderByNewTimeButton.addSelectionListener(orderBySelectionListener);
		orderByCancelationDateButton.addSelectionListener(orderBySelectionListener);


		/*
		 * Range of time values: begin and end
		 */

		final Group rangeOfTimeGroup = new Group(composite, SWT.NONE);
		rangeOfTimeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		rangeOfTimeGroup.setText(ReportWizardI18N.PaymentListOptionsWizardPage_TimeFrame);
		rangeOfTimeGroup.setToolTipText(ReportWizardI18N.PaymentListOptionsWizardPage_TimeFrame_ToolTip);
		final GridLayout rangeOfTimeGridLayout = new GridLayout();
		rangeOfTimeGridLayout.numColumns = 2;
		rangeOfTimeGroup.setLayout(rangeOfTimeGridLayout);

		final Label beginTimeLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		beginTimeLabel.setText(UtilI18N.BeginTime);
		beginTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		beginTime = new DateTimeComposite(rangeOfTimeGroup, SWT.BORDER);
		beginTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		beginTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				Date beginTimeValue = beginTime.getDate();

				// set parameter
				parameter.setBeginTime(beginTimeValue);

				// set description
				String desc = null;
				if (beginTimeValue != null) {
					I18NPattern i18nPattern = new I18NPattern();
					i18nPattern.append(UtilI18N.BeginTime);
					i18nPattern.append(": ");
					i18nPattern.append(dateFormat.format(beginTimeValue));
					desc = i18nPattern.toString();
				}
				parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_BEGIN_TIME, desc);
			}
		});

		final Label endTimeLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		endTimeLabel.setText(UtilI18N.EndTime);
		endTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		endTime = new DateTimeComposite(rangeOfTimeGroup, SWT.BORDER);
		endTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		endTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				Date endTimeValue = endTime.getDate();

				// set parameter
				parameter.setEndTime(endTimeValue);

				// set description
				String desc = null;
				if (endTimeValue != null) {
					I18NPattern i18nPattern = new I18NPattern();
					i18nPattern.append(UtilI18N.EndTime);
					i18nPattern.append(": ");
					i18nPattern.append(dateFormat.format(endTimeValue));
					desc = i18nPattern.toString();
				}
				parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_END_TIME, desc);
			}
		});


		/*
		 * Buttons to select whether the time values refer on bookingDate or newTime
		 */
		final Group useDateGroup = new Group(composite, SWT.NONE);
		useDateGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		useDateGroup.setText(ReportWizardI18N.PaymentListOptionsWizardPage_TimeBasedOn);
		useDateGroup.setToolTipText(ReportWizardI18N.PaymentListOptionsWizardPage_TimeBasedOn_ToolTip);
		final GridLayout useDateGridLayout = new GridLayout();
		useDateGridLayout.numColumns = 2;
		useDateGroup.setLayout(useDateGridLayout);

		useBookingDateButton = new Button(useDateGroup, SWT.RADIO);
		useBookingDateButton.setText(InvoiceLabel.BookingDate.getString());

		useNewTimeButton = new Button(useDateGroup, SWT.RADIO);
		useNewTimeButton.setText(UtilI18N.CreationTime);

		SelectionAdapter useDateSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					if (!ModifySupport.isDeselectedRadioButton(event)) {
						// get value
						boolean useNewTime = useNewTimeButton.getSelection();

						// set parameter
						parameter.setUseNewTimeInsteadOfBookingDate(useNewTime);

						// set description
						String desc = null;
						if (useNewTime) {
							desc = ReportWizardI18N.PaymentListOptionsWizardPage_BasedOnCreationTime;
						}
						else {
							desc = ReportWizardI18N.PaymentListOptionsWizardPage_BasedOnBookingDate;
						}
						parameter.setDescription(PaymentListReportParameter.DESCRIPTION_ID_USE_NEW_TIME_INSTEAD_OF_BOOKING_DATE, desc);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		};
		useBookingDateButton.addSelectionListener(useDateSelectionListener);
		useNewTimeButton.addSelectionListener(useDateSelectionListener);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof PaymentListReportParameter) {
			parameter = (PaymentListReportParameter) reportParameter;

			withPaymentsButton.setSelection(parameter.isWithPayments());
			withCancelationsButton.setSelection(parameter.isWithCancelations());
			orderByNewTimeButton.setSelection( ! parameter.isOrderByCancelations());
			orderByCancelationDateButton.setSelection(parameter.isOrderByCancelations());
			beginTime.setDate(parameter.getBeginTime());
			endTime.setDate(parameter.getEndTime());
			useBookingDateButton.setSelection(!parameter.isUseNewTimeInsteadOfBookingDate());
			useNewTimeButton.setSelection(parameter.isUseNewTimeInsteadOfBookingDate());
		}
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

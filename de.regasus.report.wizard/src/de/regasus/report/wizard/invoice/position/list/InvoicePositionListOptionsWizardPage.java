package de.regasus.report.wizard.invoice.position.list;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceStatus;
import com.lambdalogic.messeinfo.invoice.report.invoicePositionList.InvoicePositionListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.ModifySelectionAdapter;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class InvoicePositionListOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "InvoicePositionListOptionsWizardPage";

	private InvoicePositionListReportParameter parameter;


	// to format Date values in description
	private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

	// Widgets
	private DateComposite beginTime;
	private DateComposite endTime;
	private Button closedButton;
	private Button nonClosedButton;

	// to avoid handling events during initialization
	private boolean initializing = false;


	public InvoicePositionListOptionsWizardPage() {
		super(ID);
		setTitle(UtilI18N.TimeFrame);
		setDescription(ReportWizardI18N.InvoicePositionListOptionsWizardPage_Description);
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

		Composite centerComposite = new Composite(container, SWT.NONE);
		centerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		centerComposite.setLayout( new GridLayout() );


		Group invoiceStateGroup = new Group(centerComposite, SWT.NONE);
		invoiceStateGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		invoiceStateGroup.setText(InvoiceLabel.InvoiceStatus.getString());
		invoiceStateGroup.setLayout( new GridLayout(2, true) );

		closedButton = new Button(invoiceStateGroup, SWT.RADIO);
		closedButton.setText(InvoiceStatus.CLOSED.getString());
		closedButton.addSelectionListener(listener);

		nonClosedButton = new Button(invoiceStateGroup, SWT.RADIO);
		nonClosedButton.setText(InvoiceStatus.NON_CLOSED.getString());
		nonClosedButton.addSelectionListener(listener);


		Group timeFrameGroup = new Group(centerComposite, SWT.NONE);
		timeFrameGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		timeFrameGroup.setText(UtilI18N.TimeFrame);
		timeFrameGroup.setLayout( new GridLayout(2, false) );

		Label beginTimeLabel = new Label(timeFrameGroup, SWT.NONE);
		beginTimeLabel.setText(UtilI18N.BeginTime + ":");

		beginTime = new DateComposite(timeFrameGroup, SWT.BORDER);
		beginTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		WidgetSizer.setWidth(beginTime);
		beginTime.addModifyListener(listener);


		Label endTimeLabel = new Label(timeFrameGroup, SWT.NONE);
		endTimeLabel.setText(UtilI18N.EndTime + ":");

		endTime = new DateComposite(timeFrameGroup, SWT.BORDER);
		endTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		WidgetSizer.setWidth(endTime);
		endTime.addModifyListener(listener);
	}


	private ModifySelectionAdapter listener = new ModifySelectionAdapter() {
		@Override
		public void handleEvent(TypedEvent event) {
			// ignore events during initializing
			if (initializing) {
				return;
			}

			/*
			 * set parameters
			 */

			String desc;
			I18NPattern i18nPattern = new I18NPattern();

			/* handle invoice status
			 */

			// get value
			InvoiceStatus invoiceStatus = InvoiceStatus.CLOSED;
			if (nonClosedButton.getSelection()) {
				invoiceStatus = InvoiceStatus.NON_CLOSED;
			}

			// set parameter
			parameter.setInvoiceStatus(invoiceStatus);

			// set description
			i18nPattern.clear();
			i18nPattern.append(InvoiceLabel.InvoiceStatus);
			i18nPattern.append(": ");
			i18nPattern.append(invoiceStatus);
			desc = i18nPattern.toString();

			parameter.setDescription(InvoicePositionListReportParameter.DESCRIPTION_ID_INVOICE_STATUS, desc);


			/* handle begin date
			 */

			// consider beginDate only if invoice status is CLOSED
			Date beginTimeValue = null;
			if (invoiceStatus == InvoiceStatus.CLOSED) {
				beginTimeValue = beginTime.getDate();
			}
			parameter.setBeginDate(beginTimeValue);

			desc = null;
			if (beginTimeValue != null) {
				i18nPattern.clear();
				i18nPattern.append(UtilI18N.BeginTime);
				i18nPattern.append(": ");
				i18nPattern.append(dateFormat.format(beginTimeValue));
				desc = i18nPattern.toString();
			}
			parameter.setDescription(InvoicePositionListReportParameter.DESCRIPTION_ID_BEGIN_DATE, desc);


			/* handle end date
			 */

			// consider endDate only if invoice status is CLOSED
			Date endTimeValue = null;
			if (invoiceStatus == InvoiceStatus.CLOSED) {
				endTimeValue = endTime.getDate();
			}
			parameter.setEndDate(endTimeValue);

			desc = null;
			if (endTimeValue != null) {
				i18nPattern.clear();
				i18nPattern.append(UtilI18N.EndTime);
				i18nPattern.append(": ");
				i18nPattern.append(dateFormat.format(endTimeValue));
				desc = i18nPattern.toString();
			}
			parameter.setDescription(InvoicePositionListReportParameter.DESCRIPTION_ID_END_DATE, desc);


			/*
			 * enable/disable date fields
			 */

			beginTime.setEnabled(invoiceStatus == InvoiceStatus.CLOSED);
			endTime.setEnabled(invoiceStatus == InvoiceStatus.CLOSED);
		}
	};


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof InvoicePositionListReportParameter) {
			initializing = true;
			try {
				parameter = (InvoicePositionListReportParameter) reportParameter;

				closedButton.setSelection(parameter.getInvoiceStatus() == InvoiceStatus.CLOSED);
				nonClosedButton.setSelection(parameter.getInvoiceStatus() == InvoiceStatus.NON_CLOSED);
				beginTime.setDate(parameter.getBeginDate());
				endTime.setDate(parameter.getEndDate());
			}
			finally {
				initializing = false;
			}
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

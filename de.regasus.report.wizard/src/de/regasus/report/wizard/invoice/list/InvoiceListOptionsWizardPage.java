package de.regasus.report.wizard.invoice.list;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.report.invoiceList.InvoiceListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;


public class InvoiceListOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "InvoiceListOptionsWizardPage";

	private InvoiceListReportParameter parameter;

	private DateComposite beginDate;
	private DateComposite endDate;

	private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);


	public InvoiceListOptionsWizardPage() {
		super(ID);
		setTitle(InvoiceLabel.InvoiceDate.getString());
//		setDescription(ReportI18N.InvoiceNoRangeWizardPage_Description);
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


		final Group rangeOfTimeGroup = new Group(container, SWT.NONE);
		rangeOfTimeGroup.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 2, 1));
		rangeOfTimeGroup.setText(UtilI18N.TimeFrame);
		final GridLayout rangeOfTimeGridLayout = new GridLayout();
		rangeOfTimeGridLayout.numColumns = 2;
		rangeOfTimeGroup.setLayout(rangeOfTimeGridLayout);

		final Label beginDateLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		beginDateLabel.setText(UtilI18N.BeginTime + ":");
		beginDate = new DateComposite(rangeOfTimeGroup, SWT.BORDER);
		beginDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		WidgetSizer.setWidth(beginDate);
		beginDate.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				Date beginDateValue = beginDate.getDate();

				// set parameter
				parameter.setBeginDate(beginDateValue);

				// set description
				String desc = null;
				if (beginDateValue != null) {
					I18NPattern i18nPattern = new I18NPattern();
					i18nPattern.append(UtilI18N.BeginTime);
					i18nPattern.append(": ");
					i18nPattern.append(dateFormat.format(beginDateValue));
					desc = i18nPattern.toString();
				}
				parameter.setDescription(InvoiceListReportParameter.DESCRIPTION_ID_BEGIN_DATE, desc);
			}
		});

		final Label endDateLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		endDateLabel.setText(UtilI18N.EndTime + ":");
		endDate = new DateComposite(rangeOfTimeGroup, SWT.BORDER);
		endDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		WidgetSizer.setWidth(endDate);
		endDate.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				Date endDateValue = endDate.getDate();

				// set parameter
				parameter.setEndDate(endDateValue);

				// set description
				String desc = null;
				if (endDateValue != null) {
					I18NPattern i18nPattern = new I18NPattern();
					i18nPattern.append(UtilI18N.EndTime);
					i18nPattern.append(": ");
					i18nPattern.append(dateFormat.format(endDateValue));
					desc = i18nPattern.toString();
				}
				parameter.setDescription(InvoiceListReportParameter.DESCRIPTION_ID_END_DATE, desc);
			}
		});
	}



	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof InvoiceListReportParameter) {
			parameter = (InvoiceListReportParameter) reportParameter;

			beginDate.setDate(parameter.getBeginDate());
			endDate.setDate(parameter.getEndDate());
		}
	}



	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

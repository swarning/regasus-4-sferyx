package de.regasus.report.wizard.payengine.list.compact;

import java.text.DateFormat;

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
import com.lambdalogic.messeinfo.invoice.report.payEngineList.PayEngineListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.time.I18NDateMinute;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class PayEngineListCompactOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "PayEngineListCompactOptionsWizardPage";

	private PayEngineListReportParameter parameter;


	/**
	 * To format description
	 */
	private DateFormat dateFormat;

	// Widgets
	private DateTimeComposite beginTime;
	private DateTimeComposite endTime;


	public PayEngineListCompactOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.PayEngineListOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.PayEngineListOptionsWizardPage_Description);
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

		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);


		final Group rangeOfTimeGroup = new Group(composite, SWT.NONE);
		rangeOfTimeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		rangeOfTimeGroup.setText(UtilI18N.TimeFrame);
		final GridLayout rangeOfTimeGridLayout = new GridLayout();
		rangeOfTimeGridLayout.numColumns = 2;
		rangeOfTimeGroup.setLayout(rangeOfTimeGridLayout);

		final Label beginTimeLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		beginTimeLabel.setText(UtilI18N.BeginTime + ":");
		beginTime = new DateTimeComposite(rangeOfTimeGroup, SWT.BORDER);
		beginTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		WidgetSizer.setWidth(beginTime);
		beginTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				I18NDateMinute beginTimeValue = beginTime.getI18NDateMinute();

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
				parameter.setDescription(PayEngineListReportParameter.DESCRIPTION_ID_BEGIN_TIME, desc);
			}
		});

		final Label endTimeLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		endTimeLabel.setText(UtilI18N.EndTime + ":");
		endTime = new DateTimeComposite(rangeOfTimeGroup, SWT.BORDER);
		endTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		WidgetSizer.setWidth(endTime);
		endTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				I18NDateMinute endTimeValue = endTime.getI18NDateMinute();

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
				parameter.setDescription(PayEngineListReportParameter.DESCRIPTION_ID_END_TIME, desc);
			}
		});

	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof PayEngineListReportParameter) {
			parameter = (PayEngineListReportParameter) reportParameter;

			beginTime.setI18NDateMinute( parameter.getBeginTime() );
			endTime.setI18NDateMinute( parameter.getEndTime() );
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

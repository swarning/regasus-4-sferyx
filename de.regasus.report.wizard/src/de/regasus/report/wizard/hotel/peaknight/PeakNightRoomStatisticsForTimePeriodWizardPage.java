package de.regasus.report.wizard.hotel.peaknight;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.hotel.report.groupedRoomStat.GroupedRoomStatisticsForTimePeriodReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.datetime.DateComposite;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class PeakNightRoomStatisticsForTimePeriodWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "PeakNightRoomStatisticsForTimePeriodWizardPage";

	private GroupedRoomStatisticsForTimePeriodReportParameter parameter;

	private DateFormat dateFormat;

	// Widgets
	private DateComposite startDate;
	private DateComposite endDate;


	/**
	 * Create the wizard
	 */
	public PeakNightRoomStatisticsForTimePeriodWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.TimePeriod);
		dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));
		//
		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);
		{
			Label lblBeginDate = new Label(composite, SWT.NONE);
			lblBeginDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblBeginDate.setText(UtilI18N.BeginTime);
		}
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		layoutData.widthHint = 100;
		{
			startDate = new DateComposite(composite, SWT.NONE);
			startDate.setLayoutData(layoutData);
			startDate.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Date dateValue = startDate.getDate();
					setStartDate(dateValue);
				}
			});
		}
		{
			Label lblEndDate = new Label(composite, SWT.NONE);
			lblEndDate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			lblEndDate.setText(UtilI18N.EndTime);
		}
		{
			endDate = new DateComposite(composite, SWT.NONE);
			endDate.setLayoutData(layoutData);
			endDate.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					Date dateValue = endDate.getDate();
					setEndDate(dateValue);
				}
			});
		}
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof GroupedRoomStatisticsForTimePeriodReportParameter) {
			parameter = (GroupedRoomStatisticsForTimePeriodReportParameter) reportParameter;

			startDate.setDate(parameter.getStartDate());
			endDate.setDate(parameter.getEndDate());
		}
	}


	@Override
	public boolean isPageComplete() {
		return parameter.getStartDate() != null && parameter.getEndDate() != null;
	}


	public void setStartDate(Date beginValue) {
		if (parameter != null) {
			// set parameter
			parameter.setStartDate(beginValue);

			// set description
			String desc = null;
			if (beginValue != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(UtilI18N.BeginTime);
				sb.append(": ");
				sb.append(dateFormat.format(beginValue));
				desc = sb.toString();
			}
			parameter.setDescription("startDate", desc);
		}

		setPageComplete(isPageComplete());
	}


	public void setEndDate(Date endValue) {
		if (parameter != null) {
			// set parameter
			parameter.setEndDate(endValue);

			// set description
			String desc = null;
			if (endValue != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(UtilI18N.EndTime);
				sb.append(": ");
				sb.append(dateFormat.format(endValue));
				desc = sb.toString();
			}
			parameter.setDescription("endDate", desc);
		}

		setPageComplete(isPageComplete());
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

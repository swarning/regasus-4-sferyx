package de.regasus.report.wizard.programme.registrationsPerWeek;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.report.registrationsPerWeek.RegistrationsPerWeekReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.time.I18NDate;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;

import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class RegistrationsPerWeekSettingsWizardPage
extends WizardPage
implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.programme.registrationsPerWeek.RegistrationsPerWeekSettingsWizardPage";

	private RegistrationsPerWeekReportParameter parameter;

	/**
	 * Zur Formatierung der Description
	 */
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

	// Widgets
	private DateComposite beginDate;
	private DateComposite endDate;


	/**
	 * Create the wizard
	 */
	public RegistrationsPerWeekSettingsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.RegistrationsPerWeekSettingsWizardPage_Title);
		setDescription(ReportWizardI18N.RegistrationsPerWeekSettingsWizardPage_Description);
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
			beginDate = new DateComposite(composite, SWT.NONE);
			beginDate.setLayoutData(layoutData);
			beginDate.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					I18NDate dateValue = beginDate.getI18NDate();
					setBeginDate(dateValue);
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
					I18NDate dateValue = endDate.getI18NDate();
					setEndDate(dateValue);
				}
			});
		}
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof RegistrationsPerWeekReportParameter) {
			parameter = (RegistrationsPerWeekReportParameter) reportParameter;

			beginDate.setI18NDate(parameter.getBeginDate());
			endDate.setI18NDate(parameter.getEndDate());
		}
	}


	@Override
	public boolean isPageComplete() {
		return beginDate.getLocalDate() != null && endDate.getLocalDate() != null;
	}


	public void setBeginDate(I18NDate beginValue) {
		if (parameter != null) {
			// set parameter
			parameter.setBeginDate(beginValue);

			// set description
			String desc = null;
			if (beginValue != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(UtilI18N.BeginTime);
				sb.append(": ");
				sb.append( dateTimeFormatter.format(beginValue) );
				desc = sb.toString();
			}
			parameter.setDescription(RegistrationsPerWeekReportParameter.DESCRIPTION_BEGIN_DATE, desc);
		}

		setPageComplete(isPageComplete());
	}


	public void setEndDate(I18NDate endValue) {
		if (parameter != null) {
			// set parameter
			parameter.setEndDate(endValue);

			// set description
			String desc = null;
			if (endValue != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(UtilI18N.EndTime);
				sb.append(": ");
				sb.append( dateTimeFormatter.format(endValue) );
				desc = sb.toString();
			}
			parameter.setDescription(RegistrationsPerWeekReportParameter.DESCRIPTION_END_DATE, desc);
		}

		setPageComplete(isPageComplete());
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

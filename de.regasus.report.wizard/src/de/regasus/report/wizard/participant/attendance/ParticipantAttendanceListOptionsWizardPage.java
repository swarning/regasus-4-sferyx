package de.regasus.report.wizard.participant.attendance;

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

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.report.participantAttendanceList.ParticipantAttendanceListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.datetime.DateComposite;

import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class ParticipantAttendanceListOptionsWizardPage
extends WizardPage
implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.participant.attendance.ParticipantAttendanceListOptionsWizardPage"; 

	private ParticipantAttendanceListReportParameter parameter;


	/**
	 * Zur Formatierung der Description
	 */
	private DateFormat dateFormat;

	// Widgets
	private DateComposite beginTime;
	private DateComposite endTime;
	private Button withInvalidLeadsButton;


	public ParticipantAttendanceListOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.ParticipantAttendanceListOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.ParticipantAttendanceListOptionsWizardPage_Description);
		dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
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
		composite.setLayout(gridLayout);


		final Group rangeOfTimeGroup = new Group(composite, SWT.NONE);
		rangeOfTimeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		rangeOfTimeGroup.setText(ReportWizardI18N.ParticipantAttendanceListOptionsWizardPage_RangeOfDatesGroupLabel);
		final GridLayout rangeOfTimeGridLayout = new GridLayout();
		rangeOfTimeGridLayout.numColumns = 2;
		rangeOfTimeGroup.setLayout(rangeOfTimeGridLayout);

		final Label beginTimeLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		beginTimeLabel.setText(KernelLabel.StartTime.getString());
		beginTime = new DateComposite(rangeOfTimeGroup, SWT.NONE);
		beginTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		beginTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				Date beginTimeValue = beginTime.getDate();

				// set parameter
				parameter.setBeginDate(beginTimeValue);

				// set description
				StringBuilder desc = new StringBuilder();
				if (beginTimeValue != null) {
					desc.append(KernelLabel.StartTime.getString());
					desc.append(": "); 
					desc.append(dateFormat.format(beginTimeValue));
				}
				parameter.setDescription(ParticipantAttendanceListReportParameter.DESCRIPTION_ID_BEGIN_DATE, desc.toString());

				setPageComplete(isPageComplete());
			}
		});

		final Label endTimeLabel = new Label(rangeOfTimeGroup, SWT.NONE);
		endTimeLabel.setText(KernelLabel.EndTime.getString());
		endTime = new DateComposite(rangeOfTimeGroup, SWT.NONE);
		endTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		endTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// get value
				Date endTimeValue = endTime.getDate();

				// set parameter
				parameter.setEndDate(endTimeValue);

				// set description
				StringBuilder desc = new StringBuilder();
				if (endTimeValue != null) {
					desc.append(KernelLabel.EndTime.getString());
					desc.append(": "); 
					desc.append(dateFormat.format(endTimeValue));
				}
				parameter.setDescription(ParticipantAttendanceListReportParameter.DESCRIPTION_ID_END_DATE, desc.toString());

				setPageComplete(isPageComplete());
			}
		});

		withInvalidLeadsButton = new Button(composite, SWT.CHECK);
		withInvalidLeadsButton.setText(ReportWizardI18N.ParticipantAttendanceListOptionsWizardPage_IncludeInvalidLeadsLabel);
		withInvalidLeadsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// getValue
				boolean includeInvalidLeads = withInvalidLeadsButton.getSelection();

				// set parameter
				parameter.setIncludeInvalidLeads(includeInvalidLeads);

				// set description
				StringBuilder desc = new StringBuilder();
				if (includeInvalidLeads) {
					desc.append(ReportWizardI18N.ParticipantAttendanceListOptionsWizardPage_IncludeInvalidLeadsLabel);
				}
				parameter.setDescription(ParticipantAttendanceListReportParameter.DESCRIPTION_ID_INCLUDE_INVALID_LEADS, desc.toString());
			}
		});
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ParticipantAttendanceListReportParameter) {
			parameter = (ParticipantAttendanceListReportParameter) reportParameter;

			beginTime.setDate(parameter.getBeginDate());
			endTime.setDate(parameter.getEndDate());
			withInvalidLeadsButton.setSelection(parameter.isIncludeInvalidLeads());
		}
	}


	@Override
	public boolean isPageComplete() {
		return beginTime.getLocalDate() != null && endTime.getLocalDate() != null;
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

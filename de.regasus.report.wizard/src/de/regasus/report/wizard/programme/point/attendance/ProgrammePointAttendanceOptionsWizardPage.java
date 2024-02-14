package de.regasus.report.wizard.programme.point.attendance;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.report.programmePointAttendance.EndTimeType;
import com.lambdalogic.messeinfo.participant.report.programmePointAttendance.ProgrammePointAttendanceReportParameter;
import com.lambdalogic.messeinfo.participant.report.programmePointAttendance.StartTimeType;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionListenerAdapter;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class ProgrammePointAttendanceOptionsWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.programme.point.attendance.ProgrammePointAttendanceOptionsWizardPage";

	private static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	private ProgrammePointAttendanceReportParameter reportParameter;

	private boolean ignoreSelectionEvents = false;

	// Widgets
	private Button officialStartTimeButton;
	private Button entranceTimeButton;
	private Button realStartTimeButton;
	private Button firstLeadButton;
	private Button userDefinedStartTimeButton;
	private DateTimeComposite userDefinedStartDateTime;

	private Button officialEndTimeButton;
	private Button exitTimeButton;
	private Button realEndTimeButton;
	private Button lastLeadButton;
	private Button userDefinedEndTimeButton;
	private DateTimeComposite userDefinedEndDateTime;

	private NullableSpinner timeSlotWidthSpinner;



	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				if (!ignoreSelectionEvents && !ModifySupport.isDeselectedRadioButton(event)) {
					syncReportParameter();
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

	private ModifyListener modifyListener = new SelectionListenerAdapter(selectionListener);


	public ProgrammePointAttendanceOptionsWizardPage() {
		super(ID);
		setTitle(UtilI18N.TimeFrame);
		setDescription(ReportWizardI18N.ProgrammePointAttendanceOptionsWizardPage_Description);
	}

	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new FillLayout());
		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.makeColumnsEqualWidth = true;
		gridLayout.numColumns = 2;
		composite.setLayout(gridLayout);

		buildStartTimeGroup(composite);
		buildEndTimeGroup(composite);
		buildTimeSlotGroup(composite);
	}


	private void buildStartTimeGroup(Composite composite) {
		Group startTimeGroup = new Group(composite, SWT.NONE);

		startTimeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		startTimeGroup.setText(KernelLabel.StartTime.getString());
		startTimeGroup.setLayout(new GridLayout());

		officialStartTimeButton = new Button(startTimeGroup, SWT.RADIO);
		officialStartTimeButton.setText(ParticipantLabel.StartTimeType_OFFICIAL_START_TIME.getString());
		officialStartTimeButton.addSelectionListener(selectionListener);

		entranceTimeButton = new Button(startTimeGroup, SWT.RADIO);
		entranceTimeButton.setText(ParticipantLabel.StartTimeType_ENTRANCE_TIME.getString());
		entranceTimeButton.addSelectionListener(selectionListener);

		realStartTimeButton = new Button(startTimeGroup, SWT.RADIO);
		realStartTimeButton.setText(ParticipantLabel.StartTimeType_REAL_START_TIME.getString());
		realStartTimeButton.addSelectionListener(selectionListener);

		firstLeadButton = new Button(startTimeGroup, SWT.RADIO);
		firstLeadButton.setText(ParticipantLabel.StartTimeType_FIRST_LEAD.getString());
		firstLeadButton.addSelectionListener(selectionListener);

		userDefinedStartTimeButton = new Button(startTimeGroup, SWT.RADIO);
		userDefinedStartTimeButton.setText(ParticipantLabel.StartTimeType_USER_DEFINED.getString());
		userDefinedStartTimeButton.addSelectionListener(selectionListener);

		userDefinedStartDateTime = new DateTimeComposite(startTimeGroup, SWT.BORDER);
		userDefinedStartDateTime.addModifyListener(modifyListener);
	}


	private void buildEndTimeGroup(Composite composite) {
		Group endTimeGroup = new Group(composite, SWT.NONE);

		endTimeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		endTimeGroup.setText(KernelLabel.EndTime.getString());
		endTimeGroup.setLayout(new GridLayout());

		officialEndTimeButton = new Button(endTimeGroup, SWT.RADIO);
		officialEndTimeButton.setText(ParticipantLabel.EndTimeType_OFFICIAL_END_TIME.getString());
		officialEndTimeButton.addSelectionListener(selectionListener);

		exitTimeButton = new Button(endTimeGroup, SWT.RADIO);
		exitTimeButton.setText(ParticipantLabel.EndTimeType_EXIT_TIME.getString());
		exitTimeButton.addSelectionListener(selectionListener);

		realEndTimeButton = new Button(endTimeGroup, SWT.RADIO);
		realEndTimeButton.setText(ParticipantLabel.EndTimeType_REAL_END_TIME.getString());
		realEndTimeButton.addSelectionListener(selectionListener);

		lastLeadButton = new Button(endTimeGroup, SWT.RADIO);
		lastLeadButton.setText(ParticipantLabel.EndTimeType_LAST_LEAD.getString());
		lastLeadButton.addSelectionListener(selectionListener);

		userDefinedEndTimeButton = new Button(endTimeGroup, SWT.RADIO);
		userDefinedEndTimeButton.setText(ParticipantLabel.EndTimeType_USER_DEFINED.getString());
		userDefinedEndTimeButton.addSelectionListener(selectionListener);

		userDefinedEndDateTime = new DateTimeComposite(endTimeGroup, SWT.BORDER);
		userDefinedEndDateTime.addModifyListener(modifyListener);
	}


	private void buildTimeSlotGroup(Composite composite) {
		Group group = new Group(composite, SWT.NONE);

		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		group.setText( ParticipantLabel.TimeSlot.getString() );
		group.setLayout(new GridLayout(3, false));

		Label timeSlotWidthLabel = new Label(group, SWT.NONE);
		timeSlotWidthLabel.setText( ParticipantLabel.TimeSlot.getString() );
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(timeSlotWidthLabel);

		timeSlotWidthSpinner = new NullableSpinner(group, SWT.BORDER, true /*required*/);
		timeSlotWidthSpinner.setValue(10);
		timeSlotWidthSpinner.setMinimumAndMaximum(1, 60 * 24); // one minute, one day
		timeSlotWidthSpinner.addModifyListener(modifyListener);

		Label minutesLabel = new Label(group, SWT.NONE);
		minutesLabel.setText( KernelLabel.Minutes.getString() );
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(minutesLabel);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ProgrammePointAttendanceReportParameter) {
    		ignoreSelectionEvents = true;
    		try {
    			this.reportParameter = (ProgrammePointAttendanceReportParameter) reportParameter;


                switch (this.reportParameter.getStartTimeType()) {
        			case OFFICIAL_START_TIME:
        				officialStartTimeButton.setSelection(true);
        				break;

        			case REAL_START_TIME:
        				realStartTimeButton.setSelection(true);
        				break;

        			case ENTRANCE_TIME:
        				entranceTimeButton.setSelection(true);
        				break;

        			case FIRST_LEAD:
        				firstLeadButton.setSelection(true);
        				break;

        			case USER_DEFINED:
        				userDefinedStartTimeButton.setSelection(true);
        				break;

        			default:
        				break;
    			}

                userDefinedStartDateTime.setDate(this.reportParameter.getUserDefinedStartTime());
                userDefinedStartDateTime.setEnabled(
                	this.reportParameter.getStartTimeType() == StartTimeType.USER_DEFINED
                );


                switch (this.reportParameter.getEndTimeType()) {
        			case OFFICIAL_END_TIME:
        				officialEndTimeButton.setSelection(true);
        				break;

        			case REAL_END_TIME:
        				realEndTimeButton.setSelection(true);
        				break;

        			case EXIT_TIME:
        				exitTimeButton.setSelection(true);
        				break;

        			case LAST_LEAD:
        				lastLeadButton.setSelection(true);
        				break;

        			case USER_DEFINED:
        				userDefinedEndTimeButton.setSelection(true);
        				break;

        			default:
        				break;
    			}

                userDefinedEndDateTime.setDate(this.reportParameter.getUserDefinedEndTime());
                userDefinedEndDateTime.setEnabled(
                	this.reportParameter.getEndTimeType() == EndTimeType.USER_DEFINED
                );


                timeSlotWidthSpinner.setValue( this.reportParameter.getTimeSlotWidth() );
    		}
    		finally {
    			ignoreSelectionEvents = false;
    		}
		}
	}


	private void syncReportParameter() {
		// determine values
		StartTimeType startTimeType = null;
		if (officialStartTimeButton.getSelection()) {
			startTimeType = StartTimeType.OFFICIAL_START_TIME;
		}
		else if (realStartTimeButton.getSelection()) {
			startTimeType = StartTimeType.REAL_START_TIME;
		}
		else if (entranceTimeButton.getSelection()) {
			startTimeType = StartTimeType.ENTRANCE_TIME;
		}
		else if (firstLeadButton.getSelection()) {
			startTimeType = StartTimeType.FIRST_LEAD;
		}
		else if (userDefinedStartTimeButton.getSelection()) {
			startTimeType = StartTimeType.USER_DEFINED;
		}


		EndTimeType endTimeType = null;
		if (officialEndTimeButton.getSelection()) {
			endTimeType = EndTimeType.OFFICIAL_END_TIME;
		}
		else if (realEndTimeButton.getSelection()) {
			endTimeType = EndTimeType.REAL_END_TIME;
		}
		else if (exitTimeButton.getSelection()) {
			endTimeType = EndTimeType.EXIT_TIME;
		}
		else if (lastLeadButton.getSelection()) {
			endTimeType = EndTimeType.LAST_LEAD;
		}
		else if (userDefinedEndTimeButton.getSelection()) {
			endTimeType = EndTimeType.USER_DEFINED;
		}



		// set values to ReportParameter
		this.reportParameter.setStartTimeType(startTimeType);

		Date startDateTime = userDefinedStartDateTime.getDate();
		this.reportParameter.setUserDefinedStartTime(startDateTime);

		userDefinedStartDateTime.setEnabled(this.reportParameter.getStartTimeType() == StartTimeType.USER_DEFINED);


        this.reportParameter.setEndTimeType(endTimeType);

		Date endDateTime = userDefinedEndDateTime.getDate();
		this.reportParameter.setUserDefinedEndTime(endDateTime);

        userDefinedEndDateTime.setEnabled(this.reportParameter.getEndTimeType() == EndTimeType.USER_DEFINED);

        Integer timeSlotWidth = timeSlotWidthSpinner.getValueAsInteger();
		this.reportParameter.setTimeSlotWidth( timeSlotWidth );


		// set description
        StringBuilder desc = new StringBuilder();

        // StartTime
        desc.append(KernelLabel.StartTime.getString());
        desc.append(": ");
        if (startTimeType == StartTimeType.USER_DEFINED) {
        	String startTimeStr = "";
        	if (startDateTime != null) {
        		startTimeStr = DATE_FORMAT.format(startDateTime);
        	}
        	desc.append(startTimeStr);
        }
        else {
        	desc.append(startTimeType.getLabel());
        }
        this.reportParameter.setDescription(ProgrammePointAttendanceReportParameter.DESCRIPTION_START_TIME, desc.toString());

        // EndTime
        desc.setLength(0);
        desc.append(KernelLabel.EndTime.getString());
        desc.append(": ");
        if (endTimeType == EndTimeType.USER_DEFINED) {
        	String endTimeStr = "";
        	if (endDateTime != null) {
        		endTimeStr = DATE_FORMAT.format(endDateTime);
        	}
        	desc.append(endTimeStr);
        }
        else {
        	desc.append(endTimeType.getLabel());
        }
        this.reportParameter.setDescription(ProgrammePointAttendanceReportParameter.DESCRIPTION_END_TIME, desc.toString());

        // TimeSlotWidth
        desc.setLength(0);
        desc.append( ParticipantLabel.TimeSlot.getString() );
        desc.append(": ");
       	desc.append(timeSlotWidth);
       	desc.append(" ");
       	desc.append( KernelLabel.Minutes.getString() );
        this.reportParameter.setDescription(ProgrammePointAttendanceReportParameter.DESCRIPTION_TIME_SLOT_WIDTH, desc.toString());


        // ask the Wizard to check if the page is complete
        setPageComplete( isPageComplete() );
	}


	@Override
	public boolean isPageComplete() {
		return
			( userDefinedStartTimeButton.getSelection() == false || userDefinedStartDateTime.getDate() != null )
			&&
			( userDefinedEndTimeButton.getSelection() == false || userDefinedEndDateTime.getDate() != null )
    		&&
    		( timeSlotWidthSpinner.getValueAsInteger() != null );
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

package de.regasus.report.wizard.participant.change;

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

import com.lambdalogic.messeinfo.participant.report.participantChange.ParticipantChangeReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;

import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class ParticipantChangeOptionsWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.participant.change.ParticipantChangeOptionsWizardPage"; 


	private boolean ignoreModifyEvents = false;

	private ParticipantChangeReportParameter parameter;

	/**
	 * Zur Formatierung der Description
	 */
	private DateFormat dateFormat;

	// Widgets
	private DateTimeComposite referenceTime;



	/**
	 * Create the wizard
	 */
	public ParticipantChangeOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.ParticipantChangeOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.ParticipantChangeOptionsWizardPage_Description);
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	}


	/**
	 * Create contents of the wizard
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		container.setLayout(gridLayout);
		//
		setControl(container);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		composite.setLayout(gridLayout_1);

		final Label referenceTimeLabel = new Label(composite, SWT.NONE);
		referenceTimeLabel.setText(ReportWizardI18N.ParticipantChangeOptionsWizardPage_ReferenceTime);

		referenceTime = new DateTimeComposite(composite, SWT.BORDER);
		referenceTime.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!ignoreModifyEvents) {
    				// get value
    				Date referenceTimeValue = referenceTime.getDate();
    				setReferenceTime(referenceTimeValue);
				}
			}
		});

	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ParticipantChangeReportParameter) {
			ignoreModifyEvents = true;
			try {
    			parameter = (ParticipantChangeReportParameter) reportParameter;

    			Date referenceTimeValue = parameter.getReferenceTime();
    			if (referenceTimeValue == null) {
    				referenceTimeValue = new Date();
    			}

    			referenceTime.setDate(referenceTimeValue);
    			setReferenceTime(referenceTimeValue);
			}
			finally {
				ignoreModifyEvents = false;
			}
		}
	}


	public void setReferenceTime(Date referenceTimeValue) {
		if (parameter != null) {
			// set parameter
			parameter.setReferenceTime(referenceTimeValue);

			// set description
			String desc = null;
			if (referenceTimeValue != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(ReportWizardI18N.ParticipantChangeOptionsWizardPage_ReferenceTime);
				sb.append(": "); 
				sb.append(dateFormat.format(referenceTimeValue));
				desc = sb.toString();
			}
			parameter.setDescription(ParticipantChangeReportParameter.DESCRIPTION_REFERENCE_TIME, desc);
		}

		setPageComplete(isPageComplete());
	}


	@Override
	public boolean isPageComplete() {
		return referenceTime.getDate() != null;
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

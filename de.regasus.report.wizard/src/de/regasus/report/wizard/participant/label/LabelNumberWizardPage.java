package de.regasus.report.wizard.participant.label;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.messeinfo.participant.report.participantLabel.IParticipantLabelReportParameter;
import com.lambdalogic.messeinfo.participant.report.participantLabel.ParticipantLabelReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.widget.NullableSpinner;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;
import de.regasus.report.wizard.ui.Activator;

public class LabelNumberWizardPage extends WizardPage implements IReportWizardPage {
	public static final String ID = "de.regasus.report.wizard.participant.list.LabelNumberWizardPage";

	private NullableSpinner labelNumberSpinner;

	private ParticipantLabelReportParameter participantLabelReportParameter;


	/**
	 * Create the wizard
	 */
	public LabelNumberWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.LabelNumberWizardPage_Title);
		setDescription(ReportWizardI18N.LabelNumberWizardPage_Description);
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

		final Label labelNumberLabel = new Label(composite, SWT.NONE);
		labelNumberLabel.setText(ReportWizardI18N.LabelNumberWizardPage_LabelsPerPageLabel);

		labelNumberSpinner = new NullableSpinner(composite, SWT.BORDER);
		labelNumberSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				Integer labelNumber = getLabelNumber();
				try {
					if (participantLabelReportParameter != null) {
						participantLabelReportParameter.setLabelNumber(labelNumber);

						StringBuilder desc = new StringBuilder();
						desc.append(ReportWizardI18N.LabelNumberWizardPage_LabelsPerPageLabel);
						desc.append(": ");
						desc.append(labelNumber);

						participantLabelReportParameter.setDescription(
							IParticipantLabelReportParameter.DESCRIPTION_ID,
							desc.toString()
						);
					}
				}
				catch (ErrorMessageException e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});

		labelNumberSpinner.setMaximum(999);
		labelNumberSpinner.setMinimum(1);
		final GridData gd_labelsPerPageSpinner = new GridData();
		labelNumberSpinner.setLayoutData(gd_labelsPerPageSpinner);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ParticipantLabelReportParameter) {
			participantLabelReportParameter = (ParticipantLabelReportParameter) reportParameter;

			Integer labelNumber = participantLabelReportParameter.getLabelNumber();
			if (labelNumber == null) {
				labelNumber = 1;
			}

			labelNumberSpinner.setValue(labelNumber);
			setLabelNumber(labelNumber);
		}
	}


	protected Integer getLabelNumber() {
		return labelNumberSpinner.getValueAsInteger();
	}


	protected void setLabelNumber(Integer labelNumber) {
		if (labelNumber != null) {
			labelNumberSpinner.setValue(labelNumber.intValue());
		}
		else {
			labelNumberSpinner.setValue(1);
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

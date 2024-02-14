package de.regasus.report.wizard.participant.lead;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.report.participantLead.ParticipantLeadReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class ParticipantLeadSettingsWizardPage
extends WizardPage
implements IReportWizardPage, SelectionListener {
	public static final String ID = "de.regasus.report.wizard.participant.lead.ParticipantLeadSettingsWizardPage"; 

	private ParticipantLeadReportParameter participantLeadReportParameter;

	// Widgets
	private Button ignoreErrorsButton;


	/**
	 * Create the wizard
	 */
	public ParticipantLeadSettingsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.ParticipantLeadSettingsWizardPage_Title);
		setDescription(ReportWizardI18N.ParticipantLeadSettingsWizardPage_Description);
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
		final GridData gd_composite = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout());

		ignoreErrorsButton = new Button(composite, SWT.CHECK);
		ignoreErrorsButton.setText(ReportWizardI18N.ParticipantLeadSettingsWizardPage_IgnoreErrorsLabel);
		ignoreErrorsButton.addSelectionListener(this);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ParticipantLeadReportParameter) {
			participantLeadReportParameter = (ParticipantLeadReportParameter) reportParameter;

			ignoreErrorsButton.setSelection(participantLeadReportParameter.isIgnoreErrors());
		}
	}


	private void syncReportParameter() {
		// Werte ermitteln
		boolean ignoreErrors = ignoreErrorsButton.getSelection();

		// Werte in ReportParameter setzen
		participantLeadReportParameter.setIgnoreErrors(ignoreErrors);

		// description setzen
		StringBuilder desc = new StringBuilder();
		desc.append(ReportWizardI18N.ParticipantLeadSettingsWizardPage_IgnoreErrorsLabel);
		desc.append(": "); 
		if (ignoreErrors) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}

		participantLeadReportParameter.setDescription(ParticipantLeadReportParameter.DESCRIPTION_IGNORE_ERRORS_ID, desc.toString());
	}


	@Override
	public boolean isPageComplete() {
		return true;
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		syncReportParameter();
	}


	@Override
	public void saveReportParameters() {
		// nothing to do, because changes are saved immediately after user action
	}

}

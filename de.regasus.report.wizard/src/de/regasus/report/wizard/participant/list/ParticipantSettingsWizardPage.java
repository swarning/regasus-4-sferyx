package de.regasus.report.wizard.participant.list;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.report.participantList.ParticipantListReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class ParticipantSettingsWizardPage
extends WizardPage
implements IReportWizardPage, SelectionListener {
	public static final String ID = "de.regasus.report.wizard.participant.list.ParticipantListSettingsWizardPage"; 

	private ParticipantListReportParameter participantListReportParameter;

	// Widgets
	private Button withGroupManagersButton;
	private Button withCompanionsButton;
	private Button withAccompaniedButton;


	public ParticipantSettingsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.ParticipantSettingsWizardPage_Title);
		setDescription(ReportWizardI18N.ParticipantSettingsWizardPage_Description);
	}


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

		withGroupManagersButton = new Button(composite, SWT.CHECK);
		withGroupManagersButton.setText(ReportWizardI18N.ParticipantSettingsWizardPage_WithGroupManagersLabel);
		withGroupManagersButton.setToolTipText(ReportWizardI18N.ParticipantSettingsWizardPage_WithGroupManagersToolTip);
		withGroupManagersButton.addSelectionListener(this);

		withCompanionsButton = new Button(composite, SWT.CHECK);
		withCompanionsButton.setText(ReportWizardI18N.ParticipantSettingsWizardPage_WithCompanionsLabel);
		withCompanionsButton.setToolTipText(ReportWizardI18N.ParticipantSettingsWizardPage_WithCompanionsToolTip);
		withCompanionsButton.addSelectionListener(this);

		withAccompaniedButton = new Button(composite, SWT.CHECK);
		withAccompaniedButton.setText(ReportWizardI18N.ParticipantSettingsWizardPage_WithAccompaniedLabel);
		withAccompaniedButton.setToolTipText(ReportWizardI18N.ParticipantSettingsWizardPage_WithAccompaniedToolTip);
		withAccompaniedButton.addSelectionListener(this);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ParticipantListReportParameter) {
			participantListReportParameter = (ParticipantListReportParameter) reportParameter;

			withGroupManagersButton.setSelection(participantListReportParameter.isWithGroupManagers());
			withCompanionsButton.setSelection(participantListReportParameter.isWithCompanions());
			withAccompaniedButton.setSelection(participantListReportParameter.isWithAccompanied());
		}
	}


	private void syncReportParameter() {
		// Werte ermitteln
		boolean withGroupManagers = withGroupManagersButton.getSelection();
		boolean withCompanions = withCompanionsButton.getSelection();
		boolean withAccompanied = withAccompaniedButton.getSelection();

		// Werte in ReportParameter setzen
		participantListReportParameter.setWithGroupManagers(withGroupManagers);
		participantListReportParameter.setWithCompanions(withCompanions);
		participantListReportParameter.setWithAccompanied(withAccompanied);

		// description setzen
		StringBuilder desc = new StringBuilder();

		desc.append(ReportWizardI18N.ParticipantSettingsWizardPage_WithGroupManagersLabel);
		desc.append(": ");
		if (withGroupManagers) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}
		participantListReportParameter.setDescription(ParticipantListReportParameter.DESCRIPTION_WITH_GROUP_MANAGERS_ID, desc.toString());


		desc.setLength(0);
		desc.append(ReportWizardI18N.ParticipantSettingsWizardPage_WithCompanionsLabel);
		desc.append(": ");
		if (withCompanions) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}
		participantListReportParameter.setDescription(ParticipantListReportParameter.DESCRIPTION_WITH_COMPANIONS_ID, desc.toString());

		desc.setLength(0);
		desc.append(ReportWizardI18N.ParticipantSettingsWizardPage_WithAccompaniedLabel);
		desc.append(": ");
		if (withAccompanied) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}
		participantListReportParameter.setDescription(ParticipantListReportParameter.DESCRIPTION_WITH_ACCOMPANIED_ID, desc.toString());
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

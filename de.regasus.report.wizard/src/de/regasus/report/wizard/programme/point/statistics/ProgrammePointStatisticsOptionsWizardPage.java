package de.regasus.report.wizard.programme.point.statistics;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.report.parameter.IProgrammePointListReportParameter;
import com.lambdalogic.messeinfo.participant.report.programmePointStatistics.ProgrammePointStatisticsReportParameter;
import com.lambdalogic.messeinfo.participant.report.programmePointStatistics.ProgrammePointStatisticsReportParameter.PaymentStatus;
import com.lambdalogic.report.parameter.IReportParameter;

import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class ProgrammePointStatisticsOptionsWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "de.regasus.report.wizard.programme.point.statistics.ProgrammePointStatisticsOptionsWizardPage";

	private ProgrammePointStatisticsReportParameter parameter;


	// Widgets
	private Group grpBrutto;
	private Button btnGross;
	private Button btnNet;

	private Group grpType;
	private Button btnBookings;
	private Button btnCancelations;

	private Group grpPaymentStatus;
	private Button btnAll;
	private Button btnOnlyPaidBookings;
	private Button btnOnlyPaidParticipants;
	private Button btnOnlyUnpaidBookings;
	private Button btnOnlyUnpaidParticipants;

	private Group grpParticipantType;
	private Button btnOfferings;
	private Button btnRecipients;


	public ProgrammePointStatisticsOptionsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_Title);
		setDescription(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_Description);
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
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		composite.setLayout(gridLayout);

		// amounts (gross/net)
		grpBrutto = new Group(composite, SWT.NONE);
		grpBrutto.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		grpBrutto.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpAmounts_text);
		grpBrutto.setLayout(new GridLayout(1, false));

		btnGross = new Button(grpBrutto, SWT.RADIO);
		btnGross.setText(InvoiceLabel.gross.getString());
		btnGross.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnGross_toolTipText);

		btnNet = new Button(grpBrutto, SWT.RADIO);
		btnNet.setText(InvoiceLabel.net.getString());
		btnNet.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnNet_toolTipText);

		// Booking Type
		grpType = new Group(composite, SWT.NONE);
		grpType.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		grpType.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpType_text);
		grpType.setLayout(new GridLayout(1, false));

		btnBookings = new Button(grpType, SWT.RADIO);
		btnBookings.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnBookings_text);
		btnBookings.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnBookings_toolTipText);

		btnCancelations = new Button(grpType, SWT.RADIO);
		btnCancelations.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnCancelations_toolTipText);
		btnCancelations.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnCancelations_text);

		// Payment Status
		grpPaymentStatus = new Group(composite, SWT.NONE);
		grpPaymentStatus.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		grpPaymentStatus.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpPaymentStatus_text);
		grpPaymentStatus.setLayout(new GridLayout(1, false));

		btnAll = new Button(grpPaymentStatus, SWT.RADIO);
		btnAll.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnAll_text);
		btnAll.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnAll_toolTipText);

		btnOnlyPaidBookings = new Button(grpPaymentStatus, SWT.RADIO);
		btnOnlyPaidBookings.setBounds(0, 0, 83, 16);
		btnOnlyPaidBookings.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyPaidBookings_text);
		btnOnlyPaidBookings.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyPaidBookings_toolTipText);

		btnOnlyPaidParticipants = new Button(grpPaymentStatus, SWT.RADIO);
		btnOnlyPaidParticipants.setBounds(0, 0, 83, 16);
		btnOnlyPaidParticipants.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyPaidParticipants_text);
		btnOnlyPaidParticipants.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyPaidParticipants_toolTipText);

		btnOnlyUnpaidBookings = new Button(grpPaymentStatus, SWT.RADIO);
		btnOnlyUnpaidBookings.setBounds(0, 0, 83, 16);
		btnOnlyUnpaidBookings.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyUnpaidBookings_text);
		btnOnlyUnpaidBookings.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyUnpaidBookings_toolTipText);

		btnOnlyUnpaidParticipants = new Button(grpPaymentStatus, SWT.RADIO);
		btnOnlyUnpaidParticipants.setBounds(0, 0, 83, 16);
		btnOnlyUnpaidParticipants.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyUnpaidParticipants_text);
		btnOnlyUnpaidParticipants.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyUnpaidParticipants_toolTipText);

		// Participant Type
		grpParticipantType = new Group(composite, SWT.NONE);
		grpParticipantType.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		grpParticipantType.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpParticipantType_text);
		grpParticipantType.setLayout(new GridLayout(1, false));

		btnOfferings = new Button(grpParticipantType, SWT.RADIO);
		btnOfferings.setBounds(0, 0, 83, 16);
		btnOfferings.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOfferings_text);
		btnOfferings.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOfferings_toolTipText);

		btnRecipients = new Button(grpParticipantType, SWT.RADIO);
		btnRecipients.setBounds(0, 0, 83, 16);
		btnRecipients.setText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnRecipients_text);
		btnRecipients.setToolTipText(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnRecipients_toolTipText);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof IProgrammePointListReportParameter) {
			parameter = (ProgrammePointStatisticsReportParameter) reportParameter;

			btnGross.setSelection(parameter.isBrutto());
			btnNet.setSelection(!parameter.isBrutto());

			btnBookings.setSelection(parameter.isWithBookings());
			btnCancelations.setSelection(parameter.isWithCancelations());
			// set default
			if (!btnBookings.getSelection() && !btnCancelations.getSelection()) {
				btnBookings.setSelection(true);
			}

			btnAll.setSelection(parameter.getPaymentStatus() == null || parameter.getPaymentStatus() == PaymentStatus.all);
			btnOnlyPaidBookings.setSelection(parameter.getPaymentStatus() == PaymentStatus.onlyPaidBookings);
			btnOnlyPaidParticipants.setSelection(parameter.getPaymentStatus() == PaymentStatus.onlyPaidParticipants);
			btnOnlyUnpaidBookings.setSelection(parameter.getPaymentStatus() == PaymentStatus.onlyUnpaidBookings);
			btnOnlyUnpaidParticipants.setSelection(parameter.getPaymentStatus() == PaymentStatus.onlyUnpaidParticipants);

			btnOfferings.setSelection(!parameter.isUseRecipientParticipantTypes());
			btnRecipients.setSelection(parameter.isUseRecipientParticipantTypes());
		}
	}


	@Override
	public boolean isPageComplete() {
		// This page contains only radio buttons which all have default values.
		return true;
	}


	@Override
	public void saveReportParameters() {
		// determine values
		boolean brutto = btnGross.getSelection();

		boolean withBookings = btnBookings.getSelection();
		boolean withCancelations = btnCancelations.getSelection();

		PaymentStatus paymentStatus = PaymentStatus.all;
		if (btnOnlyPaidBookings.getSelection()) {
			paymentStatus = PaymentStatus.onlyPaidBookings;
		}
		else if (btnOnlyPaidParticipants.getSelection()) {
			paymentStatus = PaymentStatus.onlyPaidParticipants;
		}
		else if (btnOnlyUnpaidBookings.getSelection()) {
			paymentStatus = PaymentStatus.onlyUnpaidBookings;
		}
		else if (btnOnlyUnpaidParticipants.getSelection()) {
			paymentStatus = PaymentStatus.onlyUnpaidParticipants;
		}

		boolean useRecipientParticipantTypes = btnRecipients.getSelection();

		// Werte in ReportParameter setzen
		parameter.setBrutto(brutto);
		parameter.setWithBookings(withBookings);
		parameter.setWithCancelations(withCancelations);
		parameter.setPaymentStatus(paymentStatus);
		parameter.setUseRecipientParticipantTypes(useRecipientParticipantTypes);

		// set description
		StringBuilder desc = new StringBuilder();

		desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpAmounts_text);
		desc.append(": ");
		if (brutto) {
			desc.append(InvoiceLabel.gross.getString());
		}
		else {
			desc.append(InvoiceLabel.net.getString());
		}

		desc.append("\n");
		desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpType_text);
		desc.append(": ");
		if (withBookings) {
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnBookings_text);
		}
		else if (withCancelations) {
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnCancelations_text);
		}

		desc.append("\n");
		desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpPaymentStatus_text);
		desc.append(": ");
		switch (paymentStatus) {
		case all:
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnAll_text);
			break;

		case onlyPaidBookings:
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyPaidBookings_text);
			break;

		case onlyPaidParticipants:
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyPaidParticipants_text);
			break;

		case onlyUnpaidBookings:
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyUnpaidBookings_text);
			break;

		case onlyUnpaidParticipants:
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOnlyUnpaidParticipants_text);
			break;
		}


		desc.append("\n");
		desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_grpParticipantType_text);
		desc.append(": ");
		if (useRecipientParticipantTypes) {
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnRecipients_text);
		}
		else {
			desc.append(ReportWizardI18N.ProgrammePointStatisticsOptionsWizardPage_btnOfferings_text);
		}

		parameter.setDescription(ProgrammePointStatisticsReportParameter.DESCRIPTION_ID, desc.toString());
	}

}

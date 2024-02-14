package de.regasus.report.wizard.participant.list;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.report.participantList.ParticipantCVOsReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;

public class BookingsWizardPage
extends WizardPage
implements IReportWizardPage, SelectionListener {
	public static final String ID = "de.regasus.report.wizard.participant.list.BookingsWizardPage"; 

	private ParticipantCVOsReportParameter participantCVOsReportParameter;

	// Widgets
	private Button withProgrammeBookingsButton;
	private Button withHotelBookingsButton;


	/**
	 * Create the wizard
	 */
	public BookingsWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.BookingsWizardPage_Title);
		setDescription(ReportWizardI18N.BookingsWizardPage_Description);
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

		withProgrammeBookingsButton = new Button(composite, SWT.CHECK);
		withProgrammeBookingsButton.setText(ReportWizardI18N.BookingsWizardPage_WithProgrammeBookingsButton);
		withProgrammeBookingsButton.setToolTipText(ReportWizardI18N.BookingsWizardPage_WithProgrammeBookingsButtonToolTip);
		withProgrammeBookingsButton.addSelectionListener(this);

		withHotelBookingsButton = new Button(composite, SWT.CHECK);
		withHotelBookingsButton.setText(ReportWizardI18N.BookingsWizardPage_WithHotelBookingsButton);
		withHotelBookingsButton.setToolTipText(ReportWizardI18N.BookingsWizardPage_WithHotelBookingsButtonToolTip);
		withHotelBookingsButton.addSelectionListener(this);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof ParticipantCVOsReportParameter) {
			participantCVOsReportParameter = (ParticipantCVOsReportParameter) reportParameter;

			withProgrammeBookingsButton.setSelection(participantCVOsReportParameter.isWithProgrammeBookings());
			withHotelBookingsButton.setSelection(participantCVOsReportParameter.isWithHotelBookings());
		}
	}


	private void syncReportParameter() {
		if (participantCVOsReportParameter != null) {
			// Werte ermitteln
			boolean withProgrammeBookings = withProgrammeBookingsButton.getSelection();
			boolean withHotelBookings = withHotelBookingsButton.getSelection();

			// Werte in ReportParameter setzen
			participantCVOsReportParameter.setWithProgrammeBookings(withProgrammeBookings);
			participantCVOsReportParameter.setWithHotelBookings(withHotelBookings);

			// description setzen
			StringBuilder desc = new StringBuilder();

			desc.append(ReportWizardI18N.BookingsWizardPage_WithProgrammeBookingsButton);
			desc.append(": "); 
			if (withProgrammeBookings) {
				desc.append(UtilI18N.Yes);
			}
			else {
				desc.append(UtilI18N.No);
			}
			participantCVOsReportParameter.setDescription(ParticipantCVOsReportParameter.ATTRIBUTE_WITH_PROGRAMME_BOOKINGS, desc.toString());


			desc.setLength(0);
			desc.append(ReportWizardI18N.BookingsWizardPage_WithHotelBookingsButton);
			desc.append(": "); 
			if (withHotelBookings) {
				desc.append(UtilI18N.Yes);
			}
			else {
				desc.append(UtilI18N.No);
			}
			participantCVOsReportParameter.setDescription(ParticipantCVOsReportParameter.ATTRIBUTE_WITH_HOTEL_BOOKINGS, desc.toString());
		}
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

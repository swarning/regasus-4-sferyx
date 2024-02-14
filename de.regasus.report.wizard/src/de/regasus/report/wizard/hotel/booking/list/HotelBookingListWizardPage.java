package de.regasus.report.wizard.hotel.booking.list;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.hotel.report.hotelBookings.HotelBookingsReportParameter;
import com.lambdalogic.report.parameter.IReportParameter;

import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.report.dialog.IReportWizardPage;
import de.regasus.report.wizard.ReportWizardI18N;


public class HotelBookingListWizardPage extends WizardPage implements IReportWizardPage {

	public static final String ID = "HotelBookingsWizardPage";

	private HotelBookingsReportParameter hotelBookingsReportParameter;

	// Widgets
	private Button withChargeableCancelationsButton;

	private SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			syncReportParameter();
		}
	};


	public HotelBookingListWizardPage() {
		super(ID);
		setTitle(ReportWizardI18N.HotelBookingsWizardPage_Title);
		setDescription(ReportWizardI18N.HotelBookingsWizardPage_Description);
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

		withChargeableCancelationsButton = new Button(composite, SWT.CHECK);
		withChargeableCancelationsButton.setText(ReportWizardI18N.HotelBookingsWizardPage_withChargeableCancelationsLabel);
		withChargeableCancelationsButton.setToolTipText(ReportWizardI18N.HotelBookingsWizardPage_withChargeableCancelationsToolTip);
		withChargeableCancelationsButton.addSelectionListener(selectionListener);
	}


	@Override
	public void init(IReportParameter reportParameter) {
		if (reportParameter instanceof HotelBookingsReportParameter) {
			hotelBookingsReportParameter = (HotelBookingsReportParameter) reportParameter;

			withChargeableCancelationsButton.setSelection(hotelBookingsReportParameter.isWithChargeableCancelations());
		}
	}


	private void syncReportParameter() {
		// determine values from widgets
		boolean withChargeableCancelations = withChargeableCancelationsButton.getSelection();

		// set values to ReportParameter
		hotelBookingsReportParameter.setWithChargeableCancelations(withChargeableCancelations);

		// set description
		StringBuilder desc = new StringBuilder(256);

		desc.append(ReportWizardI18N.HotelBookingsWizardPage_withChargeableCancelationsLabel);
		desc.append(": ");
		if (withChargeableCancelations) {
			desc.append(UtilI18N.Yes);
		}
		else {
			desc.append(UtilI18N.No);
		}
		hotelBookingsReportParameter.setDescription(HotelBookingsReportParameter.DESCRIPTION_ID_WITH_CHARGEABLE_CANCELATIONS, desc.toString());
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

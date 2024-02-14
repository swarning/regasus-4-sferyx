package de.regasus.programme.booking.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.regasus.I18N;

/**
 * A wizard page which shows two radio buttons to determine whether the booking should happen with offerings for one
 * particular participant type, or should use programme points and let the server choose appropriate offerings.
 * <p>
 * {@link https://mi2.lambdalogic.de/jira/browse/MIRCP-105 }
 * 
 * @author manfred
 * 
 */
public class DecideBookingModePage extends WizardPage {

	public static final String NAME = "DecideBookingModePage";

	private Button bookingViaProgrammeOfferingsRadioButton;

	
	private Button bookingViaProgrammePointsRadioButton;


	public DecideBookingModePage() {
		super(NAME);

		setTitle(I18N.CreateProgrammeBookings_Text);
		setMessage(I18N.CreateProgrammeBookings_DeciceBookingMode);
	}


	/**
	 * Shows three radio buttons to determine who should be the participant
	 */
	public void createControl(Composite parent) {
		Composite controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(2, false));

		// should use programme points and let the server choose appropriate offerings
		bookingViaProgrammePointsRadioButton = new Button(controlComposite, SWT.RADIO);

		Label label1 = new Label(controlComposite, SWT.WRAP);
		label1.setText(I18N.CreateProgrammeBookings_BookingViaProgrammePointsRadioButton);
		label1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// booking should happen with offerings for one particular participant type
		bookingViaProgrammeOfferingsRadioButton = new Button(controlComposite, SWT.RADIO);

		Label label2 = new Label(controlComposite, SWT.WRAP);
		label2.setText(I18N.CreateProgrammeBookings_BookingViaProgrammeOfferingsRadioButton);
		label2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		bookingViaProgrammePointsRadioButton.setSelection(true);
		
		setControl(controlComposite);
		
		setPageComplete(true);
	}


	public boolean isBookingViaProgrammeOfferings() {
		return bookingViaProgrammeOfferingsRadioButton.getSelection();
	}

	@Override
    public boolean canFlipToNextPage() {
        return true;
    }

	@Override
	public IWizardPage getNextPage() {
		SelectProgrammeOfferingsPage selectProgrammeOfferingsPage = (SelectProgrammeOfferingsPage) getWizard().getPage(SelectProgrammeOfferingsPage.NAME);
		SelectProgrammePointsPage selectProgrammePointsPage = (SelectProgrammePointsPage) getWizard().getPage(SelectProgrammePointsPage.NAME);

		// the wizard can be finished when all pages are complete, the currently used 
		// selectXxxPage is complete when something is booked, the other page needs
		// to be completed manually 
		if (isBookingViaProgrammeOfferings()) {
			selectProgrammePointsPage.setPageComplete(true);
			return selectProgrammeOfferingsPage;
		}
		else {
			selectProgrammeOfferingsPage.setPageComplete(true);
			return selectProgrammePointsPage;
		}
		
	}

	
}

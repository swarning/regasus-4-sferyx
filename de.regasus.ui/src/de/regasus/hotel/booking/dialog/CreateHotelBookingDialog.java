package de.regasus.hotel.booking.dialog;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.data.HotelCostCoverage;
import com.lambdalogic.messeinfo.participant.data.IParticipant;

public class CreateHotelBookingDialog extends WizardDialog {


	public static CreateHotelBookingDialog create(
		Shell shell,
		List<IParticipant> participantList,
		HotelCostCoverage hotelCostCoverage
	) {
		Objects.requireNonNull(shell);
		Objects.requireNonNull(participantList);

		if ( participantList.isEmpty() ) {
			throw new IllegalArgumentException("There must be at least one participant.");
		}


		// Create the Wizard instance
		CreateHotelBookingDialog wizardDialog = new CreateHotelBookingDialog(shell, participantList);

		// Set parameters for preselection (before create(), because they are necessary before the Pages are created)
		// ignore HotelCostCoverage if it cannot be used
		if (hotelCostCoverage != null && hotelCostCoverage.isDefined() && !hotelCostCoverage.isUsed()) {
			wizardDialog.getWizard().setHotelCostCoverage(hotelCostCoverage);
		}


		// Call the create() method of the Wizard (which let the Wizard create its Pages)
		wizardDialog.create();

		return wizardDialog;
	}


	public static CreateHotelBookingDialog create(Shell shell, List<IParticipant> participantList) {
		return create(shell, participantList, null);
	}


	private CreateHotelBookingDialog(Shell shell, List<IParticipant> participantList) {
		super(shell, new CreateHotelBookingWizard(participantList));
	}


	// Define the initial width and height of the dialog.
	@Override
	protected Point getInitialSize() {
		return new Point(1024 - 20, 768 - 20);
	}


	// Overridden to get more specific type
	@Override
	protected CreateHotelBookingWizard getWizard() {
		return (CreateHotelBookingWizard) super.getWizard();
	}

}

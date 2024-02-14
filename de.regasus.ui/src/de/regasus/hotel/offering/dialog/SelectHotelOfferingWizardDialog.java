package de.regasus.hotel.offering.dialog;

import java.util.Objects;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.rcp.UtilI18N;

// REFERENCE
/**
 * Dialog to select a Hotel Offering.
 * All presented data is restricted to a single Event.
 * The dialog contains two pages. On the first page the user has to select a Hotel. The list of Hotels include all
 * Hotels for which there is a Hotel Offering in the Event.
 * The second page shows the Hotel Offerings of the previously selected Hotel.
 */
public class SelectHotelOfferingWizardDialog extends WizardDialog {

	/**
	 * Create an instance of {@link SelectHotelOfferingWizardDialog}.
	 * @param shell
	 * @param eventId
	 *  Mandatory parameter to restrict the displayed data to one event.
	 * @param initialHotelId
	 *  Optional parameter that determines the hotel that shall be preselected.
	 * @param initialOfferingId
	 *  Optional parameter that determines the hotel offering that shall be preselected.
	 * @param selectHotelDescription
	 *  A more specific description text for the page where the user selects the Hotel.
	 * @param selectOfferingDescription
	 *  A more specific description text for the page where the user selects the Hotel Offering.
	 * @return
	 */
	/* The reason for this create() method is that the order of the following steps must be strictly followed:
	 * - Create the Wizard instance
	 * - Set parameters for preselection
	 * - Call the create() method of the Wizard
	 * - Set specific WizardPage descriptions
	 */
	public static SelectHotelOfferingWizardDialog create(
		Shell shell,
		Long eventId,
		Long initialHotelId,
		Long initialOfferingId,
		String selectHotelDescription,
		String selectOfferingDescription
	) {
		Objects.requireNonNull(shell);
		Objects.requireNonNull(eventId);

		// Create the Wizard instance
		SelectHotelOfferingWizardDialog wizardDialog = new SelectHotelOfferingWizardDialog(shell, eventId);

		// Set parameters for preselection (before create(), because they are necessary before the Pages are created)
		wizardDialog.getWizard().setInitialHotelId(initialHotelId);
		wizardDialog.getWizard().setInitialOfferingId(initialOfferingId);

		// Call the create() method of the Wizard (which let the Wizard create its Pages)
		wizardDialog.create();

		// Set specific WizardPage descriptions (after create(), because the Pages don't exist before)
		wizardDialog.getWizard().setHotelSelectionDescription(selectHotelDescription);
		wizardDialog.getWizard().setOfferingSelectionDescription(selectOfferingDescription);

		return wizardDialog;
	}


	private SelectHotelOfferingWizardDialog(Shell parentShell, Long eventId) {
		super(parentShell, new SelectHotelOfferingWizard(eventId));
	}


	// Define the initial width and height of the dialog.
	@Override
	protected Point getInitialSize() {
		return new Point(1024 - 100, 768 - 100);
	}


	// Overridden to rename the "Finish" button to "Select".
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		// set different label to finish-button
		if (id == IDialogConstants.FINISH_ID) {
			label = UtilI18N.Select;
		}

		return super.createButton(parent, id, label, defaultButton);
	}


	// Overridden to get more specific type
	@Override
	protected SelectHotelOfferingWizard getWizard() {
		return (SelectHotelOfferingWizard) super.getWizard();
	}


	public Long getOfferingId() {
		return getWizard().getSelectedOfferingId();
	}


	public Long getHotelId() {
		return getWizard().getSelectedHotelId();
	}

}

package de.regasus.profile.relation;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.profile.Profile;

import de.regasus.profile.dialog.ProfileSelectionWizardPage;
import de.regasus.profile.relationtype.ProfileRelationTypeSelectionPage;

public class CreateProfileRelationDialog extends WizardDialog {

	public CreateProfileRelationDialog(Shell parentShell, Profile profile1) {
		super(parentShell, new CreateProfileRelationWizard(profile1));
	}


	public Point getPreferredSize() {
		return new Point(800, 600);
	}


	@Override
	protected void nextPressed() {
		IWizardPage currentPage = getCurrentPage();
		if (currentPage instanceof ProfileSelectionWizardPage) {
			// copy name of selected Profile to next WizardPage
			Profile profile2 = ((ProfileSelectionWizardPage) currentPage).getSelectedProfiles().get(0);
			String profile2Name = profile2.getName();

			ProfileRelationTypeSelectionPage profileRelationTypeSelectionPage =
				(ProfileRelationTypeSelectionPage) getWizard().getNextPage(currentPage);
			profileRelationTypeSelectionPage.setProfile2Name(profile2Name);
		}
		super.nextPressed();
	}

}

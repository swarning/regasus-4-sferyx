package de.regasus.profile.editor.document;

import java.io.File;

import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.StringHelper;

import de.regasus.common.composite.AbstractUploadFileDialog;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.file.job.UploadProfileFileJob;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

public class UploadProfileFileDialog extends AbstractUploadFileDialog {

	private Long profileID;


	public UploadProfileFileDialog(Shell parentShell, Long profileID) throws Exception {
		super(parentShell);

		this.profileID = profileID;
	}


	@Override
	public void okPressed() {
		try {
			// File exists because otherwise okButton were disabled
			File file = fileSelectionComposite.getFile();

			Profile profile = ProfileModel.getInstance().getProfile(profileID);
			String name = StringHelper.trim(documentNameText.getText());
			String description = StringHelper.trim(descriptionText.getText());

			UploadProfileFileJob job = new UploadProfileFileJob(
				profile,
				file,
				name,
				description
			);
			job.setUser(true);
			job.schedule();

			super.okPressed();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

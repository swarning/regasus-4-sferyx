package de.regasus.participant.editor.document;

import java.io.File;

import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.StringHelper;

import de.regasus.common.composite.AbstractUploadFileDialog;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.file.job.UploadParticipantFileJob;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class UploadParticipantFileDialog extends AbstractUploadFileDialog {

	private Long participantID;


	public UploadParticipantFileDialog(Shell parentShell, Long participantID) {
		super(parentShell);

		this.participantID = participantID;
	}


	@Override
	public void okPressed() {
		try {
			// File exists because otherwise okButton were disabled
			File file = fileSelectionComposite.getFile();

			Participant participant = ParticipantModel.getInstance().getParticipant(participantID);
			String name = StringHelper.trim(documentNameText.getText());
			String description = StringHelper.trim(descriptionText.getText());

			UploadParticipantFileJob job = new UploadParticipantFileJob(
				participant,
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

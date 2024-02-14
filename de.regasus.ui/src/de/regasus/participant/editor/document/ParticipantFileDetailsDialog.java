package de.regasus.participant.editor.document;

import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.StringHelper;

import de.regasus.common.FileSummary;
import de.regasus.common.composite.AbstractFileDetailsDialog;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantFileModel;
import de.regasus.ui.Activator;

public class ParticipantFileDetailsDialog extends AbstractFileDetailsDialog {

	public ParticipantFileDetailsDialog(Shell parentShell, FileSummary fileSummary) {
		super(parentShell, fileSummary);
	}


	@Override
	public void okPressed() {
		try {
			String description = StringHelper.trim( descriptionText.getText() );
			String name = StringHelper.trim(documentNameText.getText());

			// clone before changing its data to avoid dirty entity if update fails
			fileSummary = fileSummary.clone();

			fileSummary.setName(name);
			fileSummary.setDescription(description);

			ParticipantFileModel.getInstance().update(fileSummary);

			super.okPressed();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

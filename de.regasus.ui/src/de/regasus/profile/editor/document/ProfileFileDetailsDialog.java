package de.regasus.profile.editor.document;

import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.util.StringHelper;

import de.regasus.common.FileSummary;
import de.regasus.common.composite.AbstractFileDetailsDialog;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileFileModel;
import de.regasus.ui.Activator;

public class ProfileFileDetailsDialog extends AbstractFileDetailsDialog {

	public ProfileFileDetailsDialog(Shell parentShell, FileSummary fileSummary) {
		super(parentShell, fileSummary);
	}


	@Override
	public void okPressed() {
		try {
			String description = StringHelper.trim(descriptionText.getText());
			String name = StringHelper.trim(documentNameText.getText());

			// clone before changing its data to avoid dirty entity if update fails
			fileSummary = fileSummary.clone();

			fileSummary.setName(name);
			fileSummary.setDescription(description);

			ProfileFileModel.getInstance().update(fileSummary);

			super.okPressed();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

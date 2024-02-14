package de.regasus.profile.editor.document;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.FileSummary;
import de.regasus.common.composite.PersonDocumentComposite;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileFileModel;
import de.regasus.ui.Activator;

public class ProfileFileComposite extends PersonDocumentComposite {

	// the entity
	private Profile profile;

	// model
	private ProfileFileModel profileFileModel;


	public ProfileFileComposite(final Composite tabFolder, int style) {
		super(tabFolder, style);

		profileFileModel = ProfileFileModel.getInstance();
	}


	@Override
	protected void createPartControl() throws Exception {
		super.createPartControl();

		profileFileModel.addForeignKeyListener(this, profile.getID());
	}


	@Override
	protected ProfileFileTable createSimpleTable(Table table) {
		return new ProfileFileTable(table);
	}


	@Override
	protected void details() {
		ISelection selection = fileTable.getViewer().getSelection();
		FileSummary fileSummary = SelectionHelper.getUniqueSelected(selection);

		ProfileFileDetailsDialog dialog = new ProfileFileDetailsDialog(getShell(), fileSummary);
		dialog.create();
		dialog.getShell().setSize(600, 500);
		dialog.open();
	}


	@Override
	protected void upload() throws Exception {
		UploadProfileFileDialog dialog = new UploadProfileFileDialog(getShell(), profile.getID());
		dialog.create();
		dialog.getShell().setSize(600, 400);
		dialog.open();
	}


	@Override
	protected void delete() {
		ISelection selection = fileTable.getViewer().getSelection();
		FileSummary fileSummary = SelectionHelper.getUniqueSelected(selection);
		String message = NLS.bind(UtilI18N.ReallyDeleteOne, UtilI18N.Document, fileSummary.getName());
		boolean answer = MessageDialog.openQuestion(getShell(), UtilI18N.Question, message);
		if (answer) {
			try {
				profileFileModel.delete(fileSummary);
				syncWidgetsToEntity();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if ( ! isDisposed()) {
    					if (profile != null && profile.getID() != null) {
    						Long profileID = profile.getID();
    						List<FileSummary> fileSummaryList = profileFileModel.getProfileDocumentsByProfileId(profileID);
    						fileTable.setInput(fileSummaryList);
    					}

    					updateButtonStates();
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public Profile getProfile() {
		return profile;
	}


	public void setProfile(Profile profile) {
		if (profile == null) {
			throw new IllegalArgumentException("Parameter 'profile' must not be null");
		}

		if (this.profile != null &&
			this.profile.getID() != null &&
			! this.profile.getID().equals(profile.getID())
		) {
			throw new IllegalArgumentException("Profile.ID must not change");
		}

		this.profile = profile;

		if (isInitialized()) {
			syncWidgetsToEntity();
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (profile != null && profile.getID() != null) {
			profileFileModel.removeForeignKeyListener(this, profile.getID());
		}
	}

}

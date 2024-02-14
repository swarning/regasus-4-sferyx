package de.regasus.profile.role.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.profile.ProfileRoleModel;

public class ProfileRoleEditor
extends AbstractEditor<ProfileRoleEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "ProfileRoleEditor";

	// the entity
	private ProfileRole profileRole;

	// the model
	private ProfileRoleModel profileRoleModel;

	// **************************************************************************
	// * Widgets
	// *

	private Text nameText;
	private Text descriptionText;

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		profileRoleModel = ProfileRoleModel.getInstance();

		if (key != null) {
			// get entity
			profileRole = profileRoleModel.getProfileRole(key);

			// register at model
			profileRoleModel.addListener(this, key);
		}
		else {
			// create empty entity
			profileRole = new ProfileRole();
		}
	}


	@Override
	public void dispose() {
		if (profileRoleModel != null && profileRole.getID() != null) {
			try {
				profileRoleModel.removeListener(this, profileRole.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		super.dispose();
	}


	protected void setEntity(ProfileRole profileRole) {
		if ( ! isNew() ) {
			// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			this.profileRole = profileRole.clone();
		}
		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return I18N.ProfileRoles;
	}


	/**
	 * Create contents of the editor part
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout(2, false));

			{
    			Label nameLabel = new Label(mainComposite, SWT.RIGHT);
    			nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    			nameLabel.setText(I18N.ProfileRole_Name);
    			SWTHelper.makeBold(nameLabel);

    			nameText = new Text(mainComposite, SWT.BORDER);
    			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			}

			{
    			Label descriptionLabel = new Label(mainComposite, SWT.RIGHT);
    			descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
    			descriptionLabel.setText(I18N.ProfileRole_Desc);

    			descriptionText = new Text(mainComposite, SWT.BORDER);
    			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			}


			// sync widgets and groups to the entity
			setEntity(profileRole);

			// after sync add this as ModifyListener to all widgets and groups
			addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			// Copy the data from the widgets to the entity
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception is thrown.
				 */
				profileRole = profileRoleModel.create(profileRole);

				// Observe the model
				profileRoleModel.addListener(this, profileRole.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(profileRole.getID());

				// Set new entity
				setEntity(profileRole);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception is thrown.
				 */
				profileRoleModel.update(profileRole);

				// setEntity will be called indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException are handled separately to show the original error message.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateProfileRoleErrorMessage;
			}
			else {
				msg = I18N.EditProfileRoleErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (profileRole != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						nameText.setText(StringHelper.avoidNull(profileRole.getName()));
						descriptionText.setText(StringHelper.avoidNull(profileRole.getDescription()));

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());

						// signal that editor has no unsaved data anymore
						setDirty(false);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}

			});
		}
	}


	private void syncEntityToWidgets() {
		if (profileRole != null) {
			profileRole.setName(nameText.getText());
			profileRole.setDescription(descriptionText.getText());
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		nameText.addModifyListener(listener);
		descriptionText.addModifyListener(listener);
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};


		// the values of the info dialog
		FormatHelper formatHelper = new FormatHelper();
		String[] values = {
			String.valueOf(profileRole.getID()),
			formatHelper.formatDateTime(profileRole.getNewTime()),
			profileRole.getNewDisplayUserStr(),
			formatHelper.formatDateTime(profileRole.getEditTime()),
			profileRole.getEditDisplayUserStr()
		};


		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			I18N.ProfileRoles + ": " + UtilI18N.Info,
			labels,
			values
		);

		//infoDialog.setSize(new Point(400, 150));

		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == profileRoleModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (profileRole != null) {
					profileRole = profileRoleModel.getProfileRole(profileRole.getID());
					if (profileRole != null) {
						setEntity(profileRole);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (profileRole != null && profileRole.getName() != null) {
			name = profileRole.getName();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.ProfileRoles;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ProfileRoleEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return profileRole.getID() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (profileRole != null && profileRole.getID() != null) {
			profileRoleModel.refresh(profileRole.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				profileRole = profileRoleModel.getProfileRole(profileRole.getID());
				setEntity(profileRole);
			}
		}
	}

}

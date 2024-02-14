package de.regasus.profile.relationtype.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.profile.ProfileRelationTypeModel;

public class ProfileRelationTypeEditor
extends AbstractEditor<ProfileRelationTypeEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "ProfileRelationTypeEditor";

	// the entity
	private ProfileRelationType profileRelationType;

	// the model
	private ProfileRelationTypeModel profileRelationTypeModel;

	// **************************************************************************
	// * Widgets
	// *

	private I18NMultiText i18nMultiText;
	private Button isDirectedButton;

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		profileRelationTypeModel = ProfileRelationTypeModel.getInstance();

		if (key != null) {
			// get entity
			profileRelationType = profileRelationTypeModel.getProfileRelationType(key);

			// register at model
			profileRelationTypeModel.addListener(this, key);
		}
		else {
			// create empty entity
			profileRelationType = new ProfileRelationType();
		}
	}


	@Override
	public void dispose() {
		if (profileRelationTypeModel != null && profileRelationType.getID() != null) {
			try {
				profileRelationTypeModel.removeListener(this, profileRelationType.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		super.dispose();
	}


	protected void setEntity(ProfileRelationType profileRelationType) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		this.profileRelationType = profileRelationType.clone();
		}

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ProfileLabel.ProfileRelationType.getString();
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

			String[] labels = {
				ProfileLabel.ProfileRelationType_Name.getString(),
				ProfileLabel.ProfileRelationType_Role1.getString(),
				ProfileLabel.ProfileRelationType_Role2.getString(),
				ProfileLabel.ProfileRelationType_Desc12.getString(),
				ProfileLabel.ProfileRelationType_Desc21.getString()
			};

			String[] toolTips = {
				ProfileLabel.ProfileRelationType_Name_Desc.getString(),
				ProfileLabel.ProfileRelationType_Role1_Desc.getString(),
				ProfileLabel.ProfileRelationType_Role2_Desc.getString(),
				ProfileLabel.ProfileRelationType_Desc12_Desc.getString(),
				ProfileLabel.ProfileRelationType_Desc21_Desc.getString()
			};


			i18nMultiText = new I18NMultiText(
				mainComposite,
				SWT.NONE,
				labels,
				new boolean[] {false, true, true, true, true},
				new boolean[] {true, true, true, true, true},  // required
				LanguageProvider.getInstance()
			);
			i18nMultiText.setToolTips(toolTips);

			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			gridData.heightHint = 250;
			i18nMultiText.setLayoutData(gridData);

			isDirectedButton = new Button(mainComposite, SWT.CHECK | SWT.RIGHT);
			isDirectedButton.setText(ProfileLabel.ProfileRelationType_Directed.getString());
			isDirectedButton.setToolTipText(ProfileLabel.ProfileRelationType_Directed_Desc.getString());


			// sync widgets and groups to the entity
			setEntity(profileRelationType);

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
				profileRelationType = profileRelationTypeModel.create(profileRelationType);

				// Observe the model
				profileRelationTypeModel.addListener(this, profileRelationType.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(profileRelationType.getID());

				// Set new entity
				setEntity(profileRelationType);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception is thrown.
				 */
				profileRelationTypeModel.update(profileRelationType);

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
				msg = I18N.CreateProfileRelationTypeErrorMessage;
			}
			else {
				msg = I18N.EditProfileRelationTypeErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (profileRelationType != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						i18nMultiText.setLanguageString(UtilI18N.Name, profileRelationType.getName());
						i18nMultiText.setLanguageString(ProfileLabel.ProfileRelationType_Role1.getString(), profileRelationType.getRole1());
						i18nMultiText.setLanguageString(ProfileLabel.ProfileRelationType_Role2.getString(), profileRelationType.getRole2());
						i18nMultiText.setLanguageString(ProfileLabel.ProfileRelationType_Desc12.getString(), profileRelationType.getDescription12());
						i18nMultiText.setLanguageString(ProfileLabel.ProfileRelationType_Desc21.getString(), profileRelationType.getDescription21());

						isDirectedButton.setSelection(profileRelationType.isDirected());

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
		if (profileRelationType != null) {
			profileRelationType.setName(i18nMultiText.getLanguageString(UtilI18N.Name));
			profileRelationType.setRole1(i18nMultiText.getLanguageString(ProfileLabel.ProfileRelationType_Role1.getString()));
			profileRelationType.setRole2(i18nMultiText.getLanguageString(ProfileLabel.ProfileRelationType_Role2.getString()));
			profileRelationType.setDescription12(i18nMultiText.getLanguageString(ProfileLabel.ProfileRelationType_Desc12.getString()));
			profileRelationType.setDescription21(i18nMultiText.getLanguageString(ProfileLabel.ProfileRelationType_Desc21.getString()));
			profileRelationType.setDirected(isDirectedButton.getSelection());
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		i18nMultiText.addModifyListener(listener);

		ModifyListenerAdapter modifyListenerAdapter = new ModifyListenerAdapter(listener);
		isDirectedButton.addSelectionListener(modifyListenerAdapter);
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
			String.valueOf(profileRelationType.getID()),
			formatHelper.formatDateTime(profileRelationType.getNewTime()),
			profileRelationType.getNewDisplayUserStr(),
			formatHelper.formatDateTime(profileRelationType.getEditTime()),
			profileRelationType.getEditDisplayUserStr()
		};


		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ProfileLabel.ProfileRelationType.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);

		//infoDialog.setSize(new Point(400, 150));

		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == profileRelationTypeModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (profileRelationType != null) {
					profileRelationType = profileRelationTypeModel.getProfileRelationType(profileRelationType.getID());
					if (profileRelationType != null) {
						setEntity(profileRelationType);
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
		if (profileRelationType != null && profileRelationType.getName() != null) {
			name = profileRelationType.getName().getString();
		}
		if (StringHelper.isEmpty(name)) {
			name = ProfileLabel.ProfileRelationType.getString();
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ProfileRelationTypeEditor_DefaultToolTip;
	}




	@Override
	public boolean isNew() {
		return profileRelationType.getID() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (profileRelationType != null && profileRelationType.getID() != null) {
			profileRelationTypeModel.refresh(profileRelationType.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				profileRelationType = profileRelationTypeModel.getProfileRelationType(profileRelationType.getID());
				if (profileRelationType != null) {
					setEntity(profileRelationType);
				}
			}
		}
	}

}

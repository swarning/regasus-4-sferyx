package de.regasus.profile.customfield.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.profile.ProfileCustomFieldGroupModel;

public class ProfileCustomFieldGroupEditor
extends AbstractEditor<ProfileCustomFieldGroupEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "ProfileCustomFieldGroupEditor";

	// the entity
	private ProfileCustomFieldGroup profileCustomFieldGroup;

	// the model
	private ProfileCustomFieldGroupModel profileCustomFieldGroupModel;

	private final String[] LABELS = {
			UtilI18N.Name,
			UtilI18N.Description
	};

	// **************************************************************************
	// * Widgets
	// *

	private I18NMultiText i18nMultiText;

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		profileCustomFieldGroupModel = ProfileCustomFieldGroupModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
    		// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			profileCustomFieldGroup = profileCustomFieldGroupModel.getProfileCustomFieldGroup(key);

			// register at model
			profileCustomFieldGroupModel.addListener(this, key);
		}
		else {
			// create empty entity
			profileCustomFieldGroup = new ProfileCustomFieldGroup();
			profileCustomFieldGroup.setLocation(editorInput.getLocation());
		}

	}

	@Override
	public void dispose() {
		if (profileCustomFieldGroupModel != null && profileCustomFieldGroup.getID() != null) {
			try {
				profileCustomFieldGroupModel.removeListener(this, profileCustomFieldGroup.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ProfileCustomFieldGroup profileCustomFieldGroup) {

		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		profileCustomFieldGroup = profileCustomFieldGroup.clone();
		}

		this.profileCustomFieldGroup = profileCustomFieldGroup;


		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return ProfileLabel.ProfileCustomFieldGroup.getString();
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

			// Name and Description
			i18nMultiText = new I18NMultiText(
				mainComposite,					// parent
				SWT.NONE,						// style
				LABELS,							// LABELS
				new boolean[] {false, true},	// multiLine
				new boolean[] {true, false},	// required
				LanguageProvider.getInstance()	// languageProvider
			);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			// do NOT set gridData.heightHint cause this disables dynamic height
			i18nMultiText.setLayoutData(gridData);

			// sync widgets and groups to the entity
			setEntity(profileCustomFieldGroup);

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

			/* Entity mit den Widgets synchronisieren.
			 * Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				profileCustomFieldGroup = profileCustomFieldGroupModel.create(profileCustomFieldGroup);

				// observe the ProgrammePointTypeModel
				profileCustomFieldGroupModel.addListener(this, profileCustomFieldGroup.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(profileCustomFieldGroup.getID());

				// set new entity
				setEntity(profileCustomFieldGroup);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				profileCustomFieldGroupModel.update(profileCustomFieldGroup);

				// setEntity will be calles indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (profileCustomFieldGroup != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						Map<String, LanguageString> labelToLanguageMap = new HashMap<>();
						labelToLanguageMap.put(LABELS[0], profileCustomFieldGroup.getName());
						labelToLanguageMap.put(LABELS[1], profileCustomFieldGroup.getDescription());

						List<String> defaultLanguagePKList = LanguageProvider.getInstance().getDefaultLanguagePKList();
						i18nMultiText.setLanguageString(labelToLanguageMap, defaultLanguagePKList);

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
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
		if (profileCustomFieldGroup != null) {
			profileCustomFieldGroup.setID(profileCustomFieldGroup.getID());
			profileCustomFieldGroup.setName(i18nMultiText.getLanguageString(LABELS[0]));
			profileCustomFieldGroup.setDescription(i18nMultiText.getLanguageString(LABELS[1]));
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		i18nMultiText.addModifyListener(listener);
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.Name,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(profileCustomFieldGroup.getID()),
			getName(),
			formatHelper.formatDateTime(profileCustomFieldGroup.getNewTime()),
			profileCustomFieldGroup.getNewDisplayUserStr(),
			formatHelper.formatDateTime(profileCustomFieldGroup.getEditTime()),
			profileCustomFieldGroup.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ProfileLabel.ProfileCustomFieldGroup.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);


		infoDialog.setSize(new Point(300, 400));

		infoDialog.open();
	}

	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == profileCustomFieldGroupModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (profileCustomFieldGroup != null) {
					profileCustomFieldGroup = profileCustomFieldGroupModel.getProfileCustomFieldGroup(profileCustomFieldGroup.getID());
					if (profileCustomFieldGroup != null) {
						setEntity(profileCustomFieldGroup);
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

		if (profileCustomFieldGroup != null) {
			LanguageString ls = profileCustomFieldGroup.getName();
			if (ls != null) {
				name = ls.getString();
			}
		}

		if (StringHelper.isEmpty(name)) {
			name = I18N.ProfileCustomFieldGroupEditor_NewName;
		}

		return name;
	}

	@Override
	protected String getToolTipText() {
		return I18N.ProfileCustomFieldGroupEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return profileCustomFieldGroup.getID() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (profileCustomFieldGroup != null && profileCustomFieldGroup.getID() != null) {
			profileCustomFieldGroupModel.refresh(profileCustomFieldGroup.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				profileCustomFieldGroup = profileCustomFieldGroupModel.getProfileCustomFieldGroup(profileCustomFieldGroup.getID());
				if (profileCustomFieldGroup != null) {
					setEntity(profileCustomFieldGroup);
				}
			}
		}
	}

}

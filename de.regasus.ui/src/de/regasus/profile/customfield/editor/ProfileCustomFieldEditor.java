package de.regasus.profile.customfield.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.CustomFieldConfigParameterSet;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.messeinfo.profile.ProfileCustomField;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldGroup;
import com.lambdalogic.messeinfo.profile.ProfileCustomFieldListValue;
import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.customfield.CustomFieldComposite;
import de.regasus.common.customfield.ICustomFieldListValueFactory;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.profile.ProfileCustomFieldGroupModel;
import de.regasus.profile.ProfileCustomFieldModel;

public class ProfileCustomFieldEditor
extends AbstractEditor<ProfileCustomFieldEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "ProfileCustomFieldEditor";

	// the entity
	private ProfileCustomField customField;

	// models
	private ProfileCustomFieldModel profileCustomFieldModel;
	private ConfigParameterSetModel configParameterSetModel;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;
	private CustomFieldConfigParameterSet customFieldConfigParameterSet;

	// Widgets
	private CustomFieldComposite<ProfileCustomFieldListValue> customFieldComposite;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get models
		profileCustomFieldModel = ProfileCustomFieldModel.getInstance();
		configParameterSetModel = ConfigParameterSetModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			customField = profileCustomFieldModel.getProfileCustomField(key);

			// register at model
			profileCustomFieldModel.addListener(this, key);
		}
		else {
			// create empty entity
			customField = new ProfileCustomField();
			customField.setGroupPK(editorInput.getCustomFieldGroupPK());
			customField.setCustomFieldType(CustomFieldType.SLT);
		}


		// init ConfigurationParameterSet
		configParameterSet = configParameterSetModel.getConfigParameterSet();
		customFieldConfigParameterSet = configParameterSet.getProfile().getCustomField();
	}

	@Override
	public void dispose() {
		if (profileCustomFieldModel != null && customField.getID() != null) {
			try {
				profileCustomFieldModel.removeListener(this, customField.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ProfileCustomField profileCustomField) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			profileCustomField = profileCustomField.clone();
		}

		this.customField = profileCustomField;


		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ProfileLabel.ProfileCustomField.getString();
	}


	/**
	 * Create contents of the editor part
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite contentComposite = SWTHelper.createScrolledContentComposite(parent);

			customFieldComposite = new CustomFieldComposite<>(
				contentComposite,
				SWT.NONE,
				customFieldConfigParameterSet,
				null	// defaultLanguagePKs
			);
			SWTHelper.refreshSuperiorScrollbar(contentComposite);

			customFieldComposite.setCustomFieldListValueFactory(new ICustomFieldListValueFactory<ProfileCustomFieldListValue>() {
				@Override
				public ProfileCustomFieldListValue createCustomFieldListValue() {
					return new ProfileCustomFieldListValue();
				}
			});

			setEntity(customField);

			customFieldComposite.addModifyListener(this);
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
				customField = profileCustomFieldModel.create(customField);

				// observe the ProgrammePointTypeModel
				profileCustomFieldModel.addListener(this, customField.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(customField.getID());

				// set new entity
				setEntity(customField);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				profileCustomFieldModel.update(customField);

				// setEntity will be called indirectly in dataChange()
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
		if (customField != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						customFieldComposite.setCustomField(customField);

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
		customFieldComposite.syncEntityToWidgets();
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
			UtilI18N.EditUser,
			ProfileLabel.ProfileCustomFieldGroup.getString()
		};


		String groupName = "-";
		Long groupPK = customField.getGroupPK();
		if (groupPK != null) {
			try {
				ProfileCustomFieldGroup group = ProfileCustomFieldGroupModel.getInstance().getProfileCustomFieldGroup(groupPK);
				groupName = group.getName().getString();
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}


		// the values of the info dialog
		final String[] values = {
			String.valueOf(customField.getID()),
			getName(),
			formatHelper.formatDateTime(customField.getNewTime()),
			customField.getNewDisplayUserStr(),
			formatHelper.formatDateTime(customField.getEditTime()),
			customField.getEditDisplayUserStr(),
			groupName
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ProfileLabel.ProfileCustomField.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);


		infoDialog.setSize(new Point(300, 400));

		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == profileCustomFieldModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (customField != null) {
					customField = profileCustomFieldModel.getProfileCustomField(customField.getID());
					if (customField != null) {
						setEntity(customField);
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
		if (customField != null && customField.getID() != null) {
			name = customField.getName();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.ProfileCustomFieldEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ProfileCustomFieldEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return customField.getID() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (customField != null && customField.getID() != null) {
			profileCustomFieldModel.refresh(customField.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				customField = profileCustomFieldModel.getProfileCustomField(customField.getID());
				if (customField != null) {
					setEntity(customField);
				}
			}
		}
	}

}

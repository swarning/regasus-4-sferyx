package de.regasus.event.customfield.editor;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.CustomFieldConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.CustomFieldType;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldListValue;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.common.customfield.CustomFieldComposite;
import de.regasus.common.customfield.ICustomFieldListValueFactory;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantCustomFieldModel;

public class ParticipantCustomFieldEditor
extends AbstractEditor<ParticipantCustomFieldEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "ParticipantCustomFieldEditor";

	// the entity
	private ParticipantCustomField customField;

	// models
	private ParticipantCustomFieldModel participantCustomFieldModel;
	private ConfigParameterSetModel configParameterSetModel;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;
	private CustomFieldConfigParameterSet customFieldConfigParameterSet;

	// default languages
	private Collection<String> defaultLanguagePKs;

	// Widgets
	private CustomFieldComposite<ParticipantCustomFieldListValue> customFieldComposite;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get models
		participantCustomFieldModel = ParticipantCustomFieldModel.getInstance();
		configParameterSetModel = ConfigParameterSetModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			customField = participantCustomFieldModel.getParticipantCustomField(key);

			// register at model
			participantCustomFieldModel.addListener(this, key);
		}
		else {
			// create empty entity
			customField = new ParticipantCustomField();
			customField.setEventPK(editorInput.getEventId());
			customField.setGroupPK(editorInput.getCustomFieldGroupPK());
			customField.setCustomFieldType(CustomFieldType.SLT);
		}


		// init ConfigurationParameterSet
		Long eventPK = customField.getEventPK();
		configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
		customFieldConfigParameterSet = configParameterSet.getEvent().getParticipant().getCustomField();

		// init default languages
		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
		defaultLanguagePKs = eventVO.getLanguages();
	}


	@Override
	public void dispose() {
		if (participantCustomFieldModel != null && customField.getID() != null) {
			try {
				participantCustomFieldModel.removeListener(this, customField.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ParticipantCustomField participantCustomField) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		participantCustomField = participantCustomField.clone();
		}

		this.customField = participantCustomField;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ContactLabel.CustomField.getString();
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
				defaultLanguagePKs
			);
			SWTHelper.refreshSuperiorScrollbar(contentComposite);

			customFieldComposite.setCustomFieldListValueFactory(new ICustomFieldListValueFactory<ParticipantCustomFieldListValue>() {
				@Override
				public ParticipantCustomFieldListValue createCustomFieldListValue() {
					return new ParticipantCustomFieldListValue();
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
				customField = participantCustomFieldModel.create(customField);

				// observe the ProgrammePointTypeModel
				participantCustomFieldModel.addListener(this, customField.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(customField.getID());

				// set new entity
				setEntity(customField);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				participantCustomFieldModel.update(customField);

				// setEntity will be calles indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (Throwable t) {
			String title = null;
			if (create) {
				title = I18N.CreateParticipantCustomFieldErrorMessage;
			}
			else {
				title = I18N.EditParticipantCustomFieldErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, null, title);
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
			UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(customField.getID()),
			getName(),
			formatHelper.formatDateTime(customField.getNewTime()),
			customField.getNewDisplayUserStr(),
			formatHelper.formatDateTime(customField.getEditTime()),
			customField.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ContactLabel.CustomField.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);


		infoDialog.setSize(new Point(300, 400));

		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == participantCustomFieldModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (customField != null) {
					customField = participantCustomFieldModel.getParticipantCustomField(customField.getID());
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
		if (customField != null) {
			name = customField.getName();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.ParticipantCustomFieldEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ParticipantCustomFieldEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return customField.getID() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (customField != null && customField.getID() != null) {
			participantCustomFieldModel.refresh(customField.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				customField = participantCustomFieldModel.getParticipantCustomField(customField.getID());
				if (customField != null) {
					setEntity(customField);
				}
			}
		}
	}

}

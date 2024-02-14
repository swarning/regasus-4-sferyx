package de.regasus.eventgroup.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
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
import de.regasus.event.EventGroup;
import de.regasus.event.EventGroupModel;

public class EventGroupEditor
extends AbstractEditor<EventGroupEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "EventGroupEditor";

	// the entity
	private EventGroup eventGroup;

	// the model
	private EventGroupModel eventGroupModel;

	// widgets
	private I18NMultiText i18nMultiText;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		eventGroupModel = EventGroupModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			eventGroup = eventGroupModel.getEventGroup(key);

			// register at model
			eventGroupModel.addListener(this, key);
		}
		else {
			// create empty entity
			eventGroup = new EventGroup();
		}
	}


	@Override
	public void dispose() {
		if (eventGroupModel != null && eventGroup.getId() != null) {
			try {
				eventGroupModel.removeListener(this, eventGroup.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(EventGroup eventGroup) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		eventGroup = eventGroup.clone();
		}

		this.eventGroup = eventGroup;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.EventGroup.getString();
	}


//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ParticipantTypeEditor_InfoButtonToolTip;
//	}


	/**
	 * Create contents of the editor part
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			parent.setLayout(new GridLayout(2, false));

			String[] labels = {
				EventGroup.NAME.getString(),
				EventGroup.DESCRIPTION.getString()
			};
			i18nMultiText = new I18NMultiText(
				parent,
				SWT.NONE,
				labels,
				new boolean[] {false, true},
				new boolean[] {true, false},  // required
				LanguageProvider.getInstance()
			);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			gridData.heightHint = 250;
			i18nMultiText.setLayoutData(gridData);

			setEntity(eventGroup);

			i18nMultiText.addModifyListener(this);

			// sync widgets and groups to the entity
			setEntity(eventGroup);

			// after sync add this as ModifyListener to all widgets and groups
			i18nMultiText.addModifyListener(this);
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
				eventGroup = eventGroupModel.create(eventGroup);

				// observe the ParticipantTypeModel
				eventGroupModel.addListener(this, eventGroup.getId());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(eventGroup.getId());

				// set new entity
				setEntity(eventGroup);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				eventGroupModel.update(eventGroup);

				// setEntity will be calles indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException are handled separately in order to be able to output the original error message.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateParticipantTypeErrorMessage;
			}
			else {
				msg = I18N.EditParticipantTypeErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (eventGroup != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						i18nMultiText.setLanguageString(EventGroup.NAME.getString(), eventGroup.getName());
						i18nMultiText.setLanguageString(EventGroup.DESCRIPTION.getString(), eventGroup.getDescription());

						// set editor title
						setPartName( getName() );
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName( getName() );
						editorInput.setToolTipText( getToolTipText() );

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
		if (eventGroup != null) {
			eventGroup.setName(i18nMultiText.getLanguageString( EventGroup.NAME.getString() ));
			eventGroup.setDescription(i18nMultiText.getLanguageString( EventGroup.DESCRIPTION.getString() ));
		}
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			UtilI18N.Name
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf( eventGroup.getId() ),
			eventGroup.getNewTime().getString(),
			eventGroup.getNewDisplayUserStr(),
			eventGroup.getEditTime().getString(),
			eventGroup.getEditDisplayUserStr(),
			getName()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			Participant.PARTICIPANT_TYPE.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == eventGroupModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (eventGroup != null) {
					eventGroup = eventGroupModel.getEventGroup( eventGroup.getId() );
					if (eventGroup != null) {
						setEntity(eventGroup);
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
		if (eventGroup != null && eventGroup.getName() != null) {
			name = eventGroup.getName().getString();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.EventGroupEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.EventGroupEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return eventGroup.getId() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (eventGroup != null && eventGroup.getId() != null) {
			eventGroupModel.refresh(eventGroup.getId());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				eventGroup = eventGroupModel.getEventGroup( eventGroup.getId() );
				if (eventGroup != null) {
					setEntity(eventGroup);
				}
			}
		}
	}

}

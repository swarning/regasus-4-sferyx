package de.regasus.event.customfield.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroupLocation;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantCustomFieldGroupModel;

public class ParticipantCustomFieldGroupEditor
extends AbstractEditor<ParticipantCustomFieldGroupEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "ParticipantCustomFieldGroupEditor";

	// the entity
	private ParticipantCustomFieldGroup participantCustomFieldGroup;

	// the model
	private ParticipantCustomFieldGroupModel participantCustomFieldGroupModel;

	/**
	 * Shows whether the configuration allows formEditor.
	 */
	boolean withFormEditor = false;

	private final String[] LABELS = {
			UtilI18N.Name,
			UtilI18N.Description
	};

	// **************************************************************************
	// * Widgets
	// *

	private I18NMultiText i18nMultiText;

	private FormPositionCombo formPositionCombo;


	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		participantCustomFieldGroupModel = ParticipantCustomFieldGroupModel.getInstance();

		Long eventPK = null;

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			participantCustomFieldGroup = participantCustomFieldGroupModel.getParticipantCustomFieldGroup(key);

			eventPK = participantCustomFieldGroup.getEventPK();

			// register at model
			participantCustomFieldGroupModel.addListener(this, key);
		}
		else {
			eventPK = editorInput.getEventId();

			ParticipantCustomFieldGroupLocation location = editorInput.getLocation();

			// create empty entity
			participantCustomFieldGroup = new ParticipantCustomFieldGroup();
			participantCustomFieldGroup.setEventPK(eventPK);
			participantCustomFieldGroup.setLocation(location);
		}

		// init withFormEditor
		ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventPK);
		withFormEditor = configParameterSet.getEvent().getFormEditor().isVisible();
	}


	@Override
	public void dispose() {
		if (participantCustomFieldGroupModel != null && participantCustomFieldGroup.getID() != null) {
			try {
				participantCustomFieldGroupModel.removeListener(this, participantCustomFieldGroup.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ParticipantCustomFieldGroup participantCustomFieldGroup) {

		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		participantCustomFieldGroup = participantCustomFieldGroup.clone();
		}

		this.participantCustomFieldGroup = participantCustomFieldGroup;


		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ContactLabel.CustomFieldGroup.getString();
	}


//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ProgrammePointTypeEditor_InfoButtonToolTip;
//	}


	/**
	 * Create contents of the editor part
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = SWTHelper.createScrolledContentComposite(parent);
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

			if (withFormEditor) {
				// Position in Form
				Label positionInFormLabel = new Label(mainComposite, SWT.NONE);
				positionInFormLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
				positionInFormLabel.setText(I18N.FormPosition);

				formPositionCombo = new FormPositionCombo(mainComposite, SWT.NONE);
				formPositionCombo.setLayoutData(new GridData(SWT.DEFAULT, SWT.CENTER, true, false));
			}

			// sync widgets and groups to the entity
			setEntity(participantCustomFieldGroup);

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
				participantCustomFieldGroup = participantCustomFieldGroupModel.create(participantCustomFieldGroup);

				// observe the ProgrammePointTypeModel
				participantCustomFieldGroupModel.addListener(this, participantCustomFieldGroup.getID());

				// Set the PK of the new entity to the EditorInput
				editorInput.setKey(participantCustomFieldGroup.getID());

				// set new entity
				setEntity(participantCustomFieldGroup);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				participantCustomFieldGroupModel.update(participantCustomFieldGroup);

				// setEntity will be calles indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateParticipantCustomFieldGroupErrorMessage;
			}
			else {
				msg = I18N.EditParticipantCustomFieldGroupErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (participantCustomFieldGroup != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						EventVO eventVO = EventModel.getInstance().getEventVO(participantCustomFieldGroup.getEventPK());

						Map<String, LanguageString> labelToLanguageMap = new HashMap<>();
						labelToLanguageMap.put(LABELS[0], participantCustomFieldGroup.getName());
						labelToLanguageMap.put(LABELS[1], participantCustomFieldGroup.getDescription());

						i18nMultiText.setLanguageString(labelToLanguageMap, eventVO.getLanguages());

						// Widget is not present when module.formedit is false
						if (formPositionCombo != null) {
							if(participantCustomFieldGroup.getFormPosition() != null) {
								formPositionCombo.setEntity(participantCustomFieldGroup.getFormPosition());
							}
							else {
								formPositionCombo.setEntity(null);
							}
						}

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
		if (participantCustomFieldGroup != null) {
			participantCustomFieldGroup.setID(participantCustomFieldGroup.getID());
			participantCustomFieldGroup.setName(i18nMultiText.getLanguageString(LABELS[0]));
			participantCustomFieldGroup.setDescription(i18nMultiText.getLanguageString(LABELS[1]));

			// Widget is not present if module.formedit is false
			if (formPositionCombo != null) {
				participantCustomFieldGroup.setFormPosition(formPositionCombo.getEntity());
			}
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		i18nMultiText.addModifyListener(listener);
		// Widget is not present if module.formedit is false
		if (formPositionCombo != null) {
			formPositionCombo.addModifyListener(listener);
		}
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
			String.valueOf(participantCustomFieldGroup.getID()),
			getName(),
			formatHelper.formatDateTime(participantCustomFieldGroup.getNewTime()),
			participantCustomFieldGroup.getNewDisplayUserStr(),
			formatHelper.formatDateTime(participantCustomFieldGroup.getEditTime()),
			participantCustomFieldGroup.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ContactLabel.CustomFieldGroup.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);


		infoDialog.setSize(new Point(300, 400));

		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == participantCustomFieldGroupModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (participantCustomFieldGroup != null) {
					participantCustomFieldGroup = participantCustomFieldGroupModel.getParticipantCustomFieldGroup(participantCustomFieldGroup.getID());
					if (participantCustomFieldGroup != null) {
						setEntity(participantCustomFieldGroup);
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

		if (participantCustomFieldGroup != null) {
			LanguageString ls = participantCustomFieldGroup.getName();
			if (ls != null) {
				name = ls.getString();
			}
		}

		if (StringHelper.isEmpty(name)) {
			name = I18N.ParticipantCustomFieldGroupEditor_NewName;
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ParticipantCustomFieldGroupEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return participantCustomFieldGroup.getID() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (participantCustomFieldGroup != null && participantCustomFieldGroup.getID() != null) {
			participantCustomFieldGroupModel.refresh(participantCustomFieldGroup.getID());

			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				participantCustomFieldGroup = participantCustomFieldGroupModel.getParticipantCustomFieldGroup(participantCustomFieldGroup.getID());
				if (participantCustomFieldGroup != null) {
					setEntity(participantCustomFieldGroup);
				}
			}
		}
	}

}

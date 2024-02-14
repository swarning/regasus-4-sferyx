/**
 * ParticipantStateEditor.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.ModifyListenerAdapter;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.participant.ParticipantStateModel;

/**
 * @author huuloi
 *
 */
public class ParticipantStateEditor
extends AbstractEditor<ParticipantStateEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "ParticipantStateEditor";

	// the entity
	private ParticipantState participantState;

	// the model
	private ParticipantStateModel participantStateModel;

	// **************************************************************************
	// * Widgets
	// *

	private I18NMultiText i18nMultiText;

//	private Button requiredBySystem;

	private Button badgePrint;

	// *
	// * Widgets
	// **************************************************************************


	@Override
	protected String getTypeName() {
		return Participant.PARTICIPANT_STATE.getString();
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;
			parent.setLayout(new GridLayout(2, false));

			String[] labels = {
				UtilI18N.Name,
				UtilI18N.Description
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

//			SWTHelper.createLabel(parent, ParticipantLabel.RequiredBySystem);
//			requiredBySystem = new Button(parent, SWT.CHECK);

			SWTHelper.createLabel(parent, ParticipantLabel.BadgePrint);
			badgePrint = new Button(parent, SWT.CHECK);

			setEntity(participantState);

			i18nMultiText.addModifyListener(this);
//			requiredBySystem.addSelectionListener(new ModifyListenerAdapter(this));
			badgePrint.addSelectionListener(new ModifyListenerAdapter(this));
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	protected void setEntity(ParticipantState participantState) {

		if (! isNew() ) {
			// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			participantState = participantState.clone();
		}

		this.participantState  =  participantState;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (participantState != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						i18nMultiText.setLanguageString(UtilI18N.Name, participantState.getName());
						i18nMultiText.setLanguageString(UtilI18N.Description, participantState.getDescription());

//						requiredBySystem.setSelection(participantState.isRequiredBySystem());

						badgePrint.setSelection(participantState.isBadgePrint());

						setPartName( getName() );
						firePropertyChange(PROP_TITLE);

						editorInput.setName( getName() );
						editorInput.setToolTipText( getToolTipText() );

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
		if (participantState != null) {
			participantState.setName(i18nMultiText.getLanguageString(UtilI18N.Name));
			participantState.setDescription(i18nMultiText.getLanguageString(UtilI18N.Description));

//			participantState.setRequiredBySystem(requiredBySystem.getSelection());
			participantState.setBadgePrint(badgePrint.getSelection());
		}
	}

	@Override
	protected String getName() {
		if (isNew()) {
			return I18N.ParticipantStateEditor_NewName;
		}
		else {
			String name = null;
			if (participantState != null && participantState.getName() != null) {
				name = participantState.getName().getString();
			}
			return StringHelper.avoidNull(name);
		}
	}

	@Override
	protected String getToolTipText() {
		return I18N.ParticipantStateEditor_DefaultToolTip;
	}

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		participantStateModel = ParticipantStateModel.getInstance();

		if (key != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			participantState = participantStateModel.getParticipantState(key);

			// register at model
			participantStateModel.addListener(this, key);
		}
		else {
			// create empty entity
			participantState = new ParticipantState();
			participantState.setRequiredBySystem(false);
		}
	}

	@Override
	public boolean isNew() {
		return participantState.getID() == null;
	}

	@Override
	public void refresh() throws Exception {
		if (participantState != null && participantState.getID() != null) {
			participantStateModel.refresh(participantState.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				participantState = participantStateModel.getParticipantState(participantState.getID());
				if (participantState != null) {
					setEntity(participantState);
				}
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				participantState = participantStateModel.create(participantState);

				participantStateModel.addListener(this, participantState.getID());

				editorInput.setKey(participantState.getID());

				setEntity(participantState);
			}
			else {
				participantStateModel.update(participantState);
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateParticipantStateErrorMessage;
			}
			else {
				msg = I18N.EditParticipantStateErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == participantStateModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (participantState != null) {
					participantState = participantStateModel.getParticipantState(participantState.getID());
					if (participantState != null) {
						setEntity(participantState);
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
	public void dispose() {
		if (participantStateModel != null && participantState.getID() != null) {
			try {
				participantStateModel.removeListener(this, participantState.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		super.dispose();
	}

	@Override
	protected void openInfoDialog() {
		FormatHelper formatHelper = new FormatHelper();

		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			UtilI18N.Name,
			ParticipantLabel.RequiredBySystem.getString(),
			ParticipantLabel.BadgePrint.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		final String[] values = {
			String.valueOf(participantState.getID()),
			getName(),
			participantState.isRequiredBySystem() ? I18N.YES : I18N.NO,
			participantState.isBadgePrint() ? I18N.YES : I18N.NO,
			formatHelper.formatDateTime(participantState.getNewTime()),
			participantState.getNewDisplayUserStr(),
			formatHelper.formatDateTime(participantState.getEditTime()),
			participantState.getEditDisplayUserStr()
		};

		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			Participant.PARTICIPANT_STATE.getString() + ": " + UtilI18N.Info,
			labels,
			values);
		infoDialog.open();
	}
}

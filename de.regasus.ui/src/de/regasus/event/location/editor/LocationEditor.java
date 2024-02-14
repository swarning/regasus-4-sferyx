/**
 * LocationEditor.java
 * created on 23.09.2013 10:39:50
 */
package de.regasus.event.location.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.MultiLineText;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.event.LocationModel;
import de.regasus.ui.Activator;

public class LocationEditor
extends AbstractEditor<LocationEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "LocationEditor";

	// the entity
	private LocationVO locationVO;

	// the model
	private LocationModel locationModel;
	private EventModel eventModel;
	private ServerModel serverModel;


	// **************************************************************************
	// * Widgets
	// *

	private Text nameText;
	private MultiLineText descriptionText;

	// *
	// * Widgets
	// **************************************************************************


	@Override
	public Long getEventId() {
		Long eventPK = null;
		if (locationVO != null) {
			eventPK = locationVO.getEventPK();
		}
		return eventPK;
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == locationModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (locationVO != null) {
					locationVO = locationModel.getLocationVO(locationVO.getID());
					if (locationVO != null) {
						setEntity(locationVO);
					}
					else if (serverModel.isLoggedIn()) {
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
	protected void init() throws Exception {
		// get models
		locationModel = LocationModel.getInstance();
		eventModel = EventModel.getInstance();
		serverModel = ServerModel.getInstance();

		// handle EditorInput
		Long locationPK = editorInput.getKey();
		Long eventPK = editorInput.getEventId();

		if (locationPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			locationVO = locationModel.getLocationVO(locationPK);

			// register at model
			locationModel.addListener(this, locationPK);
		}
		else {
			// create empty entity
			locationVO = new LocationVO();
			locationVO.setEventPK(eventPK);
		}
	}


	@Override
	public void dispose() {
		if (locationModel != null && locationVO.getID() != null) {
			try {
				locationModel.removeListener(this, locationVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	@Override
	protected String getName() {
		String name = null;
		if (isNew()) {
			name = I18N.LocationEditor_NewName;
		}
		else if (locationVO.getName() != null) {
			name = locationVO.getName();
		}

		name = StringHelper.avoidNull(name);
		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long eventPK = locationVO.getEventPK();
			EventVO eventVO = eventModel.getEventVO(eventPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.LanguageEditor_DefaultToolTip);

			// the name of the location is null if the location is new
			if (locationVO.getName() != null) {
				toolTipText.append('\n');
				toolTipText.append(KernelLabel.Name.getString());
				toolTipText.append(": ");
				toolTipText.append(locationVO.getName());
			}

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.Event.getString());
			toolTipText.append(": ");
			toolTipText.append(eventVO.getMnemonic());

			toolTip = toolTipText.toString();
		}
		catch (Exception e) {
			// This shouldn't happen
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			toolTip = "";
		}
		return toolTip;
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.Location.getString();
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			parent.setLayout(new GridLayout(2, false));

			Label nameLabel = new Label(parent, SWT.NONE);
			nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			nameLabel.setText(UtilI18N.Name);

			nameText = new Text(parent, SWT.BORDER);
			nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			Label descriptionLabel = new Label(parent, SWT.NONE);
			descriptionLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			descriptionLabel.setText(UtilI18N.Description);

			descriptionText = new MultiLineText(parent, SWT.BORDER, false);
			GridData descriptionGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			descriptionText.setLayoutData(descriptionGridData);
			descriptionText.setTextLimit(LocationVO.MAX_LENGTH_DESCRIPTION);

			// set data
			setEntity(locationVO);

			nameText.addModifyListener(this);
			descriptionText.addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void setEntity(LocationVO locationVO) {
		if (!isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			locationVO = locationVO.clone();
		}

		this.locationVO = locationVO;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (locationVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						if (locationVO.getName() != null) {
							nameText.setText(locationVO.getName());
						}
						if (locationVO.getDescription() != null) {
							descriptionText.setText(locationVO.getDescription());
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


	@Override
	public boolean isNew() {
		boolean result = locationVO.getPK() == null;
		return result;
	}


	@Override
	public void refresh() throws Exception {
		if (locationVO != null && locationVO.getID() != null) {
			locationModel.refresh(locationVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				locationVO = locationModel.getLocationVO(locationVO.getID());
				if (locationVO != null) {
					setEntity(locationVO);
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
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				locationVO = locationModel.create(locationVO);

				// observe the model
				locationModel.addListener(this, locationVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(locationVO.getID());

				// set new entity
				setEntity(locationVO);
			}
			else {
				/* Save the entity.
				 * On success setEntity will be called indirectly in dataChange(),
				 * else an Exception will be thrown.
				 * The result of update() must not be assigned to the entity,
				 * because this will happen in setEntity() and there it may be cloned!
				 * Assigning the entity here would overwrite the cloned value with
				 * the one from the model. Therefore we would have inconsistent data!
				 */
				locationModel.update(locationVO);
			}
			monitor.worked(1);
		}
		catch (Exception e) {
			monitor.setCanceled(true);
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		finally {
			monitor.done();
		}
	}


	private void syncEntityToWidgets() {
		if (locationVO != null) {
			locationVO.setName(nameText.getText());
			locationVO.setDescription(descriptionText.getText());
		}
	}


	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			ParticipantLabel.Event.getString(),
			ParticipantLabel.EventID.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		// get name of event
		Long eventPK = locationVO.getEventPK();
		String eventMnemonic = null;
		try {
			EventVO eventVO =  EventModel.getInstance().getEventVO(eventPK);
			if (eventVO != null) {
				eventMnemonic = eventVO.getMnemonic();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(locationVO.getID()),
			eventMnemonic,
			StringHelper.avoidNull(eventPK),
			formatHelper.formatDateTime(locationVO.getNewTime()),
			locationVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(locationVO.getEditTime()),
			locationVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			I18N.Location + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}

/**
 * GateEditor.java
 * created on 24.09.2013 12:19:15
 */
package de.regasus.event.gate.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.GateVO;
import com.lambdalogic.messeinfo.participant.data.LocationVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.event.GateModel;
import de.regasus.event.LocationModel;
import de.regasus.ui.Activator;

public class GateEditor
extends AbstractEditor<GateEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "GateEditor";

	// the entity
	private GateVO gateVO;
	private LocationVO locationVO;

	// the model
	private GateModel gateModel;
	private LocationModel locationModel;
	private EventModel eventModel;
	private ServerModel serverModel;


	private Text nameText;

	@Override
	public Long getEventId() {
		Long result = null;
		if (locationVO != null) {
			Long eventPK = locationVO.getEventPK();
			if (eventPK != null) {
				result = eventPK;
			}
		}
		return result;
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == gateModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (gateVO != null) {
					gateVO = gateModel.getGateVO(gateVO.getID());
					if (gateVO != null) {
						setEntity(gateVO);
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
		gateModel = GateModel.getInstance();
		locationModel = LocationModel.getInstance();
		eventModel = EventModel.getInstance();
		serverModel = ServerModel.getInstance();

		// handle EditorInput
		Long gatePK = editorInput.getKey();
		Long locationPK = editorInput.getLocationPK();

		if (gatePK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			gateVO = gateModel.getGateVO(gatePK);
			locationPK = gateVO.getLocationPK();
			if (locationPK != null) {
				// get the LocationVO
				locationVO = locationModel.getLocationVO(locationPK);
			}

			// register at model
			gateModel.addListener(this, gatePK);
		}
		else {
			// get the LocationVO
			locationVO = locationModel.getLocationVO(locationPK);

			// create empty entity
			gateVO = new GateVO();
			gateVO.setLocationPK(editorInput.getLocationPK());
		}


	}


	@Override
	public void dispose() {
		if (gateModel != null && gateVO.getID() != null) {
			try {
				gateModel.removeListener(this, gateVO.getID());
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
			name = I18N.GateEditor_NewName;
		}
		else if (gateVO.getName() != null) {
			name = gateVO.getName();
		}

		name = StringHelper.avoidNull(name);
		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long locationPK = gateVO.getLocationPK();
			LocationVO locationVO = locationModel.getLocationVO(locationPK);

			EventVO eventVO = eventModel.getEventVO(locationVO.getEventPK());

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.GateEditor_DefaultToolTip);
			toolTipText.append('\n');
			toolTipText.append(I18N.Location);
			toolTipText.append(": ");
			toolTipText.append(locationVO.getName());

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.Event.getString());
			toolTipText.append(": ");
			toolTipText.append(eventVO.getMnemonic());

			toolTip = toolTipText.toString();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			toolTip = "";
		}
		return toolTip;
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.Gate.getString();
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

			// set data
			setEntity(gateVO);

			nameText.addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void setEntity(GateVO gateVO) {
		if (!isNew()) {
			// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			gateVO = gateVO.clone();
		}

		this.gateVO = gateVO;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (gateVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						if (gateVO.getName() != null) {
							nameText.setText(gateVO.getName());
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
		boolean result = gateVO.getPK() == null;
		return result;
	}


	@Override
	public void refresh() throws Exception {
		if (gateVO != null && gateVO.getID() != null) {
			gateModel.refresh(gateVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				gateVO = gateModel.getGateVO(gateVO.getID());
				if (gateVO != null) {
					setEntity(gateVO);
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
				gateVO = gateModel.create(gateVO);

				// observe the model
				gateModel.addListener(this, gateVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(gateVO.getID());

				// set new entity
				setEntity(gateVO);
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
				gateModel.update(gateVO);
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
		if (gateVO != null) {
			gateVO.setName(nameText.getText());
		}
	}


	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		final String[] labels = {
			UtilI18N.ID,
			I18N.LocationID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(gateVO.getID()),
			StringHelper.avoidNull(gateVO.getLocationPK()),
			formatHelper.formatDateTime(gateVO.getNewTime()),
			gateVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(gateVO.getEditTime()),
			gateVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ParticipantLabel.Gate.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}

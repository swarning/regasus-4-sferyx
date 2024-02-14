/**
 * GateDeviceEditor.java
 * created on 25.09.2013 11:49:58
 */
package de.regasus.common.gatedevice.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.GateDeviceVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.common.GateDeviceModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.ui.Activator;

public class GateDeviceEditor
extends AbstractEditor<GateDeviceEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "GateDeviceEditor";

	// the entity
	private GateDeviceVO gateDeviceVO;

	// the model
	private GateDeviceModel gateDeviceModel;
	private ServerModel serverModel;


	// **************************************************************************
	// * Widgets
	// *

	private Text nameText;
	private Text serialNoText;

	// *
	// * Widgets
	// **************************************************************************


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		gateDeviceModel = GateDeviceModel.getInstance();
		serverModel = ServerModel.getInstance();

		if (key != null) {
			// get entity
			gateDeviceVO = gateDeviceModel.getGateDeviceVO(key);

			// register at model
			gateDeviceModel.addListener(this, key);
		}
		else {
			// create empty entity
			gateDeviceVO = new GateDeviceVO();
		}
	}


	@Override
	public void dispose() {
		if (gateDeviceModel != null && gateDeviceVO != null) {
			try {
				gateDeviceModel.removeListener(this, gateDeviceVO.getPK());
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
			name = I18N.GateDeviceEditor_NewName;
		}
		else if (gateDeviceVO.getName() != null) {
			name = gateDeviceVO.getName();
		}

		name = StringHelper.avoidNull(name);
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.GateDeviceEditor_DefaultToolTip;
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.GateDevice.getString();
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

			Label serialNoLabel = new Label(parent, SWT.NONE);
			serialNoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			serialNoLabel.setText(ParticipantLabel.GateDevice_SerialNo.getString());

			serialNoText = new Text(parent, SWT.BORDER);
			serialNoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			// set data
			setEntity(gateDeviceVO);

			nameText.addModifyListener(this);
			serialNoText.addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void setEntity(GateDeviceVO gateDeviceVO) {
		if (!isNew()) {
			// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			gateDeviceVO = gateDeviceVO.clone();
		}

		this.gateDeviceVO = gateDeviceVO;

		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (gateDeviceVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						if (gateDeviceVO.getName() != null) {
							nameText.setText(gateDeviceVO.getName());
						}
						if (gateDeviceVO.getSerialNo() != null) {
							serialNoText.setText(gateDeviceVO.getSerialNo());
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
		boolean result = gateDeviceVO.getPK() == null;
		return result;
	}


	@Override
	public void refresh() throws Exception {
		if (gateDeviceVO != null && gateDeviceVO.getPK() != null) {
			gateDeviceModel.refresh(gateDeviceVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				gateDeviceVO = gateDeviceModel.getGateDeviceVO(gateDeviceVO.getID());
				if (gateDeviceVO != null) {
					setEntity(gateDeviceVO);
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
				gateDeviceVO = gateDeviceModel.create(gateDeviceVO);

				// observe the model
				gateDeviceModel.addListener(this, gateDeviceVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(gateDeviceVO.getID());

				// set new entity
				setEntity(gateDeviceVO);
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
				gateDeviceModel.update(gateDeviceVO);
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
		if (gateDeviceVO != null) {
			gateDeviceVO.setName(nameText.getText());
			gateDeviceVO.setSerialNo(serialNoText.getText());
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == gateDeviceModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (gateDeviceVO != null) {
					gateDeviceVO = gateDeviceModel.getGateDeviceVO(gateDeviceVO.getID());
					if (gateDeviceVO != null) {
						setEntity(gateDeviceVO);
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
	protected void openInfoDialog() {
		// the labels of the info dialog
		String[] labels = {
			UtilI18N.ID,
			UtilI18N.Name,
			UtilI18N.EditDateTime
		};

		// the values of the info dialog
				FormatHelper formatHelper = new FormatHelper();
				String[] values = {
					String.valueOf(gateDeviceVO.getPK()),
					getName(),
					formatHelper.formatDateTime(gateDeviceVO.getEditTime())
				};


				// show info dialog
				EditorInfoDialog infoDialog = new EditorInfoDialog(
					getSite().getShell(),
					ParticipantLabel.GateDevice.getString() + ": " + UtilI18N.Info,
					labels,
					values
				);

				// size up the dialog because the labels are very long
				infoDialog.setSize(new Point(300, 150));

				infoDialog.open();
	}

}

package de.regasus.programme.workgroup.editor;

import static com.lambdalogic.util.rcp.widget.SWTHelper.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.data.WorkGroupVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.WorkGroupModel;
import de.regasus.ui.Activator;

/**
 * Most of this editors functionality is now implemented in its superclass {@link AbstractCancelationTermEditor},
 * which it shares with the {@link WorkGroupEditor}. In this class we only have the stuff
 * that needs to now that this is a Cancellation for a <i>Hotel Offering</i>.
 *
 * @author manfred
 *
 */
public class WorkGroupEditor
extends AbstractEditor<WorkGroupEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "WorkGroupEditor";

	// the entity
	private WorkGroupVO workGroupVO;


	// the model
	private WorkGroupModel workGroupModel = WorkGroupModel.getInstance();

	private Text nameText;

	private Text speakerText;

	private DateTimeComposite startTimeComposite;

	private DateTimeComposite endTimeComposite;

	private Text locationText;

	private NullableSpinner capacitySpinner;

	// ******************************************************************************************
	// * Overriden EditorPart methods

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long workGroupPK = editorInput.getKey();


		if (workGroupPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			workGroupVO = workGroupModel.getWorkGroupVO(workGroupPK);

			// register at model
			workGroupModel.addListener(this, workGroupPK);
		}
		else {
			// create empty entity
			workGroupVO = new WorkGroupVO();

			workGroupVO.setProgrammePointPK(editorInput.getProgrammePointPK());
		}
	}

	@Override
	public void dispose() {
		if (workGroupModel != null && workGroupVO.getPK() != null) {
			try {
				workGroupModel.removeListener(this, workGroupVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(WorkGroupVO workGroupVO) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		workGroupVO = workGroupVO.clone();
		}

		this.workGroupVO = workGroupVO;

		syncWidgetsToEntity();
	}

	@Override
	protected String getTypeName() {
		return ParticipantLabel.WorkGroup.getString();
	}

//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.WorkGroupEditor_InfoButtonToolTip;
//	}

	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout(2, false));

			nameText = createLabelAndText(mainComposite, UtilI18N.Name, true);

			speakerText = createLabelAndText(mainComposite, I18N.Speaker);

			startTimeComposite = SWTHelper.createLabelAndDateTimeComposite(mainComposite, UtilI18N.BeginTime);

			endTimeComposite = SWTHelper.createLabelAndDateTimeComposite(mainComposite, UtilI18N.EndTime);

			locationText = createLabelAndText(mainComposite, I18N.Location);

			createLabel(mainComposite, I18N.Places);

			capacitySpinner = new NullableSpinner(mainComposite, SWT.NONE);
			capacitySpinner.setMinimum(WorkGroupVO.MIN_CAPACITY);
			capacitySpinner.setMaximum(WorkGroupVO.MAX_CAPACITY);
			capacitySpinner.setNullable(false);
			capacitySpinner.setValue(0);
			GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			layoutData.horizontalIndent = 5;
			capacitySpinner.setLayoutData(layoutData);
			WidgetSizer.setWidth(capacitySpinner);


			setEntity(workGroupVO);

			nameText.addModifyListener(this);
			speakerText.addModifyListener(this);

			startTimeComposite.addModifyListener(this);
			endTimeComposite.addModifyListener(this);

			locationText.addModifyListener(this);
			capacitySpinner.addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
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
			UtilI18N.CancelDateTime,
			UtilI18N.CancelUser
		};

		FormatHelper formatHelper = new FormatHelper();

		// the values of the info dialog
		final String[] values = {
			StringHelper.avoidNull(workGroupVO.getID()),
			formatHelper.formatDateTime(workGroupVO.getNewTime()),
			workGroupVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(workGroupVO.getEditTime()),
			workGroupVO.getEditDisplayUserStr(),
			formatHelper.formatDateTime(workGroupVO.getCancelTime()),
			workGroupVO.getCancelDisplayUserStr(),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ParticipantLabel.WorkGroup + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		boolean create = isNew();

		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				workGroupVO = workGroupModel.create(workGroupVO);

				// observe the Model
				workGroupModel.addListener(this, workGroupVO.getPK());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(workGroupVO.getPK());

				// set new entity
				setEntity(workGroupVO);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				workGroupModel.update(workGroupVO);

				// setEntity will be called indirectly in dataChange()
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


	private void syncWidgetsToEntity() {
		if (workGroupVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {

						nameText.setText(StringHelper.avoidNull(workGroupVO.getName()));
						speakerText.setText(StringHelper.avoidNull(workGroupVO.getSpeaker()));

						startTimeComposite.setDate(workGroupVO.getStartTime());
						endTimeComposite.setDate(workGroupVO.getEndTime());

						Integer capacity = workGroupVO.getCapacity();
						if (capacity != null) {
							capacitySpinner.setValue(capacity);
						}
						locationText.setText(StringHelper.avoidNull(workGroupVO.getLocation()));

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());


						// set image
						Image image = null;
						if (workGroupVO.isCancelled()) {
							image = IconRegistry.getImage(IImageKeys.WORK_GROUP_CANCELLED);
						}
						else {
							image = IconRegistry.getImage(IImageKeys.WORK_GROUP);
						}
						setTitleImage(image);


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
		if (workGroupVO != null) {

			workGroupVO.setName(StringHelper.trim(nameText.getText()));
			workGroupVO.setSpeaker(StringHelper.trim(speakerText.getText()));

			workGroupVO.setStartTime(startTimeComposite.getDate());
			workGroupVO.setEndTime(endTimeComposite.getDate());

			workGroupVO.setLocation(StringHelper.trim(locationText.getText()));
			workGroupVO.setCapacity(capacitySpinner.getValueAsInteger());
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == workGroupModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (workGroupVO != null) {
					workGroupVO = workGroupModel.getWorkGroupVO(workGroupVO.getPK());
					if (workGroupVO != null) {
						setEntity(workGroupVO);
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
	public void refresh() throws Exception {
		if (workGroupVO != null && workGroupVO.getPK() != null) {
			workGroupModel.refresh(workGroupVO.getPK());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				workGroupVO = workGroupModel.getWorkGroupVO(workGroupVO.getID());
				if (workGroupVO != null) {
					setEntity(workGroupVO);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return workGroupVO.getPK() == null;
	}


	@Override
	protected String getName() {
		if (workGroupVO != null && workGroupVO.getName() != null) {
			return workGroupVO.getName();
		}
		else {
			return I18N.NewWorkgroup;
		}
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long programmePointPK = workGroupVO.getProgrammePointPK();
			ProgrammePointVO programmePointVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);

			Long eventPK = programmePointVO.getEventPK();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.WorkGroupEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(KernelLabel.Name.getString());
			toolTipText.append(": ");
			toolTipText.append(workGroupVO.getName());

			toolTipText.append('\n');
			toolTipText.append(ParticipantLabel.ProgrammePoint.getString());
			toolTipText.append(": ");
			toolTipText.append(programmePointVO.getName().getString());

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
	public Long getEventId() {
		try {
			if (workGroupVO != null) {
				Long programmePointPK = workGroupVO.getProgrammePointPK();

				ProgrammePointVO programmePointVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);
				Long eventPK = programmePointVO.getEventPK();
				if (eventPK != null) {
					return eventPK;
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		return null;
	}

}

package de.regasus.programme.programmepointtype.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.i18n.I18NText;

import de.regasus.I18N;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.programme.ProgrammePointTypeModel;

public class ProgrammePointTypeEditor
extends AbstractEditor<ProgrammePointTypeEditorInput>
implements IRefreshableEditorPart, CacheModelListener<String> {

	public static final String ID = "ProgrammePointTypeEditor";

	// the entity
	private ProgrammePointTypeVO programmePointTypeVO;

	// the model
	private ProgrammePointTypeModel programmePointTypeModel;

	// **************************************************************************
	// * Widgets
	// *

	private I18NText names;
	private Text referenceCodeText;

	// *
	// * Widgets
	// **************************************************************************

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		// get model
		programmePointTypeModel = ProgrammePointTypeModel.getInstance();

		if (key != null) {
			// get entity
			programmePointTypeVO = programmePointTypeModel.getProgrammePointTypeVO(key);

			// register at model
			programmePointTypeModel.addListener(this, key);
		}
		else {
			// create empty entity
			programmePointTypeVO = new ProgrammePointTypeVO();
		}
	}


	@Override
	public void dispose() {
		if (programmePointTypeModel != null && programmePointTypeVO.getPK() != null) {
			try {
				programmePointTypeModel.removeListener(this, programmePointTypeVO.getPK());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ProgrammePointTypeVO programmePointTypeVO) {

		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		programmePointTypeVO = programmePointTypeVO.clone();
		}

		this.programmePointTypeVO = programmePointTypeVO;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.ProgrammePointType.getString();
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

			Composite mainComposite = new Composite(parent, SWT.NONE);
			mainComposite.setLayout(new GridLayout(2, false));


			final Label namesLabel = new Label(mainComposite, SWT.NONE);
			namesLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			namesLabel.setText(KernelLabel.Name.getString());

			names = new I18NText(mainComposite, SWT.NONE, LanguageProvider.getInstance());
			final GridData gd_names = new GridData(SWT.FILL, SWT.CENTER, true, false);
			names.setLayoutData(gd_names);

			final Label rferenceCodeLabel = new Label(mainComposite, SWT.NONE);
			rferenceCodeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			rferenceCodeLabel.setText(ParticipantLabel.ProgrammePointType_ReferenceCode.getString());

			referenceCodeText = new Text(mainComposite, SWT.BORDER);
			referenceCodeText.setTextLimit(ProgrammePointTypeVO.MAX_LENGTH_REFERENCE_CODE);
			final GridData gd_categoryText = new GridData(SWT.FILL, SWT.CENTER, true, false);
			referenceCodeText.setLayoutData(gd_categoryText);


			// sync widgets and groups to the entity
			setEntity(programmePointTypeVO);

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

			// Copy the data from the widgets to the entity
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception is thrown.
				 */
				programmePointTypeVO = programmePointTypeModel.create(programmePointTypeVO);

				// Observe the model
				programmePointTypeModel.addListener(this, programmePointTypeVO.getPK());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(programmePointTypeVO.getPK());

				// Set new entity
				setEntity(programmePointTypeVO);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception is thrown.
				 */
				programmePointTypeModel.update(programmePointTypeVO);

				// setEntity will be called indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException are handled separately to show the original error message.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			String msg = null;
			if (create) {
				msg = I18N.CreateProgrammePointTypeErrorMessage;
			}
			else {
				msg = I18N.EditProgrammePointTypeErrorMessage;
			}
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (programmePointTypeVO != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						names.setLanguageString(programmePointTypeVO.getName());
						referenceCodeText.setText(StringHelper.avoidNull(programmePointTypeVO.getReferenceCode()));

						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh EditorInput
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
		if (programmePointTypeVO != null) {
			programmePointTypeVO.setID(programmePointTypeVO.getID());
			programmePointTypeVO.setName(names.getLanguageString());
			programmePointTypeVO.setReferenceCode(referenceCodeText.getText());
		}
	}


	private void addModifyListener(final ModifyListener listener) {
		names.addModifyListener(listener);
		referenceCodeText.addModifyListener(listener);
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
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
			String.valueOf(programmePointTypeVO.getPK()),
			getName(),
			formatHelper.formatDateTime(programmePointTypeVO.getEditTime())
		};


		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ParticipantLabel.ProgrammePointType.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);

		// size up the dialog because the labels are very long
		infoDialog.setSize(new Point(300, 150));

		infoDialog.open();
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == programmePointTypeModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (programmePointTypeVO != null) {
					programmePointTypeVO = programmePointTypeModel.getProgrammePointTypeVO(programmePointTypeVO.getPK());
					if (programmePointTypeVO != null) {
						setEntity(programmePointTypeVO);
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
		if (programmePointTypeVO != null && programmePointTypeVO.getName() != null) {
			name = programmePointTypeVO.getName().getString();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.ProgrammePointTypeEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		return I18N.ProgrammePointTypeEditor_DefaultToolTip;
	}


	@Override
	public boolean isNew() {
		return programmePointTypeVO.getPK() == null;
	}


	@Override
	public void refresh() throws Exception {
		if (programmePointTypeVO != null && programmePointTypeVO.getID() != null) {
			programmePointTypeModel.refresh(programmePointTypeVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				programmePointTypeVO = programmePointTypeModel.getProgrammePointTypeVO(programmePointTypeVO.getID());
				if (programmePointTypeVO != null) {
					setEntity(programmePointTypeVO);
				}
			}
		}
	}

}

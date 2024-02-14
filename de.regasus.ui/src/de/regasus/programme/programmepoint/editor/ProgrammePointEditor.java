package de.regasus.programme.programmepoint.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.IImageKeys;
import de.regasus.IconRegistry;
import de.regasus.common.Language;
import de.regasus.common.composite.LabelTextCombinationsComposite;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

public class ProgrammePointEditor
extends AbstractEditor<ProgrammePointEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long>, EventIdProvider {

	public static final String ID = "ProgrammePointEditor";

	// the entity
	private ProgrammePointVO programmePointVO;

	// the model
	private ProgrammePointModel programmePointModel;

	// ConfigParameterSet
	private ConfigParameterSet configParameterSet;

	private List<String> languageIds;
	private List<Language> languageList;


	// Widgets
	private TabFolder tabFolder;
	private ProgrammePointGeneralComposite generalComposite;
	private ProgrammePointImageComposite imageComposite;
	private ProgrammePointStreamComposite streamComposite;
	private ProgrammePointAccessControlComposite accessControlComposite;
	private ProgrammePointVoucherComposite voucherComposite;
	private LabelTextCombinationsComposite labelTextCombinationsComposite;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long programmePointPK = editorInput.getKey();
		Long eventPK = editorInput.getEventId();

		// get models
		programmePointModel = ProgrammePointModel.getInstance();

		if (programmePointPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			programmePointVO = programmePointModel.getProgrammePointVO(programmePointPK);

			// register at model
			programmePointModel.addListener(this, programmePointPK);
		}
		else {
			// create empty entity
			programmePointVO = new ProgrammePointVO();
			programmePointVO.setEventPK(eventPK);
		}


		// get ConfigSet (eventPK must not be null)
		configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet(eventPK);

		// determine languages
		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
		languageIds = eventVO.getLanguages();
		languageList = LanguageModel.getInstance().getLanguages(languageIds);
	}


	@Override
	public void dispose() {
		if (programmePointModel != null && programmePointVO.getID() != null) {
			try {
				programmePointModel.removeListener(this, programmePointVO.getID());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(ProgrammePointVO programmePointVO) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			programmePointVO = programmePointVO.clone();
		}


		try {
			this.programmePointVO = programmePointVO;
			// set entity to other composites
			generalComposite.setProgrammePointVO(programmePointVO);
			imageComposite.setProgrammePoint(programmePointVO);
			streamComposite.setProgrammePointVO(programmePointVO);
			accessControlComposite.setProgrammePointVO(programmePointVO);
			voucherComposite.setProgrammePointVO(programmePointVO);
			labelTextCombinationsComposite.setEntity( programmePointVO.getLabelTextCombinationsVO() );

			syncWidgetsToEntity();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	protected String getTypeName() {
		return ParticipantLabel.ProgrammePoint.getString();
	}


	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.ProgrammePointEditor_InfoButtonToolTip;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			// tabFolder
			tabFolder = new TabFolder(parent, SWT.NONE);

			// General Tab
			LazyScrolledTabItem generalTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			generalTabItem.setText(I18N.ProgrammePointEditor_GeneralTabText);
			generalComposite = new ProgrammePointGeneralComposite(
				generalTabItem.getContentComposite(),
				SWT.NONE,
				languageList,
				configParameterSet
			);

			generalTabItem.refreshScrollbars();


			// Image Tab
			LazyScrolledTabItem imageTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			imageTabItem.setText(I18N.ProgrammePointEditor_ImageTabText);
			imageComposite = new ProgrammePointImageComposite(imageTabItem.getContentComposite(), SWT.NONE, languageIds);

			imageTabItem.refreshScrollbars();


			// Stream Tab
			LazyScrolledTabItem streamTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			streamTabItem.setText(I18N.ProgrammePointEditor_StreamingTabText);
			streamComposite = new ProgrammePointStreamComposite(streamTabItem.getContentComposite(), SWT.NONE);

			streamTabItem.refreshScrollbars();


			// Access Control Tab
			LazyScrolledTabItem accessControlTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			accessControlTabItem.setText(I18N.ProgrammePointEditor_AccessControlTabText);
			accessControlComposite = new ProgrammePointAccessControlComposite(accessControlTabItem.getContentComposite(), SWT.NONE);

			accessControlTabItem.refreshScrollbars();


			// Voucher Tab
			LazyScrolledTabItem voucherTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			voucherTabItem.setText(I18N.ProgrammePointEditor_VoucherTabText);
			voucherComposite = new ProgrammePointVoucherComposite(voucherTabItem.getContentComposite(), SWT.NONE);

			voucherTabItem.refreshScrollbars();


			// labelTextCombinationsComposite
			LazyScrolledTabItem labelTextCombinationsTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			labelTextCombinationsTabItem.setText(UtilI18N.Texts);

			labelTextCombinationsComposite = new LabelTextCombinationsComposite(
				labelTextCombinationsTabItem.getContentComposite(),
				SWT.NONE,
				programmePointVO.getLabelTextCombinationsVO(),
				languageList
			);

			labelTextCombinationsTabItem.refreshScrollbars();


			// set data
			setEntity(programmePointVO);

			// after sync add this as ModifyListener to all widgets and groups
			generalComposite.addModifyListener(this);
			imageComposite.addModifyListener(this);
			streamComposite.addModifyListener(this);
			accessControlComposite.addModifyListener(this);
			voucherComposite.addModifyListener(this);
			labelTextCombinationsComposite.addModifyListener(this);
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

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (create) {
				/* Save new entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				programmePointVO = programmePointModel.create(programmePointVO);

				// observe the Model
				programmePointModel.addListener(this, programmePointVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(programmePointVO.getID());

				// set new entity
				setEntity(programmePointVO);
			}
			else {
				/* Save the entity.
				 * On success setEntity will be called indirectly in dataChange(),
				 * else an Exception will be thrown.
				 * The result of update() must not be assigned to programmePointVO,
				 * because this will happen in setEntity() and there it may be cloned!
				 * Assigning programmePointVO here would overwrite the cloned value with
				 * the one from the model. Therefore we would have inconsistent data!
				 */
				programmePointModel.update(programmePointVO);
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
		if (programmePointVO != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						// set editor title
						setPartName(getName());
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName(getName());
						editorInput.setToolTipText(getToolTipText());


						// set image
						Image image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT);
						// load CVO to know if the Programme Point is fully booked
						if (programmePointVO != null && programmePointVO.getID() != null) {
							Long programmePointPK = programmePointVO.getID();
							ProgrammePointCVO programmePointCVO = programmePointModel.getProgrammePointCVO(programmePointPK);

							if ( programmePointCVO.isCancelled() ) {
								image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT_CANCELLED);
							}
							else if ( programmePointCVO.isFullyBooked() ) {
								image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT_FULLY_BOOKED);
							}
							else if ( programmePointCVO.isWarnNumberExceeded() ) {
								image = IconRegistry.getImage(IImageKeys.PROGRAMME_POINT_BOOKING_EXCEEDED);
							}

							setTitleImage(image);
						}


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
		if (programmePointVO != null) {
			generalComposite.syncEntityToWidgets();
			imageComposite.syncEntityToWidgets();
			streamComposite.syncEntityToWidgets();
			accessControlComposite.syncEntityToWidgets();
			voucherComposite.syncEntityToWidgets();
			labelTextCombinationsComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == programmePointModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (programmePointVO != null) {
					programmePointVO = programmePointModel.getProgrammePointVO( programmePointVO.getPK() );
					if (programmePointVO != null) {
						setEntity(programmePointVO);
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
		if (programmePointVO != null && programmePointVO.getID() != null) {
			programmePointModel.refresh(programmePointVO.getID());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				programmePointVO = programmePointModel.getProgrammePointVO(programmePointVO.getID());
				if (programmePointVO != null) {
					setEntity(programmePointVO);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return programmePointVO.getPK() == null;
	}


	@Override
	protected String getName() {
		String name = null;

		if (programmePointVO != null) {
			LanguageString nameLS = programmePointVO.getName();
			if (nameLS != null) {
				name = nameLS.getString();
			}
		}

		if (StringHelper.isEmpty(name)) {
			name = I18N.ProgrammePointEditor_NewName;
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long eventPK = programmePointVO.getEventPK();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.ProgrammePointEditor_DefaultToolTip);

			// the name of the programme point is null if the programme point is new
			if (programmePointVO.getName() != null) {
				toolTipText.append('\n');
				toolTipText.append(KernelLabel.Name.getString());
				toolTipText.append(": ");
				toolTipText.append(programmePointVO.getName().getString());
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
	public Long getEventId() {
		if (programmePointVO != null) {
			return programmePointVO.getEventPK();
		}
		return null;
	}


	/**
	 * When the infoButton is pressed, this method opens a little dialog with some informational data.
	 */
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
			UtilI18N.EditUser,
			UtilI18N.CancelDateTime,
			UtilI18N.CancelUser
		};

		// get name of event
		Long eventPK = programmePointVO.getEventPK();
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
			StringHelper.avoidNull(programmePointVO.getID()),
			eventMnemonic,
			StringHelper.avoidNull(eventPK),
			formatHelper.formatDateTime(programmePointVO.getNewTime()),
			programmePointVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(programmePointVO.getEditTime()),
			programmePointVO.getEditDisplayUserStr(),
			formatHelper.formatDateTime(programmePointVO.getCancelTime()),
			programmePointVO.getCancelDisplayUserStr(),
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			ParticipantLabel.ProgrammePoint.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}

}

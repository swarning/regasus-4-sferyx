package de.regasus.hotel.contingent.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.HotelConfigParameterSet;
import com.lambdalogic.messeinfo.contact.LabelTextCombinationsVO;
import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentCVO;
import com.lambdalogic.messeinfo.hotel.data.HotelContingentVO;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.common.composite.LabelTextCombinationsComposite;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.hotel.HotelContingentModel;
import de.regasus.hotel.HotelModel;

public class HotelContingentEditor
extends AbstractEditor<HotelContingentEditorInput>
implements CacheModelListener<Long>, IRefreshableEditorPart, EventIdProvider {

	public static final String ID = "HotelContingentEditor";

	// the entity
	private HotelContingentCVO hotelContingentCVO;

	// ConfigParameterSet
	private HotelConfigParameterSet hotelConfigParameterSet;

	private Long eventPK;

	private Long hotelPK;

	private Hotel hotel;

	private List<Language> languageList;

	// models
	private HotelContingentModel hotelContingentModel = HotelContingentModel.getInstance();
	private HotelModel hotelModel = HotelModel.getInstance();
	private ConfigParameterSetModel configParameterSetModel = ConfigParameterSetModel.getInstance();


	// Widgets
	private TabFolder tabFolder;
	private HotelContingentEditorCapacityComposite capacityComposite;
	private HotelContingentEditorGeneralComposite generalComposite;
	private HotelContingentEditorInfoComposite infoComposite;
	private LabelTextCombinationsComposite labelTextCombinationsComposite;


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long key = editorInput.getKey();

		if (key != null) {
			// get entity
			hotelContingentCVO = hotelContingentModel.getHotelContingentCVO(key);

			eventPK = hotelContingentCVO.getVO().getEventPK();

			hotelPK = hotelContingentCVO.getVO().getHotelPK();

			// register at model
			hotelContingentModel.addListener(this, key);
		}
		else {
			hotelPK = editorInput.getHotelPK();
			eventPK = editorInput.getEventPK();

			HotelContingentVO hotelContingentVO = new HotelContingentVO();
			hotelContingentVO.setHotelPK(hotelPK);
			hotelContingentVO.setEventPK(editorInput.getEventPK());

			hotelContingentCVO = new HotelContingentCVO();
			hotelContingentCVO.setHotelContingentVO(hotelContingentVO);

			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
			hotelContingentCVO.createVolumesForEvent(eventVO);
			hotelContingentCVO.setRoomDefinitionPKs(new ArrayList<Long>());
		}


		hotel = hotelModel.getHotel(hotelPK);
		hotelModel.addListener(this, hotelPK);

		// get ConfigParameterSet (eventPK must not be null)
		ConfigParameterSet configParameterSet = configParameterSetModel.getConfigParameterSet(eventPK);
		hotelConfigParameterSet = configParameterSet.getEvent().getHotel();

		// determine languages
		EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
		List<String> languageIds = eventVO.getLanguages();
		languageList = LanguageModel.getInstance().getLanguages(languageIds);
	}


	@Override
	public void dispose() {
		if (hotelContingentModel != null && hotelContingentCVO.getPK() != null) {
			try {
				hotelContingentModel.removeListener(this, hotelContingentCVO.getPK());
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (hotelModel != null && hotelPK != null) {
			try {
				hotelModel.removeListener(this, hotelPK);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		super.dispose();
	}


	protected void setEntity(HotelContingentCVO hotelContingentCVO) throws Exception {
		if ( ! isNew()) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		hotelContingentCVO = hotelContingentCVO.clone();
		}
		this.hotelContingentCVO = hotelContingentCVO;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return HotelLabel.HotelContingent.getString();
	}


	@Override
	protected String getInfoButtonToolTipText() {
		return I18N.HotelContingentEditor_InfoButtonToolTip;
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param parent
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			tabFolder = new TabFolder(parent, SWT.NONE);

			// capacityComposite
			LazyScrolledTabItem capacityTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			capacityTabItem.setText(HotelLabel.HotelContingent_Rooms.getString());

			capacityComposite = new HotelContingentEditorCapacityComposite(
				capacityTabItem.getContentComposite(),
				SWT.NONE,
				hotelConfigParameterSet
			);
			capacityTabItem.refreshScrollbars();


			// make that Ctl+C copies table contents to clipboard
			capacityComposite.registerCopyAction(getEditorSite().getActionBars());

			// generalComposite
			LazyScrolledTabItem generalTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			generalTabItem.setText(UtilI18N.General);

			generalComposite = new HotelContingentEditorGeneralComposite(generalTabItem.getContentComposite(), SWT.NONE);
			generalTabItem.refreshScrollbars();


			// infoComposite
			LazyScrolledTabItem infoTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			infoTabItem.setText(UtilI18N.Info);

			infoComposite = new HotelContingentEditorInfoComposite(infoTabItem.getContentComposite(), SWT.NONE);
			infoTabItem.refreshScrollbars();


			// labelTextCombinationsComposite
			LazyScrolledTabItem labelTextCombinationsTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
			labelTextCombinationsTabItem.setText(UtilI18N.Texts);

			LabelTextCombinationsVO labelTextCombinationsVO = hotelContingentCVO.getVO().getLabelTextCombinationsVO();
			labelTextCombinationsComposite = new LabelTextCombinationsComposite(
				labelTextCombinationsTabItem.getContentComposite(),
				SWT.NONE,
				labelTextCombinationsVO,
				languageList
			);

			labelTextCombinationsTabItem.refreshScrollbars();


			// set (and synchronize) the entity
			setEntity(hotelContingentCVO);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}

		// after sync add this as ModifyListener to all widgets and groups
		generalComposite.addModifyListener(this);
		infoComposite.addModifyListener(this);
		labelTextCombinationsComposite.addModifyListener(this);
		capacityComposite.addModifyListener(this);
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
			UtilI18N.EditUser
		};

		HotelContingentVO hotelContingentVO = hotelContingentCVO.getVO();

		// get name of event
		Long eventPK = hotelContingentVO.getEventPK();
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
			StringHelper.avoidNull(hotelContingentVO.getID()),
			eventMnemonic,
			StringHelper.avoidNull(eventPK),
			formatHelper.formatDateTime(hotelContingentVO.getNewTime()),
			hotelContingentVO.getNewDisplayUserStr(),
			formatHelper.formatDateTime(hotelContingentVO.getEditTime()),
			hotelContingentVO.getEditDisplayUserStr()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.HotelContingent.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);
		infoDialog.open();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/*
			 * Entity mit den Widgets synchronisieren. Dabei werden die Daten der Widgets in das Entity kopiert.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			if (isNew()) {
				/*
				 * Save new entity. On success we get the updated entity, else an Exception will be thrown.
				 */

				hotelContingentCVO = hotelContingentModel.create(hotelContingentCVO);

				hotelContingentModel.addListener(this, hotelContingentCVO.getPK());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(hotelContingentCVO.getPK());
				// set new entity
				setEntity(hotelContingentCVO);
			}
			else {
				/*
				 * Save the entity. On success we get the updated entity, else an Exception will be thrown.
				 */
				hotelContingentModel.update(hotelContingentCVO);

				// setEntity will be called indirectly in dataChange()
			}

			monitor.worked(1);
		}
		catch (ErrorMessageException e) {
			// ErrorMessageException werden gesondert behandelt um die Originalfehlermeldung ausgeben zu können.
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		if (hotelContingentCVO != null) {
			syncExecInParentDisplay(new Runnable() {

				@Override
				public void run() {
					try {
						capacityComposite.setEntity(hotelContingentCVO);
						generalComposite.setEntity(hotelContingentCVO);
						infoComposite.setEntity(hotelContingentCVO);
						labelTextCombinationsComposite.setEntity( hotelContingentCVO.getVO().getLabelTextCombinationsVO() );

						// set editor title
						// refresh the EditorInput
						syncHotelNameToEntity();

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


	private void syncHotelNameToEntity() {
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
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private void syncEntityToWidgets() {
		if (hotelContingentCVO != null) {
			capacityComposite.syncEntityToWidgets();
			generalComposite.syncEntityToWidgets();
			infoComposite.syncEntityToWidgets();
			labelTextCombinationsComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == hotelContingentModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (hotelContingentCVO != null) {
					hotelContingentCVO = hotelContingentModel.getHotelContingentCVO(hotelContingentCVO.getPK());
					if (hotelContingentCVO != null) {
						setEntity(hotelContingentCVO);
					}
					else if (ServerModel.getInstance().isLoggedIn()) {
						closeBecauseDeletion();
					}
				}
			}
			else if (event.getSource() == hotelModel) {
				if (event.getOperation() == CacheModelOperation.REFRESH ||
					event.getOperation() == CacheModelOperation.UPDATE
				) {
					hotel = hotelModel.getHotel(hotelPK);
					syncHotelNameToEntity();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void refresh() throws Exception {
		if (hotelContingentCVO != null && hotelContingentCVO.getPK() != null) {
			hotelContingentModel.refresh(hotelContingentCVO.getPK());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				hotelContingentCVO = hotelContingentModel.getExtendedHotelContingentCVO(hotelContingentCVO.getPK());
				if (hotelContingentCVO != null) {
					setEntity(hotelContingentCVO);
				}
			}
		}
	}


	@Override
	protected String getName() {
		String name = null;
		if (hotelContingentCVO == null || hotelContingentCVO.getPK() == null) {
			name = I18N.HotelContingentEditor_NewName;
		}
		else {
			name = hotelContingentCVO.getVO().getName();
			if (name == null) {
				name = hotel.getName();
			}
		}

		if (name == null) {
			name = HotelLabel.HotelContingent.getString();
		}

		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.HotelContingentEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(KernelLabel.Name.getString());
			toolTipText.append(": ");
			toolTipText.append(hotelContingentCVO.getVO().getName());

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.Hotel.getString());
			toolTipText.append(": ");
			toolTipText.append(hotel.getName1());

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
	public boolean isNew() {
		return hotelContingentCVO.getPK() == null;
	}


	@Override
	public Long getEventId() {
		return eventPK;
	}

}

package de.regasus.hotel.roomdefinition.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.hotel.data.RoomDefinitionVO;
import com.lambdalogic.messeinfo.hotel.data.RoomType;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NMultiText;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.common.Photo;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.core.ui.i18n.LanguageProvider;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.hotel.RoomDefinitionPhotoModel;

public class RoomDefinitionEditor
extends AbstractEditor<RoomDefinitionEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "RoomDefinitionEditor";

	// the entity
	private RoomDefinitionVO roomDefinitionVO;

	// models
	private RoomDefinitionModel roomDefinitionModel = RoomDefinitionModel.getInstance();
	private RoomDefinitionPhotoModel roomDefinitionPhotoModel = RoomDefinitionPhotoModel.getInstance();


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private I18NMultiText i18nMultiText;
	private final String[] LABELS = {
		UtilI18N.Name,
		UtilI18N.Description
	};

	private RoomTypeGroup roomTypeGroup;
	private RoomFacilitiesComposite roomFacilitiesComposite;
	private RoomDefinitionPhotoComposite photoComposite;

	// *
	// * Widgets
	// **************************************************************************


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long roomDefinitionPK = editorInput.getKey();

		if (roomDefinitionPK != null) {
			// Get the entity before registration as listener at the model.
			// So, if an Exception occurs when getting the entity, we don't register as listener. look at MIRCP-1129

			// get entity
			roomDefinitionVO = roomDefinitionModel.getRoomDefinitionVO(roomDefinitionPK);

			// register at model
			roomDefinitionModel.addListener(this, roomDefinitionPK);
		}
		else {
			// create empty entity
			roomDefinitionVO = new RoomDefinitionVO();
			roomDefinitionVO.setRoomType(RoomType.SINGLE);
			roomDefinitionVO.setGuestQuantity(1);
			roomDefinitionVO.setHotelPK(editorInput.getHotelPK());
		}
	}


	@Override
	public void dispose() {
		if (roomDefinitionModel != null && roomDefinitionVO.getID() != null) {
			try {
				roomDefinitionModel.removeListener(this, roomDefinitionVO.getID());
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		super.dispose();
	}


	/**
	 * Create contents of the editor part
	 *
	 * @param mainComposite
	 */
	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			tabFolder = new TabFolder(parent, SWT.NONE);

			buildGeneralTab();
			buildFacilitiesTab();
			if ( !isNew() ) {
				buildPhotoTab();
			}


			/* Sync widgets and groups to the entity.
			 *
			 * Because this editor doesn't use any sub-composites or -groups, we
			 * could call syncWidgetsToEntity() also. But for compatibility with
			 * other editors, we call setEntity().
			 */
			setEntity(roomDefinitionVO);


			i18nMultiText.addModifyListener(this);
			roomTypeGroup.addModifyListener(this);
			roomFacilitiesComposite.addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void buildGeneralTab() throws Exception {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText(UtilI18N.General);

		Composite composite = new Composite(tabItem.getContentComposite(), SWT.NONE);

		final int COLUMN_COUNT = 2;
		composite.setLayout(new GridLayout(COLUMN_COUNT, false));


		// name and description
		{
			i18nMultiText = new I18NMultiText(
				composite, 						// parent
				SWT.NONE,						// style
				LABELS,							// LABELS
				new boolean[] {true, true},		// multiLine
				new boolean[] {true, false},	// required
				LanguageProvider.getInstance()	// languageProvider
			);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			// do NOT set gridData.heightHint cause this disables dynamic height
			i18nMultiText.setLayoutData(gridData);
		}


		// room type
		roomTypeGroup = new RoomTypeGroup(composite, SWT.NONE);
		roomTypeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));


		tabItem.refreshScrollbars();
	}


	private void buildFacilitiesTab() throws Exception {
		LazyScrolledTabItem tabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
		tabItem.setText( HotelLabel.Room_Facilities.getString() );
		roomFacilitiesComposite = new RoomFacilitiesComposite(tabItem.getContentComposite(), SWT.NONE);

		tabItem.refreshScrollbars();
	}


	private void buildPhotoTab() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(I18N.PortalEditor_PhotoTab);

		photoComposite = new RoomDefinitionPhotoComposite(tabFolder, roomDefinitionVO);

		tabItem.setControl(photoComposite);

		photoComposite.addModifyListener(this);
	}


	protected void setEntity(RoomDefinitionVO roomDefinitionVO) {
		if ( ! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
    		roomDefinitionVO = roomDefinitionVO.clone();
		}

		this.roomDefinitionVO = roomDefinitionVO;

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return HotelLabel.RoomDefinition.getString();
	}


//	@Override
//	protected String getInfoButtonToolTipText() {
//		return EmailI18N.ProgrammePointEditor_InfoButtonToolTip;
//	}


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
				roomDefinitionVO = roomDefinitionModel.create(roomDefinitionVO);

				// observe the Model
				roomDefinitionModel.addListener(this, roomDefinitionVO.getID());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(roomDefinitionVO.getID());

				buildPhotoTab();

				// set new entity
				setEntity(roomDefinitionVO);
			}
			else {
				/* Save the entity.
				 * On success we get the updated entity, else an Exception will be thrown.
				 */
				roomDefinitionModel.update(roomDefinitionVO);

				// setEntity will be called indirectly in dataChange()
			}
			monitor.worked(1);


			// save order of Photos
			if (photoComposite != null && photoComposite.isModified() ) {
    			List<Photo> photoList = photoComposite.getPhotoList();
    			if (photoList != null) {
    				List<Long> orderedPhotoIds = Photo.getPKs(photoList);
    				RoomDefinitionPhotoModel.getInstance().updateOrder(orderedPhotoIds);
    			}
			}
			monitor.worked(1);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		finally {
			monitor.done();
		}
	}


	private void syncWidgetsToEntity() {
		syncExecInParentDisplay(new Runnable() {
			@Override
			public void run() {
				try {
					if (roomDefinitionVO != null) {
						i18nMultiText.setLanguageString(LABELS[0], roomDefinitionVO.getName());
						i18nMultiText.setLanguageString(LABELS[1], roomDefinitionVO.getDescription());

						roomTypeGroup.setRoomDefinitionVO(roomDefinitionVO);

						roomFacilitiesComposite.setEntity(roomDefinitionVO);
					}

					// set editor title
					setPartName(getName());
					firePropertyChange(PROP_TITLE);

					// refresh the EditorInput
					editorInput.setName(getName());
					editorInput.setToolTipText(getToolTipText());

					// signal that editor has no unsaved data anymore
					setDirty(false);

					if (roomDefinitionVO.isDeleted()) {
						i18nMultiText.setEnabled(false);
						roomFacilitiesComposite.setEnabled(false);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private void syncEntityToWidgets() {
		if (roomDefinitionVO != null) {
			roomDefinitionVO.setName(		i18nMultiText.getLanguageString(LABELS[0]));
			roomDefinitionVO.setDescription(i18nMultiText.getLanguageString(LABELS[1]));

			roomTypeGroup.syncEntityToWidgets();
			roomFacilitiesComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(final CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == roomDefinitionModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else {
					roomDefinitionVO = roomDefinitionModel.getRoomDefinitionVO(roomDefinitionVO.getPK());
					if (roomDefinitionVO != null && !roomDefinitionVO.isDeleted()) {
						setEntity(roomDefinitionVO);
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
		if (roomDefinitionVO != null && roomDefinitionVO.getID() != null) {
			roomDefinitionModel.refresh(roomDefinitionVO.getID());
			roomDefinitionPhotoModel.refreshForeignKey( roomDefinitionVO.getID() );

			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				roomDefinitionVO = roomDefinitionModel.getRoomDefinitionVO(roomDefinitionVO.getID());
				if (roomDefinitionVO != null) {
					setEntity(roomDefinitionVO);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return roomDefinitionVO.getPK() == null;
	}


	@Override
	protected String getName() {
		String name = null;
		if (roomDefinitionVO != null && roomDefinitionVO.getName() != null) {
			name = roomDefinitionVO.getName().getString();
		}
		if (StringHelper.isEmpty(name)) {
			name = I18N.RoomDefinitionEditor_NewName;
		}
		return name;
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long hotelID = roomDefinitionVO.getHotelPK();
			Hotel hotel = HotelModel.getInstance().getHotel(hotelID);


			StringBuffer toolTipText = new StringBuffer();
			toolTipText.append(I18N.RoomDefinitionEditor_DefaultToolTip);

			toolTipText.append('\n');
			toolTipText.append(KernelLabel.Name.getString());
			toolTipText.append(": ");
			if (roomDefinitionVO != null && roomDefinitionVO.getName() != null) {
				toolTipText.append(roomDefinitionVO.getName().getString());
			}

			toolTipText.append('\n');
			toolTipText.append(HotelLabel.Hotel.getString());
			toolTipText.append(": ");
			toolTipText.append(hotel.getName1());

			toolTip = toolTipText.toString();
		}
		catch (Exception e) {
			// This shouldn't happen
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			toolTip = "";
		}
		return toolTip;

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
			// UtilI18N.CreateDateTime,
			// UtilI18N.CreateUser,
			UtilI18N.EditDateTime
			// UtilI18N.EditUser
		};

		// the values of the info dialog
		final String[] values = {
			String.valueOf(roomDefinitionVO.getPK()),
			// formatHelper.formatDateTime(languageVO.getNewTime()),
			// languageVO.getNewUser(),
			formatHelper.formatDateTime(roomDefinitionVO.getEditTime())
			// languageVO.getEditUser()
		};

		// show info dialog
		final EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			HotelLabel.RoomDefinition.getString() + ": " + UtilI18N.Info,
			labels,
			values
		);

		infoDialog.open();
	}

}

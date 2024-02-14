// REFERENCE
package de.regasus.portal.portal.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.LazyScrolledTabItem;

import de.regasus.I18N;
import de.regasus.common.Photo;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalFileModel;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.portal.PortalPhotoModel;
import de.regasus.ui.Activator;


public class PortalEditor
extends AbstractEditor<PortalEditorInput>
implements IRefreshableEditorPart, EventIdProvider {

	public static final String ID = "PortalEditor";

	// the entity: Portal incl. Email Template IDs and Participant Type IDs
	private Portal portal;

	// Models
	private PortalModel portalModel = PortalModel.getInstance();
	private ParticipantTypeModel participantTypeModel = ParticipantTypeModel.getInstance();
	private EmailTemplateModel emailTemplateModel = EmailTemplateModel.getInstance();


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private PortalGeneralComposite generalComposite;
	private PortalParticipantTypeComposite portalParticipantTypeComposite;
	private PortalEmailTemplateComposite emailTemplateComposite;

	private LazyScrolledTabItem portalConfigTabItem;
	private PortalConfigContainerComposite portalConfigComposite;

	private PortalFileComposite fileComposite;
	private PortalPhotoComposite photoComposite;

	// *
	// * Widgets
	// **************************************************************************


	private CacheModelListener<Long> portalModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (event.getOperation() == CacheModelOperation.DELETE) {
				closeBecauseDeletion();
			}
			else if (portal != null) {
				portal = portalModel.getPortal( portal.getId() );

				if (portal != null) {
					setEntity(portal);
				}
				else if (ServerModel.getInstance().isLoggedIn()) {
					closeBecauseDeletion();
				}
			}
		}
	};


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long portalPK = editorInput.getKey();

		if (portalPK != null) {
			// init Editor to edit the Portal with the id portalPK

			// get entity
			portal = portalModel.getPortal(portalPK);

			// register at model
			portalModel.addListener(portalModelListener, portalPK);
		}
		else {
			throw new RuntimeException("No Portal ID");
		}
	}


	@Override
	public void dispose() {
		if (portalModel != null && portal.getId() != null) {
			try {
				portalModel.removeListener(portalModelListener, portal.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	@Override
	protected String getTypeName() {
		return PortalI18N.Portal.getString();
	}


	@Override
	protected String getInfoButtonToolTipText() {
		return UtilI18N.InfoButtonToolTip;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			// tabFolder
			tabFolder = new TabFolder(parent, SWT.NONE);


			// General Tab
			TabItem generalTabItem = new TabItem(tabFolder, SWT.NONE);
			generalTabItem.setText(UtilI18N.General);
			generalComposite = new PortalGeneralComposite(tabFolder, SWT.NONE, portal);
			generalTabItem.setControl(generalComposite);


			// Participant Type Tab
			if ( portal.getPortalConfig().isWithParticipantTypes() ) {
    			TabItem participantTypeTabItem = new TabItem(tabFolder, SWT.NONE);
    			participantTypeTabItem.setText(ParticipantLabel.ParticipantTypes.getString());
    			portalParticipantTypeComposite = new PortalParticipantTypeComposite(tabFolder, SWT.NONE, portal);
    			participantTypeTabItem.setControl(portalParticipantTypeComposite);
			}

			// Email Template Tab
			if ( portal.getPortalConfig().isWithEmails() ) {
				TabItem emailTemplateTabItem = new TabItem(tabFolder, SWT.NONE);
				emailTemplateTabItem.setText(EmailLabel.EmailTemplates.getString());
				emailTemplateComposite = new PortalEmailTemplateComposite(tabFolder, SWT.NONE);
				emailTemplateTabItem.setControl(emailTemplateComposite);
			}


			// Portal Config Tab
			if ( portal.getPortalConfig().isWithConfig() ) {
				portalConfigTabItem = new LazyScrolledTabItem(tabFolder, SWT.NONE);
				portalConfigTabItem.setText(I18N.PortalEditor_PortalConfigurationTab);
				portalConfigComposite = new PortalConfigContainerComposite(portalConfigTabItem.getContentComposite(), SWT.NONE);
				/* Refreshing the scrollbars here does not make sense, because PortalConfigContainerComposite does
				 * not contain its widgets yet. Therefore the scrollbars will be refreshed in setEntity() after the
				 * PortalConfigContainerComposite created its final content.
				 *
				 * portalConfigTabItem.refreshScrollbars();
				 */

				portalConfigTabItem.refreshScrollbars();
			}


			// File Tab
			TabItem fileTabItem = new TabItem(tabFolder, SWT.NONE);
			fileTabItem.setText(I18N.PortalEditor_PhotoTab);
			fileTabItem.setText(UtilI18N.Files);
			fileComposite = new PortalFileComposite(tabFolder, SWT.NONE, portal);
			fileTabItem.setControl(fileComposite);


			// Photo Tab
			if ( portal.getPortalConfig().isWithPhotos() ) {
    			TabItem photoTabItem = new TabItem(tabFolder, SWT.NONE);
    			photoTabItem.setText(I18N.PortalEditor_PhotoTab);

    			photoComposite = new PortalPhotoComposite(tabFolder, portal);

    			photoTabItem.setControl(photoComposite);
			}

			// set data
			setEntity(portal);


			// after sync add this as ModifyListener to all widgets and groups
			addModifyListenerToWidgets();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	private void addModifyListenerToWidgets() {
		generalComposite.addModifyListener(this);

		if (portalParticipantTypeComposite != null) {
			portalParticipantTypeComposite.addModifyListener(this);
		}

		if (emailTemplateComposite != null) {
			emailTemplateComposite.addModifyListener(this);
		}

		if (portalConfigComposite != null) {
			portalConfigComposite.addModifyListener(this);
		}

		if (photoComposite != null) {
			photoComposite.addModifyListener(this);
		}
	}


	protected void setEntity(Portal portal) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			portal = portal.clone();
		}

		this.portal = portal;

		// set entity to other Composites
		generalComposite.setPortal(portal);

		if (portalParticipantTypeComposite != null) {
			portalParticipantTypeComposite.setPortal(portal);
		}

		if (emailTemplateComposite != null) {
			emailTemplateComposite.setPortal(portal);
		}

		if (portalConfigComposite != null) {
			portalConfigComposite.setPortal(portal);
		}

		syncWidgetsToEntity();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 3);

			// Synchronize entity with the widgets. The data of the widgets is copied to the entity.
			syncEntityToWidgets();
			monitor.worked(1);

			/* Save the entity.
			 * On success setEntity will be called indirectly in dataChange(), else an Exception will be thrown.
			 * The result of update() must not be assigned to entity, because this will happen in setEntity()
			 * and there it will be cloned!
			 * Assigning the entity here would overwrite the cloned value with the one from the model.
			 * Therefore we would have inconsistent data!
			 */
			portalModel.update(portal);
			monitor.worked(1);


			// save order of Photos
			if (photoComposite != null && photoComposite.isModified()) {
    			List<Photo> photoList = photoComposite.getPhotoList();
    			if (photoList != null) {
    				List<Long> orderedPhotoIds = Photo.getPKs(photoList);
    				PortalPhotoModel.getInstance().updateOrder(orderedPhotoIds);
    			}
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
		if (portal != null) {
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


	private void syncEntityToWidgets() throws Exception {
		if (portal != null) {
			generalComposite.syncEntityToWidgets();

			if (portalParticipantTypeComposite != null) {
				portalParticipantTypeComposite.syncEntityToWidgets();
			}

			if (emailTemplateComposite != null) {
				emailTemplateComposite.syncEntityToWidgets();
			}

			if (portalConfigComposite != null) {
				portalConfigComposite.syncEntityToWidgets();
			}
		}
	}


	@Override
	public void refresh() throws Exception {
		if (portal != null && portal.getId() != null) {
			portalModel.refresh( portal.getId() );

			if ( portal.getPortalConfig().isWithParticipantTypes() ) {
				participantTypeModel.refresh();
			}

			if ( portal.getPortalConfig().isWithEmails() ) {
				emailTemplateModel.refresh();
			}

			PortalFileModel.getInstance().refreshForeignKey( portal.getId() );

			if ( portal.getPortalConfig().isWithPhotos() ) {
				PortalPhotoModel.getInstance().refreshForeignKey( portal.getId() );
			}

			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				portal = portalModel.getPortal(portal.getId());
				if (portal != null) {
					setEntity(portal);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return portal.getId() == null;
	}


	@Override
	protected String getName() {
		if (isNew()) {
			return I18N.PortalEditor_NewName;
		}
		else {
			String name = portal.getName();
			return StringHelper.avoidNull(name);
		}
	}


	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long eventPK = portal.getEventId();
			EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);

			StringBuilder toolTipText = new StringBuilder();
			toolTipText.append(I18N.PortalEditor_DefaultToolTip);

			if (portal.getName() != null) {
				toolTipText.append('\n');
				toolTipText.append(KernelLabel.Name.getString());
				toolTipText.append(": ");
				toolTipText.append(portal.getName());
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
		if (portal != null) {
			return portal.getEventId();
		}
		return null;
	}


	private String getEventMnemonic() {
		String eventMnemonic = "";
		try {
			Long eventPK = getEventId();
			if (eventPK != null) {
				EventVO eventVO =  EventModel.getInstance().getEventVO(eventPK);
    			if (eventVO != null) {
    				eventMnemonic = eventVO.getMnemonic();
    			}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return eventMnemonic;
	}


	/**
	 * When the infoButton is pressed, this method opens a small dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// the labels of the info dialog
		String[] labels = {
			UtilI18N.ID,
			ParticipantLabel.Event.getString(),
			ParticipantLabel.EventID.getString(),
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser
		};


		// the values of the info dialog
		String[] values = {
			StringHelper.avoidNull(portal.getId()),
			getEventMnemonic(),
			StringHelper.avoidNull( getEventId() ),
			portal.getNewTime().getString(),
			portal.getNewDisplayUserStr(),
			portal.getEditTime().getString(),
			portal.getEditDisplayUserStr()
		};


		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			getTypeName() + ": " + UtilI18N.Info,
			labels,
			values
		);

		//infoDialog.setSize(new Point(400, 150));

		infoDialog.open();
	}

}

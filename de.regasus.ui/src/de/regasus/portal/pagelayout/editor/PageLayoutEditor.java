package de.regasus.portal.pagelayout.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
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
import de.regasus.portal.PageLayout;
import de.regasus.portal.PageLayoutFileModel;
import de.regasus.portal.PageLayoutModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;


public class PageLayoutEditor
extends AbstractEditor<PageLayoutEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "PageLayoutEditor";

	// the entity: PageLayout
	private PageLayout pageLayout;

	private List<String> languageList;

	// the model
	private PageLayoutModel pageLayoutModel;


	// *****************************************************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private PageLayoutGeneralComposite generalComposite;
	private PageLayoutMenuComposite menuComposite;
	private PageLayoutHeaderFooterComposite headerComposite;
	private PageLayoutHeaderFooterComposite footerComposite;
	private PageLayoutLinkListComposite linkComposite;
	private PageLayoutStyleComposite styleComposite;

	// *
	// * Widgets
	// *****************************************************************************************************************


	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long pageLayoutId = editorInput.getKey();
		Long portalPK = editorInput.getPortalPK();

		// get models
		pageLayoutModel = PageLayoutModel.getInstance();

		if (pageLayoutId != null) {
			// get entity
			pageLayout = pageLayoutModel.getPageLayout(pageLayoutId);

			portalPK = pageLayout.getPortalId();

			// register at model
			pageLayoutModel.addListener(this, pageLayoutId);
		}
		else {
			// create empty entity
			pageLayout = new PageLayout();
			pageLayout.setPortalId(portalPK);
		}

		// determine languageList from Portal
		Portal portal = PortalModel.getInstance().getPortal(portalPK);
		languageList = portal.getLanguageList();
	}


	@Override
	public void dispose() {
		if (pageLayoutModel != null && pageLayout.getId() != null) {
			try {
				pageLayoutModel.removeListener(this, pageLayout.getId());

				// remove all (potential large) Files associated with this PageLayout from cache (to save memory)
				PageLayoutFileModel.getInstance().removeContentFromCache( pageLayout.getId() );
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	protected void setEntity(PageLayout pageLayout) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			pageLayout = pageLayout.clone();
		}

		this.pageLayout = pageLayout;

		// set entity to other composites
		generalComposite.setPageLayout(pageLayout);
		menuComposite.setPageLayout(pageLayout);
		headerComposite.setPageLayout(pageLayout);
		footerComposite.setPageLayout(pageLayout);
		linkComposite.setPageLayout(pageLayout);
		styleComposite.setPageLayout(pageLayout);

		syncWidgetsToEntity();
	}


	@Override
	protected String getTypeName() {
		return PortalI18N.PageLayout.getString();
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
			{
    			TabItem generalTabItem = new TabItem(tabFolder, SWT.NONE);
    			generalTabItem.setText(UtilI18N.General);
    			generalComposite = new PageLayoutGeneralComposite(tabFolder, SWT.NONE);
    			generalTabItem.setControl(generalComposite);
			}

			// Menu Tab
			{
				TabItem menuTabItem = new TabItem(tabFolder, SWT.NONE);
				menuTabItem.setText(I18N.PageLayoutEditor_MenuTabName);
				menuComposite = new PageLayoutMenuComposite(tabFolder, SWT.NONE, languageList);
				menuTabItem.setControl(menuComposite);
			}

			// Header Tab
			{
    			TabItem headerTabItem = new TabItem(tabFolder, SWT.NONE);
    			headerTabItem.setText(I18N.PageLayoutEditor_HeaderTabName);
    			headerComposite = PageLayoutHeaderFooterComposite.buildHeaderInstance(tabFolder, SWT.NONE, languageList);
    			headerTabItem.setControl(headerComposite);
			}

			// Footer Tab
			{
    			TabItem footerTabItem = new TabItem(tabFolder, SWT.NONE);
    			footerTabItem.setText(I18N.PageLayoutEditor_FooterTabName);
    			footerComposite = PageLayoutHeaderFooterComposite.buildFooterInstance(tabFolder, SWT.NONE, languageList);
    			footerTabItem.setControl(footerComposite);
			}

			// Link Tab
			{
    			TabItem linkTabItem = new TabItem(tabFolder, SWT.NONE);
    			linkTabItem.setText(I18N.PageLayoutEditor_LinkTabName);
    			linkComposite = new PageLayoutLinkListComposite(tabFolder, SWT.NONE, languageList);
    			linkTabItem.setControl(linkComposite);
			}

			// Style Tab
			{
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(I18N.PageLayoutEditor_StyleTabName);
				styleComposite = new PageLayoutStyleComposite(tabFolder, SWT.NONE, pageLayout);
				tabItem.setControl(styleComposite);
			}


			// set data
			setEntity(pageLayout);

			// after sync add this as ModifyListener to all widgets and groups
			generalComposite.addModifyListener(this);
			menuComposite.addModifyListener(this);
			headerComposite.addModifyListener(this);
			footerComposite.addModifyListener(this);
			linkComposite.addModifyListener(this);
			styleComposite.addModifyListener(this);
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
				pageLayout = pageLayoutModel.create(pageLayout);

				// observe the Model
				pageLayoutModel.addListener(this, pageLayout.getId());

				// Set the Long of the new entity to the EditorInput
				editorInput.setKey(pageLayout.getId());

				// set new entity
				setEntity(pageLayout);
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
				pageLayoutModel.update(pageLayout);
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
		if (pageLayout != null) {
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


	private void syncEntityToWidgets() {
		if (pageLayout != null) {
			generalComposite.syncEntityToWidgets();
			menuComposite.syncEntityToWidgets();
			headerComposite.syncEntityToWidgets();
			footerComposite.syncEntityToWidgets();
			linkComposite.syncEntityToWidgets();
			styleComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == pageLayoutModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (pageLayout != null) {
					pageLayout = pageLayoutModel.getPageLayout(pageLayout.getId());
					if (pageLayout != null) {
						setEntity(pageLayout);
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
		if (pageLayout != null && pageLayout.getId() != null) {
			pageLayoutModel.refresh( pageLayout.getId() );
			PageLayoutFileModel.getInstance().refreshForeignKey( pageLayout.getId() );


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				pageLayout = pageLayoutModel.getPageLayout(pageLayout.getId());
				if (pageLayout != null) {
					setEntity(pageLayout);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return pageLayout.getId() == null;
	}


	@Override
	protected String getName() {
		if (isNew()) {
			return I18N.PageLayoutEditor_NewName;
		}
		else {
			String name = pageLayout.getName();
			return StringHelper.avoidNull(name);
		}
	}



	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long portalPK = pageLayout.getPortalId();
			Portal portal = PortalModel.getInstance().getPortal(portalPK);

			StringBuilder toolTipText = new StringBuilder();
			toolTipText.append(I18N.PageLayoutEditor_DefaultToolTip);

			if (pageLayout.getName() != null) {
				toolTipText.append('\n');
				toolTipText.append(KernelLabel.Name.getString());
				toolTipText.append(": ");
				toolTipText.append(pageLayout.getName());
			}

			toolTipText.append('\n');
			toolTipText.append(PortalI18N.Portal.getString());
			toolTipText.append(": ");
			toolTipText.append(portal.getName());

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
	 * When the infoButton is pressed, this method opens a small dialog with some informational data.
	 */
	@Override
	protected void openInfoDialog() {
		// labels of the info dialog
		List<String> labelList = new ArrayList<>();
		labelList.add(UtilI18N.ID);
		labelList.add(UtilI18N.CreateDateTime);
		labelList.add(UtilI18N.CreateUser);
		labelList.add(UtilI18N.EditDateTime);
		labelList.add(UtilI18N.EditUser);
		labelList.add(PageLayout.STANDARD.getString());


		// values of the info dialog
		List<String> valueList = new ArrayList<>();
		valueList.add( StringHelper.avoidNull(pageLayout.getId()) );
		valueList.add( pageLayout.getNewTime().getString() );
		valueList.add( pageLayout.getNewDisplayUserStr() );
		valueList.add( pageLayout.getEditTime().getString() );
		valueList.add( pageLayout.getEditDisplayUserStr() );
		valueList.add( pageLayout.isStandard() ? UtilI18N.Yes : UtilI18N.No );


		// show info dialog
		EditorInfoDialog infoDialog = new EditorInfoDialog(
			getSite().getShell(),
			getTypeName() + ": " + UtilI18N.Info,
			labelList.toArray( new String[labelList.size()] ),
			valueList.toArray( new String[valueList.size()] )
		);

		//infoDialog.setSize(new Point(400, 150));

		infoDialog.open();
	}

}

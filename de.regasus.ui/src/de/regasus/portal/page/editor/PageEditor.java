package de.regasus.portal.page.editor;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.i18n.I18NHtmlEditor;

import de.regasus.I18N;
import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.EditorInfoDialog;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.portal.Page;
import de.regasus.portal.PageModel;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalModel;
import de.regasus.ui.Activator;


public class PageEditor
extends AbstractEditor<PageEditorInput>
implements IRefreshableEditorPart, CacheModelListener<Long> {

	public static final String ID = "PageEditor";

	// the entity: Page
	private Page page;

	private Long portalPK;
	private List<Language>languageList;

	// the model
	private PageModel pageModel;


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private PageContentComposite contentComposite;
	private I18NHtmlEditor articleHeaderEditor;
	private I18NHtmlEditor articleDescriptionEditor;
	private I18NHtmlEditor asideEditor;
	private PageLinkListComposite linkComposite;

	// *
	// * Widgets
	// **************************************************************************

	// ******************************************************************************************
	// * Overwritten EditorPart methods

	@Override
	protected void init() throws Exception {
		// handle EditorInput
		Long pageId = editorInput.getKey();

		// get models
		pageModel = PageModel.getInstance();

		// get entity
		page = pageModel.getPage(pageId);


		// register at model
		pageModel.addListener(this, pageId);

		// determine languageList from Portal
		portalPK = page.getPortalId();
		Portal portal = PortalModel.getInstance().getPortal(portalPK);
		List<String> languageIds = portal.getLanguageList();
		languageList = LanguageModel.getInstance().getLanguages(languageIds);
	}


	@Override
	public void dispose() {
		if (pageModel != null && page.getId() != null) {
			try {
				pageModel.removeListener(this, page.getId());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		super.dispose();
	}


	@Override
	protected String getTypeName() {
		return PortalI18N.Page.getString();
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

			// Content Tab
			{
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(I18N.PageEditor_ContentTabName);
				contentComposite = new PageContentComposite(tabFolder, SWT.NONE, portalPK, languageList);
				tabItem.setControl(contentComposite);
			}

			// Article Header Tab
			{
    			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    			tabItem.setText(I18N.PageEditor_ArticleHeader);
    			articleHeaderEditor = new I18NHtmlEditor(tabFolder, SWT.BORDER, languageList);
    			tabItem.setControl(articleHeaderEditor);
			}

			// ArticleDescription Tab
			{
    			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    			tabItem.setText(I18N.PageEditor_ArticleDescription);
    			articleDescriptionEditor = new I18NHtmlEditor(tabFolder, SWT.BORDER, languageList);
    			tabItem.setControl(articleDescriptionEditor);
			}

			// Aside Tab
			{
    			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    			tabItem.setText( Page.ASIDE.getString() );
    			asideEditor = new I18NHtmlEditor(tabFolder, SWT.BORDER, languageList);
    			tabItem.setControl(asideEditor);
			}

			// Link Tab
			{
    			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    			tabItem.setText(I18N.PageEditor_LinkTabName);
    			linkComposite = new PageLinkListComposite(tabFolder, SWT.NONE, languageList);
    			tabItem.setControl(linkComposite);
			}


			// set data
			setEntity(page);

			// after sync add this as ModifyListener to all widgets and groups
			contentComposite.addModifyListener(this);
			articleHeaderEditor.addModifyListener(this);
			articleDescriptionEditor.addModifyListener(this);
			asideEditor.addModifyListener(this);
			linkComposite.addModifyListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void setFocus() {
		tabFolder.setFocus();
	}


	protected void setEntity(Page page) {
		if (! isNew() ) {
    		// clone data to avoid impact to cache if save operation fails (see MIRCP-409)
			page = page.clone();
		}

		this.page = page;

		// set entity to other composites
		contentComposite.setPage(page);
		articleHeaderEditor.setLanguageString( page.getArticleHeader() );
		articleDescriptionEditor.setLanguageString( page.getArticleDescription() );
		asideEditor.setLanguageString( page.getAside() );
		linkComposite.setPage(page);

		syncWidgetsToEntity();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 2);

			/*
			 * Synchronize entity with the widgets. The data of the widgets is copied to the entity.
			 */
			syncEntityToWidgets();
			monitor.worked(1);

			/* Save the entity.
			 * On success setEntity will be called indirectly in dataChange(),
			 * else an Exception will be thrown.
			 * The result of update() must not be assigned to the entity,
			 * because this will happen in setEntity() and there it may be cloned!
			 * Assigning the entity here would overwrite the cloned value with
			 * the one from the model. Therefore we would have inconsistent data!
			 */
			pageModel.update(page);

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
		if (page != null) {
			syncExecInParentDisplay(new Runnable() {
				@Override
				public void run() {
					try {
						// set editor title
						setPartName( getName() );
						firePropertyChange(PROP_TITLE);

						// refresh the EditorInput
						editorInput.setName( getName() );
						editorInput.setToolTipText( getToolTipText() );

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
		if (page != null) {
			contentComposite.syncEntityToWidgets();

			page.setArticleHeader( articleHeaderEditor.getLanguageString() );
			page.setArticleDescription( articleDescriptionEditor.getLanguageString() );
			page.setAside( asideEditor.getLanguageString() );

			linkComposite.syncEntityToWidgets();
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if (event.getSource() == pageModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					closeBecauseDeletion();
				}
				else if (page != null) {
					page = pageModel.getPage( page.getId() );
					if (page != null) {
						setEntity(page);
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
		if (page != null && page.getId() != null) {
			pageModel.refresh(page.getId());


			/* Reload data if the editor is still dirty.
			 * The models only fire events if the data really changed (isSameVersion()).
			 * So if the data has not changed on the server the editor receives no CacheModelEvent
			 * and is still dirty.
			 */
			if (isDirty()) {
				page = pageModel.getPage( page.getId() );
				if (page != null) {
					setEntity(page);
				}
			}
		}
	}


	@Override
	public boolean isNew() {
		return page.getId() == null;
	}


	@Override
	protected String getName() {
		if (isNew()) {
			return I18N.PageEditor_NewName;
		}
		else {
			return page.getName().getString();
		}
	}



	@Override
	protected String getToolTipText() {
		String toolTip = null;
		try {
			Long portalPK = page.getPortalId();
			Portal portal = PortalModel.getInstance().getPortal(portalPK);

			StringBuilder toolTipText = new StringBuilder();
			toolTipText.append(I18N.PageEditor_DefaultToolTip);

			if ( !LanguageString.isEmpty(page.getName()) ) {
				toolTipText.append('\n');
				toolTipText.append(Page.NAME.getString());
				toolTipText.append(": ");
				toolTipText.append( page.getName().getString() );
			}

			if ( !LanguageString.isEmpty(page.getStepTitle()) ) {
				toolTipText.append('\n');
				toolTipText.append(Page.STEP_TITLE.getString());
				toolTipText.append(": ");
				toolTipText.append( page.getStepTitle().getString() );
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
		// the labels of the info dialog
		String[] labels = {
			UtilI18N.ID,
			UtilI18N.CreateDateTime,
			UtilI18N.CreateUser,
			UtilI18N.EditDateTime,
			UtilI18N.EditUser,
			Page.KEY.getString(),
			Page.NAME.getString(),
			Page.STATIC_ACCESS.getString(),
			Page.FIXED_STRUCTURE.getString()
		};

		// the values of the info dialog
		String[] values = {
			StringHelper.avoidNull(page.getId()),
			page.getNewTime().getString(),
			page.getNewDisplayUserStr(),
			page.getEditTime().getString(),
			page.getEditDisplayUserStr(),
			page.getKey(),
			page.getName().getString(),
			page.isStaticAccess() ? UtilI18N.Yes : UtilI18N.No,
			page.isFixedStructure() ? UtilI18N.Yes : UtilI18N.No
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

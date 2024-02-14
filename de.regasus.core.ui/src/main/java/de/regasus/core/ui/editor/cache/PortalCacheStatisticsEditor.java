package de.regasus.core.ui.editor.cache;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.regasus.LookupService;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.portal.PortalCacheStatistics;

public class PortalCacheStatisticsEditor extends AbstractEditor<PortalCacheStatisticsEditorInput> implements IRefreshableEditorPart {

	public static final String ID = "PortalCacheStatisticsEditor";


	// widgets
	private PortalCacheStatisticsComposite portalStatisticsComposite;


	@Override
	protected void init() throws Exception {
	}


	@Override
	protected String getTypeName() {
		return CoreI18N.PortalCacheStatisticsEditor_Name;
	}


	@Override
	protected String getName() {
		return CoreI18N.PortalCacheStatisticsEditor_Name;
	}


	@Override
	protected String getToolTipText() {
		return null;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			portalStatisticsComposite = new PortalCacheStatisticsComposite(parent, SWT.NONE);


			refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	@Override
	public void refresh() {
		try {
			PortalCacheStatistics portalCacheStatistics = LookupService.getPortalMgr().getPortalCacheStatistics();
			portalStatisticsComposite.setCacheStatistics(portalCacheStatistics);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public boolean isNew() {
		return false;
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
	}

}

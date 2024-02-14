package de.regasus.core.ui.editor.cache;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.regasus.LookupService;
import de.regasus.common.FileCacheStatistics;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.core.ui.editor.IRefreshableEditorPart;

public class FileCacheStatisticsEditor extends AbstractEditor<FileCacheStatisticsEditorInput> implements IRefreshableEditorPart {

	public static final String ID = "FileCacheStatisticsEditor";


	// widgets
	private FileCacheStatisticsComposite fileStatisticsComposite;


	@Override
	protected void init() throws Exception {
	}


	@Override
	protected String getTypeName() {
		return CoreI18N.FileCacheStatisticsEditor_Name;
	}


	@Override
	protected String getName() {
		return CoreI18N.FileCacheStatisticsEditor_Name;
	}


	@Override
	protected String getToolTipText() {
		return null;
	}


	@Override
	protected void createWidgets(Composite parent) {
		try {
			this.parent = parent;

			fileStatisticsComposite = new FileCacheStatisticsComposite(parent, SWT.NONE);


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
			FileCacheStatistics fileCacheStatistics = LookupService.getFileMgr().getFileCacheStatistics();
			fileStatisticsComposite.setCacheStatistics(fileCacheStatistics);
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

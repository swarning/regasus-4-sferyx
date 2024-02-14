package de.regasus.report.view;

import static de.regasus.LookupService.getReportMgr;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.xml.XMLContainer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportListModel;

/**
 * This action can be executed either by the "Generate"-button in the UserReportEditor, or
 * by the common mechanism of ViewActions in the toolbar of the UserReportTreeView.
 *
 * @author manfred
 *
 */
public class GenerateReportAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener, CacheModelListener<Long> {

	public static final String ID = "com.lambdalogic.mi.reporting.ui.action.GenerateReportAction";

	private final IWorkbenchWindow window;
	//private IStructuredSelection selection;
	private UserReportVO currentSelectedUserReportVO;

	// Models
	private UserReportListModel userReportListModel = UserReportListModel.getInstance();


	public GenerateReportAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(ReportI18N.GenerateReportAction_Text);
		setToolTipText(ReportI18N.GenerateReportAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.report.ui.Activator.PLUGIN_ID,
			de.regasus.report.IImageKeys.GENERATE_REPORT
		));

		if (window != null) {
			window.getSelectionService().addSelectionListener(this);
		}
		userReportListModel.addListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
		userReportListModel.removeListener(this);
	}


	@Override
	public void run() {
		try {
			if (currentSelectedUserReportVO != null && currentSelectedUserReportVO.getID() != null) {
				XMLContainer reportRequest = currentSelectedUserReportVO.getXMLRequest();
				generateReport(reportRequest, null);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	/**
	 * @param template is null if the template to be used is the one on the server
	 */
	public static void generateReport(final XMLContainer reportRequest, final DocumentContainer template) {
		try {
			BusyCursorHelper.busyCursorWhile(new Runnable() {

				@Override
				public void run() {
					try {
						DocumentContainer document = null;

						if (template != null && template.getContent() != null) {
							document = getReportMgr().generateReportDocument(reportRequest, template);
						}
						else {
							document = getReportMgr().generateReportDocument(reportRequest);
						}

						if (document != null) {
							try {
								/* save and open generated report file
								 * This code is referenced by
								 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
								 * Adapt the wiki document if this code is moved to another class or method.
								 */
								File file = document.open();
								System.out.println("Saved report to " + file.getAbsolutePath());
							}
							catch (Exception e) {
								com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);

								save(document);
							}
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}

			});
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, GenerateReportAction.class.getName(), t);
		}
	}


	private static void save(final DocumentContainer document) {
		final String[] fileName = new String[1];

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setText(ReportI18N.GenerateReportAction_SaveReport);

				String extension = "*." + document.getFilePostfix();
				dialog.setFilterExtensions(new String[] {extension});

				// init dialog with file name
				String initialFileName = document.getFileName();
				if (initialFileName != null) {
					dialog.setFileName(initialFileName);
				}

				// Dialog öffnen
				fileName[0] = dialog.open();
			}
		});


		// if user choosed a file
		if (fileName[0] != null) {
			BusyCursorHelper.busyCursorWhile(new Runnable() {

				@Override
				public void run() {
					try {
		        		// save file
		        		File file = new File(fileName[0]);
		        		try {
							FileHelper.writeFile(file, document.getContent());
						}
		        		catch (IOException e) {
		    				RegasusErrorHandler.handleApplicationError(
		    					Activator.PLUGIN_ID,
		    					getClass().getName(),
		    					e,
		    					ReportI18N.GenerateReportAction_FileCouldNotBeSaved
		    				);
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}

			});
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		boolean enable = false;
		if (incoming instanceof IStructuredSelection) {
			final IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				final Object selectedElement = selection.getFirstElement();
				if (selectedElement instanceof UserReportVO) {
					currentSelectedUserReportVO = (UserReportVO) selectedElement;
					enable = currentSelectedUserReportVO.isComplete();
				}
			}
		}
		setEnabled(enable);
	}


	@Override
	public void dataChange(final CacheModelEvent<Long> event) {
		/* Auf Änderungen an UserReports reagieren:
		 * Wenn der selektierte UserReport geändert wird, überprüfen, ob
		 * dieser generiert werden kann und die Action entsprechend
		 * aktivieren/deaktivieren.
		 * Ein UserReport kann generiert werden, wenn alle notwendigen
		 * Parameter vorhandenen sind. UserReportVO.isComplete() liefert
		 * dann true.
		 */
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (event.getSource() == userReportListModel) {
						if (event.getOperation() == CacheModelOperation.UPDATE) {
							if (currentSelectedUserReportVO != null) {
								Long currentSelectedUserReportPK = currentSelectedUserReportVO.getID();
								if (currentSelectedUserReportPK != null) {
									for (Long userReportPK : event.getKeyList()) {
										if (currentSelectedUserReportPK.equals(userReportPK)) {
											UserReportVO userReportVO = userReportListModel.getUserReportVO(userReportPK);
											setEnabled(userReportVO.isComplete());
											break;
										}
									}
								}
							}
						}
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}

}

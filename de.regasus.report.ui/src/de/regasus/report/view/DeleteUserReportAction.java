package de.regasus.report.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.report.ReportI18N;
import de.regasus.report.editor.UserReportEditor;
import de.regasus.report.editor.UserReportEditorInput;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.model.UserReportListModel;

public class DeleteUserReportAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.reporting.ui.action.DeleteUserReportAction"; 

	private UserReportTreeView userReportTreeView;
	private final IWorkbenchWindow window;
	private Object selectedElement;
	
	
	
	public DeleteUserReportAction(IWorkbenchWindow window, UserReportTreeView userReportTreeView) {
		super();
		this.window = window;
		this.userReportTreeView = userReportTreeView;
		setId(ID);
		setText(ReportI18N.DeleteUserReportAction_DeleteUserReportText);
		setToolTipText(ReportI18N.DeleteUserReportAction_DeleteUserReportToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.report.ui.Activator.PLUGIN_ID, 
			de.regasus.report.IImageKeys.DELETE_REPORT
		));
	
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	public void run() {
		if (selectedElement instanceof UserReportVO) {
			final UserReportVO userReportVO = (UserReportVO) selectedElement;
			if (userReportVO != null) {
				// Auftragsbestätigung
				final String language = Locale.getDefault().getLanguage();
				final String title = ReportI18N.DeleteUserReportConfirmation_Title;
				String message = ReportI18N.DeleteUserReportConfirmation_Message;
				// Im Abfragetext den Namen des zu löschenden Reports einfügen
				final String name = userReportVO.getName().getString(language);
				message = message.replaceFirst("<name>", name); 
				// Open the Dialog
				final boolean deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
				
				// If the user answered 'Yes' in the dialog
				if (deleteOK) {
					BusyCursorHelper.busyCursorWhile(new Runnable() {
						
						public void run() {
							try {
								// UserReport löschen
								UserReportListModel.getInstance().delete(userReportVO);
							}
							catch (Throwable t) {
								String msg = ReportI18N.DeleteUserReportErrorMessage;
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
								// Wenn beim Löschen ein Fehler auftritt abbrechen, damit die Editoren nicht geschlossen werden.
								return;
							}
							
							// Editor des gelöschten UserReports schließen
							UserReportEditor.closeEditor(userReportVO.getID());
						}
						
					});
				}
			}			
		}
		else if (selectedElement instanceof UserReportDirVO && userReportTreeView != null) {
			final UserReportDirVO userReportDirVO = (UserReportDirVO) selectedElement;
			if (userReportDirVO != null && userReportDirVO.getParentID() != null) {
				// Auftragsbestätigung
				final String title = ReportI18N.DeleteUserReportDirConfirmation_Title;
				String message = ReportI18N.DeleteUserReportDirConfirmation_Message;
				// Im Abfragetext den Namen des zu löschenden Reports einfügen
				final String name = userReportDirVO.getName();
				message = message.replaceFirst("<name>", name); 
				// If the user answered 'Yes' in the dialog
				final boolean deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
				
				// If the user answered 'Yes' in the dialog
				if (deleteOK) {
					BusyCursorHelper.busyCursorWhile(new Runnable() {
						
						public void run() {
							try {
								// UserReportDir löschen
								UserReportDirListModel.getInstance().delete(userReportDirVO);
							}
							catch (Throwable t) {
								String msg = ReportI18N.DeleteUserReportDirErrorMessage;
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
								// Wenn beim Löschen ein Fehler auftritt abbrechen, damit die Editoren nicht geschlossen werden.
								return;
							}
							
							// Elternverzeichnis im Baum selektieren
							try {
								Long parentPK = userReportDirVO.getParentID();
								if (parentPK != null) {
									UserReportDirVO parentVO =
										UserReportDirListModel.getInstance().getUserReportDirVO(parentPK);
									if (parentVO != null) {
										userReportTreeView.show(parentVO);
									}
								}
							}
							catch (Throwable t) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
							}
							
							// verwaiste Editoren suchen und schließen
							closeOrphanedEditors();
						}
						
					});
				}
			}			
		}
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		boolean enable = false;
		selectedElement = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				selectedElement = selection.getFirstElement();
				if (selectedElement instanceof UserReportVO) {
					enable = true;
					setText(ReportI18N.DeleteUserReportAction_DeleteUserReportText);
					setToolTipText(ReportI18N.DeleteUserReportAction_DeleteUserReportToolTip);
					setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						de.regasus.report.ui.Activator.PLUGIN_ID, 
						de.regasus.report.IImageKeys.DELETE_REPORT
					));					
				}
				else if (selectedElement instanceof UserReportDirVO) {
					final UserReportDirVO userReportDirVO = (UserReportDirVO) selectedElement;
					// Deaktivieren, wenn Selektion im TableView stattfand oder der Wurzelknoten selektiert wurde.
					enable = userReportTreeView != null && userReportDirVO.getParentID() != null;
					setText(ReportI18N.DeleteUserReportAction_DeleteDirText);
					setToolTipText(ReportI18N.DeleteUserReportAction_DeleteDirToolTip);
					setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						de.regasus.report.ui.Activator.PLUGIN_ID, 
						de.regasus.report.IImageKeys.DELETE_DIRECTORY
					));					
				}
				else {
					selectedElement = null;
				}
			}
		}
		setEnabled(enable);
	}

	
	/**
	 * Closes all UserReportEditors which have no data in the UserReportListModel.
	 */
	private void closeOrphanedEditors() {
		try {
			Collection<UserReportVO> userReportVOs = UserReportListModel.getInstance().getAllUserReportVOs();
			
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				final IWorkbenchPage[] pages = windows[i].getPages();
				for (int j = 0; j < pages.length; j++) {
					final IWorkbenchPage page = pages[j];
					IEditorReference[] editorRefs = page.getEditorReferences();
					
					final List<IEditorPart> editorsToClose = new ArrayList<IEditorPart>();
					
					for (int k = 0; k < editorRefs.length; k++) {
						IEditorPart editor = editorRefs[k].getEditor(false /*restore*/);
						IEditorInput input = editor.getEditorInput();
						if (input instanceof UserReportEditorInput) {
							UserReportEditorInput userReportEditorInput = (UserReportEditorInput) input;
							Long userReportPK = userReportEditorInput.getKey();
							if (userReportPK != null) {
								boolean exist = false;	
								for (UserReportVO userReportVO : userReportVOs) {
									if (userReportPK.equals(userReportVO.getID())) {
										exist = true;
										break;
									}
								}
								if ( ! exist) {
									editorsToClose.add(editor);
								}
							}
						}
					}
					
					if (!editorsToClose.isEmpty()) {
						SWTHelper.asyncExecDisplayThread(new Runnable() {
							public void run() {
								for (IEditorPart editor : editorsToClose) {
									page.closeEditor(editor, false /*save*/);	
								}
							}
						});
					}
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}
}

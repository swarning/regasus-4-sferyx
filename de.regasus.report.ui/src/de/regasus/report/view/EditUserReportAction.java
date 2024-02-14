package de.regasus.report.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.report.ReportI18N;
import de.regasus.report.editor.UserReportEditor;
import de.regasus.report.editor.UserReportEditorInput;

public class EditUserReportAction
extends Action 
implements ActionFactory.IWorkbenchAction, ISelectionListener {
	
	public static final String ID = "com.lambdalogic.mi.reporting.ui.action.EditUserReportAction"; 
	
	private UserReportTreeView userReportTreeView;
	private final IWorkbenchWindow window;
	private Object selectedElement;
	
	
	public EditUserReportAction(IWorkbenchWindow window, UserReportTreeView userReportTreeView) {
		super();
		this.window = window;
		this.userReportTreeView = userReportTreeView;
		setId(ID);
		setText(ReportI18N.EditUserReportAction_TextUserReport);
		setToolTipText(ReportI18N.EditUserReportAction_ToolTipUserReport);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.report.ui.Activator.PLUGIN_ID, 
			de.regasus.report.IImageKeys.EDIT_REPORT
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	
	public void run() {
		if (selectedElement instanceof UserReportVO) {
			UserReportVO userReportVO = (UserReportVO) selectedElement;
			if (userReportVO != null) {
				try {
					userReportVO = userReportVO.clone();
					UserReportEditorInput editorInput = UserReportEditorInput.getEditInstance(userReportVO.getID());
					IWorkbenchPage page = window.getActivePage();
					page.openEditor(editorInput, UserReportEditor.ID);
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), t, ReportI18N.EditUserReportAction_Error);
				}
			}
			else {
				System.out.println("EditPersonAction.run(): userReportVO is null."); 
			}
		}
		else if (selectedElement instanceof UserReportDirVO && userReportTreeView != null) {
			// The generic root directory must not be editable!
			final UserReportDirVO userReportDirVO = (UserReportDirVO) selectedElement;
			if (userReportDirVO.getParentID() != null) {
				userReportTreeView.renameUserReportDirVOTreeNode();
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
					setText(ReportI18N.EditUserReportAction_TextUserReport);
					setToolTipText(ReportI18N.EditUserReportAction_ToolTipUserReport);
					setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						de.regasus.report.ui.Activator.PLUGIN_ID, 
						de.regasus.report.IImageKeys.EDIT_REPORT
					));					
				}
				else if (selectedElement instanceof UserReportDirVO) {
					// The generic root directory must not be editable!
					final UserReportDirVO userReportDirVO = (UserReportDirVO) selectedElement;
					enable = userReportTreeView != null && userReportDirVO.getParentID() != null;
					setText(ReportI18N.EditUserReportAction_TextUserReportDir);
					setToolTipText(ReportI18N.EditUserReportAction_ToolTipUserReportDir);
					setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						de.regasus.report.ui.Activator.PLUGIN_ID, 
						de.regasus.report.IImageKeys.EDIT_DIRECTORY
					));
				}
				else {
					selectedElement = null;
				}
			}
		}
		setEnabled(enable);
	}

}

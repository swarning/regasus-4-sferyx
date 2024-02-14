package de.regasus.report.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.report.ReportI18N;
import de.regasus.report.editor.UserReportEditor;
import de.regasus.report.editor.UserReportEditorInput;

public class CreateUserReportAction 
extends Action 
implements ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.reporting.ui.action.CreateUserReportAction"; 

	private final IWorkbenchWindow window;
	private Object selectedElement;
	
	
	public CreateUserReportAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(ReportI18N.CreateUserReportAction_Text);
		setToolTipText(ReportI18N.CreateUserReportAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.report.ui.Activator.PLUGIN_ID, 
			de.regasus.report.IImageKeys.CREATE_REPORT
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	

	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	
	public void run() {
		// Set new UserReports directory
		if (selectedElement != null) {
			Long userReportDirPK = null;
			// if current selection is a directory
			if (selectedElement instanceof UserReportDirVO) {
				// set the selected directory
				final UserReportDirVO userReportDirVO = (UserReportDirVO) selectedElement;
				userReportDirPK = userReportDirVO.getID();
			}
			// if current selection is a UserReport
			else if (selectedElement instanceof UserReportVO) {
				// set the same directory
				final UserReportVO selectedUserReportVO = (UserReportVO) selectedElement;
				userReportDirPK = selectedUserReportVO.getUserReportDirID();
			}
			
			IWorkbenchPage page = window.getActivePage();
			UserReportEditorInput editorInput = UserReportEditorInput.getCreateInstance(userReportDirPK);
			try {
				page.openEditor(editorInput, UserReportEditor.ID);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e, ReportI18N.CreateUserReportAction_Error);
			}
		}
	}

	
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedElement = null;
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;
			if (selection.size() == 1) {
				selectedElement = selection.getFirstElement();
				if ( ! (selectedElement instanceof UserReportDirVO || selectedElement instanceof UserReportVO)) {
					selectedElement = null;
				}
			}
		}
		setEnabled(selectedElement != null);
	}

}

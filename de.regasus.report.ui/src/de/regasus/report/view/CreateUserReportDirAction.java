package de.regasus.report.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportDirListModel;

public class CreateUserReportDirAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.reporting.ui.action.CreateUserReportDirAction"; 

	private final UserReportTreeView userReportTreeView;
	private final IWorkbenchWindow window;
	private Object selectedElement;
	
	
	public CreateUserReportDirAction(UserReportTreeView userReportTreeView) {
		super();
		this.userReportTreeView = userReportTreeView;
		this.window = userReportTreeView.getSite().getWorkbenchWindow();
		setId(ID);
		setText(ReportI18N.CreateUserReportDirAction_Text);
		setToolTipText(ReportI18N.CreateUserReportDirAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			de.regasus.report.ui.Activator.PLUGIN_ID, 
			de.regasus.report.IImageKeys.CREATE_DIRECTORY
		));
		
		window.getSelectionService().addSelectionListener(this);
	}
	

	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	
	public void run() {
		// Set new UserReports directory
		if (selectedElement != null) {
			final UserReportDirVO newUserReportDirVO = new UserReportDirVO();
			
			// if current selection is a directory
			if (selectedElement instanceof UserReportDirVO) {
				// set the selected directory
				final UserReportDirVO selectedUserReportDirVO = (UserReportDirVO) selectedElement;
				newUserReportDirVO.setParentID(selectedUserReportDirVO.getID());
			}
			// if current selection is a UserReport
			else if (selectedElement instanceof UserReportVO) {
				// set the same directory
				final UserReportVO selectedUserReportVO = (UserReportVO) selectedElement;
				newUserReportDirVO.setParentID(selectedUserReportVO.getUserReportDirID());
			}
			
			newUserReportDirVO.setName(ReportI18N.CreateUserReportDirAction_DefaultFolderName);

			final UserReportDirVO[] result = new UserReportDirVO[1];
			try {
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					public void run() {
						try {
							result[0] = UserReportDirListModel.getInstance().create(newUserReportDirVO);
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, ReportI18N.CreateUserReportDirErrorMessage);
						}
					}
				});
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}

			if (result[0] != null) {
				userReportTreeView.show(result[0]);
				userReportTreeView.renameUserReportDirVOTreeNode();
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

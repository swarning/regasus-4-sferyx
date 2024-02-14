package de.regasus.report.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportListModel;
import de.regasus.report.ui.Activator;

/**
 * Action for pasting a Node from the clipboard into a TreeViewer.
 */
@SuppressWarnings("restriction")
public class PasteUserReportAction extends Action {

	protected StructuredViewer viewer;


	public PasteUserReportAction(StructuredViewer viewer) {
		this.viewer = viewer;

		setText(WorkbenchMessages.Workbench_paste);
		setToolTipText(WorkbenchMessages.Workbench_pasteToolTip);

		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
	}


	@Override
	public void run() {
		Clipboard clipboard = new Clipboard( Display.getDefault() );
		try {
			// determine source UserReportPK from clipboard
			ReportTreeTransferContainer container =
				(ReportTreeTransferContainer) clipboard.getContents(ReportTreeTransfer.getInstance());
			if (container == null) {
				return;
			}
			Long sourceUserReportPK = container.getId();

			// determine target UserReportDir from current selection
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			Object target = selection.getFirstElement();
			Long targetUserReportDirPK = null;
			if (target instanceof UserReportDirVO) {
				targetUserReportDirPK = ((UserReportDirVO) target).getID();
			}
			else if (target instanceof UserReportVO) {
				targetUserReportDirPK = ((UserReportVO) target).getUserReportDirID();
			}
			else {
				return;
			}

			// copy source UserReport to target UserReportDir
			UserReportListModel.getInstance().copyUserReport(sourceUserReportPK, targetUserReportDirPK);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, ReportI18N.CopyUserReportErrorMessage);
		}
		finally {
			clipboard.dispose();
		}
	}

}

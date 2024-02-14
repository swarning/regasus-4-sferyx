package de.regasus.report.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.util.rcp.ClipboardHelper;

/**
 * Action for copying the selected UserReport node to the clipboard.
 */
@SuppressWarnings("restriction")
public class CopyUserReportAction extends Action {

	protected StructuredViewer viewer;


	public CopyUserReportAction(StructuredViewer viewer, IWorkbenchWindow window) {
		this.viewer = viewer;

		setText(WorkbenchMessages.Workbench_copy);
		setToolTipText(WorkbenchMessages.Workbench_copyToolTip);

		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
	}


	/**
	 * The currently selected node is put with two different data formats (Report, Text) into the system clipboard
	 */
	@Override
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();

		if ( ! selection.isEmpty()) {
			Object selectedElement = selection.getFirstElement();

			if (selectedElement instanceof UserReportVO) {
				// put ReportTreeTransfer and info text to clipboard
				UserReportVO userReportVO = (UserReportVO) selectedElement;

				long id = userReportVO.getID();
				String className = userReportVO.getClass().getName();

				ReportTreeTransferContainer container = new ReportTreeTransferContainer(id, className);

				Clipboard clipboard = new Clipboard( Display.getDefault() );
				try {
    				clipboard.setContents(
    					new Object[] {
    						container,
    						userReportVO.getCopyInfo()
    					},
    					new Transfer[] {
    						ReportTreeTransfer.getInstance(),
    						TextTransfer.getInstance()
    					}
    				);
				}
				finally {
					clipboard.dispose();
				}
			}
			else if (selectedElement instanceof UserReportDirVO) {
				// put only info text to clipboard
				UserReportDirVO userReportDirVO = (UserReportDirVO) selectedElement;
				ClipboardHelper.copyToClipboard( userReportDirVO.getCopyInfo() );
			}
		}
	}

}

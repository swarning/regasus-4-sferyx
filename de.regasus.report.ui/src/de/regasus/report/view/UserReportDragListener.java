package de.regasus.report.view;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

public class UserReportDragListener extends DragSourceAdapter {

	private StructuredViewer viewer;

	public UserReportDragListener(StructuredViewer viewer) {
		this.viewer = viewer;
	}


	/**
	 * Everything in the UserReportTree may be dragged, except the root. 
	 * 
	 * The distinction between move and copy has to wait until the user lets the mouse go.
	 */
	public void dragStart(DragSourceEvent event) {
		Object selectedNode = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		
		if (selectedNode instanceof UserReportDirVO) {
			UserReportDirVO dir = (UserReportDirVO) selectedNode;
			boolean isRoot = (dir.getParentID() == null); 
			event.doit = ! isRoot;
		}
		else {
			event.doit = true;
		}
	}


	/**
	 * When the dragging user lets the mouse go, the receiving application tells what
	 * data type transfers it understands. Depending on which, either the complete
	 * ReportTransferContainer is transmittet, or only a string. (In contrast to the
	 * copy actions, which always copies both types, because at the time of copying it is
	 * not known where the data should be pasted).
	 */
	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Object firstElement = selection.getFirstElement();

		if (ReportTreeTransfer.getInstance().isSupportedType(event.dataType)) {
			long id = 0;
			String className = null;

			if (firstElement instanceof UserReportVO) {
				UserReportVO userReportVO = (UserReportVO) firstElement;
				id = userReportVO.getID();
				className = userReportVO.getClass().getName();
			}
			else if (firstElement instanceof UserReportDirVO) {
				UserReportDirVO userReportDirVO = (UserReportDirVO) firstElement;
				id = userReportDirVO.getID();
				className = userReportDirVO.getClass().getName();
			}

			event.data = new ReportTreeTransferContainer(id, className);
		}
		else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			if (firstElement instanceof UserReportVO) {
				UserReportVO userReportVO = (UserReportVO) firstElement;
				event.data = userReportVO.getName().getString();
			}
			else if (firstElement instanceof UserReportDirVO) {
				UserReportDirVO userReportDirVO = (UserReportDirVO) firstElement;
				event.data = userReportDirVO.getName();
			}
		}

	}


	
	public void dragFinished(DragSourceEvent event) {
		if (event.doit) {
			viewer.refresh();
		}
	}

}

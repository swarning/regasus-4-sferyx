package de.regasus.report.view;

import java.util.logging.Logger;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

import com.lambdalogic.messeinfo.exception.DirtyWriteException;
import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.model.UserReportListModel;
import de.regasus.report.ui.Activator;

public class UserReportTreeDropAdapter extends ViewerDropAdapter {
	private static Logger log = Logger.getLogger("ui.UserReportTreeDropAdapter"); 
	
	public UserReportTreeDropAdapter(TreeViewer viewer) {
		super(viewer);
		setScrollExpandEnabled(true);
	}

	/**
	 * Method declared on ViewerDropAdapter
	 */
	public boolean validateDrop(Object target, int op, TransferData type) {
		return ReportTreeTransfer.getInstance().isSupportedType(type);
	}

	/**
	 * Method declared on ViewerDropAdapter
	 */
	public boolean performDrop(Object data) {
		log.info("performDrop"); 
		try {
			if (data instanceof ReportTreeTransferContainer) {
				ReportTreeTransferContainer reportTreeTransferContainer = (ReportTreeTransferContainer) data;

				// dropTarget bestimmen
				Object objTarget = getCurrentTarget();
				Long targetUserReportDirPK = null;
				if (objTarget == null) {
					targetUserReportDirPK = UserReportDirListModel.getInstance().getVisibleRoot().getID();
				}
				else if (objTarget instanceof UserReportVO) {
					UserReportVO targetUserReportVO = (UserReportVO) objTarget;
					targetUserReportDirPK = targetUserReportVO.getUserReportDirID();
				}
				else if (objTarget instanceof UserReportDirVO) {
					targetUserReportDirPK = ((UserReportDirVO) objTarget).getID();
				}
				else {
					return false;
				}
				
				log.info("dropTarget=UserReportDirVO(" + targetUserReportDirPK + ")");  //$NON-NLS-2$

				int currentOperation = getCurrentOperation();
				
				// dropSource bestimmen und Operation durchführen
				if (UserReportVO.class.getName().equals(reportTreeTransferContainer.getClassName())) {
					if (currentOperation == DND.DROP_MOVE) {
						Long sourceUserReportPK = reportTreeTransferContainer.getId();
						
						try {
							UserReportListModel.getInstance().moveUserReportToDir(
								sourceUserReportPK,
								targetUserReportDirPK
							);
						}
						catch (DirtyWriteException e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, ReportI18N.MoveUserReportDirtyWriteMessage);
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, ReportI18N.MoveUserReportErrorMessage);
						}
					}
					else if (currentOperation == DND.DROP_COPY) {
						
						/*
						 *  Copy shall take place directly on the server, so that the templates
						 *  don't need to be sent back and forth. 
						 */
						
						Long sourceUserReportPK = reportTreeTransferContainer.getId();

						try {
							UserReportListModel.getInstance().copyUserReport(sourceUserReportPK, targetUserReportDirPK);
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, ReportI18N.CopyUserReportErrorMessage);
						}
					}
				}
				else if (UserReportDirVO.class.getName().equals(reportTreeTransferContainer.getClassName())) {
					if (currentOperation == DND.DROP_MOVE) {
						Long pk = reportTreeTransferContainer.getId();
						UserReportDirVO sourceUserReportDirVO = UserReportDirListModel.getInstance().getUserReportDirVO(pk);

						// wenn sich das Zielverzeichnis nicht ändert abbrechen
						if (targetUserReportDirPK.equals(sourceUserReportDirVO.getParentID())) {
							return false;
						}

						// wenn sich das verschobenes und Zielverzeichnis gleich sind abbrechen
						if (targetUserReportDirPK.equals(sourceUserReportDirVO.getID())) {
							return false;
						}

						// zu veränderndes Objekt klonen, falls die Änderung fehlschlägt
						sourceUserReportDirVO = sourceUserReportDirVO.clone();

						sourceUserReportDirVO.setParentID(targetUserReportDirPK);
						try {
							UserReportDirListModel.getInstance().update(sourceUserReportDirVO);
						}
						catch (Throwable t) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, ReportI18N.UpdateUserReportDirErrorMessage);
						}
					}
					else {
						// Kopieren von Verzeichnissen wird nicht unterstützt
						return false;
					}
				}
				log.info("dropSource=" + reportTreeTransferContainer); 
			}
		} 
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
//		TreeViewer viewer = (TreeViewer) getViewer();
		
		//cannot drop a UserReport onto itself or a child
//		for (int i = 0; i < toDrop.length; i++) {
//			if (toDrop[i].equals(target) || target.hasParent(toDrop[i])) {
//				return false;
//			}
//		}
//		
//		for (int i = 0; i < toDrop.length; i++) {
//			toDrop[i].setParent(target);
//			viewer.add(target, toDrop[i]);
//			viewer.reveal(toDrop[i]);
//		}
		return true;
	}

    /**
     * Unterdrückt LOCATION_BEFORE und LOCATION_AFTER und gibt stattderer
     * LOCATION_ON zurück.
     */
	@Override
    protected int determineLocation(DropTargetEvent event) {
		int location = super.determineLocation(event);
		if (location == LOCATION_BEFORE || location == LOCATION_AFTER) {
			location = LOCATION_ON;
		}
        return location;
    }

	
}
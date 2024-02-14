package de.regasus.report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;

public class ReportSelectionHelper {

	public static List<UserReportDirVO> getUserReportDirVOs(ISelection selection) {
		List<UserReportDirVO> userReportDirVOs = null;
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			
			userReportDirVOs = new ArrayList<>();
			
			Iterator<?> iterator = sselection.iterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				
				if (o instanceof UserReportDirVO) {
					userReportDirVOs.add((UserReportDirVO) o);
				}
			}
		}
		
		return userReportDirVOs;
	}

	/**
	 * Helper-Method for CommandHandlers to determine the selected User Report Directories  
	 * from the current selection in the UserReportTreeView.
	 */
	public static List<UserReportDirVO> getUserReportDirVOs(ExecutionEvent event) throws Exception {
		// Determine the Participants
		List<UserReportDirVO> userReportDirVOs = null;
		
		// The active part is no ParticipantProvider: Get the selected Participant(s).
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null) {
			userReportDirVOs = getUserReportDirVOs(selection);
		}
		
		return userReportDirVOs;
	}

	
	public static UserReportDirVO getUserReportDirVO(ExecutionEvent event) throws Exception {
		UserReportDirVO userReportDirVO = null;
		List<UserReportDirVO> userReportDirVOs = getUserReportDirVOs(event);
		if (userReportDirVOs != null && userReportDirVOs.size() == 1) {
			userReportDirVO = userReportDirVOs.get(0);
		}
		return userReportDirVO;
	}

}

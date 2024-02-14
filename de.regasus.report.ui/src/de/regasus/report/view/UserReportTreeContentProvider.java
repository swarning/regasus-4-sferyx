package de.regasus.report.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.model.UserReportListModel;
import de.regasus.report.ui.Activator;

public class UserReportTreeContentProvider implements ITreeContentProvider {
//	private static Logger log = Logger.getLogger("ui.UserReportTreeContentProvider");
	
	private UserReportDirListModel userReportDirListModel;
	private UserReportListModel userReportListModel;
	
	
	public UserReportTreeContentProvider() {
		super();
		userReportDirListModel = UserReportDirListModel.getInstance();
		
		userReportListModel = UserReportListModel.getInstance();
	}
	

	@SuppressWarnings("unchecked") 
	public Object[] getChildren(Object element) {
		List result = new ArrayList();
		
		if (element instanceof UserReportDirVO) {
			try {
				Long parentPK = ((UserReportDirVO) element).getID();
				
				// Unterverzeichnisse hinzufügen
				Collection<UserReportDirVO> userReportDirVOs = userReportDirListModel.getAllUserReportDirVOs();
				if (userReportDirVOs != null) {
					for (UserReportDirVO userReportDirVO : userReportDirVOs) {
						if (parentPK == null && userReportDirVO.getParentID() == null || parentPK != null
							&& parentPK.equals(userReportDirVO.getParentID())) {
							result.add(userReportDirVO);
						}
					}
				}
				
				// Wenn Parent nicht die Wurzel ist
				if (parentPK != null) {
					// Reports hinzufügen
					Collection<UserReportVO> userReportVOs = userReportListModel.getAllUserReportVOs();
					if (userReportVOs != null) {
						for (UserReportVO userReportVO : userReportVOs) {
							if (parentPK == null && 
								userReportVO.getUserReportDirID() == null 
								|| 
								parentPK != null && 
								parentPK.equals(userReportVO.getUserReportDirID())
							) {
								result.add(userReportVO);
							}
						}
					}
				}
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
		
		return result.toArray();			
	}


	public Object getParent(Object element) {
		UserReportDirVO result = null;
		Long parentPK = null;
		if (element instanceof UserReportDirVO) {
			parentPK = ((UserReportDirVO) element).getParentID();
		}
		else if (element instanceof UserReportVO) {
			parentPK = ((UserReportVO) element).getUserReportDirID();
		}
		if (parentPK != null) {
			try {
				result = userReportDirListModel.getUserReportDirVO(parentPK);
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
		return result;
	}


	public boolean hasChildren(Object element) {
		boolean result = false;
		
		if (element instanceof UserReportDirVO) {
			try {
				Long parentPK = ((UserReportDirVO) element).getID();
				
				// prüfen, ob Unterverzeichnisse existieren
				Collection<UserReportDirVO> userReportDirVOs = userReportDirListModel.getAllUserReportDirVOs();
				if (userReportDirVOs != null) {
					for (UserReportDirVO userReportDirVO : userReportDirVOs) {
						if (parentPK == null && userReportDirVO.getParentID() == null || parentPK != null
							&& parentPK.equals(userReportDirVO.getParentID())) {
							result = true;
							break;
						}
					}
				}
				
				// Wenn Parent nicht die Wurzel ist
				if (parentPK != null) {
					// prüfen ob Reports existieren
					Collection<UserReportVO> userReportVOs = userReportListModel.getAllUserReportVOs();
					if (userReportVOs != null) {
						for (UserReportVO userReportVO : userReportVOs) {
							if (parentPK == null && 
								userReportVO.getUserReportDirID() == null 
								|| 
								parentPK != null && 
								parentPK.equals(userReportVO.getUserReportDirID())
							) {
								result = true;
								break;
							}
						}
					}
				}
			}
			catch (Throwable t) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
			}
		}
		
		return result;			
	}


	public Object[] getElements(Object element) {
		return getChildren(element);
	}


	public void dispose() {
	}


	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}

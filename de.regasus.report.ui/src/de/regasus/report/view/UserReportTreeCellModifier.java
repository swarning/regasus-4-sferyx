package de.regasus.report.view;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TreeItem;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ReportI18N;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.ui.Activator;

public class UserReportTreeCellModifier implements ICellModifier {
//	private static Logger log = Logger.getLogger("ui.UserReportTreeCellModifier");
	
	private boolean enabled;
	
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
//		log.info("UserReportTreeCellModifier.setEnabled(" + enabled + ")");
		this.enabled = enabled;
	}

	public boolean canModify(Object element, String property) {
		return 
			enabled && 
			(element instanceof UserReportDirVO) &&
			((UserReportDirVO) element).getParentID() != null;
	}

	public Object getValue(Object element, String property) {
		// user report directory: show directory name
		if (element instanceof UserReportDirVO){
			UserReportDirVO userReportDirVO = (UserReportDirVO) element;
			return userReportDirVO.getName(); 
		}
		else{
			return null;
		}
	}

	public void modify(Object element, String property, Object value) {
		TreeItem treeItem = (TreeItem) element;
		if (treeItem.getData() instanceof UserReportDirVO){
			UserReportDirVO userReportDirVO = (UserReportDirVO) treeItem.getData();
			
			String name = null;
			if (value != null) {
				name = value.toString();
			}
			if (name != null) {
				name = name.trim();
				if (name.length() == 0) {
					name = null;
				}
			}
			if (name != null) {
				userReportDirVO.setName(name);
				try {
					UserReportDirListModel.getInstance().update(userReportDirVO);
				}
				catch (Throwable t) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, ReportI18N.UpdateUserReportDirErrorMessage);
				}
				setEnabled(false);
			}
		}
	}

}

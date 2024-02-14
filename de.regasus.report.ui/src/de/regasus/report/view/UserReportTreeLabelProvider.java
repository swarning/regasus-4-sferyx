package de.regasus.report.view;

import java.util.Locale;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;
import com.lambdalogic.messeinfo.report.data.UserReportVO;

import de.regasus.report.IImageKeys;
import de.regasus.report.IconRegistry;


public class UserReportTreeLabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		Image image = null;
		if (element instanceof UserReportDirVO){
			image = IconRegistry.getImage(IImageKeys.DIRECTORY); 
		}
		else if (element instanceof UserReportVO) {
			UserReportVO userReportVO = (UserReportVO) element;
			if (userReportVO.isComplete()) {
				image = IconRegistry.getImage(IImageKeys.REPORT_COMPLETE);
			}
			else {
				image = IconRegistry.getImage(IImageKeys.REPORT_INCOMPLETE);	
			}
		}
		return image;
	}


	public String getText(Object element) {
		// user report directory: show directory name
		if (element instanceof UserReportDirVO){
			UserReportDirVO userReportDirVO = (UserReportDirVO) element;
			return userReportDirVO.getName(); 
		}
		// user report
		else if(element instanceof UserReportVO){
			UserReportVO userReportVO = (UserReportVO) element;
			return userReportVO.getName().getString(Locale.getDefault());
		}
		else{
			return null;
		}
	}


	public void addListener(ILabelProviderListener arg0) {
	}


	public void dispose() {
	}


	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}


	public void removeListener(ILabelProviderListener arg0) {
	}
}

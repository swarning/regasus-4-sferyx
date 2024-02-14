package de.regasus.report;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import de.regasus.report.view.UserReportTreeView;

public class ReportingPerspective implements IPerspectiveFactory {

	public static final String ID = "com.lambdalogic.mi.reporting.ui.ReportingPerspective"; 
	
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		
		IFolderLayout leftLayout = layout.createFolder(
			ID + ".left", 
			IPageLayout.LEFT,
			0.35f,
			layout.getEditorArea()
		);
		leftLayout.addView(UserReportTreeView.ID);
//		leftLayout.addView(UserReportTableView.ID);
	}

}

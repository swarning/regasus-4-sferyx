package de.regasus.portal;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.portal.view.PortalView;
import de.regasus.ui.Activator;

public class PortalPerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.PortalPerspective";


	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		try {
			IFolderLayout leftLayout = layout.createFolder(
				"left",
				IPageLayout.LEFT,
				0.35f,
				layout.getEditorArea()
			);

			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet();

			if (configParameterSet == null || configParameterSet.getPortal().isVisible()) {
				leftLayout.addView(PortalView.ID);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

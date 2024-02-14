package de.regasus.profile;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.customfield.view.ProfileCustomFieldTreeView;
import de.regasus.profile.relationtype.view.ProfileRelationTypeView;
import de.regasus.profile.role.view.ProfileRoleView;
import de.regasus.ui.Activator;

public class ProfileMasterDataPerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.ProfileMasterDataPerspective";


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
			ProfileConfigParameterSet profileConfigParameterSet = null;
			if (configParameterSet != null) {
				profileConfigParameterSet = configParameterSet.getProfile();
			}

			if (profileConfigParameterSet == null || profileConfigParameterSet.isVisible()) {
				if (profileConfigParameterSet == null || profileConfigParameterSet.getProfileRelation().isVisible()) {
					leftLayout.addView(ProfileRelationTypeView.ID);
				}
				if (profileConfigParameterSet == null || profileConfigParameterSet.getProfileRole().isVisible()) {
					leftLayout.addView(ProfileRoleView.ID);
				}
				if (profileConfigParameterSet == null || profileConfigParameterSet.getCustomField().isVisible()) {
					leftLayout.addView(ProfileCustomFieldTreeView.ID);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

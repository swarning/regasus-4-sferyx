package de.regasus.profile;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ProfileConfigParameterSet;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.view.ParticipantToProfileView;
import de.regasus.participant.view.pref.ParticipantToProfileViewPreference;
import de.regasus.profile.relation.view.ProfileRelationView;
import de.regasus.profile.search.ProfileSearchView;
import de.regasus.profile.search.pref.ProfileSearchViewPreference;
import de.regasus.ui.Activator;

public class ProfilePerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.ProfilePerspective";


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
				if (profileConfigParameterSet == null || profileConfigParameterSet.isVisible()) {
					ProfileSearchViewPreference.getInstance().initialize(); // delete previously saved preferences
					leftLayout.addView(ProfileSearchView.ID);
				}
				if (profileConfigParameterSet == null || profileConfigParameterSet.getProfileRelation().isVisible()) {
					leftLayout.addView(ProfileRelationView.ID);
				}
				if (profileConfigParameterSet == null || profileConfigParameterSet.isVisible()) {
					ParticipantToProfileViewPreference.getInstance().initialize(); // delete previously saved preferences
					leftLayout.addView(ParticipantToProfileView.ID);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

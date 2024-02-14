package de.regasus.participant;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventView;
import de.regasus.participant.view.ParticipantSearchView;
import de.regasus.participant.view.ParticipantTreeView;
import de.regasus.participant.view.pref.ParticipantSearchViewPreference;
import de.regasus.participant.view.pref.ParticipantTreeViewPreference;
import de.regasus.ui.Activator;

public class ParticipantPerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.ParticipantPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		try {
			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet();

			if (configParameterSet == null || configParameterSet.getEvent().isVisible()) {
        		layout.addView(
        			EventView.ID,
        			IPageLayout.LEFT,
        			0.35f,
        			layout.getEditorArea()
        		);

        		IFolderLayout leftLayout = layout.createFolder(
        			ID + ".left",
        			IPageLayout.BOTTOM,
        			0.2f,
        			EventView.ID
        		);


        		ParticipantSearchViewPreference.getInstance().initialize(); // delete previously saved preferences
        		leftLayout.addView(ParticipantSearchView.ID);

        		ParticipantTreeViewPreference.getInstance().initialize(); // delete previously saved preferences
				leftLayout.addView(ParticipantTreeView.ID);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

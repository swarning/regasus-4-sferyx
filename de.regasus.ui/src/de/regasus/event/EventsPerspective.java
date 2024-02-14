package de.regasus.event;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;

import de.regasus.common.gatedevice.view.GateDeviceView;
import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventMasterDataView;
import de.regasus.event.view.pref.EventMasterDataViewPreference;
import de.regasus.participant.state.view.ParticipantStateView;
import de.regasus.participant.type.view.ParticipantTypeView;
import de.regasus.programme.programmepointtype.view.ProgrammePointTypeView;
import de.regasus.ui.Activator;

public class EventsPerspective implements IPerspectiveFactory {

	public static final String ID = "de.regasus.EventsPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		try {
			ConfigParameterSet configParameterSet = ConfigParameterSetModel.getInstance().getConfigParameterSet();

			if (configParameterSet == null || configParameterSet.getEvent().isVisible()) {
				IFolderLayout leftLayout = layout.createFolder(
					ID + ".left",
					IPageLayout.LEFT,
					0.4f,
					layout.getEditorArea()
				);

				EventMasterDataViewPreference.getInstance().initialize(); // delete previously saved preferences
				leftLayout.addView(EventMasterDataView.ID);

    			if (configParameterSet == null || configParameterSet.getProgramme().isVisible()) {
    				leftLayout.addView(ProgrammePointTypeView.ID);
    			}

    			if (configParameterSet == null || configParameterSet.getGateDevice().isVisible()) {
					leftLayout.addView(GateDeviceView.ID);
				}

    			leftLayout.addView(ParticipantTypeView.ID);
    			leftLayout.addView(ParticipantStateView.ID);
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

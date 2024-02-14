package de.regasus.app.main;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.error.ErrorHandler;

import de.regasus.core.ui.AbstractApplicationWorkbenchAdvisor;


public class ApplicationWorkbenchAdvisor extends AbstractApplicationWorkbenchAdvisor {

	/**
	 * The VM can be started with the ID of a desired perspective, given as property "initialPerspectiveId"
	 */
	private static final String propertyInitialPerspectiveId = System.getProperty("initialPerspectiveId");

	/**
	 * The Participant perspective is currently the default initial perspective of MI3 when no other
	 * perspective is desired via a property.
	 */
	private static final String participantPerspectiveId  = "de.regasus.ParticipantPerspective";


	/**
	 * When no particular perspective is desired, and the default  initial perspective (the Participant perspective) is
	 * not present in this VM (meaning the plugin or feature was excluded), we try to start with the Perspective
	 * showing the Console view.
	 */
	private static final String debugPerspectiveId = "de.regasus.core.ui.DebugPerspective";


	/**
	 * Returns the ID of the initial Perspective as String.
	 */
	@Override
	public String getInitialWindowPerspectiveId() {
		if (existsPerspective(propertyInitialPerspectiveId) ) {
			// The VM might have been started with the ID of a desired perspective, given as property
			return propertyInitialPerspectiveId;
		}
		else if (existsPerspective(participantPerspectiveId)) {
			// Otherwise try the participant perspective
			return participantPerspectiveId;
		}
		else if (existsPerspective(debugPerspectiveId)){
			// Otherwise try the debug perspective
			return debugPerspectiveId;
		}
		else {
			// Last choice: no perspective
			return null;
		}
	}


	/**
	 * Finds out whether a perspective with the given ID is present in this VM.
	 */
	private boolean existsPerspective(String id) {
		try {
			IPerspectiveDescriptor descriptor = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id);
			if (descriptor != null) {
				return true;
			}
		}
		catch(IllegalStateException e) {
			ErrorHandler.logError(e);
		}
		return false;
	}

}

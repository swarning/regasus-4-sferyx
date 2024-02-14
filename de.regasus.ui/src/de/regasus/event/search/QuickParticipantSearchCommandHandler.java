package de.regasus.event.search;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityView;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantPerspective;
import de.regasus.participant.view.ParticipantSearchView;
import de.regasus.ui.Activator;

/**
 * See: https://lambdalogic.atlassian.net/wiki/spaces/REGASUS/pages/2513076233/Quick+Search
 */
public class QuickParticipantSearchCommandHandler {

	public static final String COMMAND_ID = "de.regasus.ui.command.QuickParticipantSearch";


	@Execute
	public void execute(
		MApplication application,
		EModelService modelService,
		EPartService partService
	) {
		try {
			List<Object> list = modelService.findElements(application, QuickSearchField.ID, Object.class);

			if ( ! list.isEmpty()) {
				// find QuickSearchField
				MContribution toolControlImpl = (MContribution) list.get(0);
				QuickSearchField searchField = (QuickSearchField) toolControlImpl.getObject();

				// read searchValue
				String searchValue = searchField.getText();


				// If there is text in the search field, open participant perspective and let its search view do the work
				if (searchField.hasFocus() && searchValue.length() > 0) {
					// switch to ParticipantPerspective
					List<MPerspective> perspectives = modelService.findElements(
						application,				// searchRoot
						ParticipantPerspective.ID,	// id of the element to search for
		                MPerspective.class,			// type of element to be searched for
		                null						// List of tags that needs to match
					);

					if ( ! perspectives.isEmpty() ) {
						MPerspective participantPerspective = perspectives.get(0);
						partService.switchPerspective(participantPerspective);
					}

					// activate ParticipantSearchView
					List<MPart> parts = modelService.findElements(
						application,				// searchRoot
						ParticipantSearchView.ID,	// id of the element to search for
		                MPart.class,				// type of element to be searched for
		                null						// List of tags that needs to match
					);

					if ( ! parts.isEmpty() ) {
						MPart part = parts.get(0);
						partService.activate(part, true /*requiresFocus*/);
						CompatibilityView compatibilityView = (CompatibilityView) part.getObject();
						ParticipantSearchView participantSearchView = (ParticipantSearchView) compatibilityView.getView();

						// let its search view do the work
						participantSearchView.doQuickSearch(searchValue);
					}

				}
				// If no text in search field, put cursor there and store the information that user wanted to search for participants
				else {
					searchField.setFocus();
					searchField.setSearchTarget(SearchTarget.PARTICIPANT);
				}

			}
			else {
				System.err.println("Could not find element with ID " + QuickSearchField.ID);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

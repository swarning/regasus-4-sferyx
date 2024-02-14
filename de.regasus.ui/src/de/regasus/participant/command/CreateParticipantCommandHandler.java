package de.regasus.participant.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.TypeHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventSelectionHelper;
import de.regasus.event.dialog.EventSelectionDialog;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.ui.Activator;

/**
 * Handler for "Teilnehmer erzeugen...", which can be started via the "Veranstaltung" menu and the
 * "Verantstaltung" view.
 *
 * <p>
 * Tries to find the "current" event from Event View or active ParticipantEditor,
 * then opens the {@link ParticipantEditor}.
 * <p>
 * The handling of the Insert key in the ParticipantSearchComposite is done there, because the
 * keyListener knows that it has to take the Event selected in the ComboViewer.
 *
 * @author manfred
 *
 */
public class CreateParticipantCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			Shell shell = window.getShell();


			Long eventPK = null;

			String withEventSelectionValue = event.getParameter("withEventSelection");
			boolean withEventSelection = TypeHelper.toBoolean(withEventSelectionValue, false);

			eventPK = EventSelectionHelper.getEventID(event);

			if (eventPK == null) {
				withEventSelection = true;
			}

			if (withEventSelection) {
				List<Long> initSelectedEventPKs = createArrayList(eventPK);
				EventSelectionDialog dialog = new EventSelectionDialog(
					shell,
					null,	// hideEventPKs
					initSelectedEventPKs,
					false	// multiSelection
				);


				int result = dialog.open();
				if (result == 0) {
					List<EventVO> selectedEvents = dialog.getSelectedEvents();
					if ( notEmpty(selectedEvents) ) {
						eventPK = selectedEvents.get(0).getID();
					}
				}
				else {
					eventPK = null;
				}
			}

			if (eventPK != null) {
				IWorkbenchPage page = window.getActivePage();
				ParticipantEditorInput editorInput = ParticipantEditorInput.getCreateInstance(eventPK);
				try {
					page.openEditor(editorInput, ParticipantEditor.ID);
				}
				catch (PartInitException e) {
					RegasusErrorHandler.handleApplicationError(
						Activator.PLUGIN_ID,
						getClass().getName(),
						e,
						I18N.CreateProfileAction_Error);
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, this.getClass().getName(), t);
		}
		return null;
	}

}

package de.regasus.programme.programmepoint.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.event.editor.EventEditor;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

public class DeleteProgrammePointHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			boolean deleteOK = MessageDialog.openConfirm(
				HandlerUtil.getActiveShell(event),
				UtilI18N.Question,
				I18N.DeleteProgrammePointAction_Confirmation
			);

			if (deleteOK) {
				// determine IDs of Programme Points to be deleted
				List<Long> programmePointIds = new ArrayList<>();
				Iterator<?> iterator = currentSelection.iterator();
				while (iterator.hasNext()) {
					Object object = iterator.next();
					if (object instanceof ProgrammePointTreeNode) {
						ProgrammePointTreeNode node = (ProgrammePointTreeNode) object;
						ProgrammePointVO programmePointVO = node.getValue();
						programmePointIds.add( programmePointVO.getPK() );
					}
				}


				// find dirty Event Editors which reference a Programme Point which is going to be deleted
				List<IEditorPart> dirtyEditors = EventEditor.getDirtyEditors(EventEditor.class);
				for (IEditorPart editorPart : dirtyEditors) {
					EventEditor eventEditor = (EventEditor) editorPart;
					Long eventPK = eventEditor.getEventId();

					EventVO eventVO;
					try {
						eventVO = EventModel.getInstance().getEventVO(eventPK);
					}
					catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
						return null;
					}

					Long digitalEventLeadProgrammePointId = eventVO.getDigitalEventLeadProgrammePointId();
					if (digitalEventLeadProgrammePointId != null) {
						if (programmePointIds.contains(digitalEventLeadProgrammePointId)) {
							IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							IWorkbenchPage page = workbenchWindow.getActivePage();
							/* Save the editor.
							 * The result will be true if the user chooses Yes or No!
							 * Only if the user chooses Cancel the result is false!
							 */
							if ( ! page.saveEditor(editorPart, true)) {
								return false;
							}
						}
					}
				}


				// delete Programme Points
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					@Override
					public void run() {
						for (Long programmePointId : programmePointIds) {
							try {
								ProgrammePointModel.getInstance().delete(programmePointId);
							}
							catch (Exception e) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
							}
						}
					}
				});
			}
		}
		return null;
	}

}

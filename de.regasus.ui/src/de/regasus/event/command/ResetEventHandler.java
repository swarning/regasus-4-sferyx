package de.regasus.event.command;

import java.util.Locale;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventModel;
import de.regasus.event.EventSelectionHelper;
import de.regasus.ui.Activator;

public class ResetEventHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Iterate through whatever is currently selected
		final IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);

		if (!currentSelection.isEmpty()) {
			try {
				EventVO eventVO = EventSelectionHelper.getEventVO(event);
				if (eventVO != null) {
					// open confirmation dialog
					String eventLabel = eventVO.getLabel(Locale.getDefault());
					String message = I18N.ResetEventAction_Confirmation.replace("<eventLabel>", eventLabel);

					boolean resetOK = MessageDialog.openConfirm(
						HandlerUtil.getActiveShell(event),
						UtilI18N.Question,
						message
					);

					if (resetOK) {
						EventModel.getInstance().resetEvent( eventVO.getID() );
					}

				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}
}

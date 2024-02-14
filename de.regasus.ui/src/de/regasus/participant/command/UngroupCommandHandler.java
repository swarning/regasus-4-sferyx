package de.regasus.participant.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.ui.Activator;

public class UngroupCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			final List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
			
			if (participantList != null && !participantList.isEmpty()) {

				// ask question
				boolean answer = MessageDialog.openQuestion(
					HandlerUtil.getActiveShellChecked(event), 
					UtilI18N.Question, 
					I18N.Ungroup_Question
				);


				if (answer) {

					BusyCursorHelper.busyCursorWhile(new Runnable() {
						public void run() {
							try {
								 SWTHelper.syncExecDisplayThread(new Runnable() {
									public void run() {
										try {
											ParticipantModel.getInstance().ungroup(participantList);
										}
										catch (Exception e) {
											RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
										}
									}
								});
							}
							catch (Exception e) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
							}
	
						}
					});
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return null;
	}
	
}

package de.regasus.participant.command;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.view.ParticipantSearchView;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

/**
 * Handler for "Personendaten in Profil kopieren", which can be started via the popup menü of the "Teilnehmersuche"
 * {@link ParticipantSearchView}.
 * 
 * <p>
 * Does not assume that there is any editor open.
 * 
 * @author manfred
 * 
 */
public class ParticipantCopyToProfileCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Determine the Participants
			final List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
			
			if (participantList != null && !participantList.isEmpty()) {
				// ask question
				String question = null;
				if (participantList.size() == 1) {
					question = I18N.ParticipantCopyToProfile_Question;
				}
				else {
					question = I18N.ParticipantsCopyToProfile_Question;
				}
				
				boolean answer = MessageDialog.openQuestion(
					HandlerUtil.getActiveShellChecked(event), 
					UtilI18N.Question,
					question
				);


				if (answer) {
					try {
						BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {

							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								monitor.beginTask(UtilI18N.Working, participantList.size());
								
								for (IParticipant iParticipant : participantList) {
									try {
										// The model method fires already update events, but for the profile only
										ProfileModel.getInstance().copyPersonDataFromParticipant(iParticipant.getPK());
										monitor.worked(1);
										if (monitor.isCanceled()) {
											break;
										}
									}
									catch (Exception e) {
										RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
									}
								}
								monitor.done();

						}});
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
				
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		
		return null;
	}

}

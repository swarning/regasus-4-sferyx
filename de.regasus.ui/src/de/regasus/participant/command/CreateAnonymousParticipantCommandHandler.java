/**
 * 
 */
package de.regasus.participant.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.dialog.CreateAnonymousParticipantWizard;
import de.regasus.ui.Activator;


public class CreateAnonymousParticipantCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			final Shell shell = HandlerUtil.getActiveShell(event);
			
			IParticipant iParticipant = ParticipantSelectionHelper.getParticipant(event);
			
			if (iParticipant != null) {
				Participant groupManager = null;
				if (iParticipant instanceof Participant) {
					groupManager = (Participant) iParticipant;
				}
				else {
					Long participantPK = iParticipant.getPK();
					if (participantPK != null) {
						groupManager = ParticipantModel.getInstance().getParticipant(participantPK);
					}
				}
				
				if (groupManager != null) {
					CreateAnonymousParticipantWizard wizard = new CreateAnonymousParticipantWizard(groupManager);
					WizardDialog wizardDialog = new WizardDialog(shell, wizard);
					wizardDialog.addPageChangedListener(wizard);
					
					wizardDialog.create();
					wizardDialog.getShell().pack();
					
					int returnCode = wizardDialog.open();
					if (returnCode == WizardDialog.OK) {
						final int count = wizard.getCount();
						Participant templateParticipant = wizard.getTemplateParticipant();
						
						ParticipantModel.getInstance().createAnonymousGroupMembers(
							count, 
							templateParticipant
						);
						
						
						// show final message
						SWTHelper.syncExecDisplayThread(new Runnable() {
							public void run() {
								try {						
									// Show dialog that tells the user how many Participant have been changed certificate Print from the selected once.
									String title = I18N.CreateAnonymousParticipantWizard_Title;
									
									String message = I18N.CreateAnonymousParticipants_FinalMessage;
									message = message.replaceFirst("<count>", String.valueOf(count));
				  
									MessageDialog.openInformation(shell, title, message);
								}
								catch (Exception e) {
									RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
								}
							}
						});			
					}
				}
				else {
					Exception e = new Exception("No participant selected.");
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

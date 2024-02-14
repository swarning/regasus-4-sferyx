package de.regasus.participant.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

/**
 * Handler for "Personendaten von Profil kopieren", which can be started via the "Editor" menu when the
 * {@link ParticipantEditor} is active.
 * 
  * @author manfred
 * 
 */
public class ParticipantCopyToProfileEditorCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ParticipantEditor participantEditor = (ParticipantEditor) HandlerUtil.getActiveEditorChecked(event);
		final Long pk = (Long) participantEditor.getKey();

		try {
			Shell shell = HandlerUtil.getActiveShellChecked(event);

			boolean answer =
				MessageDialog.openQuestion(shell, UtilI18N.Question, I18N.ParticipantCopyToProfile_Question);
			if (answer) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					public void run() {
						try {
							ProfileModel.getInstance().copyPersonDataFromParticipant(pk);
							
							SWTHelper.syncExecDisplayThread(new Runnable() {

								public void run() {
									try {
										participantEditor.refresh();
									}
									catch (Exception e) {
										RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
									}
								}});
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}

					}
				});
				
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return null;
	}


	@Override
	public boolean isEnabled() {
		IEditorPart activeEditor =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof ParticipantEditor) {
			ParticipantEditor participantEditor = (ParticipantEditor) activeEditor;
			Long profileID = participantEditor.getParticipant().getProfileID();
			if (profileID != null) {
				return true;
			}
		}
		return false;
	}

}

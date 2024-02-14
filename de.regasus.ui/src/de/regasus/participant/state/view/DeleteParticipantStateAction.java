/**
 * DeleteParticipantStateAction.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.state.editor.ParticipantStateEditor;

/**
 * @author huuloi
 *
 */
public class DeleteParticipantStateAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {
	
	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantState.DeleteParticipantStateAction";
	
	private final IWorkbenchWindow window;
	private List<ParticipantState> selectedParticipantStates = new ArrayList<ParticipantState>();
	
	public DeleteParticipantStateAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.DeleteParticipantStateAction_Text);
		setToolTipText(I18N.DeleteParticipantStateAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID, 
			IImageKeys.DELETE
		));
	
		window.getSelectionService().addSelectionListener(this);
	}
	
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}
	
	@Override
	public void run() {
		if (! selectedParticipantStates.isEmpty()) {
			// Auftragsbestätigung
			boolean deleteOK = false;
			if (selectedParticipantStates.size() == 1) {
				final String language = Locale.getDefault().getLanguage();
				final String title = I18N.DeleteParticipantStateConfirmation_Title;
				String message = I18N.DeleteParticipantStateConfirmation_Message;
				
				// Im Abfragetext den Namen der zu löschenden Teilnehmerstatus einfügen
				final ParticipantState participantState = selectedParticipantStates.get(0);
				final String name = participantState.getName().getString(language);
				message = message.replaceFirst("<name>", name); 
				
				// Dialog öffnen
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = I18N.DeleteParticipantStateListConfirmation_Title;
				String message = I18N.DeleteParticipantStateListConfirmation_Message;
				
				// Dialog öffnen
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			
			// If the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {
					
					public void run() {
						final List<Long> participantStateIDs = ParticipantState.getPrimaryKeyList(selectedParticipantStates);
						try {
							List<ParticipantState> copies = new ArrayList<ParticipantState>(selectedParticipantStates);
							for (ParticipantState participantState : copies) {
								// Teilnehmerstatus löschen
								ParticipantStateModel.getInstance().delete(participantState);
							}
						}
						catch (Throwable t) {
							String msg = I18N.DeleteParticipantStateErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							// Wenn beim Löschen ein Fehler auftritt abbrechen, damit die Editoren nicht geschlossen werden.
							return;
						}
						
						// Nach Editoren suchen und schließen
						ParticipantStateEditor.closeEditors(participantStateIDs);
					}
				});
			}
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		selectedParticipantStates.clear();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection selected = (IStructuredSelection) selection;
			
			for (Iterator it = selected.iterator(); it.hasNext();) {
				Object selectedElement = (Object)it.next();
				if (selectedElement instanceof ParticipantState) {
					ParticipantState participantState = (ParticipantState) selectedElement;
					selectedParticipantStates.add(participantState);
				}
				else {
					selectedParticipantStates.clear();
					break;
				}
			}
		}
		setEnabled(!selectedParticipantStates.isEmpty());
	}

}

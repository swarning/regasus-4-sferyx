package de.regasus.participant.type.view;

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

import com.lambdalogic.util.rcp.BusyCursorHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.participant.type.editor.ParticipantTypeEditor;

public class DeleteParticipantTypeAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID = "com.lambdalogic.mi.masterdata.ui.action.participantType.DeleteParticipantTypeAction"; 

	private final IWorkbenchWindow window;
	private List<ParticipantType> selectedParticipantTypes = new ArrayList<>();



	public DeleteParticipantTypeAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.DeleteParticipantTypeAction_Text);
		setToolTipText(I18N.DeleteParticipantTypeAction_ToolTip);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
			Activator.PLUGIN_ID,
			IImageKeys.DELETE
		));

		window.getSelectionService().addSelectionListener(this);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}


	@Override
	public void run() {
		if (! selectedParticipantTypes.isEmpty()) {
			// Auftragsbestätigung
			boolean deleteOK = false;
			if (selectedParticipantTypes.size() == 1) {
				final String language = Locale.getDefault().getLanguage();
				final String title = I18N.DeleteParticipantTypeConfirmation_Title;
				String message = I18N.DeleteParticipantTypeConfirmation_Message;

				// Im Abfragetext den Namen der zu löschenden Teilnehmerart einfügen
				final ParticipantType participantType = selectedParticipantTypes.get(0);
				final String name = participantType.getName().getString(language);
				message = message.replaceFirst("<name>", name); 

				// Dialog öffnen
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = I18N.DeleteParticipantTypeListConfirmation_Title;
				String message = I18N.DeleteParticipantTypeListConfirmation_Message;

				// Dialog öffnen
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}

			// If the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						/* Get the PKs now, because selected... will indirectly updated
						 * while deleteting the entities via the model. After deleting
						 * there're no entities selected.
						 */
						final List<Long> participantTypePKs = ParticipantType.getPrimaryKeyList(selectedParticipantTypes);

						try {
							List<ParticipantType> copies = new ArrayList<>(selectedParticipantTypes);
							for (ParticipantType participantType : copies) {
								// Teilnehmerart löschen
								ParticipantTypeModel.getInstance().delete(participantType);
							}
						}
						catch (Throwable t) {
							String msg = I18N.DeleteParticipantTypeErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							// Wenn beim Löschen ein Fehler auftritt abbrechen, damit die Editoren nicht geschlossen werden.
							return;
						}

						// Nach Editoren suchen und schließen
						ParticipantTypeEditor.closeEditors(participantTypePKs);
					}

				});
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedParticipantTypes.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;

			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object selectedElement = it.next();
				if (selectedElement instanceof ParticipantType) {
					ParticipantType participantType = (ParticipantType) selectedElement;
					selectedParticipantTypes.add(participantType);
				}
				else {
					selectedParticipantTypes.clear();
					break;
				}
			}
		}
		setEnabled(!selectedParticipantTypes.isEmpty());
	}

}

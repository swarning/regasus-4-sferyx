package de.regasus.common.language.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import de.regasus.common.Language;
import de.regasus.common.language.editor.LanguageEditor;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;

public class DeleteLanguageAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID = "de.regasus.core.ui.language.DeleteLanguageAction";

	private final IWorkbenchWindow window;
	private List<Language> selectedLanguageVOs = new ArrayList<>();



	public DeleteLanguageAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.DeleteLanguageAction_Text);
		setToolTipText(I18N.DeleteLanguageAction_ToolTip);
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
		if (!selectedLanguageVOs.isEmpty()) {
			boolean deleteOK = false;
			if (selectedLanguageVOs.size() == 1) {
				String title = I18N.DeleteLanguageConfirmation_Title;
				String message = I18N.DeleteLanguageConfirmation_Message;

				Language language = selectedLanguageVOs.get(0);
				String name = language.getName().getString();
				message = message.replaceFirst("<name>", name);

				// open Dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = I18N.DeleteLanguageListConfirmation_Title;
				String message = I18N.DeleteLanguageListConfirmation_Message;
				// Open the Dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}

			// If the user answered 'Yes' in the dialog
			if (deleteOK) {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						/* Get the PKs now, because selected... will indirectly updated
						 * while deleting the entities via the model. After deleting
						 * there're no entities selected.
						 */
						final List<String> languageIds = Language.getPKs(selectedLanguageVOs);

						try {
							if (selectedLanguageVOs.size() == 1) {
								// delete Language
								LanguageModel.getInstance().delete(selectedLanguageVOs.get(0));
							}
							else {
								// Sprachen löschen
								LanguageModel.getInstance().delete(selectedLanguageVOs);
							}
						}
						catch (Throwable t) {
							String msg = I18N.DeleteLanguageErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							// Wenn beim Löschen ein Fehler auftritt abbrechen, damit die Editoren nicht geschlossen werden.
							return;
						}

						// Nach Editoren suchen und schließen
						LanguageEditor.closeEditors(languageIds);
					}

				});
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedLanguageVOs.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;

			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				Object selectedElement = it.next();
				if (selectedElement instanceof Language) {
					Language language = (Language) selectedElement;
					selectedLanguageVOs.add(language);
				}
				else {
					selectedLanguageVOs.clear();
					break;
				}
			}
		}
		setEnabled(!selectedLanguageVOs.isEmpty());
	}

}

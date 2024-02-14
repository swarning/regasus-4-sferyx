package de.regasus.common.country.view;

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
import de.regasus.common.Country;
import de.regasus.common.country.editor.CountryEditor;
import de.regasus.core.CountryModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.IImageKeys;

public class DeleteCountryAction
extends Action
implements ActionFactory.IWorkbenchAction, ISelectionListener {

	public static final String ID = "de.regasus.core.ui.country.DeleteCountryAction";

	private final IWorkbenchWindow window;
	private List<Country> selectedCountryList = new ArrayList<>();



	public DeleteCountryAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setId(ID);
		setText(I18N.DeleteCountryAction_Text);
		setToolTipText(I18N.DeleteCountryAction_ToolTip);
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
		if (!selectedCountryList.isEmpty()) {
			// confirmation
			boolean deleteOK = false;
			if (selectedCountryList.size() == 1) {
				final String language = Locale.getDefault().getLanguage();
				final String title = I18N.DeleteCountryConfirmation_Title;
				String message = I18N.DeleteCountryConfirmation_Message;
				// Im Abfragetext den Namen des zu löschenden Landes einfügen
				Country country = selectedCountryList.get(0);
				String name = country.getName().getString(language);
				message = message.replaceFirst("<name>", name);
				// open Dialog
				deleteOK = MessageDialog.openQuestion(window.getShell(), title, message);
			}
			else {
				final String title = I18N.DeleteCountryListConfirmation_Title;
				String message = I18N.DeleteCountryListConfirmation_Message;
				// Open the Dialog
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
						List<String> countryIds = Country.getPKs(selectedCountryList);

						try {
							if (selectedCountryList.size() == 1) {
								// delete Country
								CountryModel.getInstance().delete(selectedCountryList.get(0));
							}
							else {
								// Länder löschen
								CountryModel.getInstance().delete(selectedCountryList);
							}
						}
						catch (Throwable t) {
							String msg = I18N.DeleteCountryErrorMessage;
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t, msg);
							// Wenn beim Löschen ein Fehler auftritt abbrechen, damit die Editoren nicht geschlossen werden.
							return;
						}

						// Nach Editoren suchen und schließen
						CountryEditor.closeEditors(countryIds);
					}

				});
			}
		}
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		selectedCountryList.clear();
		if (incoming instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) incoming;

			for (Iterator<?> it = selection.iterator(); it.hasNext();) {
				Object selectedElement = it.next();
				if (selectedElement instanceof Country) {
					Country country = (Country) selectedElement;
					selectedCountryList.add(country);
				}
				else {
					selectedCountryList.clear();
					break;
				}
			}
		}
		setEnabled( ! selectedCountryList.isEmpty() );
	}

}

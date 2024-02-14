package de.regasus.onlineform.admin.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.RegistrationFormConfigListTreeNode;
import de.regasus.event.view.RegistrationFormConfigTreeNode;
import de.regasus.onlineform.admin.ui.Activator;
import de.regasus.onlineform.editor.RegistrationFormConfigEditor;
import de.regasus.onlineform.editor.RegistrationFormConfigEditorInput;

public class CreateWebsiteConfigCommandHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {

			// Find out for which event the RegistrationFormConfig is to be created
			ISelection selection = HandlerUtil.getCurrentSelection(event);

			RegistrationFormConfigListTreeNode registrationFormConfigListTreeNode = null;
			if (SelectionHelper.isNonemptySelectionOf(selection, RegistrationFormConfigListTreeNode.class)) {
				registrationFormConfigListTreeNode = SelectionHelper.getUniqueSelected(selection);
			}
			else if(SelectionHelper.isNonemptySelectionOf(selection, RegistrationFormConfigTreeNode.class)) {
				RegistrationFormConfigTreeNode configTreeNode = SelectionHelper.getUniqueSelected(selection);
				registrationFormConfigListTreeNode = (RegistrationFormConfigListTreeNode) configTreeNode.getParent();
			}

			if (registrationFormConfigListTreeNode == null) {
				System.err.println("No RegistrationFormConfigListTreeNode in selection " + selection);
				return null;
			}

			EventTreeNode eventTreeNode = (EventTreeNode) registrationFormConfigListTreeNode.getParent();
			EventVO eventVO = eventTreeNode.getValue();

			// Open an editor for a new RegistrationFormConfig for th event
			RegistrationFormConfig config = new RegistrationFormConfig(eventVO);

			RegistrationFormConfigEditorInput editorInput = new RegistrationFormConfigEditorInput(config);

			IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			activePage.openEditor(editorInput, RegistrationFormConfigEditor.ID);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}
}

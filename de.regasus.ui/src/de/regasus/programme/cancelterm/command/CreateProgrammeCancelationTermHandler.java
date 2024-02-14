package de.regasus.programme.cancelterm.command;

import java.util.Locale;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.ProgrammeCancelationTermTreeNode;
import de.regasus.event.view.ProgrammeOfferingTreeNode;
import de.regasus.event.view.ProgrammePointListTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.cancelterm.dialog.CreateProgrammeCancelationTermsWizard;
import de.regasus.programme.cancelterm.dialog.CreateProgrammeCancelationTermsWizardMode;
import de.regasus.programme.cancelterm.editor.ProgrammeCancelationTermEditor;
import de.regasus.programme.cancelterm.editor.ProgrammeCancelationTermEditorInput;
import de.regasus.ui.Activator;

public class CreateProgrammeCancelationTermHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();



		// Check if we have to open the wizard
		CreateProgrammeCancelationTermsWizard wizard = null;

		if (object instanceof ProgrammePointTreeNode) {
			ProgrammePointTreeNode programmePointTreeNode = (ProgrammePointTreeNode) object;
			Long eventPK = programmePointTreeNode.getValue().getEventPK();
			Long programmePointPK = programmePointTreeNode.getValue().getPK();

			String objectName = programmePointTreeNode.getValue().getName().getString();
			CreateProgrammeCancelationTermsWizardMode mode = CreateProgrammeCancelationTermsWizardMode.PROGRAMME_POINT;

			// Create the wizard now, open after the if's
			wizard = new CreateProgrammeCancelationTermsWizard(programmePointPK, eventPK, mode, objectName);

		}
		else if (object instanceof ProgrammePointListTreeNode) {
			ProgrammePointListTreeNode programmePointListTreeNode = (ProgrammePointListTreeNode) object;
			EventTreeNode eventTreeNode = (EventTreeNode) programmePointListTreeNode.getParent();
			Long eventPK = eventTreeNode.getValue().getPrimaryKey();

			String objectName = eventTreeNode.getValue().getName(Locale.getDefault());
			CreateProgrammeCancelationTermsWizardMode mode = CreateProgrammeCancelationTermsWizardMode.EVENT;

			// Create the wizard now, open after the if's
			wizard = new CreateProgrammeCancelationTermsWizard(null, eventPK, mode, objectName);

		}
		else if (object instanceof EventTreeNode) {
			EventTreeNode eventTreeNode = (EventTreeNode) object;
			Long eventPK = eventTreeNode.getValue().getPrimaryKey();

			String objectName = eventTreeNode.getValue().getName(Locale.getDefault());
			CreateProgrammeCancelationTermsWizardMode mode = CreateProgrammeCancelationTermsWizardMode.EVENT;

			// Create the wizard now, open after the if's
			wizard = new CreateProgrammeCancelationTermsWizard(null, eventPK, mode, objectName);
		}

		if (wizard != null){
			// Open the wizard
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			dialog.create();
			dialog.getShell().setSize(700, 600);
			dialog.open();

			// Don't open any editor
			return null;
		}


		// Check if we have to open the editor

		// Find out EventPK
		Long programmeOfferingPK = null;

		Long eventPK = null;

		if (object instanceof ProgrammeOfferingTreeNode) {
			ProgrammeOfferingTreeNode programmeOfferingTreeNode = (ProgrammeOfferingTreeNode) object;
			programmeOfferingPK = programmeOfferingTreeNode.getProgrammeOfferingPK();
			eventPK = programmeOfferingTreeNode.getEventId();
		}
		else if (object instanceof ProgrammeCancelationTermTreeNode) {
			ProgrammeCancelationTermTreeNode programmeCancelationTermTreeNode = (ProgrammeCancelationTermTreeNode) object;
			programmeOfferingPK = programmeCancelationTermTreeNode.getProgrammeOfferingPK();
			eventPK = programmeCancelationTermTreeNode.getEventId();
		}


		if (programmeOfferingPK != null && eventPK != null) {
			// Open editor for new ProgrammeCancelationTermVO
			ProgrammeCancelationTermEditorInput input = ProgrammeCancelationTermEditorInput.getCreateInstance(programmeOfferingPK);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
					input,
					ProgrammeCancelationTermEditor.ID
				);
			}
			catch (PartInitException e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}

		return null;
	}

}

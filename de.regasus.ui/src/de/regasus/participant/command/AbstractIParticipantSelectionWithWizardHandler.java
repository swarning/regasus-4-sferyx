package de.regasus.participant.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.exception.ErrorMessageException;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.ui.Activator;

/**
 * Common superclass for all command handlers that are to create and open a wizard 
 * with the list of selected participants.
 */
abstract public class AbstractIParticipantSelectionWithWizardHandler extends AbstractHandler {

	protected ExecutionEvent executionEvent;
	
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.executionEvent = event;
		
		try {
			// Determine the Participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);

			// This should not happen !
			if (CollectionsHelper.empty(participantList)) {
				System.err.println("Empty participant list encountered in " + getClass().getName());
				return null;
			}

			Wizard wizard = createWizard(participantList);
			if (wizard != null) {
    			WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
    			wizardDialog.create();
    			
    			Point size = getSize();
    			if (size != null) {
    				wizardDialog.getShell().setSize(size);
    			}
    			
    			wizardDialog.open();
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

	
	protected Shell getShell() {
		return HandlerUtil.getActiveShell(executionEvent);
	}
	
	
	abstract protected Wizard createWizard(List<IParticipant> participantList)
	throws ErrorMessageException;
	
	
	/**
	 * Dient dem Überschreiben der Dialoggroesse.
	 * @return
	 */
	protected Point getSize() {
		return null;
	}
	
}

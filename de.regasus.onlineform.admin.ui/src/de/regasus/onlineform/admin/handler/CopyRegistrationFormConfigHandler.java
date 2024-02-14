package de.regasus.onlineform.admin.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.RegistrationFormConfigTreeNode;
import de.regasus.onlineform.admin.dialog.CopyRegistrationFormConfigWizard;
import de.regasus.onlineform.admin.ui.Activator;

public class CopyRegistrationFormConfigHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			RegistrationFormConfigTreeNode configTreeNodes = SelectionHelper.getUniqueSelected(selection);
			RegistrationFormConfig config = configTreeNodes.getValue();
			
			Wizard wizard = new CopyRegistrationFormConfigWizard(config);

			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			dialog.create();
			dialog.getShell().setSize(700, 600);
			dialog.open();
			
			
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}
}

package de.regasus.onlineform.admin.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.view.RegistrationFormConfigTreeNode;
import de.regasus.onlineform.admin.ui.Activator;

public class CreateSystemEmailsCommandHandler extends AbstractHandler implements IHandler {

	/**
	 * In case that a RegistrationFormConfig was already created, and somehow the generated system emails for
	 * confirmation an invitation disappear, the user can use this command to re-create them whithout having to
	 * re-create the whole config.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			RegistrationFormConfigTreeNode registrationFormConfigTreeNode = SelectionHelper.getUniqueSelected(selection);
			RegistrationFormConfig config = registrationFormConfigTreeNode.getValue();
			EmailTemplateModel.getInstance().createStandardEmailTemplates(config);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

package de.regasus.onlineform.admin.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.view.RegistrationFormConfigTreeNode;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.onlineform.admin.ui.Activator;

public class DeleteRegistrationFormConfigHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		final List<RegistrationFormConfigTreeNode> registrationFormConfigTreeNodes = SelectionHelper.toList(selection);

		String message;
		if (registrationFormConfigTreeNodes.size() == 1) {
			message = NLS.bind(UtilI18N.ReallyDeleteOne, de.regasus.onlineform.OnlineFormI18N.WebsiteConfiguration, registrationFormConfigTreeNodes.get(0).getValue().getWebId());
		}
		else {
			message = NLS.bind(UtilI18N.ReallyDeleteMultiple, de.regasus.onlineform.OnlineFormI18N.WebsiteConfigurations);
		}

		boolean deleteOK = MessageDialog.openConfirm(
			HandlerUtil.getActiveShell(event),
			UtilI18N.Question,
			message
		);

		if (deleteOK) {
			BusyCursorHelper.busyCursorWhile(new Runnable() {

				@Override
				public void run() {
					RegistrationFormConfigModel model = RegistrationFormConfigModel.getInstance();
					for (RegistrationFormConfigTreeNode node : registrationFormConfigTreeNodes) {
						RegistrationFormConfig regFormConfigPK = node.getValue();
						try {
							model.delete(regFormConfigPK);
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				}
			});
		}
		return null;
	}
}

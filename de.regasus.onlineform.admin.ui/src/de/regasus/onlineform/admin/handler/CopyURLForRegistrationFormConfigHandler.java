package de.regasus.onlineform.admin.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.view.RegistrationFormConfigTreeNode;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.onlineform.admin.ui.Activator;

public class CopyURLForRegistrationFormConfigHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			RegistrationFormConfigTreeNode configTreeNodes = SelectionHelper.getUniqueSelected(selection);
			RegistrationFormConfig config = configTreeNodes.getValue();
			String webId = config.getWebId();
			String url = RegistrationFormConfigModel.getInstance().getOnlineWebappUrl(webId);

			ClipboardHelper.copyToClipboard(url);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

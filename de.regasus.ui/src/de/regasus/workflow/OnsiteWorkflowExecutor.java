package de.regasus.workflow;

import static de.regasus.LookupService.getUserMgr;

import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.account.data.UserGroupVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;
import com.lambdalogic.report.script.GroovyScriptContext;

import de.regasus.I18N;
import de.regasus.LookupService;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;
import groovy.lang.GroovyRuntimeException;

public class OnsiteWorkflowExecutor {

	private GroovyScriptContext scriptContext = new GroovyScriptContext();

	public void run(String script, ParticipantCVO participantCVO, Shell shell) {

		// Even if I DID use a model, there would be a server access
		// done by UserAccountSearchModel. Of course we COULD do
		// the following also in the ServerModel and store the set
		// of groups there.
		List<UserGroupVO> userGroupVOs = getUserMgr().getCurrentUserGroupVOs();
		List<String> userGroups = AbstractVO.getPKs(userGroupVOs);

		String user = ServerModel.getInstance().getUser();

		scriptContext.setVariable("p", participantCVO);
		scriptContext.setVariable("user", user);
		scriptContext.setVariable("userGroups", userGroups);
		scriptContext.setVariable("badge", new BadgeWorkflowService());
		scriptContext.setVariable("dialog", new DialogWorkflowService(shell, participantCVO.getName()));
		scriptContext.setVariable("payment", new PaymentWorkflowService(shell));
		scriptContext.setVariable("server", new ServerWorkflowService());

		// add LookupService to ScriptContext
		LookupService lookupService = new LookupService();
		scriptContext.setVariable(LookupService.LOOKUP_SERVICE, lookupService);
		scriptContext.setVariable(LookupService.LOAD_HELPER, lookupService);

    	try {
    		scriptContext.runCachedGroovyScript(script);
		}
		catch (GroovyRuntimeException e) {
			RegasusErrorHandler.handleError(
				Activator.PLUGIN_ID, getClass().getName(),
				e,
				I18N.OnsiteWorkflowScriptError + "\n\n" + e.getLocalizedMessage(),
				de.regasus.core.ui.CoreI18N.Config_OnsiteWorkflow
			);
		}
	}

}

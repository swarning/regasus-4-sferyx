package de.regasus.programme.programmepoint.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.editor.AbstractEditor;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.ui.Activator;

public class CancelProgrammePointHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		Shell shell = HandlerUtil.getActiveShell(event);

		try {
			if (object instanceof ProgrammePointTreeNode) {
				ProgrammePointVO programmePointVO = ((ProgrammePointTreeNode) object).getValue();
				Long programmePointPK = programmePointVO.getID();

				String programmePointName = programmePointVO.getName().getString();
				boolean confirmed = MessageDialog.openQuestion(
					shell,
					UtilI18N.Confirm,
					I18N.CancelProgrammePointConfirmation_Message.replace("<name>", programmePointName)
				);

				if (confirmed) {
					/* Ask to to save all editors.
					 * Actually not all editors have to be saved but only those which are related to this
					 * Programme Point (ProgramePointEditor, WorkGroupEditor, ProgrammeOfferingEditor and
					 * ProgrammeCancleationTermEditor). However, to keep things simple all editors have to be saved
					 * before going on.
					 */
					if ( AbstractEditor.saveAllEditors() ) {
    					ProgrammePointModel.getInstance().cancel(programmePointPK);

    					MessageDialog.openInformation(
    						shell,
    						UtilI18N.Info,
    						I18N.CancelProgrammePointSuccess_Message.replace("<name>", programmePointName)
    					);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

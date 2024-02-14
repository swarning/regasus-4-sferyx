package de.regasus.programme.workgroup.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.event.view.EventTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.WorkGroupActionModel;
import de.regasus.ui.Activator;

public class RemoveWorkGroupAssociationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		try {
			Shell shell = HandlerUtil.getActiveShell(event);

			if (object instanceof EventTreeNode) {
				Long eventPK = ((EventTreeNode) object).getEventId();

				EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
				I18NPattern message = new I18NPattern();
				message.add(I18N.RemoveWorkGroupAssociationHandler_Event_Dialog_Label);
				message.putReplacement("<eventMnemonic>", eventVO.getMnemonic());
				boolean answer = MessageDialog.openQuestion(
					shell,
					I18N.RemoveWorkGroupAssociationHandler_Dialog_Title,
					message.getString()
				);

				if (answer) {
					WorkGroupActionModel.getInstance().removeWorkGroupAssociationByEvent(eventPK);
				}
			}
			else if (object instanceof ProgrammePointTreeNode) {
				Long programmePointPK = ((ProgrammePointTreeNode) object).getProgrammePointPK();

				ProgrammePointVO programmePointVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);
				I18NPattern message = new I18NPattern();
				message.add(I18N.RemoveWorkGroupAssociationHandler_ProgrammePoint_Dialog_Label);
				message.putReplacement("<programmePointName>", programmePointVO.getName());
				boolean answer = MessageDialog.openQuestion(
					shell,
					I18N.RemoveWorkGroupAssociationHandler_Dialog_Title,
					message.getString()
				);

				if (answer) {
					WorkGroupActionModel.getInstance().removeWorkGroupAssociationByProgrammePoint(programmePointPK);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

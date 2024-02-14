package de.regasus.programme.workgroup.command;

import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.event.EventIdProvider;
import de.regasus.event.EventModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.WorkGroupActionModel;
import de.regasus.programme.programmepoint.IProgrammePointIdProvider;

public class AssignWorkGroupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection currentSelection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object object = currentSelection.getFirstElement();

		Shell shell = HandlerUtil.getActiveShell(event);
		boolean stopOnError = MessageDialog.openQuestion(
			shell,
			I18N.AssignWorkGroupHandler_Dialog_Title,
			I18N.AssignWorkGroupHandler_Dialog_Message
		);

		String successMessage = null;
		List<Exception> exceptions = null;
		try {
			if (object instanceof EventIdProvider) {
				Long eventPK = ((EventIdProvider) object).getEventId();

				// assign work groups in Event
				exceptions = WorkGroupActionModel.getInstance().assignWorkGroupsByEvent(eventPK, stopOnError);

				// assign work groups of all Programme Booking of Event
				if ( empty(exceptions) ) {
    				successMessage = I18N.AssignWorkGroupHandler_SuccessMessage_Event;

    				try {
    					EventVO eventVO = EventModel.getInstance().getEventVO(eventPK);
    					successMessage = successMessage.replaceFirst("<name>", eventVO.getLabel().getString());
    				}
    				catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}
			}
			else if (object instanceof IProgrammePointIdProvider) {
				Long programmePointPK = ((IProgrammePointIdProvider) object).getProgrammePointId();

				// assign work groups of all Programme Booking of Programme Point
				exceptions = WorkGroupActionModel.getInstance().assignWorkGroupsByProgrammePoint(programmePointPK, stopOnError);

				// if no errors occured: build message to show in information dialog
				if (CollectionsHelper.empty(exceptions)) {
    				successMessage = I18N.AssignWorkGroupHandler_SuccessMessage_ProgrammePoint;

    				try {
    					ProgrammePointVO ppVO = ProgrammePointModel.getInstance().getProgrammePointVO(programmePointPK);
    					successMessage = successMessage.replaceFirst("<name>", ppVO.getName().getString());
    				}
    				catch (Exception e) {
						com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
					}
				}
			}
		}
		catch (Exception e) {
			exceptions = createArrayList(e);
		}


		// open dialog
		if ( empty(exceptions) ) {
			// open info dialog
			MessageDialog.openInformation(shell, UtilI18N.Info, successMessage);
		}
		else {
			// open error dialog
			ExceptionDialog exceptionDialog = new ExceptionDialog(shell, exceptions);
			exceptionDialog.open();
		}

		return null;
	}


	public static class ExceptionDialog extends Dialog {
		private List<Exception> exceptions;

		public ExceptionDialog(Shell parentShell, List<Exception> exceptions) {
			super(parentShell);
			this.exceptions = exceptions;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			Label label = new Label(composite, SWT.NONE);
			label.setText(I18N.AssignWorkGroupHandler_ExceptionDialog_Label);

			Text errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.VERTICAL | SWT.HORIZONTAL);
			errorMessageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			StringBuffer allMessages = new StringBuffer();
			for (Exception exception : exceptions) {
				if (allMessages.length() > 0) {
					allMessages.append("\n\n");
				}

				allMessages.append(exception.getMessage());
			}

			errorMessageText.setText(allMessages.toString());

			return composite;
		}

		@Override
		protected Point getInitialSize() {
			return new Point(500, 400);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(UtilI18N.ErrorInfo);
		}

		@Override
		protected boolean isResizable() {
			return true;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		}

	}

}

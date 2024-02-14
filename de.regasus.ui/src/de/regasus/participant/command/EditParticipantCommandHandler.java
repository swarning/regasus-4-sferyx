package de.regasus.participant.command;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.invoice.data.InvoiceCVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.SelectionHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.ui.Activator;

public class EditParticipantCommandHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			final ISelection selection = HandlerUtil.getCurrentSelection(event);
			
			IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

			// If the context menu in the InvoiceSearchView was activated
			if (SelectionHelper.isNonemptySelectionOf(selection, InvoiceCVO.class)) {
				// MIRCP-2954 - Don't produce error when trying to open multiple participants in accountancy
				List<InvoiceCVO> invoiceCVOs = SelectionHelper.toList(selection);
				for (InvoiceCVO invoiceCVO : invoiceCVOs) {
					openParticipantEditor(page, invoiceCVO);
				}
			}
			else {
				List<Long> participantIDs = ParticipantSelectionHelper.getParticipantIDs(selection);
				if (CollectionsHelper.notEmpty(participantIDs)) {
					for (Long participantID : participantIDs) {
						openParticipantEditor(page, participantID);
					}
				}
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	
		return null;
	}

	
	public static void openParticipantEditor(IWorkbenchPage page, Long participantID) {
		ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(participantID);
		try {
			page.openEditor(editorInput, ParticipantEditor.ID);
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditParticipantCommandHandler.class.getName(), e);
		}
	}


	public static void openParticipantEditor(IWorkbenchPage page, InvoiceCVO invoiceCVO) {
		Long recipientPK = invoiceCVO.getVO().getRecipientPK();
		ParticipantEditorInput editorInput = ParticipantEditorInput.getEditInstance(recipientPK);
		try {
			IEditorPart editorPart = page.openEditor(editorInput, ParticipantEditor.ID);
			
			// select the invoice
			ParticipantEditor participantEditor = (ParticipantEditor) editorPart;
			participantEditor.selectInvoice(invoiceCVO.getPK());
		}
		catch (PartInitException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, EditParticipantCommandHandler.class.getName(), e);
		}
	}
	
}

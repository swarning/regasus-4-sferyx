package de.regasus.participant.command.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.FileHelper;

import de.regasus.I18N;
import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.participant.ParticipantFileModel;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.ui.Activator;

public class DownloadPrintParticipantFilesCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// Compute the IDs from the selected participants
			List<IParticipant> participantList = ParticipantSelectionHelper.getParticipants(event);
			List<Long> participantIds = new ArrayList<>( participantList.size() );
			for (IParticipant iParticipant : participantList) {
				participantIds.add( iParticipant.getPK() );
			}


			// collect all Participant Files (FileSummary) of all Participants
			ParticipantFileModel participantFileModel = ParticipantFileModel.getInstance();
			List<FileSummary> fileSummaryList = new ArrayList<>();
			for (Long participantId : participantIds) {
				List<FileSummary> participantFileSummaryList = participantFileModel.getParticipantFileSummaryListByParticipantId(participantId);
				fileSummaryList.addAll(participantFileSummaryList);
			}


			// If the selected participants doesn't have any document, there is nothing to do
			Shell shell = HandlerUtil.getActiveShell(event);
			if ( fileSummaryList.isEmpty() ) {
				MessageDialog.openInformation(shell, UtilI18N.Info, I18N.SelectedParticipantsHaveNoDocuments);
				return null;
			}


			// calculate size of all files
			long size = 0;
			for (FileSummary fileSummary : fileSummaryList) {
				size += fileSummary.getSize();
			}


			// Prepare the message informing on the needed space
			String readableFileSize = FileHelper.computeReadableFileSize(size);
			String message = I18N.SelectedParticipantsHaveCountDocumentsOfSize
				.replace("<count>", String.valueOf(fileSummaryList.size()))
				.replace("<size>", readableFileSize);


			// Open dialog to ask for directory to save to, and whether to print or not
			DownloadFileDialog dialog = new DownloadFileDialog(shell, message);
			int code = dialog.open();

			if (Window.OK == code) {
				// Let download (and possibly print) happen in a job that can be cancelled
				File directory = dialog.getDirectory();
				boolean shouldPrint = dialog.isShouldPrint();

				DownloadPrintFilesJob job = new DownloadPrintFilesJob(participantList, directory, shouldPrint);
				job.setUser(true);
				job.schedule();
			}
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

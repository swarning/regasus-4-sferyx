package de.regasus.participant.command.download;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.util.StreamHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.I18N;
import de.regasus.auth.AuthorizationException;
import de.regasus.common.FileModel;
import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.rws.HttpStatus;
import de.regasus.core.ui.rws.RegasusWebServiceUtil;
import de.regasus.participant.ParticipantFileModel;
import de.regasus.ui.Activator;

public class DownloadPrintFilesJob extends Job {

	private List<? extends IParticipant> participants;

	private File directory;
	private boolean shouldPrint;


	public DownloadPrintFilesJob(List<? extends IParticipant> participants, File directory, boolean shouldPrint) {
		super(I18N.DownloadPrintFiles);

		this.participants = participants;
		this.directory = directory;
		this.shouldPrint = shouldPrint;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(I18N.DownloadPrintFiles, participants.size());

		// Prepare a list of error records to have a log of what went wrong
		List<ErrorRecord> errorRecords = new ArrayList<>();

		// Do the work for all participants, but stop if cancelled
		for (IParticipant participant : participants) {
			monitor.subTask(participant.getName());

			printDocumentsOf(participant, errorRecords);

			if (monitor.isCanceled()) {
				monitor.done();
				return Status.CANCEL_STATUS;
			}
			monitor.worked(1);
		}
		monitor.done();

		try {
			// If there have been errors, show them
			if (! errorRecords.isEmpty()) {
				File errorLogFile = new File(directory, "ErrorLog.txt");
				PrintWriter out = new PrintWriter(errorLogFile);
				out.println(
					Participant.NUMBER.getString() +
					"\t" +
					UtilI18N.File + "/" + UtilI18N.Error);

				for (ErrorRecord errorRecord : errorRecords) {
					out.println(errorRecord.toString());
				}
				out.close();

				Desktop.getDesktop().open(errorLogFile);
			}

			// Open the directory if no automatic printing was done
			if (!shouldPrint) {
				Desktop.getDesktop().open(directory);
			}
		}
		catch (IOException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return Status.OK_STATUS;
	}


	private void printDocumentsOf(IParticipant participant, List<ErrorRecord> errorRecords) {
		String number = participant.getNumber().toString();

		ParticipantFileModel participantFileModel = ParticipantFileModel.getInstance();

		CloseableHttpClient httpClient = null;
		try {
			List<FileSummary> fileSummaryList = participantFileModel.getParticipantFileSummaryListByParticipantId( participant.getPK() );

			if ( ! fileSummaryList.isEmpty() ) {
				// Create directory for participant with name <paddedNumber>-<name>
				String name = participant.getName(true);
				String subdirName = name + ", " + participant.getNumber();
				File subDirectory = new File(directory, subdirName);
				subDirectory.mkdir();

				final int TIMEOUT = 5 * 60 * 1000; // timeout for download: 5 minutes
				RegasusWebServiceUtil regasusWebServiceUtil = new RegasusWebServiceUtil().timeoutMillis(TIMEOUT);
				httpClient = regasusWebServiceUtil.buildClient();


				for (FileSummary fileSummary : fileSummaryList) {


					CloseableHttpResponse httpResponse = null;
					InputStream inputStream = null;
					OutputStream outputStream = null;
					try {
						// build URL
						String url = FileModel.buildWebServiceUrl(fileSummary);

						// get data via GET request
						httpResponse = new RegasusWebServiceUtil().sendGetRequest(httpClient, url);

						// check status code 200
						int statusCode = httpResponse.getStatusLine().getStatusCode();
						if (statusCode == HttpStatus.UNAUTHORIZED_401 || statusCode == HttpStatus.FORBIDDEN_403) {
							throw new AuthorizationException(com.lambdalogic.util.rcp.UtilI18N.AccessDenied);
						}
						else if (statusCode != HttpStatus.OK_200) {
							throw new ErrorMessageException("HTTP GET request returned with status code " + statusCode + " though "
								+ HttpStatus.OK_200 + " was expected.");
						}

						// get InputStream with data
						HttpEntity entity = httpResponse.getEntity();
						inputStream = entity.getContent();

						// open export file
						String fileName = fileSummary.getExternalFileName();
						File file = new File(subDirectory, fileName);
						outputStream = new FileOutputStream(file);

						// Save the OutputStream
						StreamHelper.copy(inputStream, outputStream);

						if (shouldPrint) {
							Desktop.getDesktop().print(file);
							Thread.sleep(3000);
						}
					}
					catch (Exception e) {
						errorRecords.add(new ErrorRecord(number, fileSummary.getExternalPath()));
					}
					finally {
						HttpClientUtils.closeQuietly(httpResponse);
						StreamHelper.closeQuietly(inputStream);
						StreamHelper.closeQuietly(outputStream);
					}


				}
			}
		}
		catch (Exception e) {
			errorRecords.add(new ErrorRecord(number, e.getMessage()));
		}
		finally {
			HttpClientUtils.closeQuietly(httpClient);
		}

	}
}

class ErrorRecord {
	private String participantNumber;
	private String fileName;

	public ErrorRecord(String participantNumber, String fileName) {
		this.participantNumber = participantNumber;
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return participantNumber + ";" + fileName;
	}
}

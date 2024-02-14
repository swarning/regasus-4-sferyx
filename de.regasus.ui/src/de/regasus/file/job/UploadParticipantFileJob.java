package de.regasus.file.job;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.StreamHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.ProgressMonitorInputStream;

import de.regasus.auth.AuthorizationException;
import de.regasus.core.ServerModel;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.rws.HttpStatus;
import de.regasus.core.ui.rws.RegasusWebServiceUtil;
import de.regasus.participant.ParticipantFileModel;

public class UploadParticipantFileJob extends Job {

	private static final int PROGRESS_MONITOR_UNIT_SIZE = 1024;


	private Participant participant;
	private File file;
	private String name;
	private String description;

	private IProgressMonitor monitor;


	public UploadParticipantFileJob(
		Participant participant,
		File file,
		String name,
		String description
	) {
		super(KernelLabel.Upload.getString());

		this.participant = participant;
		this.file = file;
		this.name = name;
		this.description = description;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;

		long startTime = System.currentTimeMillis();
		try {
			return upload();
		}
		finally {
			// log duration
			System.out.println(DateHelper.getTimeLagString(System.currentTimeMillis() - startTime));
		}
	}


	private IStatus upload() {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		FileInputStream fileInputStream = null;
		try {
			// determine total size of uncompressed data
			long fileSize = file.length();


			// init monitor with expected file size in kB
			int work = (int) (fileSize / PROGRESS_MONITOR_UNIT_SIZE);
			monitor.beginTask(file.getName(), work);

			// open file
			fileInputStream = new FileInputStream(file);

			ProgressMonitorInputStream progressMonitorInputStream = new ProgressMonitorInputStream(
				fileInputStream,
				monitor,
				PROGRESS_MONITOR_UNIT_SIZE
			);

			// build URL to RWS
			URIBuilder uriBuilder = new URIBuilder(
				ServerModel.getInstance().getWebServiceUrl() + "/participants/" + participant.getID() + "/files"
			);
			uriBuilder.addParameter("extPath", file.getAbsolutePath());
			uriBuilder.addParameter("name", name);
			uriBuilder.addParameter("desc", description);
			uriBuilder.addParameter("size", String.valueOf(fileSize));

			String url = uriBuilder.build().toString();

			final int TIMEOUT = 5 * 60 * 1000; // timeout for upload: 5 minutes
			RegasusWebServiceUtil regasusWebServiceUtil = new RegasusWebServiceUtil().timeoutMillis(TIMEOUT);
			httpClient = regasusWebServiceUtil.buildClient();

			// send data via POST request
			httpResponse = regasusWebServiceUtil.sendPostRequest(httpClient, url, progressMonitorInputStream);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.UNAUTHORIZED_401 || statusCode == HttpStatus.FORBIDDEN_403) {
				throw new AuthorizationException(com.lambdalogic.util.rcp.UtilI18N.AccessDenied);
			}
			else if (statusCode != HttpStatus.CREATED_201) {
				throw new ErrorMessageException("HTTP POST request returned with status code " + statusCode + " though "
					+ HttpStatus.CREATED_201 + " was expected.");
			}


			updateModelData();

			return Status.OK_STATUS;
		}
		catch (Throwable t) {
			String message = t.getMessage();

			System.err.println(message);

			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, t);
		}
		finally {
			// close HTTP request
			HttpClientUtils.closeQuietly(httpResponse);
			HttpClientUtils.closeQuietly(httpClient);
			StreamHelper.closeQuietly(fileInputStream);

			// finish monitor
			monitor.done();
		}
	}


	private void updateModelData() throws Exception {
		ParticipantFileModel.getInstance().refreshForeignKey(participant.getID());
	}

}

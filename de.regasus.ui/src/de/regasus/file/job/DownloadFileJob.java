package de.regasus.file.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.StreamHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.ProgressMonitorStreamHelper;

import de.regasus.auth.AuthorizationException;
import de.regasus.common.FileModel;
import de.regasus.common.FileSummary;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.rws.HttpStatus;
import de.regasus.core.ui.rws.RegasusWebServiceUtil;

public class DownloadFileJob extends Job {

	private FileSummary fileSummary;
	private File file;

	private IProgressMonitor monitor;


	public DownloadFileJob(FileSummary fileSummary, File file) {
		super(KernelLabel.Download.getString());

		this.fileSummary = fileSummary;
		this.file = file;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;

		long startTime = System.currentTimeMillis();
		try {
			return download();
		}
		finally {
			// log duration
			System.out.println(DateHelper.getTimeLagString(System.currentTimeMillis() - startTime));
		}
	}


	private IStatus download() {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			long fileSize = fileSummary.getSize();

			// init monitor with expected file size in kB
			final int BUFFER_SIZE = 1024;
			int work = (int) (fileSize / BUFFER_SIZE);
			String fileName = FileHelper.getName( fileSummary.getExternalPath() );
			monitor.beginTask(fileName, work);

			// build URL
			String url = FileModel.buildWebServiceUrl(fileSummary);

			// get data via GET request
			final int TIMEOUT = 5 * 60 * 1000; // timeout for download: 5 minutes
			RegasusWebServiceUtil regasusWebServiceUtil = new RegasusWebServiceUtil().timeoutMillis(TIMEOUT);
			httpClient = regasusWebServiceUtil.buildClient();
			httpResponse = regasusWebServiceUtil.sendGetRequest(httpClient, url);


			// check status code 200
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.UNAUTHORIZED_401 || statusCode == HttpStatus.FORBIDDEN_403) {
				throw new AuthorizationException(com.lambdalogic.util.rcp.UtilI18N.AccessDenied);
			}
			else if (statusCode != HttpStatus.OK_200) {
				throw new ErrorMessageException("HTTP GET request returned with status code " + statusCode + " though "
					+ HttpStatus.OK_200 + " was expected.");
			}

			// init monitor with expected file size in kB
			int expectedKB = (int) (fileSize / BUFFER_SIZE);
			monitor.beginTask(fileName, expectedKB);

			// get InputStream with data
			HttpEntity entity = httpResponse.getEntity();
			inputStream = entity.getContent();

			// open export file
			outputStream = new FileOutputStream(file);

			// Save the OutputStream
			ProgressMonitorStreamHelper.copy(inputStream, outputStream, BUFFER_SIZE, monitor);

			return Status.OK_STATUS;
		}
		catch (Throwable t) {
			String message = t.getLocalizedMessage();

			System.err.println(message);

			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, t);
		}
		finally {
			// close HTTP request
			HttpClientUtils.closeQuietly(httpResponse);
			HttpClientUtils.closeQuietly(httpClient);
			StreamHelper.closeQuietly(inputStream);
			StreamHelper.closeQuietly(outputStream);

			// finish monitor
			monitor.done();
		}
	}

}

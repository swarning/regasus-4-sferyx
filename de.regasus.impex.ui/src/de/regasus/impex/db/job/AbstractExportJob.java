package de.regasus.impex.db.job;

import static de.regasus.impex.Constants.SIZE_HEADER;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StreamHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.auth.AuthorizationException;
import de.regasus.core.ui.rws.HttpStatus;
import de.regasus.core.ui.rws.RegasusWebServiceUtil;
import de.regasus.impex.ExportStreamHelper;
import de.regasus.impex.ExportStreamHelper.IStreamMonitor;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;

public abstract class AbstractExportJob extends Job {

	private static final int TIMEOUT_MILLIS = 60 * 60 * 1000; // 1 hour

	private File file;

	private long expectedSize;

	private IProgressMonitor monitor;


	/**
	 * URL that is called to get the export data.
	 * @return
	 */
	protected abstract String getURL();


	protected AbstractExportJob(String name, File file) {
		super(name);
		this.file = file;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;

		long startTime = System.currentTimeMillis();

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			RegasusWebServiceUtil regasusWebServiceUtil = new RegasusWebServiceUtil().timeoutMillis(TIMEOUT_MILLIS);
			httpClient = regasusWebServiceUtil.buildClient();

			// get data via GET request
			httpResponse = regasusWebServiceUtil.sendGetRequest(httpClient, getURL());

			// check status code 200
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.FORBIDDEN_403) {
				throw new AuthorizationException(com.lambdalogic.util.rcp.UtilI18N.AccessDenied);
			}
			else if (statusCode != HttpStatus.OK_200) {
				// get InputStream with data and print it to System.err
				HttpEntity entity = httpResponse.getEntity();
				inputStream = entity.getContent();
				String errorMessage = StreamHelper.getString(inputStream);
				System.err.println(errorMessage);

				throw new ErrorMessageException("HTTP GET request returned with status code " + statusCode + " though "
					+ HttpStatus.OK_200 + " was expected.");
			}

			// get expected size from HTTP header
			Header sizeHeader = httpResponse.getFirstHeader(SIZE_HEADER);
			expectedSize = Long.valueOf(sizeHeader.getValue());

			System.out.println("Expected: " + expectedSize + " (" + FormatHelper.formatBytes(expectedSize) + ")");

			// init monitor with expected file size in kB
			int expectedKB = (int) (expectedSize / 1000L);
			monitor.beginTask(ImpexI18N.ExportJob_Task, expectedKB);

			// get InputStream with data
			inputStream = httpResponse.getEntity().getContent();

			// open export file
			outputStream = new FileOutputStream(file);


			StreamMonitor streamMonitor = new StreamMonitor();

			// Save the OutputStream without the checkSequence at the beginning and the end.
			ExportStreamHelper.copy(inputStream, outputStream, streamMonitor);

			/* If you want to save the whole outputStream incl. the check sequence at the beginning and the end
			 * use
			 * StreamHelper.copy(inputStream, outputStream, BUFFER_SIZE);
			 * instead.
			 */


			System.out.println(
				  "Exported: "
				+ streamMonitor.getTotalByteCount()
				+ " (" + FormatHelper.formatBytes(streamMonitor.getTotalByteCount()) + ") to "
				+ file.getAbsolutePath()
			);

			if (streamMonitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
		catch (Throwable t) {
			String message = t.getLocalizedMessage();
			if (StringHelper.isEmpty(message)) {
				message = ImpexI18N.ExportJob_ErrorMessage + ".";
			}
			else {
				message = ImpexI18N.ExportJob_ErrorMessage + ": " + message;
			}

			System.err.println(message);

			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, t);
		}
		finally {
			HttpClientUtils.closeQuietly(httpResponse);
			StreamHelper.closeQuietly(inputStream);
			StreamHelper.closeQuietly(outputStream);

			// finish monitor
			monitor.done();

			// log duration
			System.out.println(DateHelper.getTimeLagString(System.currentTimeMillis() - startTime));
		}
	}


	private class StreamMonitor implements IStreamMonitor {

		private boolean canceled = false;

		private StringBuilder monitorMsg = new StringBuilder(1024);

		// number of bytes that have been read and written so far
		long totalByteCount = 0;

		// number of bytes that have not been monitored so far (monitor expects chunks of 1 kB)
		int unmonitoredByteCount = 0;

		// time when the monitor sub task has been updated recently
		long lastMonitor = System.currentTimeMillis();


		@Override
		public void worked(int work) {
			// add new number of bytes
			unmonitoredByteCount += work;
			totalByteCount += work;

			int kbCount = unmonitoredByteCount / 1000;
			if (kbCount > 0) {
				monitor.worked(kbCount);
				unmonitoredByteCount = unmonitoredByteCount % 1000;
			}

			// update monitor sub task only every 200 ms
			if (System.currentTimeMillis() - lastMonitor > 200) {
				// build message
				monitorMsg.setLength(0);
				monitorMsg.append( FormatHelper.formatBytes(totalByteCount) );
				monitorMsg.append(" / ");
				monitorMsg.append( FormatHelper.formatBytes(expectedSize) );
				monitorMsg.append(" (");
				monitorMsg.append(UtilI18N.estimated);
				monitorMsg.append(")");

				monitor.subTask(monitorMsg.toString());

				lastMonitor = System.currentTimeMillis();
			}


			if (monitor.isCanceled()) {
				file.delete();
				canceled = true;
			}

		}


		@Override
		public boolean isCanceled() {
			return canceled;
		}


		public long getTotalByteCount() {
			return totalByteCount;
		}

	}

}

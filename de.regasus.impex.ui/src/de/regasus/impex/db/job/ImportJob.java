package de.regasus.impex.db.job;

import static de.regasus.impex.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.lambdalogic.messeinfo.kernel.interfaces.ImportMetadata;
import com.lambdalogic.messeinfo.kernel.interfaces.ImportSettings;
import com.lambdalogic.util.DateHelper;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.StreamHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.ProgressMonitorInputStream;
import com.lambdalogic.util.rcp.dialog.MessageDetailsDialog;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.auth.AuthorizationException;
import de.regasus.common.GateDeviceModel;
import de.regasus.core.CountryModel;
import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.LanguageModel;
import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.rws.HttpStatus;
import de.regasus.core.ui.rws.RegasusWebServiceUtil;
import de.regasus.event.EventGroupModel;
import de.regasus.event.EventModel;
import de.regasus.finance.CostCenter1Model;
import de.regasus.finance.CostCenter2Model;
import de.regasus.finance.CurrencyModel;
import de.regasus.finance.CustomerAccountModel;
import de.regasus.finance.ImpersonalAccountModel;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.hotel.HotelChainModel;
import de.regasus.hotel.HotelModel;
import de.regasus.hotel.RoomDefinitionModel;
import de.regasus.impex.ImpexI18N;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.report.model.UserReportDirListModel;
import de.regasus.report.model.UserReportListModel;
import de.regasus.users.UserAccountModel;
import de.regasus.users.UserGroupModel;


public class ImportJob extends Job {

	private static final int TIMEOUT_MILLIS = 5 * 60 * 1000; // 5 minutes

	private static final int PROGRESS_MONITOR_UNIT_SIZE = 1024;


	private File file;
	private ImportMetadata importMetadata;
	private ImportSettings importSettings;

	private IProgressMonitor monitor;


	public ImportJob(
		File file,
		ImportMetadata importMetadata,
		ImportSettings importSettings
	) {
		super(ImpexI18N.ImportJob_Name);

		this.file = file;
		this.importMetadata = importMetadata;
		this.importSettings = importSettings;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;

		long startTime = System.currentTimeMillis();
		try {
			return importData();
		}
		finally {
			// log duration
			System.out.println("Duration: " + DateHelper.getTimeLagString(System.currentTimeMillis() - startTime));
		}
	}


	private IStatus importData() {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		FileInputStream fileInputStream = null;
		InputStream responseInputStream = null;
		try {
			// init monitor with expected file size in kb
			int work = (int) (file.length() / PROGRESS_MONITOR_UNIT_SIZE);
			monitor.beginTask(ImpexI18N.ImportJob_Task, work);

			// open file
			fileInputStream = new FileInputStream(file);

			ProgressMonitorInputStream progressMonitorInputStream = new ProgressMonitorInputStream(
				fileInputStream,
				monitor,
				PROGRESS_MONITOR_UNIT_SIZE
			);

			// build URL to RWS
			URIBuilder uriBuilder = new URIBuilder(ServerModel.getInstance().getWebServiceUrl() + "/import");

			// append query parameter String according to values in importSettings
			importSettings.appendQueryParameters(uriBuilder);


			/* Send PUT request asynchronous via RestEasy */
			// write data via PUT request
			System.out.println("Sending PUT request");


			/* Send PUT request synchronous via Apache HTTP Client */

			RegasusWebServiceUtil regasusWebServiceUtil = new RegasusWebServiceUtil().timeoutMillis(TIMEOUT_MILLIS);
			httpClient = regasusWebServiceUtil.buildClient();

			httpResponse = regasusWebServiceUtil.sendPutRequest(httpClient, uriBuilder.toString(), progressMonitorInputStream);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			System.out.println("Status code of response: " + statusCode);

			if (statusCode == HttpStatus.FORBIDDEN_403) {
				throw new AuthorizationException(com.lambdalogic.util.rcp.UtilI18N.AccessDenied);
			}
			else if (statusCode != HttpStatus.OK_200 && statusCode != HttpStatus.NO_CONTENT_204) {
				throw new ErrorMessageException("HTTP GET request returned with status code " + statusCode + " though "
					+ HttpStatus.OK_200 + " was expected.");
			}


			// get InputStream with response data
			/* The web service is not sending response, because we did not set the query parameter sendResponse
			 * (ImportConstants.SEND_RESPONSE) to "yes". Therefore the HttpEntity is null.
			 *
			 * Currently we are sending the HTTP PUT request via Apache HTTP Client which is not able to process
			 * the request asynchronous. Therefore we can read the reponse not before the whole import is done which
			 * does not make sense.
			 *
			 * If we would use another library like RestEasy we would be able to read the response immediately.
			 */
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				System.out.println("Reading response input stream...");
    			responseInputStream = entity.getContent();



    			/* Read from response input stream */

    			InputStreamReader responseInputStreamReader = new InputStreamReader(responseInputStream);
    			BufferedReader responseBufferedReader = new BufferedReader(responseInputStreamReader, 2048);
    			String line = null;
    			while ((line = responseBufferedReader.readLine()) != null) {
    				System.out.println("Server response: " + line);

    				if (line.startsWith(IMPORT_RESPONSE_BEGIN)) {
    				}
    				else if (line.startsWith(IMPORT_RESPONSE_DELETE_EVENT)) {
    					monitor.subTask(ImpexI18N.ImportJob_DeletingEvent);
    					System.out.println(ImpexI18N.ImportJob_DeletingEvent);
    				}
    				else if (line.startsWith(IMPORT_RESPONSE_DONE)) {
    					// calculate work
    					int beginPos = IMPORT_RESPONSE_DONE.length();
    					int endPos = line.indexOf(" ");
    					long totalDone = TypeHelper.toLong( line.substring(beginPos, endPos) );

    					System.out.println("Imported: " + FormatHelper.formatBytes(totalDone));
    				}
    				else if (line.startsWith(IMPORT_RESPONSE_TABLE)) {
    					String table = line.substring(IMPORT_RESPONSE_TABLE.length());
    					String name = ImpexI18N.ImportJob_ImportingTableData;
    					name = name.replace("<table>", table);
    					monitor.subTask(name);
    					System.out.println(name);
    				}
    				else if (line.startsWith(IMPORT_RESPONSE_FILE)) {
    					String fileName = line.substring(IMPORT_RESPONSE_FILE.length() );
    					String name = ImpexI18N.ImportJob_ImportingFile;
    					name = name.replace("<file>", fileName);
    					monitor.subTask(name);
    					System.out.println(name);
    				}
    				else if (line.startsWith(IMPORT_RESPONSE_END)) {
    					// ignore
    				}
    				else if (line.startsWith(IMPORT_RESPONSE_ERROR)) {
    					final String errorMessage = line.substring(IMPORT_RESPONSE_ERROR.length());

    					// read stack trace from input stream
    					final StringBuilder stackTrace = new StringBuilder(32000);
    					while ((line = responseBufferedReader.readLine()) != null) {
    						stackTrace.append(line);
    						stackTrace.append('\n');
    					}


    					/* Uncomment the following code to show an additional error message that includes the stacktrace.
    					 * Otherwise only the standard error message is shown, because of returning IStatus.ERROR.
    					 * But this does not include the stack trace.
    					 * However, the stacktrace is printed to system error anyway.
    					 */
    					// show error message dialog
    					SWTHelper.syncExecDisplayThread(new Runnable() {
    						@Override
    						public void run() {
    							try {
    								MessageDetailsDialog.openError(
    									"Importfehler",			// title T O D O: EmailI18N
    									errorMessage,			// message
    									stackTrace.toString()	// details
    								);
    							}
    							catch (Exception e) {
    								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
    							}
    						}
    					});


    					// print error message and stacktrace to system error
    					System.err.println(errorMessage);
    					System.err.println(stackTrace);

    					// return with error status
    					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage);
    				}

    				if (monitor.isCanceled()) {
    					// close httpResponse to stop the import (closing inputStream does not work)
    					httpResponse.close();
    					System.out.println("Import cancelled!");
    					return Status.CANCEL_STATUS;
    				}

    			}
			}


			updateModelData();

			return Status.OK_STATUS;
		}
		catch (Throwable t) {
			String message = t.getLocalizedMessage();
			if (StringHelper.isEmpty(message)) {
				message = ImpexI18N.ImportJob_ErrorMessage + ".";
			}
			else {
				message = ImpexI18N.ImportJob_ErrorMessage + ": " + message;
			}

			System.err.println(message);

			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, t);
		}
		finally {
			HttpClientUtils.closeQuietly(httpClient);
			HttpClientUtils.closeQuietly(httpResponse);
			StreamHelper.closeQuietly(responseInputStream);
			StreamHelper.closeQuietly(fileInputStream);

			// finish monitor
			monitor.done();
			System.out.println("Import done!");
		}
	}


	private void updateModelData() throws Exception {
		// refresh Models of data that is available and not ignored

		// Event
		if (importMetadata.isEventAvailable() && ! importSettings.isIgnoreEvent()) {
			EventGroupModel.getInstance().refresh();
			EventModel.getInstance().refresh();

			if ( !importSettings.isIgnoreSecondaryData() ) {
				PaymentSystemSetupModel.getInstance().refresh();
				ProgrammePointTypeModel.getInstance().refresh();
				ParticipantTypeModel.getInstance().refresh();
				HotelChainModel.getInstance().refresh();
				HotelModel.getInstance().refresh();
				RoomDefinitionModel.getInstance().refresh();
				UserReportDirListModel.getInstance().refresh();
				UserReportListModel.getInstance().refresh();
			}
		}

		// Country
		if (importMetadata.isCountryAvailable() && ! importSettings.isIgnoreCountry()) {
			CountryModel.getInstance().refresh();
		}

		// Language
		if (importMetadata.isLanguageAvailable() && ! importSettings.isIgnoreLanguage()) {
			LanguageModel.getInstance().refresh();
		}

		// Currency
		if (importMetadata.isCurrencyAvailable() && ! importSettings.isIgnoreCurrency()) {
			CurrencyModel.getInstance().refresh();
		}

		// CreditCardType
		if (importMetadata.isCreditCardTypeAvailable() && ! importSettings.isIgnoreCreditCardType()) {
			CreditCardTypeModel.getInstance().refresh();
		}

		// PayEngineSetup
		if (importMetadata.isPaymentSystemSetupAvailable() && ! importSettings.isIgnorePaymentSystemSetup()) {
			PaymentSystemSetupModel.getInstance().refresh();
		}

		// Accountancy
		if (importMetadata.isAccountancyAvailable() && ! importSettings.isIgnoreAccountancy()) {
			CostCenter1Model.getInstance().refresh();
			CostCenter2Model.getInstance().refresh();
			CustomerAccountModel.getInstance().refresh();
			ImpersonalAccountModel.getInstance().refresh();
		}

		// GateDevice
		if (importMetadata.isGateDeviceAvailable() && ! importSettings.isIgnoreGateDevice()) {
			GateDeviceModel.getInstance().refresh();
		}

		// ProgrammePointType
		if (importMetadata.isProgrammePointTypeAvailable() && ! importSettings.isIgnoreProgrammePointType()) {
			ProgrammePointTypeModel.getInstance().refresh();
		}

		// ParticipantState
		if (importMetadata.isParticipantStateAvailable() && ! importSettings.isIgnoreParticipantState()) {
			ParticipantStateModel.getInstance().refresh();
		}

		// ParticipantType
		if (importMetadata.isParticipantTypeAvailable() && ! importSettings.isIgnoreParticipantType()) {
			ParticipantTypeModel.getInstance().refresh();
		}

		// Hotel
		if (importMetadata.isHotelAvailable() && ! importSettings.isIgnoreHotel()) {
			HotelChainModel.getInstance().refresh();
			HotelModel.getInstance().refresh();
			RoomDefinitionModel.getInstance().refresh();
		}

		// User
		if (importMetadata.isUserAvailable() && ! importSettings.isIgnoreUser()) {
			UserAccountModel.getInstance().refresh();
			UserGroupModel.getInstance().refresh();
		}

		// Report
		if (importMetadata.isReportAvailable() && ! importSettings.isIgnoreReport()) {
			UserReportDirListModel.getInstance().refresh();
			UserReportListModel.getInstance().refresh();
		}

	}

}

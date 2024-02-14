package de.regasus.email.dispatch;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.email.EmailDispatchOrderModel;

public class DispatchMailJob extends Job {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private SmtpSettingsVO smtpSettings;

	private Long emailTemplateID;

	private List<Long> abstractPersonIds;

	private DispatchMode dispatchMode;

	private static int DEFAULT_DISPATCH_CHUNK_SIZE = 10;

	public DispatchMailJob(
		SmtpSettingsVO settings,
		Long emailTemplateID,
		List<Long> abstractPersonIds,
		DispatchMode dispatchMode
	) {
		super(EmailLabel.Dispatch.getString());

		this.smtpSettings = settings;
		this.emailTemplateID = emailTemplateID;
		this.abstractPersonIds = abstractPersonIds;
		this.dispatchMode = dispatchMode;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			EmailDispatchOrderModel emailDispatchOrderModel = EmailDispatchOrderModel.getInstance();


			if (dispatchMode == DispatchMode.IMMEDIATE_CLIENT) {
				log.debug("DispatchMode is IMMEDIATE_CLIENT: Calling EmailDispatchOrderModel.dispatchImmediatelyFromClient(...)");

				EmailDispatchOrder dispatchOrder = emailDispatchOrderModel.dispatchImmediatelyFromClient(
					smtpSettings,
					emailTemplateID,
					abstractPersonIds
				);
				showResult(dispatchOrder);
			}
			else if (dispatchMode == DispatchMode.IMMEDIATE_SERVER) {
				log.debug("DispatchMode is IMMEDIATE_SERVER: Calling EmailDispatchOrderModel.dispatchImmediatelyOnServer(...) in chunks of " + smtpSettings.getChunkSize());

				// Remote call, the EmailDispatchService acts in the servers VM
				monitor.beginTask(EmailLabel.Sending.getString(), abstractPersonIds.size() );
				List<EmailDispatchOrder> dispatchOrders = new ArrayList<>();


				/* If the number of emails (one for each abstractPersonId) exceeds chunkSize, the emails to be sent
				 * are devided into chunks. This is done by the server as well but for another reason. The server
				 * does it to prevent the SMTP server from too many emails at a time. Some email servers have
				 * limitations, e.g. not more than 30 emails per minute.
				 *
				 * Here, we divide to avoid running into an HTTP timeout!
				 *
				 * But even here we have to assure to send not more than chunkSize email per timeIntercal seconds.
				 * The reason is that our chunking here abrogates the chunking on the server side, because each
				 * request to send emails contains not more than chunkSize emails to be sent. So the server actually
				 * does not chunk at all and the server handles each request independently.
				 */

				int chunkSize = smtpSettings.getChunkSize();
				if (chunkSize == 0) {
					// deactivate chunking
					chunkSize = Integer.MAX_VALUE;
				}
				int timeInterval = smtpSettings.getTimeInterval();

				log.debug("Sending " + abstractPersonIds.size() + " emails (from server) (chunkSize: " + chunkSize + ", timeInterval: " + timeInterval + ")");

				long sleepTime = 0L;
				long startTime = 0L;

				int fromIndex = 0;
				do {
					// wait before starting the next dispatch
					if (sleepTime > 0) {
						log.debug("Waiting " + sleepTime + " ms before continuing with next email dispatch");
						Thread.sleep(sleepTime);
					}


					// Build a sublist of mails to be sent as chunk
					int toIndex = Math.min(abstractPersonIds.size(), fromIndex + chunkSize);
					List<Long> subList = new ArrayList<>(abstractPersonIds.subList(fromIndex, toIndex));

					// send a chunkSize of emails
					log.debug("Starting next chunk of " + subList.size() + " emails");
					{
    					// Show intermediate information which emails are to be sent now
    					String taskName;
    					if (toIndex > fromIndex + 1) {
    						taskName = EmailLabel.Email.getString() + " " + (fromIndex+1) + " - " + toIndex;
    					}
    					else {
    						taskName = EmailLabel.Email.getString() + " " + (fromIndex+1);
    					}

    					monitor.setTaskName(taskName);

    					// Send and store the result
    					startTime = System.currentTimeMillis();
    					EmailDispatchOrder dispatchOrder = emailDispatchOrderModel.dispatchImmediatelyOnServer(
    						smtpSettings,
    						emailTemplateID,
    						subList,
    						null // variables
    					);
    					dispatchOrders.add(dispatchOrder);

    					// Give information that the chunk is done
    					monitor.worked(subList.size());
					}
					log.debug("Finished current chunk of " + subList.size() + " emails");

					fromIndex = toIndex;

					// calculate next sleep time
					long duration = System.currentTimeMillis() - startTime;
					sleepTime = (timeInterval * 1000L) - duration;
				}
				while (fromIndex < abstractPersonIds.size());

				log.debug("Finished sending " + abstractPersonIds.size() + " emails");

				showResult(dispatchOrders);
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}


	private void showResult(final EmailDispatchOrder dispatchOrder) {
		List<EmailDispatchOrder> singleton = Collections.singletonList(dispatchOrder);
		showResult(singleton);
	}


	private void showResult(final List<EmailDispatchOrder> dispatchOrders) {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				I18NPattern message = EmailDispatchOrder.getStatusCountMessage(dispatchOrders);
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), UtilI18N.Info, message.getString());
			}
		});
	}

}

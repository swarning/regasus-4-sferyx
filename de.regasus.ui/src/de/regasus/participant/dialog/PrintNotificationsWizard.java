package de.regasus.participant.dialog;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.report.od.DocumentFormat;

import de.regasus.I18N;

public class PrintNotificationsWizard extends Wizard {

	private List<? extends IParticipant> participants;

	private NotificationOverviewPage notificationOverviewPage;

	private NotificationTemplatesPage notificationTemplatesPage;

	private NotificationPrintOptionsPage notificationPrintOptionsPage;


	// **************************************************************************
	// * Constructors
	// *

	public PrintNotificationsWizard(List<? extends IParticipant> participants) {
		this.participants = participants;
	}


	// **************************************************************************
	// * Overridden Methods
	// *

	@Override
	public void addPages() {
		notificationOverviewPage = new NotificationOverviewPage(participants);
		addPage(notificationOverviewPage);

		notificationTemplatesPage = new NotificationTemplatesPage(participants);
		addPage(notificationTemplatesPage);

		notificationPrintOptionsPage = new NotificationPrintOptionsPage();
		notificationTemplatesPage.addTemplateChangeListener(notificationPrintOptionsPage);
		addPage(notificationPrintOptionsPage);
	}


	@Override
	public String getWindowTitle() {
		return I18N.PrintNotifications;
	}


	@Override
	public boolean performFinish() {
		List<Long> templatePKs = notificationTemplatesPage.getCheckedTemplatePKs();

		boolean markProgramPointBookingsAsConfirmed =
			notificationPrintOptionsPage.isMarkProgrammeBookingsAsConfirmed();
		
		boolean markHotelPointBookingsAsConfirmed = notificationPrintOptionsPage.isMarkHotelBookingsAsConfirmedButton();

		DocumentFormat format = notificationPrintOptionsPage.getSelectedFormat();

		boolean shouldPrint = notificationPrintOptionsPage.shouldPrint();

		String printer = notificationPrintOptionsPage.getPrinterName();

		PrintNotificationsJob printNotificationsJob = new PrintNotificationsJob(
			participants,
			templatePKs,
			format,
			shouldPrint,
			printer,
			markHotelPointBookingsAsConfirmed,
			markProgramPointBookingsAsConfirmed
		);

		printNotificationsJob.setUser(true);
		printNotificationsJob.schedule();

		return true;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		return 
			notificationOverviewPage.getBookingNoteCount() > 0 && 
			notificationTemplatesPage.getCheckedTemplatePKs().size() > 0 &&
			notificationPrintOptionsPage.getSelectedFormat() != null;
	}
	
}

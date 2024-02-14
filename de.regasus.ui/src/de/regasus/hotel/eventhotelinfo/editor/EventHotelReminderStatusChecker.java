package de.regasus.hotel.eventhotelinfo.editor;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.hotel.Hotel;
import com.lambdalogic.messeinfo.hotel.HotelMessage;
import com.lambdalogic.messeinfo.hotel.data.EventHotelReminderStatus;
import com.lambdalogic.messeinfo.hotel.data.EventHotelReminderVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.model.StatusChecker;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.event.EventModel;
import de.regasus.hotel.EventHotelInfoModel;
import de.regasus.hotel.HotelModel;

public class EventHotelReminderStatusChecker implements StatusChecker {
	
	private static boolean isShowingDialog = false;
	

	@Override
	public void checkStatus() {
		try {
			final List<EventHotelReminderVO> failedReminders = getEventHotelInfoMgr().getFailedRemindersOfCurrentUser();
			if (CollectionsHelper.notEmpty(failedReminders)) {
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						showInfoToUserAboutFailedReminders(failedReminders);
					}
				});
			}
		}
		catch(Throwable t) {
			System.err.println("No status check possible: " + t.getMessage());
		}
	}

	
	private void showInfoToUserAboutFailedReminders(List<EventHotelReminderVO> failedReminders) {
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			if (shell.isVisible() && ! shell.getMinimized() && ! isShowingDialog) {
				String title = UtilI18N.Error;
				String[] buttons = {UtilI18N.Show, UtilI18N.Confirm, HotelMessage.RemindMeLater.getString()}; 
				
				HotelModel hotelModel = HotelModel.getInstance();
				EventModel eventModel = EventModel.getInstance();
				
				StringBuilder sb = new StringBuilder();
				sb.append(HotelMessage.SomeEventHotelRemindersCouldNotBeSent.getString());
				sb.append("\n\n");
				for (EventHotelReminderVO reminder : failedReminders) {
					
					Hotel hotel = hotelModel.getHotel(reminder.getHotelPK());
					String hotelName = hotel.getName1();
					
					EventVO eventVO = eventModel.getEventVO(reminder.getEventPK());
					String eventName = eventVO.getName(Locale.getDefault());
					
					sb.append(eventName);
					sb.append("\n");
					sb.append(hotelName);
					sb.append("\n\n");
				}
				String message = sb.toString();
				
				MessageDialog dialog = new MessageDialog(
					shell,					// parentShell
					title,					// dialogTitle
					null,					// dialogTitleImage
					message,				// dialogMessage 
					MessageDialog.ERROR,	// dialogImageType 
					buttons,				// dialogButtonLabels
					0						// defaultIndex
				);
				
				/* Avoid multiple dialogs when an error state is detected, but the user is absent 
				 * and doesn't close the dialog.
				 */
				isShowingDialog = true;
				int result = dialog.open();
				isShowingDialog = false;
				
				if (result == 0) {
					acknowledge(failedReminders);
					openEditorsFor(failedReminders);
				} 
				else if (result == 1) {
					acknowledge(failedReminders);
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

	
	/**
	 * Set the status of the mentioned failed reminders to acknowledged on the server  
	 * @param failedReminders
	 */
	private void acknowledge(List<EventHotelReminderVO> failedReminders) {
		List<Long> eventHotelReminderPKs = EventHotelReminderVO.getPKs(failedReminders);
		
		// We bypass the EventHotelInfoModel here, because we don't want to load unnecessarily
		// lots of EventHotelInfos and their associated other Reminders 
		getEventHotelInfoMgr().setStatusOfEventHotelReminders(EventHotelReminderStatus.ERROR_ACK, eventHotelReminderPKs);
		
		// However, for those EventHotelInfos that are already loaded, fire a refresh 
		List<Long> eventHotelInfoPKs = new ArrayList<Long>();
		for (EventHotelReminderVO reminder : failedReminders) {
			eventHotelInfoPKs.add(reminder.getEventHotelInfoPK());
		}
		try {
			EventHotelInfoModel.getInstance().refresh(eventHotelInfoPKs);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}
	

	private void openEditorsFor(List<EventHotelReminderVO> failedReminders) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		for (EventHotelReminderVO reminderVO : failedReminders) {
			
			Long hotelID = reminderVO.getHotelPK();
			Long eventID = reminderVO.getEventPK();
			EventHotelInfoEditorInput editorInput = new EventHotelInfoEditorInput(eventID, hotelID);
			editorInput.setShowReminderTab(true);
			try {
				page.openEditor(editorInput, EventHotelInfoEditor.ID);
			}
			catch (PartInitException e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}

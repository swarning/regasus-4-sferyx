package de.regasus.participant.badge;

import static de.regasus.LookupService.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.lambdalogic.messeinfo.exception.BadgeDemandsFullPaymentException;
import com.lambdalogic.messeinfo.exception.DoubleBadgeException;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.participant.data.BadgeDocumentCVO;
import com.lambdalogic.util.ArrayHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.auth.AuthorizationException;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.dialog.AuthenticationDialog;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.badge.pref.BadgePrintPreference;
import de.regasus.ui.Activator;

/**
 * Enthält die Logik zum Drucken von Badges.
 */
public class BadgePrintController {

	private int answer;

	private Boolean authenticated = Boolean.FALSE;


	public BadgePrintController() {
	}


	public void createBadgeWithDocument(Long participantPK, String name, Integer number)
	throws InvalidValuesException, AuthorizationException, ErrorMessageException {
		createBadgeWithDocument(
			participantPK,
			name,
			number,
			false,	// withCancel
			true	// checkDoubleBadges
		);
	}


	public void createBadgeWithDocument(final List<BadgePrintData> badgePrintDataList)
	throws InvalidValuesException, AuthorizationException, ErrorMessageException {
		if (badgePrintDataList == null || badgePrintDataList.isEmpty()) {
			return;
		}

		if (badgePrintDataList.size() == 1) {
			BadgePrintData badgePrintData = badgePrintDataList.get(0);
			createBadgeWithDocument(
				badgePrintData.participantPK,
				badgePrintData.name,
				badgePrintData.number,
				false,	// withCancel
				true	// checkDoubleBadges
			);
		}
		else {
			/* Aus badgePrintDataList die herausfiltern, deren TN keine
			 * aktiviertes Badges haben.
			 */
			final List<BadgePrintData> filteredBadgePrintDataList = filterBadgePrintData(badgePrintDataList);
			final int totalCount = badgePrintDataList.size();
			final int withoutBadgeCount = filteredBadgePrintDataList.size();
			final int withBadgeCount = totalCount - withoutBadgeCount;

			final boolean[] cancel = {false};

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					if (withBadgeCount > 0) {

						// **************************************************************************
						// * Ask user how to handle double badges
						// *

						String msg = I18N.BadgePrintController_DoubleBadge_Message;
						msg = msg.replaceFirst("<totalCount>", String.valueOf(totalCount));
						msg = msg.replaceFirst("<withBadgeCount>", String.valueOf(withBadgeCount));

						String[] options = new String[] {
							I18N.BadgePrintController_DoubleBadge_Skip,
							I18N.BadgePrintController_DoubleBadge_CreateNew,
							UtilI18N.Cancel
						};

						int defaultIndex = 0;

						if (SystemHelper.isMacOSX()) {
							ArrayHelper.reverse(options);
							defaultIndex = options.length - 1;
						}

						MessageDialog dialog = new MessageDialog(
							null,	// Shell
							I18N.BadgePrintController_BadgePrintTitle,
							null,	// DialogTitleImage
							msg,
							MessageDialog.QUESTION,
							options,
							defaultIndex
						);
						int answer = dialog.open();
						if (SystemHelper.isMacOSX()) {
							if (answer != SWT.DEFAULT) {
								answer = options.length - 1 - answer;
							}
						}

						// *
						// * Ask user how to handle double badges
						// **************************************************************************

						if (answer == 0) {
							// Überspringen
							badgePrintDataList.clear();
							badgePrintDataList.addAll(filteredBadgePrintDataList);
						}
						else if (answer == 1) {
							// Neue Badges erzeugen
						}
						else if (answer == 2 || answer == SWT.DEFAULT) {
							// Cancel
							cancel[0] = true;
						}
					}
					else {
						String msg = I18N.BadgePrintController_BadgePrintMessage;
						msg = msg.replaceFirst("<totalCount>", String.valueOf(totalCount));

						String[] options = new String[] {
							UtilI18N.Continue,
							UtilI18N.Cancel
						};

						MessageDialog dialog = new MessageDialog(
							null,	// Shell
							I18N.BadgePrintController_BadgePrintTitle,
							null,	// DialogTitleImage
							msg,
							MessageDialog.QUESTION,
							options,
							0
						);
						int answer = dialog.open();

						if (answer == 1 || answer == SWT.DEFAULT) {
							// Cancel
							cancel[0] = true;
						}
					}
				}
			});

			if (!cancel[0] && !badgePrintDataList.isEmpty()) {
				BusyCursorHelper.busyCursorWhile(new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						int totalCount = badgePrintDataList.size();
						monitor.beginTask(UtilI18N.Working, totalCount);

						int count = 1;


						// read time to wait between two print jobs from preferences
						Integer waitTime = BadgePrintPreference.getInstance().getWaitTime();


						for (BadgePrintData badgePrintData : badgePrintDataList) {
							try {
								String message = I18N.PrintingBadgeForXInBracketsCountOfTotalCount;
								message = message.replace("<name>", badgePrintData.name);
								message = message.replace("<count>", String.valueOf(count));
								message = message.replace("<totalCount>", String.valueOf(totalCount));
								monitor.subTask(message);

								count++;

								boolean cancel = createBadgeWithDocument(
									badgePrintData.participantPK,
									badgePrintData.name,
									badgePrintData.number,
									true,	// withCancel
									false	// checkDoubleBadges
								);


								if (cancel) {
									break;
								}

								monitor.worked(1);
								if (monitor.isCanceled()) {
									break;
								}

								if (count < totalCount && waitTime != null && waitTime > 0) {
									// Wait the configured time before starting next print job
									message = I18N.BadgePrintController_WaitMessage;
									message = message.replaceAll("<waitTime>", String.valueOf(waitTime));
									monitor.subTask(message);

									Thread.sleep(waitTime);
								}
							}
							catch (Exception e) {
								RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
							}
						}
						monitor.done();
					}

				});
			}
		}
	}


	private boolean createBadgeWithDocument(
		final Long participantPK,
		final String name,
		final Integer number,
		final boolean withCancel,
		boolean checkDoubleBadges
	)
	throws InvalidValuesException, AuthorizationException, ErrorMessageException {
		BadgeDocumentCVO badgeDocumentCVO = null;

		boolean checkFullPayment = true;

		while (badgeDocumentCVO == null) {
			try {
				badgeDocumentCVO = ParticipantModel.getInstance().createBadgeWithDocument(
					participantPK,
					checkDoubleBadges,
					checkFullPayment
				);
			}
			catch (DoubleBadgeException e) {
				SWTHelper.syncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						String msg = I18N.DoubleBadgeException_Message;
						msg = msg.replaceFirst("<name>", name);
						msg = msg.replaceFirst("<number>", number.toString());

						String[] options = null;
						if (withCancel) {
							options = new String[] {I18N.DoubleBadgeException_No, I18N.DoubleBadgeException_Yes, I18N.DoubleBadgeException_Cancel};
						}
						else {
							options = new String[] {I18N.DoubleBadgeException_No, I18N.DoubleBadgeException_Yes};
						}

						int defaultIndex = 0;

						if (SystemHelper.isMacOSX()) {
							ArrayHelper.reverse(options);
							defaultIndex = options.length - 1;
						}

						MessageDialog dialog = new MessageDialog(
							null,	// Shell
							I18N.DoubleBadgeException_Title,
							null,	// DialogTitleImage
							msg,
							MessageDialog.QUESTION,
							options,
							defaultIndex
						);
						answer = dialog.open();
						if (SystemHelper.isMacOSX()) {
							if (answer != SWT.DEFAULT) {
								answer = options.length - 1 - answer;
							}
						}
					}
				});


				if (answer == 0) {
					// No
					break;
				}
				else if (answer == 1) {
					// Yes
					checkDoubleBadges = false;
				}
				else {
					// Cancel: answer == 2 || answer == SWT.DEFAULT
					return true;
				}
			}
			catch (BadgeDemandsFullPaymentException e) {

				SWTHelper.syncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						String msg = I18N.BadgeDemandsFullPaymentException_Message;
						msg = msg.replaceFirst("<name>", name);
						msg = msg.replaceFirst("<number>", number.toString());

						String[] options = null;
						if (withCancel) {
							options = new String[] {I18N.BadgeDemandsFullPaymentException_No, I18N.BadgeDemandsFullPaymentException_Yes, I18N.BadgeDemandsFullPaymentException_Cancel};
						}
						else {
							options = new String[] {I18N.BadgeDemandsFullPaymentException_No, I18N.BadgeDemandsFullPaymentException_Yes};
						}

						int defaultIndex = 0;

						if (SystemHelper.isMacOSX()) {
							ArrayHelper.reverse(options);
							defaultIndex = options.length - 1;
						}

						MessageDialog dialog = new MessageDialog(
							null,	// Shell
							//Display.getDefault().getActiveShell(),	// Shell
							I18N.BadgeDemandsFullPaymentException_Title,
							null,	// DialogTitleImage
							msg,
							MessageDialog.QUESTION,
							options,
							defaultIndex
						);

						answer = dialog.open();
						if (SystemHelper.isMacOSX()) {
							if (answer != SWT.DEFAULT) {
								answer = options.length - 1 - answer;
							}
						}
					}
				});


				if (answer == 0) {
					// No
					break;
				}
				else if (answer == 1) {
					// Yes
					if (authenticated != Boolean.TRUE) {
						// Nutzername und Passwort abfragen und prüfen, ob diese das Recht haben ...
						Boolean authticateResult = authenticate();
						if (authticateResult != null) {
							authenticated = authticateResult;
						}
						else {
							// Cancel
							return true;
						}
					}

					if (authenticated) {
						checkFullPayment = false;
					}
				}
				else {
					// Cancel: answer == 2 || answer == SWT.DEFAULT
					return true;
				}
			}
			catch (Throwable e) {
				return handleError(e);
			}
		}


		if (badgeDocumentCVO != null) {
			try {
				System.out.println(
					"Printing Badge: Badge-No. = " + badgeDocumentCVO.getBadgeVO().getBadgeNo() +
					", Participant-ID = " + badgeDocumentCVO.getBadgeVO().getParticipantPK()
				);

				/* save and print generated badge file
				 * This code is referenced by
				 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
				 * Adapt the wiki document if this code is moved to another class or method.
				 */
				badgeDocumentCVO.getDocumentContainer().print();
			}
			catch (Exception e) {
				return handleError(e);
			}
		}

		return false;
	}


	/**
	 * Liefert eine Liste ohne die TN, die bereits ein Badge haben.
	 *
	 * @param badgePrintDataList
	 * @return
	 * @throws AuthorizationException
	 */
	public static List<BadgePrintData> filterBadgePrintData(List<BadgePrintData> badgePrintDataList)
	throws AuthorizationException {
		// PKs isolieren
		List<Long> participantPKs = new ArrayList<>(badgePrintDataList.size());
		for (BadgePrintData badgePrintData : badgePrintDataList) {
			participantPKs.add(badgePrintData.participantPK);
		}

		// Participant-PKs, der TN entfernen, die ein aktiviertes Badge haben
		participantPKs = getBadgeMgr().getParticipantPKsWithoutBadge(participantPKs);

		// badgePrintDataList
		List<BadgePrintData> resultBadgePrintDataList = new ArrayList<>(participantPKs.size());

		Iterator<Long> participantPKsIter = participantPKs.iterator();
		Iterator<BadgePrintData> badgePrintDataListIter = badgePrintDataList.iterator();

		/* Kopiere alle BadgePrintData aus badgePrintDataList, deren
		 * participantPK in participantPKs enthalten ist.
		 *
		 * Hinweis: Die Elemente in badgePrintDataList und badgePrintDataList sind
		 *          analog sortiert.
		 *
		 * Es wird über participantPKs iteriert und für jeden ParticipantPK
		 * das passende BadgePrintData gesucht. Wegen der analogen Sortierung
		 * muss badgePrintDataList nur einmal durchlaufen werden!
		 */
		while (participantPKsIter.hasNext()) {
			Long participantPK = participantPKsIter.next();

			// finde passendes BadgePrintData
			while (badgePrintDataListIter.hasNext()) {
				BadgePrintData badgePrintData = badgePrintDataListIter.next();
				if (badgePrintData.participantPK.equals(participantPK)) {
					resultBadgePrintDataList.add(badgePrintData);
					break;
				}
			}
		}

		return resultBadgePrintDataList;
	}


	public static boolean handleError(final Throwable e) {
		final int[] answer = {-1};

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {

				MessageDialog dialog = new MessageDialog(
					null,	// Shell
					I18N.BadgeException_Title,
					null,	// DialogTitleImage
					e.getMessage(),
					MessageDialog.INFORMATION,
					new String[] {UtilI18N.OK, UtilI18N.Cancel},
					0
				);

				answer[0] = dialog.open();
			}
		});

		return answer[0] == 1;
	}


	/**
	 * Fordert den Nutzer auf, sich zu authentifizieren.
	 * @return TRUE: Der Nutzer hat sich erfolgreich authentifiziert
	 * 		   FALSE: TRUE: Der Nutzer hat sich nicht authentifiziert
	 *         null: Der Nutzer hat den Vorgang abgebrochen.
	 * @throws AuthorizationException
	 */
	public static Boolean authenticate() throws AuthorizationException {
		final Boolean[] authenticated = {Boolean.FALSE};

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {

				try {
					String userName = null;
					int counter = 0;
					while (authenticated[0] == Boolean.FALSE) {
						counter++;

						final AuthenticationDialog authenticationDialog = new AuthenticationDialog(Display.getDefault().getActiveShell());
						if (counter > 1) {
							authenticationDialog.setFailedMessage();
							if (userName != null) {
								authenticationDialog.setUserName(userName);
							}
						}

						int authenticationDialogResult = authenticationDialog.open();
						if (authenticationDialogResult == 0) {
							userName = authenticationDialog.getUserName();
							String password = authenticationDialog.getPassword();

							authenticated[0] = getUserMgr().checkUserPassword(userName, password);
						}
						else {
							authenticated[0] = null;
						}
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}

			}
		});

		return authenticated[0];
	}


//	private void handleOpenOffice() {
//		Boolean openOfficeRunning = OpenOfficeHelper.isOpenOfficeRunning();
//		if (openOfficeRunning == null) {
//			openOfficeRunning = Boolean.FALSE;
//		}
//		if (!openOfficeRunning) {
//			String[] options = {UtilI18N.Yes, UtilI18N.No, UtilI18N.Cancel};
//			int defaultIndex = 0;
//			if (SystemHelper.isMacOSX()) {
//				ArrayHelper.reverse(options);
//				defaultIndex = options.length - 1;
//			}
//
//			MessageDialog dialog = new MessageDialog(
//				null,	// Shell
//				EmailI18N.BadgePrintController_OpenOfficeDlg_Title,
//				null,	// DialogTitleImage
//				EmailI18N.BadgePrintController_OpenOfficeDlg_Message,
//				MessageDialog.QUESTION,
//				options,
//				defaultIndex
//			);
//
//
//			answer = dialog.open();
//			if (SystemHelper.isMacOSX()) {
//				if (answer != SWT.DEFAULT) {
//					answer = options.length - 1 - answer;
//				}
//			}
//
//			if (answer == 0) {
//				// Yes
//				try {
//					OpenOfficeHelper.startOpenOffice();
//				}
//				catch (IOException e) {
//					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//				}
//			}
//			else if (answer == 1) {
//				// No
//			}
//			else if (answer == 2 || answer == SWT.DEFAULT) {
//				// Cancel
//				return;
//			}
//		}
//	}

}

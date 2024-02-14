package de.regasus.participant.badge;

import static de.regasus.LookupService.getBadgeMgr;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;

import com.lambdalogic.messeinfo.exception.BadgeDemandsFullPaymentException;
import com.lambdalogic.messeinfo.exception.DoubleBadgeException;
import com.lambdalogic.messeinfo.exception.InvalidValuesException;
import com.lambdalogic.messeinfo.participant.ParticipantMessage;
import com.lambdalogic.messeinfo.participant.data.BadgeVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.ArrayHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.error.ErrorHandler.ErrorLevel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.auth.AuthorizationException;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

/**
 * Print Badges.
 */
public class BadgeOpenController {

	private int answer;

	private Boolean authenticated = Boolean.FALSE;

	private List<BadgeVO> createBadgeVOs = new ArrayList<>();

	public BadgeOpenController() {
	}


	public void createAndOpenBadge(Long participantPK, String name, Integer number)
	throws InvalidValuesException, AuthorizationException, ErrorMessageException {
		BadgePrintData badgePrintData = new BadgePrintData();
		badgePrintData.participantPK = participantPK;
		badgePrintData.name = name;
		badgePrintData.number = number;

		List<BadgePrintData> badgePrintDataList = new ArrayList<>(1);
		badgePrintDataList.add(badgePrintData);

		createAndOpenBadge(badgePrintDataList);
	}


	public void createAndOpenBadge(final List<BadgePrintData> badgePrintDataList)
	throws InvalidValuesException, AuthorizationException, ErrorMessageException {
		if (badgePrintDataList == null || badgePrintDataList.isEmpty()) {
			return;
		}

		if (badgePrintDataList.size() == 1) {
			BadgePrintData badgePrintData = badgePrintDataList.get(0);
			createBadge(
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
			final List<BadgePrintData> filteredBadgePrintDataList = BadgePrintController.filterBadgePrintData(badgePrintDataList);
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

						for (BadgePrintData badgePrintData : badgePrintDataList) {
							try {
								String message = I18N.PrintingBadgeForXInBracketsCountOfTotalCount;
								message = message.replace("<name>", badgePrintData.name);
								message = message.replace("<count>", String.valueOf(count));
								message = message.replace("<totalCount>", String.valueOf(totalCount));
								monitor.subTask(message);

								count++;

								boolean cancel = createBadge(
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


		// generate the document
		List<Long> badgePKs = BadgeVO.getPKs(createBadgeVOs);

		try {
			List<DocumentContainer> documentContainerList = getBadgeMgr().getBadgeDocumentList(
				badgePKs,
				null // String formatKey
			);

			if (documentContainerList != null) {
				for (DocumentContainer documentContainer : documentContainerList) {
					/* save and open generated badge file
					 * This code is referenced by
					 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
					 * Adapt the wiki document if this code is moved to another class or method.
					 */
					documentContainer.open();
				}
			}
		}
		catch (ErrorMessageException e) {
			if (e.getErrorCode().equals(ParticipantMessage.EventHasNoBadgeTemplate.name())) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e, ErrorLevel.USER);
			}
			else {
				throw e;
			}
		}
		catch (IOException e) {
			BadgePrintController.handleError(e);
		}
	}


	private boolean createBadge(
		final Long participantPK,
		final String name,
		final Integer number,
		final boolean withCancel,
		boolean checkDoubleBadges
	)
	throws InvalidValuesException, AuthorizationException, ErrorMessageException {
		BadgeVO badgeVO = null;

		boolean checkFullPayment = true;

		while (badgeVO == null) {
			try {
				badgeVO = ParticipantModel.getInstance().createBadge(
					participantPK,
					checkDoubleBadges,
					checkFullPayment
				);

				if (badgeVO != null) {
					createBadgeVOs.add(badgeVO);
				}
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

				final String details = e.getI18NMessage().getString();

				SWTHelper.syncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						String msg = I18N.BadgeDemandsFullPaymentException_Message;
						msg = msg.replaceFirst("<name>", name);
						msg = msg.replaceFirst("<number>", number.toString());
						msg = msg.replaceFirst("<details>", details);

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
						Boolean authticateResult = BadgePrintController.authenticate();
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
				return BadgePrintController.handleError(e);
			}
		}

		return false;
	}

}

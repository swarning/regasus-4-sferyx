package de.regasus.email.dispatch.command;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.i18n.I18NPattern;
import com.lambdalogic.messeinfo.email.DispatchStatus;
import com.lambdalogic.messeinfo.email.EmailDispatch;
import com.lambdalogic.messeinfo.email.EmailDispatchOrder;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailMessage;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.SmtpHelper;
import com.lambdalogic.messeinfo.email.SmtpSettingsVOHelper;
import com.lambdalogic.messeinfo.email.data.SmtpSettingsVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.IParticipant;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.Property;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailDispatchOrderModel;
import de.regasus.email.dispatch.DispatchCommandMode;
import de.regasus.email.dispatch.DispatchMailJob;
import de.regasus.email.dispatch.DispatchMode;
import de.regasus.email.dispatch.dialog.EmailDispatchWizard;
import de.regasus.email.template.search.view.EmailTemplateSearchView;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantProvider;
import de.regasus.participant.ParticipantSelectionHelper;
import de.regasus.profile.editor.ProfileEditor;
import de.regasus.profile.search.ProfileSearchView;
import de.regasus.ui.Activator;

/**
 * When a set of participants, profiles, or one email template is selected, this Handler opens a wizard to select the
 * missing information (template, recipients), possibly enter different {@link SmtpSettingsVO}, and determines the
 * {@link DispatchMode}.
 * <p>
 * One {@link EmailDispatchOrder} and an according number of {@link EmailDispatch}es are created in the database, all
 * with the {@link DispatchStatus#NEW}.
 * <p>
 * Depending on the {@link DispatchMode}, they are either tried to be sent immediately, either on the client or on the
 * server, or at a scheduled time on the server.
 */
public class SendEmailCommandHandler extends AbstractHandler {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );


	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		try {
			DispatchCommandMode mode = null;
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			IWorkbenchPart part = HandlerUtil.getActivePart(event);

			log.debug("Current selection: " + selection);
			log.debug("Current part: " + part);

			final List<Long> abstractPersonIds = new ArrayList<>();
			final SmtpSettingsVO settings = new SmtpSettingsVO();
			EventVO eventVO = null;
			Long emailTemplateID = null;

			List<IParticipant> participantList = null;
			if (selection != null) {
				participantList = ParticipantSelectionHelper.getParticipants(selection, false);
			}

			if (participantList != null && !participantList.isEmpty()) {
				log.debug("Participants from current selection: " + participantList.size());
				mode = DispatchCommandMode.PARTICIPANTS_SELECTED_SEARCH_TEMPLATE;

				// Collect all participant and decide whether there is a unique event
				// for which to take the
				boolean eventUnique = true;
				Long eventPK = null;
				for (IParticipant participant : participantList) {
					abstractPersonIds.add(participant.getPK());

					Long tmpEventPK = participant.getEventId();
					if (eventPK == null) {
						eventPK = tmpEventPK;
					}
					else if (!eventPK.equals(tmpEventPK)) {
						eventUnique = false;
						MessageDialog.openWarning(HandlerUtil.getActiveShell(event), UtilI18N.Warning, EmailMessage.ParticipantsFromDifferentEvents.getString());
					}
				}

				if (eventUnique && eventPK != null) {
					eventVO = EventModel.getInstance().getEventVO(eventPK);
				}
			}
			else if (part instanceof ParticipantProvider) {
				mode = DispatchCommandMode.PARTICIPANTS_SELECTED_SEARCH_TEMPLATE;
				ParticipantProvider participantProvider = (ParticipantProvider) part;
				IParticipant participant = participantProvider.getIParticipant();

				log.debug("Getting Participant from " + part + " which is ParticipantProvider: " + participant);

				abstractPersonIds.add(participant.getPK());

				// Find the event
				Long eventPK = participant.getEventId();
				eventVO = EventModel.getInstance().getEventVO(eventPK);
			}
			else if (part instanceof ProfileSearchView) {
				mode = DispatchCommandMode.PROFILES_SELECTED_SEARCH_TEMPLATE;

				for (Profile profile : SelectionHelper.toList(selection, Profile.class)) {
					abstractPersonIds.add(profile.getID());
				}

				log.debug("Getting Profiles from " + part + ": " + abstractPersonIds);
			}
			else if (part instanceof ProfileEditor) {
				mode = DispatchCommandMode.PROFILES_SELECTED_SEARCH_TEMPLATE;

				ProfileEditor profileEditor = (ProfileEditor) part;
				Profile profile = profileEditor.getProfile();
				abstractPersonIds.add(profile.getID());

				log.debug("Getting Profile from ProfileEditor: " + profile);
			}
			else if (part instanceof EmailTemplateSearchView) {
				EmailTemplateSearchView etsv = (EmailTemplateSearchView) part;
				Long eventPKLong = etsv.getEventPK();

				EmailTemplate emailTemplateSD = (EmailTemplate) SelectionHelper.getUniqueSelected(selection);
				if (emailTemplateSD != null) {
					emailTemplateID = emailTemplateSD.getID();
				}

				log.debug("Getting EmailTemplate from EmailTemplateSearchView: " + emailTemplateID);

				if (eventPKLong != null) {
					mode = DispatchCommandMode.TEMPLATE_SELECTED_SEARCH_PARTICIPANTS;
					eventVO = EventModel.getInstance().getEventVO(eventPKLong);
				}
				else {
					mode = DispatchCommandMode.GENERAL_TEMPLATE_SELECTED_SEARCH_PROFILES;
				}
			}

			if (eventVO != null) {
				settings.copyFrom(eventVO.getSmtpSettingsVO());
			}
			else {
				Collection<Property> propertyList = PropertyModel.getInstance().getPublicPropertyList();
				settings.copyFrom(new SmtpSettingsVOHelper(propertyList).createFromProperties());
			}


			EmailDispatchWizard emailDispatchWizard = new EmailDispatchWizard(eventVO, mode, settings);

			// Open a wizard to select or confirm the template and possibly to change the settings

			WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), emailDispatchWizard);
			wizardDialog.create();
			wizardDialog.getShell().setSize(600, 800);
			int code = wizardDialog.open();
			if (code == Window.OK) {

				// Fetch the settings and the selected EmailTemplate
				final DispatchMode dispatchMode = emailDispatchWizard.getDispatchMode();

				if (dispatchMode == DispatchMode.IMMEDIATE_CLIENT) {
					// Check whether on the given SMTP-Server, there is an SMTP service listening at the specified port
					if (!SmtpHelper.isSmtpServerReachable(settings.getHost(), settings.getPort())) {
						String message =
							NLS.bind(EmailLabel.SmtpServerNotReachable.getString(), settings.getHost(), settings
								.getPort());
						MessageDialog.openError(HandlerUtil.getActiveShell(event), UtilI18N.Error, message);
						return null;
					}
				}

				EmailTemplate emailTemplateSD = null;
				switch (mode) {
					case PARTICIPANTS_SELECTED_SEARCH_TEMPLATE:
						emailTemplateSD = emailDispatchWizard.getEmailTemplateSearchData();
						break;
					case PROFILES_SELECTED_SEARCH_TEMPLATE:
						emailTemplateSD = emailDispatchWizard.getEmailTemplateSearchData();
						break;
					case EVENT_SELECTED_SEARCH_TEMPLATE_AND_PARTICIPANTS:
						emailTemplateSD = emailDispatchWizard.getEmailTemplateSearchData();
						abstractPersonIds.clear();
						abstractPersonIds.addAll(emailDispatchWizard.getAbstractPersonIds());
						break;
					case TEMPLATE_SELECTED_SEARCH_PARTICIPANTS:
					case GENERAL_TEMPLATE_SELECTED_SEARCH_PROFILES:
						abstractPersonIds.clear();
						abstractPersonIds.addAll(emailDispatchWizard.getAbstractPersonIds());
						break;
				}

				if (emailTemplateSD != null) {
					emailTemplateID = emailTemplateSD.getID();
				}

				if (dispatchMode == DispatchMode.IMMEDIATE_CLIENT || dispatchMode == DispatchMode.IMMEDIATE_SERVER) {
					DispatchMailJob dispatchMailJob = new DispatchMailJob(
						settings,
						emailTemplateID,
						abstractPersonIds,
						dispatchMode
					);
					dispatchMailJob.setUser(true);
					dispatchMailJob.schedule();
				}
				else if (dispatchMode == DispatchMode.SCHEDULED_SERVER) {
					try {
						Date scheduledDate = emailDispatchWizard.getScheduledDate();

						EmailDispatchOrder dispatchOrder = EmailDispatchOrderModel.getInstance().schedule(
							settings,
							scheduledDate,
							emailTemplateID,
							abstractPersonIds
						);

						I18NPattern message = EmailDispatchOrder.getStatusCountMessage(Collections.singletonList(dispatchOrder));
						MessageDialog.openInformation(Display.getDefault().getActiveShell(), UtilI18N.Info, message.getString());
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return null;
	}

}

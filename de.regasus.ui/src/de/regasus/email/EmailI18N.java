package de.regasus.email;

import org.eclipse.osgi.util.NLS;

public class EmailI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.email.i18n-email-ui";

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, EmailI18N.class);
	}


	public static String AttachmentCouldntBeOpened;

	public static String CreateEmailTemplate_Text;
	public static String CreateEmailTemplate_ToolTip;

	public static String DeleteEmailTemplate_Text;
	public static String DeleteEmailTemplate_ToolTip;
	public static String DeleteSelectedAttachments;

	public static String EditEmailTemplate_Text;
	public static String EditEmailTemplate_ToolTip;
	public static String EmailAndSmtpSettings_ToolTip;
	public static String EmailSettings_ProduceAndSendOnClient;
	public static String EmailSettings_ProduceAndSendOnServer;
	public static String EmailSettings_ProduceAndSendOnServerScheduled;
	public static String EmailSettings_ScheduledDate;
	public static String Error_NoSmtpHost;
	public static String Error_ScheduledDispatchInPast;
	public static String EventSpecificTemplates;

	public static String GenericTemplate;
	public static String GenericTemplates;
	public static String InvalidDataForDispatch;
	public static String NewEmailTemplate;
	public static String NoStoredEmailContents;
	public static String NotDeletedSinceDispatchOrdersExist;
	public static String NotSameEvent;

	public static String RefreshEmailTemplate_Text;
	public static String RefreshEmailTemplate_ToolTip;

	public static String SelectSampleRecipient;
	public static String SmtpSettingsPage_Event_Description;
	public static String SmtpSettingsPage_NoEvent_Description;
	public static String SmtpSettingsPage_ThisDispatch_Description;

	public static String TemplateSelection;

	public static String DuplicateToSameEvent;
	public static String DuplicateAsEventIndependentTemplate;
	public static String DuplicateToOtherEvent;

	public static String DuplicateEmailTemplate_ToolTip;

	public static String DuplicateEmailTemplate_Text;

	public static String ShowOnlyUnsuccessfulEmailDispatches;

	public static String CouldNotDeleteEmailTemplates_ExistingEmailDispatchOrders_DeleteAnywayQuestion;

	public static String DuplicationSettings;

	public static String NoInvoiceNumberRangesForSelectedEvent;

	public static String SelectEventToCopyEmailTemplateInto;

	public static String DispatchModePreferencePage_Description;

	public static String DispatchModePreferencePage_Mode;

	public static String DispatchMode_IMMEDIATE_CLIENT;

	public static String DispatchMode_IMMEDIATE_SERVER;

	public static String DispatchMode_SCHEDULED_SERVER;

	public static String WebId;
	public static String WebId_desc;

	public static String UsageByOnlineForm;

	public static String MissingSampleParticipant;


	private EmailI18N() {
	}

}

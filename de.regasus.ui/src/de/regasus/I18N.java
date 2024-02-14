package de.regasus;

import org.eclipse.osgi.util.NLS;

public class I18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.i18n-regasus-ui";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, I18N.class);
	}


	private I18N() {
	}


	public static String Label;
	public static String Tooltip;

	public static String Speaker;
	public static String Location;
	public static String Locations;
	public static String Places;
	public static String ParticipantEditor;

	public static String ProofProvidedSetToFalseMessage;

	public static String BadgeTemplate;

	public static String TemplateDelete_Text;
	public static String TemplateDelete_ToolTip;

	public static String TemplateDownload_Text;
	public static String TemplateDownload_ToolTip;

	public static String TemplateEdit_Text;
	public static String TemplateEdit_ToolTip;

	public static String EditExistingTemplateFile_Text;

	public static String BadgeTemplates;

	public static String TemplateUpload_Text;
	public static String TemplateUpload_ToolTip;

	public static String TemplateAutoUpload_Text;
	public static String TemplateAutoUpload_ToolTip;

	public static String BadgeTemplateUploadDialogTitle;

	public static String CountryEditor_DefaultToolTip;

	public static String CountryEditor_NewName;

	public static String CountryView_ToolTip;

	public static String CreateEntityErrorMessage;

	public static String CreateEntitySuccessMessage;

	public static String CreateCountryAction_Text;

	public static String CreateCountryAction_ToolTip;

	public static String CreateCountryErrorMessage;

	public static String CreateHotelChain;

	public static String CreateHotelContingent;

	public static String CreateHotel_AdditionalRoomDefinition;

	public static String CreateHotel_AdditionalInformation;

	public static String CreateInvoiceNumberRangeAction_Text;

	public static String CreateInvoiceNumberRangeAction_ToolTip;

	public static String CreateLanguageAction_Text;

	public static String CreateLanguageAction_ToolTip;

	public static String CreateLanguageErrorMessage;

	public static String Delete_Hotel_ConfirmationMessage;

	public static String Delete_Hotel_ErrorMessage;

	public static String Delete_Hotels_ConfirmationMessage;

	public static String DeleteHotelChainConfirmation_Title;

	public static String DeleteHotelChainConfirmation_Message;

	public static String DeleteHotelChainListConfirmation_Title;

	public static String DeleteHotelChainListConfirmation_Message;

	public static String DeleteCountryAction_Text;

	public static String DeleteCountryAction_ToolTip;

	public static String DeleteCountryConfirmation_Message;

	public static String DeleteCountryConfirmation_Title;

	public static String DeleteCountryErrorMessage;

	public static String DeleteCountryListConfirmation_Message;

	public static String DeleteCountryListConfirmation_Title;

	public static String DeleteEventGroupConfirmation;

	public static String DeleteEventConfirmation;
	public static String DeleteEventConfirmation2;

	public static String DeleteEventJobName;

	public static String DeleteInvoiceNoRangeAction_Confirmation;

	public static String DeleteLanguageAction_Text;

	public static String DeleteLanguageAction_ToolTip;

	public static String DeleteLanguageConfirmation_Message;

	public static String DeleteLanguageConfirmation_Title;

	public static String DeleteLanguageErrorMessage;

	public static String DeleteLanguageListConfirmation_Message;

	public static String DeleteLanguageListConfirmation_Title;

	public static String DeleteProgrammeCancelationTermAction_Confirmation;

	public static String DeleteProgrammeOfferingAction_Confirmation;

	public static String DeleteProgrammePointAction_Confirmation;

	public static String DeletePortal_Confirmation;
	public static String DeletePortal_Confirmation2;

	public static String DeletePageLayout_Confirmation;

	public static String EditCountryAction_Text;

	public static String EditCountryAction_ToolTip;

	public static String EditCountryErrorMessage;

	public static String EditHotelChainAction_Text;

	public static String EditHotelChainAction_ToolTip;

	public static String EditLanguageAction_Text;

	public static String EditLanguageAction_ToolTip;

	public static String EditLanguageErrorMessage;

	public static String AvailableParticipantStates;
	public static String ChosenParticipantStates;

	public static String AvailableParticipantTypes;
	public static String ChosenParticipantTypes;

	public static String EventEditor_BadgeSettings;

	public static String EventEditor_StreamingSettings;

	public static String EventEditor_BankConnection;

	public static String EventEditor_FinanceTabText;

	public static String EventEditor_FinanceTab_PaymentProvider;

	public static String EventEditor_Counters;

	public static String EventEditor_CustomFieldsTabText;

	public static String EventEditor_DefaultToolTip;

	public static String EventEditor_GeneralTabText;

	public static String EventEditor_InfoButtonToolTip;

	public static String EventEditor_NewName;

	public static String EventEditor_ParticipantTypeTabText;

	public static String EventEditor_SmtpTabText;

	public static String EventEditor_CertificatePolicyTabText;

	public static String EventEditor_TemplatesTabText;

	public static String EventGroupEditor_NewName;
	public static String EventGroupEditor_DefaultToolTip;


	public static String EventDefaultsComposite_CopyDefaultValuesForLodgePrice;
	public static String EventDefaultsComposite_CopyDefaultValuesForLodgePrice_Tooltip;

	public static String EventDefaultsComposite_CopyDefaultValuesForBreakfastPrice;
	public static String EventDefaultsComposite_CopyDefaultValuesForBreakfastPrice_Tooltip;

	public static String EventDefaultsComposite_CopyDefaultValuesForAdditional1Price;
	public static String EventDefaultsComposite_CopyDefaultValuesForAdditional1Price_Tooltip;

	public static String EventDefaultsComposite_CopyDefaultValuesForAdditional2Price;
	public static String EventDefaultsComposite_CopyDefaultValuesForAdditional2Price_Tooltip;

	public static String EventFilter;
	public static String EventFilter_Desc;

	public static String EventMasterDataEditAction_ErrorMessage_OpenEditor;

	public static String EventMasterDataEditAction_Text_Generic;

	public static String EventMasterDataEditAction_ToolTip_Generic;

	public static String EventMasterDataEditAction_ToolTip_HotelCancelationTerm;

	public static String EventMasterDataEditAction_ToolTip_InvoiceNoRange;

	public static String EventMasterDataEditAction_ToolTip_ProgrammeCancelationTerm;

	public static String EventMasterDataEditAction_ToolTip_WaitList;

	public static String EventMasterDataRefreshAction_Text;

	public static String EventMasterDataRefreshAction_ToolTip;


	public static String EventHotelInfoCancelationTermsComposite_CreateReminder;


	public static String HotelCancelationTermEditor_DefaultToolTip;

	public static String HotelCancelationTermEditor_InfoButtonToolTip;

	public static String HotelChainEditor_DefaultToolTip;

	public static String HotelChainEditor_NewName;

	public static String HotelEditor_DefaultToolTip;

	public static String HotelEditor_NewName;

	public static String HotelChainView_ToolTip;

	public static String InvoiceNoRangeEditor_Days;

	public static String InvoiceNoRangeEditor_DefaultToolTip;

	public static String InvoiceNoRangeEditor_ExportableToolTip;

	public static String InvoiceNoRangeEditor_AuditProofToolTip;

	public static String InvoiceNoRangeEditor_InfoButtonToolTip;

	public static String InvoiceNoRangeEditor_NewName;

	public static String InvoiceNoRangeEditor_Payment1;

	public static String InvoiceNoRangeEditor_Payment2;

	public static String InvoiceNoRangeEditor_Payment3;

	public static String InvoiceNoRangeEditor_PaymentTerms;

	public static String InvoiceNoRangeEditor_PayTimeDateLabel;

	public static String InvoiceNoRangeEditor_PayTimeDateToolTip;

	public static String InvoiceNoRangeEditor_PayTimeDaysLabel;

	public static String InvoiceNoRangeEditor_PayTimeDaysToolTip;

	public static String InvoiceNoRangeEditor_PayTimePercentLabel;

	public static String InvoiceNoRangeEditor_PayTimePercentToolTip;

	public static String InvoiceNoRangeEditor_Reminder1;

	public static String InvoiceNoRangeEditor_Reminder2;

	public static String InvoiceNoRangeEditor_Reminder3;

	public static String InvoiceNoRangeEditor_Reminder4;

	public static String InvoiceNoRangeEditor_Reminder5;

	public static String InvoiceNoRangeEditor_ReminderGroupLabel;

	public static String InvoiceNoRangeEditor_ReminderGroupToolTip;

	public static String InvoiceNoRangeListTreeNode_Text;

	public static String InvoiceNoRangeTreeNode_Text;

	public static String LanguageEditor_DefaultToolTip;

	public static String LanguageEditor_NewName;

	public static String LanguageView_ToolTip;

	public static String NoteTemplates;

	public static String NoteTemplateUploadDialogTitle;

	public static String EditHotelContingent;

	public static String ProgrammeCancelationTermEditor_DefaultToolTip;

	public static String ProgrammeCancelationTermEditor_InfoButtonToolTip;


	public static String ProgrammeOfferingEditor_DefaultToolTip;

	public static String ProgrammeOfferingEditor_GeneralTabText;

	public static String ProgrammeOfferingEditor_InfoButtonToolTip;

	public static String ProgrammeOfferingEditor_NewName;

	public static String ProgrammeOfferingEditor_WebTabText;



	public static String ProgrammePointEditor_AccessControlTabText;

	public static String ProgrammePointEditor_DefaultToolTip;

	public static String ProgrammePointEditor_GeneralTabText;
	public static String ProgrammePointEditor_ImageTabText;

	public static String ProgrammePointEditor_InfoButtonToolTip;

	public static String ProgrammePointEditor_NewName;

	public static String ProgrammePointEditor_StreamingTabText;
	public static String ProgrammePointStreamComposite_LiveStreamGroup_Text;
	public static String ProgrammePointStreamComposite_VideoStreamGroup_Text;

	public static String ProgrammePointEditor_VoucherTabText;

	public static String CancelProgrammePointConfirmation_Message;
	public static String CancelProgrammePointSuccess_Message;

	public static String ProgrammePointListTreeNode_Text;

	public static String ProgrammePointSelectionDialog_Title;
	public static String ProgrammePointSelectionDialog_Message;

	public static String ProgrammePointTypeSelectionDialog_Title;
	public static String ProgrammePointTypeSelectionDialog_Message;



	public static String RefreshCountryAction_Text;

	public static String RefreshCountryAction_ToolTip;

	public static String RefreshHotelChainAction_Text;

	public static String RefreshHotelChainAction_ToolTip;

	public static String RefreshLanguageAction_Text;

	public static String RefreshLanguageAction_ToolTip;

	public static String Rename_Text;

	public static String ResetEventAction_Confirmation;

	public static String WaitListEditor_BenefitRecipientTableColumn;

	public static String WaitListEditor_BookButton;

	public static String WaitListEditor_BookConfirm;

	public static String WaitListEditor_BookConfirmAlsoSave;

	public static String WaitListEditor_BookConfirmDialogTitle;

	public static String WaitListEditor_CancelButton;

	public static String WaitListEditor_InvoiceRecipientTableColumn;

	public static String WaitListEditor_OfferingTableColumn;

	public static String WaitListEditor_ServerMessageAfterBookingDialogTitle;

	public static String WaitlistEditorInput_Name;

	public static String WaitlistTreeNode_Text;

	public static String WaitListEditor_DefaultToolTip;


	public static String WorkGroupEditor_DefaultToolTip;


	public static String RoomDefinitionEditor_DefaultToolTip;

	public static String RoomDefinitionEditor_NewName;

	public static String CreateRoomDefinition;

	public static String CustomFieldComposite_ValueList;

	public static String CreateHotel;

	public static String ResizePhotos;
	public static String ResizePhotos_DialogMessage;
	public static String ResizePhotos_ErrorMissingSettings;

	public static String Delete_RoomDefinition_ErrorMessage;

	public static String Delete_RoomDefinition_ConfirmationMessage;

	public static String Delete_RoomDefinitions_ConfirmationMessage;

	public static String DeleteHotelContingentAction_Confirmation;

	public static String SelectHotelForContingent;

	public static String DeleteHotelOfferingAction_Confirmation;

	public static String DeleteHotelCancelationTermAction_Confirmation;

	public static String ContingentHasNoRoomDefinitions;

	public static String HotelsWithContingentsForThisEvent;

	public static String CreateHotelCancelationTermsForEvent;

	public static String CreateHotelCancelationTermsForHotel;

	public static String CreateHotelCancelationTermsForContingent;

	public static String CreateHotelCancelationTerms;

	public static String CheckOverlappingIntervals;

	public static String CheckOverlappingIntervals_DontCreateWhenOverlappingTakesPlace;

	public static String CheckOverlappingIntervals_CreateAnyway;

	public static String CreateProgrammeCancelationTermsForProgrammePoint;

	public static String CreateProgrammeCancelationTermsForEvent;

	public static String CreateProgrammeCancelationTerms;

	public static String CreateCancelationTermsOptionsWizardPage_title;
	public static String CreateCancelationTermsOptionsWizardPage_message;

	// Participant type
	public static String CreateParticipantTypeErrorMessage;
	public static String DeleteParticipantTypeErrorMessage;
	public static String EditParticipantTypeErrorMessage;

	public static String CreateParticipantTypeAction_Text;
	public static String CreateParticipantTypeAction_ToolTip;
	public static String DeleteParticipantTypeAction_Text;
	public static String DeleteParticipantTypeAction_ToolTip;
	public static String EditParticipantTypeAction_Text;
	public static String EditParticipantTypeAction_ToolTip;
	public static String RefreshParticipantTypeAction_Text;
	public static String RefreshParticipantTypeAction_ToolTip;

	public static String ParticipantTypeEditor_NewName;
	public static String ParticipantTypeEditor_DefaultToolTip;

	public static String DeleteParticipantTypeConfirmation_Title;
	public static String DeleteParticipantTypeConfirmation_Message;
	public static String DeleteParticipantTypeListConfirmation_Title;
	public static String DeleteParticipantTypeListConfirmation_Message;


	// Programme point type
	public static String CreateProgrammePointTypeErrorMessage;
	public static String DeleteProgrammePointTypeErrorMessage;
	public static String EditProgrammePointTypeErrorMessage;

	public static String CreateProgrammePointTypeAction_Text;
	public static String CreateProgrammePointTypeAction_ToolTip;
	public static String DeleteProgrammePointTypeAction_Text;
	public static String DeleteProgrammePointTypeAction_ToolTip;
	public static String EditProgrammePointTypeAction_Text;
	public static String EditProgrammePointTypeAction_ToolTip;
	public static String RefreshProgrammePointTypeAction_Text;
	public static String RefreshProgrammePointTypeAction_ToolTip;

	public static String ProgrammePointTypeEditor_NewName;
	public static String ProgrammePointTypeEditor_DefaultToolTip;

	public static String DeleteProgrammePointTypeConfirmation_Title;
	public static String DeleteProgrammePointTypeConfirmation_Message;
	public static String DeleteProgrammePointTypeListConfirmation_Title;
	public static String DeleteProgrammePointTypeListConfirmation_Message;


	public static String ThisEventIsClosed;
	public static String CloseEvent;
	public static String DeleteHistory;
	public static String DeleteHistoryTooltip;
	public static String DeleteAcl;
	public static String DeleteAclTooltip;
	public static String DeleteLeads;
	public static String DeleteLeadsTooltip;
	public static String DeleteCreditCards;
	public static String DeleteCreditCardsTooltip;
	public static String DeletePortalPhotos;
	public static String DeletePortalPhotosTooltip;
	public static String CloseEventDescriptionLabel;

	public static String PasteOfObjectToNodeNotAllowed;

	public static String EventHotelInfoEditor_DefaultToolTip;

	public static String EventHotelInfoEditor_TextTab;

	public static String EventHotel_HotelInfo;
	public static String EventHotel_Statistics;

	public static String CopyData;
	public static String CopyData_ToolTip;
	public static String NewWorkgroup;
	public static String Delete_WorkGroup_ConfirmationMessage;

	public static String AssignWorkGroupHandler_ExceptionDialog_Label;
	public static String AssignWorkGroupHandler_Dialog_Title;
	public static String AssignWorkGroupHandler_Dialog_Message;
	public static String AssignWorkGroupHandler_SuccessMessage_Event;
	public static String AssignWorkGroupHandler_SuccessMessage_ProgrammePoint;

	public static String RemoveWorkGroupAssociationHandler_Dialog_Title;
	public static String RemoveWorkGroupAssociationHandler_Event_Dialog_Label;
	public static String RemoveWorkGroupAssociationHandler_ProgrammePoint_Dialog_Label;

	public static String CopyEventWizard_Title;
	public static String CopyEventWizardEventPage_Title;
	public static String CopyEventWizardEventPage_Message;
	public static String CopyEventWizardEventPage_timeShiftGroup;
	public static String CopyEventWizardEventPage_originalPeriod;
	public static String CopyEventWizardEventPage_destPeriod;
	public static String CopyEventWizardEventPage_timeShift;
	public static String CopyEventWizardEventPage_year;
	public static String CopyEventWizardEventPage_month;
	public static String CopyEventWizardEventPage_day;

	public static String DeleteNoteTemplateInSpiteOfReferencingEmailTemplates;


	// ParticipantCustomFieldGroup
	public static String CreateParticipantCustomFieldGroupErrorMessage;
	public static String DeleteParticipantCustomFieldGroupErrorMessage;
	public static String EditParticipantCustomFieldGroupErrorMessage;

	public static String CreateParticipantCustomFieldGroupAction_Text;
	public static String CreateParticipantCustomFieldGroupAction_ToolTip;
	public static String DeleteParticipantCustomFieldGroupAction_Text;
	public static String DeleteParticipantCustomFieldGroupAction_ToolTip;
	public static String EditParticipantCustomFieldGroupAction_Text;
	public static String EditParticipantCustomFieldGroupAction_ToolTip;

	public static String ParticipantCustomFieldGroupEditor_NewName;
	public static String ParticipantCustomFieldGroupEditor_DefaultToolTip;

	public static String DeleteParticipantCustomFieldGroupConfirmation_Title;
	public static String DeleteParticipantCustomFieldGroupConfirmation_Message;
	public static String DeleteParticipantCustomFieldGroupListConfirmation_Title;
	public static String DeleteParticipantCustomFieldGroupListConfirmation_Message;


	// ParticipantCustomField
	public static String CreateParticipantCustomFieldErrorMessage;
	public static String DeleteParticipantCustomFieldErrorMessage;
	public static String EditParticipantCustomFieldErrorMessage;

	public static String CreateParticipantCustomFieldAction_Text;
	public static String CreateParticipantCustomFieldAction_ToolTip;
	public static String DeleteParticipantCustomFieldAction_Text;
	public static String DeleteParticipantCustomFieldAction_ToolTip;
	public static String EditParticipantCustomFieldAction_Text;
	public static String EditParticipantCustomFieldAction_ToolTip;

	public static String ParticipantCustomFieldEditor_NewName;
	public static String ParticipantCustomFieldEditor_DefaultToolTip;

	public static String DeleteParticipantCustomFieldConfirmation_Title;
	public static String DeleteParticipantCustomFieldConfirmation_Message;
	public static String DeleteParticipantCustomFieldListConfirmation_Title;
	public static String DeleteParticipantCustomFieldListConfirmation_Message;
	public static String DeleteParticipantCustomFieldWithValuesConfirmation_Title;
	public static String DeleteParticipantCustomFieldWithValuesConfirmation_Message;


	// ProfileCustomField
	public static String DeleteProfileCustomFieldConfirmation_Title;
	public static String DeleteProfileCustomFieldConfirmation_Message;
	public static String DeleteProfileCustomFieldListConfirmation_Title;
	public static String DeleteProfileCustomFieldListConfirmation_Message;
	public static String DeleteProfileCustomFieldWithValuesConfirmation_Title;
	public static String DeleteProfileCustomFieldWithValuesConfirmation_Message;
	public static String DeleteProfileCustomFieldGroupConfirmation_Title;
	public static String DeleteProfileCustomFieldGroupConfirmation_Message;
	public static String DeleteProfileCustomFieldGroupListConfirmation_Title;
	public static String DeleteProfileCustomFieldGroupListConfirmation_Message;


	public static String FormPosition;

	public static String ParticipantStateEditor_NewName;
	public static String ParticipantStateEditor_DefaultToolTip;
	public static String CreateParticipantStateErrorMessage;
	public static String EditParticipantStateErrorMessage;
	public static String CreateParticipantStateAction_Text;
	public static String CreateParticipantStateAction_ToolTip;
	public static String DeleteParticipantStateAction_Text;
	public static String DeleteParticipantStateAction_ToolTip;
	public static String DeleteParticipantStateConfirmation_Title;
	public static String DeleteParticipantStateConfirmation_Message;
	public static String DeleteParticipantStateListConfirmation_Title;
	public static String DeleteParticipantStateListConfirmation_Message;
	public static String DeleteParticipantStateErrorMessage;
	public static String EditParticipantStateAction_Text;
	public static String EditParticipantStateAction_ToolTip;
	public static String RefreshParticipantStateAction_Text;
	public static String RefreshParticipantStateAction_ToolTip;
	public static String ParticipantStateView_ToolTip;
	public static String NO;
	public static String YES;

	public static String ParticipantCustomFieldValue_Add;
	public static String ParticipantCustomFieldValue_Edit;

	public static String CreateProfileCustomFieldGroup;
	public static String CreateProfileCustomField;

	public static String ProfileCustomFieldGroupLocationTreeNode_TAB_1_tooltip;
	public static String ProfileCustomFieldGroupLocationTreeNode_TAB_2_tooltip;
	public static String ProfileCustomFieldGroupLocationTreeNode_TAB_3_tooltip;
	public static String ProfileCustomFieldGroupLocationTreeNode_PSNLT_tooltip;
	public static String ProfileCustomFieldGroupLocationTreeNode_PSNRT_tooltip;
	public static String ProfileCustomFieldEditor_NewName;
	public static String ProfileCustomFieldEditor_DefaultToolTip;
	public static String ProfileCustomFieldGroupEditor_NewName;
	public static String ProfileCustomFieldGroupEditor_DefaultToolTip;
	public static String UpdateEventDirtyWriteMessage;

	public static String LocationEditor_NewName;
	public static String LocationEditor_DefaultToolTip;
	public static String DeleteLocationAction_Confirmation;

	public static String GateEditor_DefaultToolTip;
	public static String DeleteGateAction_Confirmation;
	public static String LocationID;
	public static String GateEditor_NewName;
	public static String GateDeviceView_ToolTip;
	public static String CreateGateDeviceAction_Text;
	public static String CreateGateDeviceAction_ToolTip;
	public static String GateDeviceEditor_NewName;
	public static String GateDeviceEditor_DefaultToolTip;
	public static String EditGateDeviceAction_Text;
	public static String EditGateDeviceAction_ToolTip;
	public static String DeleteGateDeviceAction_Text;
	public static String DeleteGateDeviceAction_ToolTip;
	public static String DeleteGateDeviceConfirmation_Title;
	public static String DeleteGateDeviceConfirmation_Message;
	public static String DeleteGateDeviceListConfirmation_Title;
	public static String DeleteGateDeviceListConfirmation_Message;
	public static String DeleteGateDeviceErrorMessage;
	public static String RefreshGateDeviceAction_Text;
	public static String RefreshGateDeviceAction_ToolTip;
	public static String SendTestMail;

	public static String HotelSearchComposite_FoundHotelsLabel;
	public static String HotelSearchComposite_ResultCountLimit;
	public static String HotelSearchComposite_ResultCountLimit_description;

	public static String HotelContingentSearchComposite_FoundHotelContingentsLabel;
	public static String HotelContingentSearchComposite_ResultCountLimit;
	public static String HotelContingentSearchComposite_ResultCountLimit_description;

	public static String ParticipantCustomFieldGroupLocationTreeNode_TAB_1_tooltip;
	public static String ParticipantCustomFieldGroupLocationTreeNode_TAB_2_tooltip;
	public static String ParticipantCustomFieldGroupLocationTreeNode_TAB_3_tooltip;
	public static String EventEditor_EventCustomFieldsComposite_locationLabel;
	public static String EventEditor_EventCustomFieldsComposite_locationLabel_tooltip;

	public static String PortalEditor_NewName;
	public static String PortalEditor_DefaultToolTip;
	public static String PortalEditor_PhotoTab;
	public static String PortalEditor_PortalConfigurationTab;

	public static String PortalEditor_ParticipantTypesAccessible;
	public static String PortalEditor_ParticipantTypesForMainParticipant;
	public static String PortalEditor_ParticipantTypesForCompanions;
	public static String PortalEditor_ParticipantTypesWithCompanions;
	public static String PortalEditor_SendEmailButtonDescription;
	public static String PortalEditor_EmailTemplateTableDescription;


	public static String PortalView_ToolTip;
	public static String RefreshPortalAction_Text;
	public static String RefreshPortalAction_ToolTip;


	public static String CreatePortalDialog_Title;
	public static String CreatePortalDialog_Message;

	public static String AddPortalFileDialog_Title;
	public static String AddPortalFileDialog_Message;

	public static String PageLayoutEditor_NewName;
	public static String PageLayoutEditor_DefaultToolTip;
	public static String PageLayoutEditor_HeaderTabName;
	public static String PageLayoutEditor_FooterTabName;
	public static String PageLayoutEditor_LinkTabName;
	public static String PageLayoutEditor_MenuTabName;
	public static String PageLayoutEditor_StyleTabName;

	public static String PageLayoutEditor_StandardStyleComposite_Colors;
	public static String PageLayoutEditor_StandardStyleComposite_Fonts;
	public static String PageLayoutEditor_StandardStyleComposite_Dimensions;
	public static String PageLayoutEditor_StandardStyleComposite_CssCouldNotBeGenerated;
	public static String PageLayoutEditor_StandardStyleComposite_PrimaryColor;
	public static String PageLayoutEditor_StandardStyleComposite_PrimaryDarkColor;
	public static String PageLayoutEditor_StandardStyleComposite_PrimaryLightColor;
	public static String PageLayoutEditor_StandardStyleComposite_BackgroundColor;
	public static String PageLayoutEditor_StandardStyleComposite_PageBackgroundColor;
	public static String PageLayoutEditor_StandardStyleComposite_AccentColor;
	public static String PageLayoutEditor_StandardStyleComposite_PrimaryTextColor;
	public static String PageLayoutEditor_StandardStyleComposite_SecondaryTextColor;
	public static String PageLayoutEditor_StandardStyleComposite_InputColor;
	public static String PageLayoutEditor_StandardStyleComposite_DividerColor;
	public static String PageLayoutEditor_StandardStyleComposite_AsideColor;
	public static String PageLayoutEditor_StandardStyleComposite_LinkColor;
	public static String PageLayoutEditor_StandardStyleComposite_NavbarColor;
	public static String PageLayoutEditor_StandardStyleComposite_HeaderColor;
	public static String PageLayoutEditor_StandardStyleComposite_HeaderTextColor;
	public static String PageLayoutEditor_StandardStyleComposite_FooterColor;
	public static String PageLayoutEditor_StandardStyleComposite_FooterTextColor;
	public static String PageLayoutEditor_StandardStyleComposite_BaseFontSize;
	public static String PageLayoutEditor_StandardStyleComposite_InputFontSize;
	public static String PageLayoutEditor_StandardStyleComposite_WebfontUrl;
	public static String PageLayoutEditor_StandardStyleComposite_FontFamilyRegular;
	public static String PageLayoutEditor_StandardStyleComposite_FontFamilyBold;
	public static String PageLayoutEditor_StandardStyleComposite_FontFamilyItalic;
	public static String PageLayoutEditor_StandardStyleComposite_PageWidth;
	public static String PageLayoutEditor_StandardStyleComposite_FullWidth;
	public static String PageLayoutEditor_StandardStyleComposite_WrappedLayout;
	public static String PageLayoutEditor_StandardStyleComposite_HeaderHeight;
	public static String PageLayoutEditor_StandardStyleComposite_FooterHeight;
	public static String PageLayoutEditor_StandardStyleComposite_AsideBorderWidth;
	public static String PageLayoutEditor_StandardStyleComposite_NavigationBarHeight;
	public static String PageLayoutEditor_StandardStyleComposite_NavigationBarPosition;
	public static String PageLayoutEditor_StandardStyleComposite_NavigationBarPositionRelative;
	public static String PageLayoutEditor_StandardStyleComposite_NavigationBarPositionFixed;
	public static String PageLayoutEditor_StandardStyleComposite_GenerateCss;
	public static String PageLayoutEditor_StandardStyleComposite_CopyResources;
	public static String PageLayoutEditor_StandardStyleComposite_CompileLessToCss;

	public static String PageLayoutFaviconComposite_FaviconImage;

	public static String PageLayoutHeaderComposite_HeaderImage;
	public static String PageLayoutHeaderComposite_HeaderText;
	public static String PageLayoutFooterComposite_FooterImage;
	public static String PageLayoutFooterComposite_FooterText;

	public static String PageEditor_NewName;
	public static String PageEditor_DefaultToolTip;
	public static String PageEditor_Icon;
	public static String PageEditor_ArticleHeader;
	public static String PageEditor_ArticleDescription;
	public static String PageEditor_LinkTabName;
	public static String PageEditor_ContentTabName;
	public static String PageEditor_Condition;
	public static String PageEditor_Visibility;
	public static String PageEditor_Required;
	public static String PageEditor_ReadOnly;
	public static String PageEditor_CreateSection;

	public static String PageEditor_CreateCertificateComponent;
	public static String PageEditor_CreateDigitalEventComponent;
	public static String PageEditor_CreateEmailComponent;
	public static String PageEditor_CreateFieldComponent;
	public static String PageEditor_CreateFileComponent;
	public static String PageEditor_CreateGroupMemberTableComponent;
	public static String PageEditor_CreateHotelSearchCriteriaComponent;
	public static String PageEditor_CreateHotelSearchFilterComponent;
	public static String PageEditor_CreateHotelSearchResultComponent;
	public static String PageEditor_CreateHotelDetailsComponent;
	public static String PageEditor_CreateHotelBookingComponent;
	public static String PageEditor_CreateEditBookingComponent;
	public static String PageEditor_CreateHotelTotalAmountComponent;
	public static String PageEditor_CreateHotelSummaryComponent;
	public static String PageEditor_CreateScriptComponent;
	public static String PageEditor_CreateTextComponent;
	public static String PageEditor_CreateOpenAmountComponent;
	public static String PageEditor_CreateTotalAmountComponent;
	public static String PageEditor_CreatePaymentComponent;
	public static String PageEditor_CreatePaymentWithFeeComponent;
	public static String PageEditor_CreatePortalTableComponent;
	public static String PageEditor_CreatePrintComponent;
	public static String PageEditor_CreateProgrammeBookingComponent;
	public static String PageEditor_CreateSendLetterOfInvitationComponent;
	public static String PageEditor_CreateStreamComponent;
	public static String PageEditor_CreateSummaryComponent;
	public static String PageEditor_CreateUploadComponent;
	public static String PageEditor_CreateSpeakerArrivalDepartureComponent;
	public static String PageEditor_CreateSpeakerRoomTypeComponent;


	public static String CreatePageContentDialog_Title;
	public static String CreatePageContentDialog_Message;

	public static String GroupMemberTable_GroupText_AvailableTableColumns;
	public static String GroupMemberTable_GroupText_AvailableButtons;
	public static String GroupMemberTable_GroupText_NamesOfTableColumns;
	public static String GroupMemberTable_GroupText_ButtonToAddNewGroupMember;
	public static String GroupMemberTable_GroupText_ButtonToEditGroupMembersPersonalData;
	public static String GroupMemberTable_GroupText_ButtonToEditGroupMembersProgrammeBookings;
	public static String GroupMemberTable_GroupText_ButtonToCancelGroupMember;
	public static String GroupMemberTable_GroupText_ButtonToSendConfirmationEmail;


	public static String PageEditor_AvailableItems;
	public static String PageEditor_FieldContent;
	public static String PageEditor_Labels;

	public static String PortalTableComponentComposite_RenameLabelsDescription;

	public static String ProgrammeBookingComponentComposite_BookingCount;
	public static String ProgrammeBookingComponentComposite_RenameLabelsDescription;

	public static String LiveStream;
	public static String VideoStream;

	public static String UpdateProfileCustomFieldGroupLocationnErrorMessage;

	public static String SelectProgrammePointsComposite_AvailableProgrammePoints;
	public static String SelectProgrammePointsComposite_SelectedProgrammePoints;

	public static String PageLayoutLinkDialog_Title;
	public static String PageLayoutLinkDialog_Message;

	public static String ChooseProgrammePointsComposite_AvailableProgrammePoints;
	public static String ChooseProgrammePointsComposite_ChosenProgrammePoints;

	public static String ChooseProgrammePointTypesComposite_AvailableProgrammePointTypes;
	public static String ChooseProgrammePointTypesComposite_ChosenProgrammePointTypes;

	public static String PageLayoutStyleSelectionComposite_UserDefined;


	public static String CopyPortalWizard_Title;
	public static String CopyPortalWizard_Description;
	public static String CopyPortalWizard_WindowTitle;
	public static String CopyPortalSettingsWizardPage_Message;
	public static String CopyPortalSettingsWizardPage_MnemonicToolTip;
	public static String CopyPortalSettingsWizardPage_CopyPhotosButtonText;

	public static String CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourGroupText;
	public static String CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourErrorToolTip;
	public static String CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourIgnoreToolTip;
	public static String CopyPortalSettingsWizardPage_MissingParticipantTypeBehaviourCopyToolTip;

	public static String CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourGroupText;
	public static String CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourErrorToolTip;
	public static String CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourIgnoreToolTip;
	public static String CopyPortalSettingsWizardPage_MissingCustomFieldBehaviourCopyToolTip;

	public static String CopyPortalSettingsWizardPage_MissingProgrammePointBehaviourGroupText;
	public static String CopyPortalSettingsWizardPage_MissingProgrammePointBehaviourErrorToolTip;
	public static String CopyPortalSettingsWizardPage_MissingProgrammePointBehaviourIgnoreToolTip;


	public static String AddressGroup_CopyAddressFromGroupManager;

	public static String EventView_FullyBookedProgrammePoints;
	public static String EventView_ProgrammePointsWithExceededWarnNumber;

	public static String AmountForClearing;

	public static String AmountForClearingSameSign;

	public static String AmountForClearingNotLargerThanPayment;

	public static String AmountForRefund;

	public static String AmountForRefundNotLargerThanPayment;

	public static String AnonymousParticipantPage_LanguageLabel;

	public static String AnonymousParticipantPage_LastnameLabel;

	public static String AnonymousParticipantPage_AnonymousParticipantGroupText;

	public static String AnonymousParticipantPage_ParticipantQuantityLabel;

	public static String AnonymousParticipantPage_ParticipantTypeLabel;

	public static String AnonymousParticipantPage_Title;

	public static String AnonymizeWizardDialog_FinishButtonText;
	public static String AnonymizeWizardDialog_PrintButtonText;

	public static String QuickSearch;

	public static String AssignCompanionsWizard_Title;
	public static String AssignCompanionsWizard_Message;

	public static String AssignGroupManagerWizard_Title;
	public static String AssignGroupManagerWizard_Message;

	public static String AssignGroupMemberWizard_Title;
	public static String AssignGroupMemberWizard_Message;

	public static String AssignProfileAndParticipantAction_Text;

	public static String AssignProfileWizard_SearchParticipantPage_Description;

	public static String BadgePrintPreferencePage_Description;

	public static String BadgePrintPreferencePage_BadgePrintWaitTime;

	public static String Bookings;

	public static String BookedAt;

	public static String CanceledAt;

	public static String CancelParticipant_SubordinateToggle;

	public static String CancelOneParticipantByOrganiser_Question;
	public static String CancelManyParticipantsByOrganiser_Question;
	public static String CancelParticipantsByOrganiser;
	public static String CancelParticipantsByOrganiser_FinalMessage;

	public static String CancelOneParticipantByParticipant_Question;
	public static String CancelManyParticipantsByParticipant_Question;
	public static String CancelParticipantsByParticipant;
	public static String CancelParticipantsByParticipant_FinalMessage;

	public static String CollectiveChange;
	public static String CollectiveChangeParticipantCustomFields;
	public static String CollectiveChangeProfileCustomFields;

	public static String CollectiveChangeCustomFields_NoOldCustomField;
	public static String CollectiveChangeCustomFieldsDialog_Title;
	public static String CollectiveChangeCustomFieldsDialog_Message;
	public static String CollectiveChangeCustomFields_FinalMessage;

	public static String CollectiveChangeParticipantStateDialog_Title;
	public static String CollectiveChangeParticipantStateDialog_Message;
	public static String CollectiveChangeParticipantState_FinalMessage;

	public static String CollectiveChangeParticipantTypeDialog_Title;
	public static String CollectiveChangeParticipantTypeDialog_Message;
	public static String CollectiveChangeParticipantType_FinalMessage;

	public static String CollectiveChangeRegistrationDateDialog_Title;
	public static String CollectiveChangeRegistrationDateDialog_Message;
	public static String CollectiveChangeRegistrationDate_FinalMessageSet;
	public static String CollectiveChangeRegistrationDate_FinalMessageDelete;

	public static String CollectiveChangeCertificatePrintDialog_Title;
	public static String CollectiveChangeCertificatePrintDialog_Message;
	public static String CollectiveChangeCertificatePrint_FinalMessageSet;
	public static String CollectiveChangeCertificatePrint_FinalMessageDelete;

	public static String CollectiveChangeNotificationTimesDialog_Title;
	public static String CollectiveChangeNotificationTimesDialog_Message;
	public static String CollectiveChangeNotificationTimes_FinalMessage;

	public static String CommandNotPossibleForNewParticipant;

	public static String CopyParticipantsToOtherEvent_Text;

	public static String CopyParticipantsToOtherEventWizard_finalDialogTitle;
	public static String CopyParticipantsToOtherEventWizard_finalDialogMessageOneParticipant;
	public static String CopyParticipantsToOtherEventWizard_finalDialogMessageManyParticipants;

	public static String CreateClearingWith;

	public static String CreateBookings;
	public static String CreateProgrammeBookings_Text;

	public static String CreateProgrammeBookings_SelectProgrammOffers;

	public static String CreateParticipantsFromProfiles;
	public static String CreateParticipantsFromProfiles_FinalMessage;

	public static String CreateCompanionWizard_Title;

	public static String CreateParticipantAction_Tooltip;

	public static String CreateAnonymousParticipantWizard_Title;
	public static String CreateAnonymousParticipants_FinalMessage;

	public static String CreateParticipantErrorMessage;

	public static String CreateCorrespondenceAction_Text;

	public static String CreateProfileAction_Error;

	public static String CreateProfileAction_Text;

	public static String CreateProfileAction_ToolTip;

	public static String CreateProfileErrorMessage;

	public static String CreateProfilesFromParticipants;
	public static String CreateProfileFromParticipant_Question;
	public static String CreateProfilesFromParticipants_Confirmation_OneNew;
	public static String CreateProfilesFromParticipants_Confirmation_AllNew;
	public static String CreateProfilesFromParticipants_Confirmation_SomeAlreadyExist;

	public static String CreateProfilesFromParticipants_Question;

	public static String DeleteManyProfilesConfirmation_Message;
	public static String DeleteManyProfilesConfirmation_Title;
	public static String DeleteManyProfiles_FinalMessage;
	public static String DeleteOneProfileConfirmation_Message;
	public static String DeleteOneProfileConfirmation_Title;

	public static String DeleteManyParticipantsConfirmation_Message;
	public static String DeleteManyParticipantsConfirmation_Title;
	public static String DeleteManyParticipants_FinalMessage;
	public static String DeleteOneParticipantConfirmation_Message;
	public static String DeleteOneParticipantConfirmation_Title;
	public static String DeleteParticipantErrorMessage;
	public static String DeleteParticipantEnforceConfirmation_Title;
	public static String DeleteParticipantEnforceConfirmation_Message;

	public static String DeleteProfileErrorMessage;

	public static String EditProfileAction_Text;

	public static String EditProfileAction_ToolTip;

	public static String Email_Dispatched;

	public static String Error_CopyToSameEventNotPossible;

	public static String EventPage_SelectEventForParticipantsToBeCopied;

	public static String EventPage_SelectEventForParticipantToBeAssigned;

	public static String EventPage_SelectEventIntoWhichParticipantsAreToBeCopied;

	public static String EventPage_SelectEventInWhichToCreateParticipant;

	public static String EventPage_SelectEventInWhichToCreateParticipants;

	public static String EventPage_Title;

	public static String EventPage_Desc_OneOrMore;

	public static String EventPage_Desc_One;

	public static String EventParticipantTypeAndStatePage_Description;

	public static String EventParticipantTypeAndStatePage_Title;

	public static String EventSelectionDialog_Title;
	public static String EventSelectionDialog_Message;

	public static String AssignEventGroup_Title;
	public static String AssignEventGroup_Message;

	public static String ForceWizardPage_Title;
	public static String ForceWizardPage_Description;

	public static String GlobalDocumentDelete_ConfirmTitle;

	public static String GlobalDocumentDelete_ConfirmMessage;

	public static String GlobalDocumentUploadDialog_Title;

	public static String GlobalImprintEditor_Text;

	public static String GlobalImprintEditor_ToolTip;

	public static String GlobalPrivacyPolicyEditor_Text;

	public static String GlobalPrivacyPolicyEditor_ToolTip;

	public static String GlobalTermsAndConditionsEditor_Text;

	public static String GlobalTermsAndConditionsEditor_ToolTip;

	public static String ParticipantAssignProfile_Text;

	public static String ParticipantCopyFromProfile_Question;

	public static String ParticipantCopyToProfile_Question;

	public static String ParticipantEditAction_Text;

	public static String ParticipantEditAction_ToolTip;

	public static String ParticipantEditor_Connections;

	public static String ParticipantEditor_ChangeAnonymousParticipantLastNameDialog_Question;

	public static String ParticipantEditor_DefaultToolTip;

	public static String ParticipantEditor_InfoButtonToolTip;

	public static String ParticipantEditor_NewName;

	public static String ParticipantEditor_Profile;

	public static String ParticipantsCopyFromProfile_Question;

	public static String ParticipantsCopyToProfile_Question;

	public static String ParticipantSearchComposite_FoundParticipantsLabel;

	public static String ParticipantSearchComposite_SearchButtonText;

	public static String ParticipantSearchPage_SelectParticipantsToBeCopied;

	public static String ParticipantSearchPage_SelectParticipantToBeAssignedToProfile;

	public static String ParticipantTreeRefreshAction_Text;

	public static String ParticipantTreeRefreshAction_ToolTip;

	public static String ParticipantTreeView_Count_ToolTip;

	public static String ParticipantTreeView_sortByNameButton_Name;

	public static String ParticipantTreeView_sortByNameButton_ToolTip;

	public static String ParticipantTreeView_sortByNumberButton_Name;

	public static String ParticipantTreeView_sortByNumberButton_ToolTip;

	public static String ParticipantsOfEvent;

	public static String ParticipantTypeAndStatePage_Title;
	public static String ParticipantTypeAndStatePage_Description;
	public static String ParticipantTypeAndStatePage_ParticipantType_Tooltip;

	public static String ParticipantWebTokenDialog_Title;
	public static String ParticipantWebTokenDialog_Message;

	public static String LinkParticipantsWizardPage_Title;
	public static String LinkParticipantsWizardPage_Description;
	public static String LinkParticipantsWizardPage_LinkButton;
	public static String LinkParticipantsWizardPage_DontLinkButton;

	public static String ParticipantTypeView_ToolTip;

	public static String ProfileAssignParticipantAction_Text;

	public static String ProfileAssignParticipantAction_Tooltip;

	public static String ProfileEditor_DefaultToolTip;

	public static String ProfileEditor_NewName;

	public static String UserCredentialsGroup_UserName_Label;
	public static String UserCredentialsGroup_Profile_UserName_ToolTip;
	public static String UserCredentialsGroup_Participant_UserName_ToolTip;
	public static String UserCredentialsGroup_PasswordHash_Label;
	public static String UserCredentialsGroup_PasswordHash_ToolTip;
	public static String UserCredentialsGroup_SetPassword_Label;
	public static String UserCredentialsGroup_SetPassword_ToolTip;
	public static String UserCredentialsGroup_ChangePasswordDialog_Title;
	public static String UserCredentialsGroup_ChangePasswordDialog_Message;

	public static String ProfileSearchComposite_FoundProfilesLabel;

	public static String ProfileSearchComposite_SearchButtonText;

	public static String ProfileSearchPage_Description;

	public static String ProfileSearchPage_Title;

	public static String ProgrammePointTypeView_ToolTip;

	public static String RemoveFromGroup_Question;

	public static String SelectProgrammeOfferingsPage_showFullyBookedOfferingsButton;

	public static String Uncompanion_Question;

	public static String Ungroup_Question;

	public static String UpdateParticipantDirtyWriteMessage;

	public static String UpdateParticipantErrorMessage;

	public static String UpdateProfileErrorMessage;

	public static String AddressGroup_StandardAddressLabelToolTip;

	public static String PersonGroup_LessToolTip;

	public static String PersonGroup_MoreToolTip;

	public static String PersonGroup_StandardSalutationToolTip;

	public static String EnterOrSelectFunction;

	public static String DoubleBadgeException_Title;
	public static String DoubleBadgeException_Message;
	public static String DoubleBadgeException_Yes;
	public static String DoubleBadgeException_No;
	public static String DoubleBadgeException_Cancel;

	public static String BadgePrintController_BadgePrintTitle;
	public static String BadgePrintController_BadgePrintMessage;
	public static String BadgePrintController_DoubleBadge_Message;
	public static String BadgePrintController_DoubleBadge_CreateNew;
	public static String BadgePrintController_DoubleBadge_Skip;
	public static String BadgePrintController_OpenOfficeDlg_Title;
	public static String BadgePrintController_OpenOfficeDlg_Message;

	public static String BadgeDemandsFullPaymentException_Title;
	public static String BadgeDemandsFullPaymentException_Message;
	public static String BadgeDemandsFullPaymentException_Yes;
	public static String BadgeDemandsFullPaymentException_No;
	public static String BadgeDemandsFullPaymentException_Cancel;

	public static String BadgeException_Title;

	public static String ParticipantOverviewForm_AccountancyStatus;
	public static String ParticipantOverviewForm_SumOfAllInvoices;
	public static String ParticipantOverviewForm_SumOfAllPayments;
	public static String ParticipantOverviewForm_SumOfAllIncomingPayments;
	public static String ParticipantOverviewForm_SumOfAllRefunds;
	public static String ParticipantOverviewForm_OpenAmount;

	public static String Payment_Cancellation;
	public static String Payment_Booking;
	public static String Payment_Type;
	public static String Amount;

	public static String Invoice_Printed;
	public static String Invoice_Amount_Gross;
	public static String Invoice_Number;
	public static String Invoice_Date;
	public static String Invoice_Closed;
	public static String CloseInvoiceCommandHandler_OperationMessage;
	public static String CloseInvoiceCommandHandler_Title;
	public static String CloseInvoiceCommandHandler_FinalMessage;

	public static String ProgrammeBooking;
	public static String ProgrammeBooking_Cancellation;

	public static String HotelBooking;
	public static String HotelBooking_Cancellation;

	public static String ShowCancelled;

	public static String CreateProgrammeBookings_InfosForParticipant;

	public static String CreateProgrammeBookings_GroupManagerOrMainParticipantOrThemselfLabel;

	public static String CreateProgrammeBookings_EachParticipantThemSelfRadioButton;
	public static String CreateProgrammeBookings_OneInvoiceRecipientForAllSelectedParticipants;

	public static String CreateProgrammeBookings_DeciceBookingMode;

	public static String CreateProgrammeBookings_BookingViaProgrammePointsRadioButton;

	public static String CreateProgrammeBookings_BookingViaProgrammeOfferingsRadioButton;

	public static String CreateProgrammeBookings_SelectProgrammPoints;

	public static String CreateProgrammeBookings_OnlyOnce;
	// public static String CreateProgrammeBookings_OnlyOnce;

	public static String OpenLiveStream;
	public static String OpenLiveStream_Description;
	public static String OpenVideoStream;
	public static String OpenVideoStream_Description;

	public static String ReferenceTime;


	public static String ChangeInvoiceRecipient;

	public static String ChangeBenefitRecipient;

	public static String BookingDetails;

	public static String CancelBooking;

	public static String CancelCancel;

	public static String CancelBooking_OptionallyChooseCancelationTerms;

	public static String HotelBookingDetails;
	public static String HotelBooking_InfoForHotel;
	public static String HotelBooking_InfoForGuest;
	public static String HotelBooking_HotelInfo_General;
	public static String HotelBooking_HotelInfo_Payment;

	public static String CreateHotelBookingForNParticipants;

	public static String CreateHotelBooking_HotelSelectionCriteriaPage_Title;
	public static String CreateHotelBooking_HotelSelectionCriteriaPage_Message;
	public static String CreateHotelBooking_HotelOfferingsTablePage_Title;
	public static String CreateHotelBooking_HotelOfferingsTablePage_Message;
	public static String CreateHotelBookingPaymentConditionsPage_Title;
	public static String CreateHotelBookingPaymentConditionsPage_Message;
	public static String CreateHotelBookingInfoPage_Title;
	public static String CreateHotelBookingInfoPage_Message;
	public static String CreateHotelBookingOverviewPage_Title;
	public static String CreateHotelBookingOverviewPage_Message;

	public static String SelectInvoiceRecipientPage_Title;
	public static String SelectInvoiceRecipientPage_Message;

	public static String CopyFromParticipant;

	public static String HotelContingentEditor_DefaultToolTip;
	public static String HotelContingentEditor_NewName;
	public static String HotelContingentEditor_InfoButtonToolTip;
	public static String HotelContingentEditor_TimePeriodTooLarge_Title;
	public static String HotelContingentEditor_RoomCapacities;
	public static String HotelContingentEditor_RoomCapacitiesTable_TrueSize;
	public static String HotelContingentEditor_RoomCapacitiesTable_BookSize;
	public static String HotelContingentEditor_RoomCapacitiesTable_PublicSize;

	public static String HotelContingentEditor_TimePeriodTooLarge_Message;
	public static String HotelOfferingEditor_DefaultToolTip;
	public static String HotelOfferingEditor_InfoButtonToolTip;
	public static String HotelOfferingEditor_NewName;

	public static String HotelSelectionWizardPage_Description;
	public static String HotelOfferingSelectionWizardPage_Description;

	public static String HotelCostCoverage_SelectHotelDescription;
	public static String HotelCostCoverage_SelectOfferingDescription;
	public static String HotelCostCoverage_StatusText_Used;
	public static String HotelCostCoverage_StatusText_NotUsed;
	public static String HotelCostCoverage_SelectOffering;
	public static String HotelCostCoverage_RemoveOffering;


	public static String AdaptAddressesOfNGroupMembers_Question;

	public static String AdaptingAddressesOfGroupMembers;

	public static String AnalyzingAddressesOfGroupMembers;

	public static String AdaptCancellationOfBookings;

	public static String AdaptCancellationOfNBookings_Question;

	public static String PrintNotifications;

	public static String NotificationOverviewPage_Message;

	public static String CloseInvoicesQuestion;

	public static String CancelPaymentQuestion;
	public static String CancelPaymentQuestionForElectronicPayents;

	public static String CancelClearingsQuestion;

	public static String CreateAutomaticClearingQuestion;
	public static String CreateAutomaticClearingOperationMessage;

	public static String PayEngineRefundQuestion;
	public static String PayEngineRefundOperationMessage;

	public static String DatatransPage1_UseAliasButton;
	public static String DatatransPage1_DontUseAliasButton;
	public static String DatatransPage1_Title;
	public static String DatatransPage1_Description;

	public static String DatatransPage2_Title;
	public static String DatatransPage2_Description;
	public static String DatatransPage2_SubmitButton;
	public static String DatatransPage2_Header;
	public static String DatatransPage2_HeaderWithAlias;
	public static String DatatransPage2_HeaderForAliasRequest;

	public static String FilterActive;
	public static String OpenFilterDialog;

	public static String FormatDialogText;

	public static String GenerateInvoiceDocumentsDialog_Title;
	public static String GenerateInvoiceDocumentsDialog_Message;
	public static String GenerateInvoiceDocumentsDialog_MergeDocuments;

	public static String PayEnginePaymentWizard_Error_NoPayment;
	public static String PayEnginePaymentWizard_Error_MoreThanOnePayment;
	public static String PayEngineStartPage_UseAliasButton;
	public static String PayEngineStartPage_DontUseAliasButton;
	public static String PayEngineStartPage_Title;
	public static String PayEngineStartPage_Message;
	public static String PayEngineStartPage_AliasDescription;

	public static String PayEngineBrowserPage_Title;
	public static String PayEngineBrowserPage_Message;
	public static String PayEngine_SubmitButton;
	public static String PayEngine_HtmlHeader;
	public static String PayEngine_HtmlHeaderForAliasRequest;
	public static String PayEngine_AliasUsage;

	public static String PayEngineAliasPage_Title;
	public static String PayEngineAliasPage_Message;
	public static String PayEngineAliasPage_StartTransactionButton;
	public static String PayEngineAliasPage_Msg_StartTransaction;
	public static String PayEngineAliasPage_Msg_Success;
	public static String PayEngineAliasPage_Msg_Failure;
	public static String PayEngineAliasPage_Msg_Exception;

	public static String PaymentWillMakeMoneyTransactionHint;

	public static String CreatePaymentAmountPage_Title;
	public static String CreatePaymentAmountPage_Message;
	public static String CreatePaymentAmountPage_CreditorTooltip;
	public static String CreatePaymentAmountPage_DebitorTooltip;

	public static String IncreaseReminderLevelCommandHandler_OperationMessage;
	public static String IncreaseReminderLevelCommandHandler_Title;
	public static String IncreaseReminderLevelCommandHandler_FinalMessage;
	public static String IncreaseReminderStateQuestion;

	public static String RestartReminderLevelCommandHandler_OperationMessage;
	public static String RestartReminderLevelCommandHandler_Title;
	public static String RestartReminderLevelCommandHandler_FinalMessage;
	public static String RestartReminderQuestion;

	public static String StopReminderLevelCommandHandler_OperationMessage;
	public static String StopReminderLevelCommandHandler_Title;
	public static String StopReminderLevelCommandHandler_FinalMessage;
	public static String StopReminderQuestion;

	public static String PaymentReceiptFormat;

	public static String InvoiceSearchComposite_FoundInvoicesLabel;

	public static String ActionIsApplicableForMofNSelectedInvoices;

	public static String ActionNotApplicableForAnySelectedInvoice;

	public static String OpenBadgeButton_name;

	public static String OpenBadgeButton_tooltip;

	public static String PrintBadgeButton_name;

	public static String PrintBadgeButton_tooltip;

	public static String EnableDisableBadgeButton_name;

	public static String EnableDisableBadgeButton_tooltip;

	public static String BadgeNotForKnownParticipant;

	public static String AssignBadgeButton_name;

	public static String AssignBadgeButton_tooltip;

	public static String WaitingForScannedBadgeToBeAssigned;

	public static String BadgeIdAlreadyExistingForceAssignmentQuestion;

	public static String ChangeOfMultipleBookings;

	public static String ChangeOfMultipleBookingsMessage;

	public static String SelectedBookingsNotInSameContingent;

	public static String PrintingBadgeForXInBracketsCountOfTotalCount;

	public static String BadgePrintController_WaitMessage;

	public static String UnlinkProfileCommandHandler_Confirmation;

	public static String UnlinkParticipantCommandHandler_Confirmation;

	public static String NoRelevantNotifications;

	public static String PayWizardDialog_FinishButton;
	public static String ChargeWizardDialog_FinishButton;

	public static String NoPaymentSystem_Title;
	public static String NoPaymentSystem_Message;

	public static String DatatransErrorNoPost;

	public static String PayEngineErrorNoPost;
	public static String PayEngineErrorManyResponses;
	public static String PayEngineErrorAlert;

	public static String CreditCardAliasGroup_NewAliasButton;
	public static String DatatransAliasWizard_WindowTitle;
	public static String PayEngineAliasWizard_WindowTitle;

	public static String PayEngine_NoSetupMessage;
	public static String PayEngine_IncompleteSetupMessage;

	public static String SelectedParticipantsDontBelongToSameEvent;

	public static String UndefinedEvent;

	public static String SelectOffering;

	public static String EnterQuantity;

	public static String BookedQuantity;
	public static String BookingPrice;
	public static String BasePrice;

	public static String AlsoDoubleclickOnOffer;

	public static String Booking;
	public static String Cancellation;

	public static String ReallyCancelSelectedBookings;

	public static String PayEngineRefundTransactionPage_Title;

	public static String PayEngineAliasPage_Msg_RefundPending;

	public static String SelectNobility;
	public static String SelectAdminTitle;

	public static String ProfileRelationTypeSelectionPage_Title;
	public static String ProfileRelationTypeSelectionPage_Description;

	public static String CreateProfileRelation_Title;

	public static String ProfileSearchPage_SameProfileError;

	public static String CreateProfileRelation_ToolTip;

	public static String DeleteOneProfileRelationConfirmation_Title;

	public static String DeleteOneProfileRelationConfirmation_Message;

	public static String DeleteManyProfileRelationConfirmation_Title;

	public static String DeleteManyProfileRelationConfirmation_Message;

	public static String DeleteProfileRelationErrorMessage;

	public static String ProfileRelationTypeView_ToolTip;

	public static String CreateProfileRelationTypeAction_Text;
	public static String CreateProfileRelationTypeAction_ToolTip;

	public static String ProfileRelationTypeEditor_DefaultToolTip;

	public static String ProfileRelationView_RelationsOfProfile;
	public static String ProfileRelationView_Column_ProfileRelationTypeDesc;
	public static String ProfileRelationView_Column_OtherProfile;

	public static String CreateProfileRelationTypeErrorMessage;
	public static String EditProfileRelationTypeErrorMessage;
	public static String EditProfileRelationTypeAction_Text;
	public static String EditProfileRelationTypeAction_ToolTip;
	public static String DeleteProfileRelationTypeAction_Text;
	public static String DeleteProfileRelationTypeAction_ToolTip;
	public static String DeleteProfileRelationTypeConfirmation_Title;
	public static String DeleteProfileRelationTypeConfirmation_Message;
	public static String DeleteProfileRelationTypeListConfirmation_Title;
	public static String DeleteProfileRelationTypeListConfirmation_Message;
	public static String DeleteProfileRelationTypeErrorMessage;
	public static String RefreshProfileRelationTypeAction_Text;
	public static String RefreshProfileRelationTypeAction_ToolTip;
	public static String DeleteProfileRelationAction_Text;
	public static String DeleteProfileRelationAction_ToolTip;

	public static String ParticipantTypeAndStatePage_ConnectedProfiles;
	public static String ParticipantTypeAndStatePage_MainProfile;
	public static String ParticipantTypeAndStatePage_Role;
	public static String ParticipantTypeAndStatePage_Relation;

	public static String ParticipantTypeAndStatePage_RelatedProfileDescription;

	public static String ParticipantSearchComposite_ResultCountLimit;
	public static String ParticipantSearchComposite_ResultCountLimit_description;

	public static String ParticipantSearch;

	public static String ParticipantToProfileView_CreateProfileFromParticipant;
	public static String ParticipantToProfileView_CreateProfileFromParticipant_tooltip;
	public static String ParticipantToProfileView_CreateProfileFromParticipant_ConfirmationMsg;
	public static String ParticipantToProfileView_CreateProfileFromParticipantSummaryMsg;
	public static String ParticipantToProfileView_LinkParticipantWithProfile;
	public static String ParticipantToProfileView_LinkParticipantWithProfile_tooltip;
	public static String ParticipantToProfileView_LinkParticipantWithProfile_ConfirmationMsg;
	public static String ParticipantToProfileView_DuplicateSearch;
	public static String ParticipantToProfileView_CheckLastName;
	public static String ParticipantToProfileView_CheckFirstName;
	public static String ParticipantToProfileView_CheckEmail;
	public static String ParticipantToProfileView_CheckCity;
	public static String ParticipantToProfileView_MatchingProfiles;

	public static String EventHasNoCertificatePolicy;
	public static String OpenCertificateCommandHandler_FinalMessage;
	public static String PrintCertificateCommandHandler_FinalMessage;

	public static String DuplicateDialog_Title;
	public static String DuplicateDialog_DuplicateLabel;

	public static String ProfileRoleEditor_DefaultToolTip;
	public static String CreateProfileRoleErrorMessage;
	public static String EditProfileRoleErrorMessage;

	public static String ProfileRoleView_ToolTip;
	public static String CreateProfileRoleAction_Text;
	public static String CreateProfileRoleAction_ToolTip;
	public static String EditProfileRoleAction_Text;
	public static String EditProfileRoleAction_ToolTip;
	public static String DeleteProfileRoleAction_Text;
	public static String DeleteProfileRoleAction_ToolTip;
	public static String DeleteProfileRoleConfirmation_Title;
	public static String DeleteProfileRoleConfirmation_Message;
	public static String DeleteProfileRoleListConfirmation_Title;
	public static String DeleteProfileRoleListConfirmation_Message;
	public static String DeleteProfileRoleErrorMessage;
	public static String RefreshProfileRoleAction_Text;
	public static String RefreshProfileRoleAction_ToolTip;
	public static String AssignToRole;
	public static String SelectRoleForAssignment;
	public static String ProfileRoles;
	public static String ProfileRole_Desc;
	public static String ProfileRole_Name;

	public static String OnsiteWorkflowScriptError;

	public static String VolumeTable_BookSizeNotSmallerThanNumberOfBookings_Title;
	public static String VolumeTable_BookSizeNotSmallerThanNumberOfBookings_Message;

	public static String VolumeTable_BookSizeNotNegative_Title;
	public static String VolumeTable_BookSizeNotNegative_Message;

	public static String OptionalHotelBookingComposite_CurrentlyBlocked;
	public static String OptionalHotelBookingManagementComposite_ShowExpiredBookings;

	public static String OfferingEditor_ChangeBruttoMessage;
	public static String OfferingEditor_ChangeCurrencyMessage;

	public static String CopyCustomFieldValuesOptionWizardPage_Title;
	public static String CopyCustomFieldValuesOptionWizardPage_Description;

	public static String ParticipantsFromDifferentEvents;

	public static String EmailTemplateSelectionPage_title;
	public static String EmailTemplateSelectionPage_desc;
	public static String EmailTemplateSelectionPage_sendEmailCheckbox_text;
	public static String EmailTemplateSelectionPage_sendEmailCheckbox_tooltip;
	public static String WebId;

	public static String SendPaymentConfimationEmailHandler_NoEmailTemplate_title;
	public static String SendPaymentConfimationEmailHandler_NoEmailTemplate_message;

	public static String SendPaymentLinkEmailHandler_NoEmailTemplate_title;
	public static String SendPaymentLinkEmailHandler_NoEmailTemplate_message;
	public static String SendPaymentLinkEmailHandler_NoPaymentUrl_title;
	public static String SendPaymentLinkEmailHandler_NoPaymentUrl_message;
	public static String SendPaymentLinkEmailErrorMessage;

	public static String SendPaymentConfirmationDialog_message;
	public static String SendPaymentConfirmationDialog_title;

	public static String SendRefundConfirmationDialog_message;
	public static String SendRefundConfirmationDialog_title;

	public static String RefundNotPossibleBecauseAmountAlreadyRefunded;

	public static String PaymentDetailsComposite_CancelPayment;
	public static String PaymentDetailsComposite_SendPaymentReceiptByEmail;
	public static String PaymentDetailsComposite_SendPaymentReceiptByEmail_tooltip;
	public static String PaymentDetailsComposite_SendRefundReceiptByEmail;
	public static String PaymentDetailsComposite_SendRefundReceiptByEmail_tooltip;

	public static String EditPaymentDialog_title;
	public static String EditPaymentDialog_message;

	public static String CollectiveChangeProfileRolesDialog_Add;
	public static String CollectiveChangeProfileRolesDialog_Add_description;
	public static String CollectiveChangeProfileRolesDialog_Remove;
	public static String CollectiveChangeProfileRolesDialog_Remove_description;
	public static String CollectiveChangeProfileRolesDialog_Set;
	public static String CollectiveChangeProfileRolesDialog_Set_description;
	public static String CollectiveChangeOfProfileRolesDialog_title;
	public static String CollectiveChangeOfProfileRolesDialog_message;

	public static String Evaluate;
	public static String Script_PressCtrlEnterToEvaluate;
	public static String VariablesAndResults;

	public static String InconsistentInvoiceNumberRanges_Message;

	// MIRCP-2833 - Download and print documents of multiple participants
	public static String DownloadPrintParticipantFiles;
	public static String DownloadPrintParticipantFilesHints;
	public static String DownloadPrintFiles;
	public static String PrintToStandardPrinter;
	public static String SelectedParticipantsHaveNoDocuments;
	public static String SelectedParticipantsHaveCountDocumentsOfSize;

	public static String ProfileSelectionWizardPage_Title;
	public static String ParticipantSelectionWizardPage_Title;

	public static String AnonymizeWizard_ProfileSelectionWizardPage_Description;
	public static String AnonymizeWizard_ParticipantSelectionWizardPage_Description;
	public static String AnonymizeWizard_PrintDocumentDialogTitle;
	public static String AnonymizeWizard_PrintDocumentDialogMessage;

	public static String ProfileSelectionDialog_Title;
	public static String ProfileSelectionDialog_Description_One;

	public static String ParticipantSelectionDialog_Title;
	public static String ParticipantSelectionDialog_Description_One;
	public static String ParticipantSelectionDialog_Description_OneOrMany;
	public static String ParticipantSelectionDialog_Description_FixedNumber;

	public static String AnonymizeSelectedProfilesAndParticipants1WizardPage_Title;
	public static String AnonymizeSelectedProfilesAndParticipants1WizardPage_Description;
	public static String AnonymizeSelectedProfilesAndParticipants2WizardPage_Title;
	public static String AnonymizeSelectedProfilesAndParticipants2WizardPage_Description;

	public static String SpecialCondition;
	public static String YesIfNotNew;
	public static String YesIfNotNew_Desc;

	public static String SafeEditorBeforeDragEvent;
	public static String SafeEditorBeforeDragCustomFieldGroup;
	public static String SafeEditorBeforeDragCustomField;
	public static String SafeEditorBeforeDragPage;
	public static String SafeEditorBeforeDragProgrammePoint;
	public static String SafeEditorBeforeDragProgrammeOffering;
	public static String SafeEditorBeforeDragHotelContingent;

	public static String MalformedUrlInLineN;

}

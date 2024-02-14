package de.regasus.impex;

import org.eclipse.osgi.util.NLS;

public class ImpexI18N extends NLS {

	public static final String BUNDLE_NAME = "de.regasus.impex.i18n-impex";

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ImpexI18N.class);
	}



	public static String SSLException;

	public static String ODSImportAction_Completed;

	public static String ODSImport_Statistics;

	public static String ODSImport_StatisticsWithErrors;

	public static String ODSImportAction_Failed;

	public static String ODSImportWizard_eventPageDescription;

	public static String ODSImportWizardPage_CheckMainCityLabel;

	public static String ODSImportWizardPage_CheckEmailLabel;

	public static String ODSImportWizardPage_CheckFirstNameLabel;

	public static String ODSImportWizardPage_CheckLastNameLabel;

	public static String ODSImportWizardPage_DuplicateSearchGroupText;

	public static String ODSImportWizardPage_Description;

	public static String ODSImportWizardPage_FileLabel;

	public static String ODSImportWizardPage_GroupManagerTypeLabel;

	public static String ODSImportWizardPage_ImportContentGroupText;

	public static String ODSImportWizardPage_LanguageLabel;

	public static String ODSImportWizardPage_MaxErrorLabel;

	public static String ODSImportWizardPage_ParticipantStateLabel;

	public static String ODSImportWizardPage_ParticipantTypeLabel;

	public static String ODSImportWizardPage_SeparatorComboLabel;

	public static String ODSParticipantImportWizard_Title;

	public static String ODSProfileImportWizard_Title;


//	====================================================

	public static String XmlExportWizard_Title;
	public static String XmlImportWizard_Title;
	public static String AccountancyDataPage_Title;
	public static String AccountancyDataPage_Export_Description;
	public static String AccountancyDataPage_Import_Description;
	public static String SettingsPage_Title;
	public static String SettingsPage_Export_Description;
	public static String SettingsPage_Import_Description;
	public static String FilePage_Export_Description;
	public static String FilePage_Import_Description;

	public static String ScanndyDataImportWizard_Title;
	public static String ScanndyDataImportWizard_SuccessMsg;
	public static String ScanndyDataImportWizard_ErrorMsg;
	public static String ScanndyDataImportWizard_eventPageDecription;
	public static String ScanndyDataImportWizard_programmePointPageDecription;

	public static String EIVFoBiCreateWizardMsg;
	public static String EIVFoBiCreateWizard_Title;
	public static String EIVFoBiCreateWizard_eventPageDecription;
	public static String EIVFoBiCreateWizard_programmePointPageDecription;

	public static String EIVFoBiExportWizard_Title;
	public static String EIVFoBiExportWizard_eventPageDecription;
	public static String EIVFoBiExport_programmePointPageDecription;
	public static String EIVFoBiExport_moveOffProgrammePointPageTitle;
	public static String EIVFoBiExport_moveOffProgrammePointPageDecription;
	public static String EIVFoBiExportWizardPage_Title;
	public static String EIVFoBiExportWizardPage_Description;
	public static String EIVFoBiExportWizardPage_File;
	public static String EIVFoBiExportWizardPage_FileToolTip;
	public static String EIVFoBiExportWizardPage_FileButtonToolTip;
	public static String EIVFoBiExportWizardPage_Email;
	public static String EIVFoBiExportWizardPage_EmailToolTip;
	public static String EIVFoBiExportWizardPage_OnlyNotExportedButton;
	public static String EIVFoBiExportWizardPage_OnlyNotExportedButtonToolTip;
	public static String EIVFoBiExportWizardPage_MarkTransmittedButton;
	public static String EIVFoBiExportWizardPage_MarkTransmittedButtonToolTip;
	public static String EIVFoBiExportWizard_NoDataMsg;
	public static String EIVFoBiExportWizard_FinalMsg;

	public static String XmlImpex_Security;
	public static String XmlImpex_DataStore;
	public static String XmlImpex_Languages;
	public static String XmlImpex_Currencies;
	public static String XmlImpex_Countries;
	public static String XmlImpex_UserReports;
	public static String XmlImpex_ProgrammePointTypes;
	public static String XmlImpex_ParticipantStates;
	public static String XmlImpex_ParticipantTypes;
	public static String XmlImpex_GateDevices;
	public static String XmlImpex_Hotels;
	public static String XmlImpex_WebPos;
	public static String XmlImpex_Datatrans;
	public static String XmlImpex_IgnoreErrors;
	public static String XmlImpex_Invoices;
	public static String XmlImpex_InvoiceNoRanges;
	public static String XmlImpex_CostCenters;
	public static String XmlImpex_CostObjects;
	public static String XmlImpex_ImpersonalAccounts;
	public static String XmlImpex_TaxRates;
	public static String XmlImpex_CustomerAccounts;
	public static String XmlImpex_CreditCardTypes;
	public static String XmlImpex_PayEngine;
	public static String XmlImpex_PayEngineSetups;
	public static String XmlImportWizard_Error;
	public static String XmlExportWizard_Error;
	public static String XmlImpex_Profiles;
	public static String XmlImpex_EmailTemplates;
	public static String XmlImpex_EmailDispatches;
	public static String XmlImpex_GlobalCongis;
	public static String XmlImpex_ProfileRelationTypes;
	public static String XmlImpex_ProfileRelations;

	public static String ConnectingTheServerPleaseWait;

	public static String ReadingFile;

	public static String ScanndyDataImportFileWizardPage_OpenTitle;
	public static String ScanndyDataImportFileWizardPage_OpenDesc;


	public static String ExportBtn;
	public static String ImportBtn;
	public static String ExportJob_ErrorMessage;
	public static String ExportJob_Task;

	public static String EventExportDialog_Title;
	public static String EventExportDialog_Message;
	public static String EventExportJob_Name;

	public static String MasterDataExportDialog_Title;
	public static String MasterDataExportDialog_Message;
	public static String MasterDataExportJob_Name;

	public static String ImportDialog_Title;
	public static String ImportDialog_Message;
	public static String ImportDialog_IgnoreSecondaryDataBtn;
	public static String ImportDialog_IgnoreSecondaryDataBtn_tooltip;
	public static String ImportDialog_MasterDataGroup;

	public static String ImportDialog_countryButton_tooltip;
	public static String ImportDialog_languageButton_tooltip;
	public static String ImportDialog_currencyButton_tooltip;
	public static String ImportDialog_creditCardTypeButton_tooltip;
	public static String ImportDialog_payEngineSetupButton_tooltip;
	public static String ImportDialog_accountancyButton_tooltip;
	public static String ImportDialog_gateDeviceButton_tooltip;
	public static String ImportDialog_programmePointTypeButton_tooltip;
	public static String ImportDialog_participantStateButton_tooltip;
	public static String ImportDialog_participantTypeButton_tooltip;
	public static String ImportDialog_hotelButton_tooltip;
	public static String ImportDialog_userButton_tooltip;


	public static String ImportJob_Name;
	public static String ImportJob_Task;
	public static String ImportJob_ErrorMessage;

	public static String ImportJob_DeletingEvent;
	public static String ImportJob_ImportingTableData;
	public static String ImportJob_ImportingFile;

	public static String ReportExportDialog_Title;
	public static String ReportExportDialog_Message;
	public static String ReportExportJob_Name;


	private ImpexI18N() {
	}

}

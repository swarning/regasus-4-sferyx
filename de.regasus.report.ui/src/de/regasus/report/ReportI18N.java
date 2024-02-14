package de.regasus.report;

import org.eclipse.osgi.util.NLS;

public class ReportI18N extends NLS {
	public static final String BUNDLE_NAME = "de.regasus.report.i18n-report-ui";


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ReportI18N.class);
	}


	public static String ReportWizardFactory_UnknownWizard;

	public static String CreateUserReportAction_Text;
	public static String CreateUserReportAction_ToolTip;
	public static String CreateUserReportAction_Error;

	public static String CreateUserReportDirAction_Text;
	public static String CreateUserReportDirAction_ToolTip;
	public static String CreateUserReportDirAction_DefaultFolderName;

	public static String CreateUserReportDirErrorMessage;
	public static String DeleteUserReportDirErrorMessage;
	public static String UpdateUserReportDirErrorMessage;

	public static String CreateUserReportErrorMessage;
	public static String UpdateUserReportErrorMessage;
	public static String MoveUserReportErrorMessage;
	public static String UpdateUserReportDirtyWriteMessage;
	public static String MoveUserReportDirtyWriteMessage;
	public static String DeleteUserReportErrorMessage;
	public static String CopyUserReportErrorMessage;

	public static String DeleteUserReportAction_DeleteDirText;
	public static String DeleteUserReportAction_DeleteDirToolTip;
	public static String DeleteUserReportAction_DeleteUserReportText;
	public static String DeleteUserReportAction_DeleteUserReportToolTip;
	public static String DeleteUserReportConfirmation_Title;
	public static String DeleteUserReportConfirmation_Message;
	public static String DeleteUserReportDirConfirmation_Title;
	public static String DeleteUserReportDirConfirmation_Message;

	public static String EditUserReportAction_TextUserReport;
	public static String EditUserReportAction_TextUserReportDir;
	public static String EditUserReportAction_ToolTipUserReport;
	public static String EditUserReportAction_ToolTipUserReportDir;
	public static String EditUserReportAction_Error;

	public static String GenerateReportAction_Text;
	public static String GenerateReportAction_ToolTip;
	public static String GenerateReportAction_SaveReport;
	public static String GenerateReportAction_FileCouldNotBeSaved;

	public static String RefreshUserReportListAction_Text;
	public static String RefreshUserReportListAction_ToolTip;

	public static String TemplateDownloadAction_Text;
	public static String TemplateDownloadAction_ToolTip;
	public static String TemplateDownloadAction_SaveTemplateFileDialog_Title;
	public static String TemplateDownloadAction_FileCouldNotBeSaved;
	public static String TemplateDownloadAction_TemplateCouldNotBeDownloaded;

	public static String UserReportEditor_DeleteTemplateButtonText;
	public static String UserReportEditor_DeleteTemplateButtonToolTip;
	public static String UserReportEditor_LoadTemplateButtonText;
	public static String UserReportEditor_LoadTemplateButtonToolTip;
	public static String UserReportEditor_RefreshTemplateButtonText;
	public static String UserReportEditor_RefreshTemplateButtonToolTip;
	public static String UserReportEditor_Parameter;
	public static String UserReportEditor_ParameterDesc;
	public static String UserReportEditor_ParameterXML;
	public static String UserReportEditor_EditReportParameterButtonText;
	public static String UserReportEditor_EditReportParameterButtonToolTip;
	public static String UserReportEditor_GenerateReportButtonText;
	public static String UserReportEditor_GenerateReportButtonToolTip;
	public static String UserReportEditor_CreateDateTime;
	public static String UserReportEditor_EditDateTime;
	public static String UserReportEditor_CreateUser;
	public static String UserReportEditor_EditUser;
//	public static String UserReportEditor_CreatedOn;
//	public static String UserReportEditor_EditedOn;
//	public static String UserReportEditor_AtTime;
//	public static String UserReportEditor_ByUser;
	public static String UserReportEditor_OverwriteDefaultTemplateDialog_Title;
	public static String UserReportEditor_OverwriteDefaultTemplateDialog_Message;
	public static String UserReportEditor_OpenTemplateFileDialog_Title;
	public static String UserReportEditor_FileCouldNotBeLoaded;
	public static String UserReportEditor_InfoButtonToolTip;
	public static String UserReportEditor_InfoLabelTemplateID;
	public static String UserReportEditor_InfoLabelDirectoryID;

	public static String UserReportEditor_NewName;
	public static String UserReportEditor_DefaultToolTip;

	public static String UserReportEditor_InvalidXMLRequest;

	public static String UserReportTableView_Description;
	public static String UserReportTableView_Name;
	public static String UserReportTableView_Path;

	public static String DefaultWizard_TitlePrefix;

	public static String DocumentFormatWizardPage_Title;
	public static String DocumentFormatWizardPage_Description;
	public static String DocumentFormatWizardPage_FileFormat;
	public static String DocumentFormatWizardPage_FileExtension;

	public static String LanguageWizardPage_Title;
	public static String LanguageWizardPage_Description;
	public static String LanguageWizardPage_Less;
	public static String LanguageWizardPage_LessToolTip;
	public static String LanguageWizardPage_More;
	public static String LanguageWizardPage_MoreToolTip;


	public static String ReportWizardDialog_GenerateButton;
	public static String ReportWizardDialog_FinishButton;

	public static String UserReportTreeView_Title;

	public static String UserReportTreeView_ToolTip;

	public static String UserReportEditor_EditTemplateButtonText;

	public static String UserReportEditor_EditTemplateButtonToolTip;


	private ReportI18N() {
	}

}

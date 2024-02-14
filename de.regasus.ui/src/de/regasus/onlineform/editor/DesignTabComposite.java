package de.regasus.onlineform.editor;

import static com.lambdalogic.messeinfo.email.EmailTemplateSystemRole.*;
import static com.lambdalogic.util.CollectionsHelper.empty;
import static com.lambdalogic.util.StringHelper.isNotEmpty;
import static de.regasus.LookupService.*;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.email.EmailTemplate;
import com.lambdalogic.messeinfo.email.EmailTemplateComparator;
import com.lambdalogic.messeinfo.email.EmailTemplateRegistrationFormHelper;
import com.lambdalogic.messeinfo.email.EmailTemplateSystemRole;
import com.lambdalogic.messeinfo.kernel.AbstractEntity2;
import com.lambdalogic.messeinfo.kernel.sql.SQLParameter;
import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup_Location_Position_Comparator;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.participant.data.FormProgrammePointTypeConfigVO;
import com.lambdalogic.messeinfo.participant.data.FormProgrammePointTypeConfigVO_Position_Comparator;
import com.lambdalogic.messeinfo.participant.data.ParticipantSearchData;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.messeinfo.participant.interfaces.IFormProgrammePointTypeConfigManager;
import com.lambdalogic.messeinfo.regasus.RegistrationFormConfig;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.CurrencyAmount;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.TypeHelper;
import com.lambdalogic.util.Vigenere2;
import com.lambdalogic.util.rcp.BrowserHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateComposite;
import com.lambdalogic.util.rcp.widget.ColorChooser;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.email.EmailTemplateModel;
import de.regasus.event.ParticipantType;
import de.regasus.onlineform.OnlineFormConstants;
import de.regasus.onlineform.OnlineFormI18N;
import de.regasus.onlineform.RegistrationFormConfigModel;
import de.regasus.onlineform.dialog.FormProgrammePointTypeConfigDialog;
import de.regasus.onlineform.dialog.HiddenCustomFieldsDialog;
import de.regasus.onlineform.provider.ParticipantCustomFieldPKLabelProvider;
import de.regasus.onlineform.provider.ParticipantTypePKLabelProvider;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantSearchModel;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.type.combo.ParticipantTypeCombo;
import de.regasus.programme.ProgrammeOfferingModel;
import de.regasus.programme.ProgrammePointModel;
import de.regasus.programme.programmepoint.ProgrammePointLabelProvider;
import de.regasus.ui.Activator;

public class DesignTabComposite extends Composite {

	// entities
	private RegistrationFormConfig registrationFormConfig;

	private EventVO eventVO;

	// models
	private ProgrammePointModel ppModel = ProgrammePointModel.getInstance();

	private ProgrammeOfferingModel poModel = ProgrammeOfferingModel.getInstance();

	private RegistrationFormConfigModel registrationFormConfigModel = RegistrationFormConfigModel.getInstance();

	private ParticipantCustomFieldGroupModel pcfgModel = ParticipantCustomFieldGroupModel.getInstance();


	// widgets
	private ColorChooser backgroundColorChooser;

	private ColorChooser foregroundColorChooser;

	private ColorChooser panelHeaderColorChooser;

	private ColorChooser fontPanelHeaderColorChooser;

	private ColorChooser panelColorChooser;

	private ColorChooser outsideColorChooser;

	private ColorChooser generatedBannerColorChooser;

	private ColorChooser generatedBannerFontColorChooser;

	private ColorChooser buttonBackgroundColorChooser;

	private ColorChooser buttonBorderColorChooser;

	private ColorChooser buttonFontColorChooser;

	private ColorChooser linksColorChooser;

	private Button showGeneratedBannerButton;

	private LeftCenterRightComposite leftCenterRightComposite;
	private LeftCenterRightComposite logoutButtonAlignmentComposite;
	private LeftCenterRightComposite printButtonAlignmentComposite;

	private Button hideNavbarButton;

	private Button hideBordersButton;

	private Button hideConstraintsWhenNoErrorButton;

	private Button hidePageHeadersButton;

	private Button hideSmokerOptionButton;

	private Button showLinkNewRegistrationButton;

	private Button autoCloseLastPageButton;

	private Button hideLogoutButton;

	private Button showInputRequiredHintButton;

	private Text logoutUrlText;

	private DateComposite startTime;

	private DateComposite endTime;

	private Text alternativeDomain;

	private Button redirectToDigitalEventButton;

	private ModifySupport modifySupport = new ModifySupport(this);

	private Button openWebsiteButton;

	private Button eventProgrammePointTypeConfigButton;

	private Button hiddenParticipantTypesButton;

	private Button hiddenCustomFieldsButton;

	private Button hiddenParticipantCustomFieldsButton;

	private Button exportLinksButton;

	private Button setCcPaymentFeeButton;

	private ParticipantTypeCombo participantTypeCombo;

	private Button confirmationMailsButton;

	private Button confirmationCallationMailsButton;

	private Button allowOverlappingBookingsButton;

	private Button showAvailablePlacesButton;

	private Button allowInputOfBookingInfoButton;

	private Label firstPagePasswordLabel;

	private Text firstPagePassword;


	/**
	 * This tab shows lots of widgets within three groups:
	 * <ul>
	 * <li>Functions group on the left, with buttons that start something</i>
	 * <li>Configuration group on top, for the registration period</i>
	 * <li>Layout group in the rest, for colors and other stuff</i>
	 * <ul>
	 * The groups as such are not modelled within own classes, just created as group.
	 */
	public DesignTabComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(3, false));

		// ******************************************
		// Functions group
		Group functionsGroup = new Group(this, SWT.NONE);
		functionsGroup.setLayout(new GridLayout(1, false));
		functionsGroup.setText(OnlineFormI18N.Functions);
		functionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));

		openWebsiteButton = new Button(functionsGroup, SWT.PUSH);
		openWebsiteButton.setText(OnlineFormI18N.Website);
		openWebsiteButton.setToolTipText(OnlineFormI18N.OpenFormInBrowser);
		openWebsiteButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		openWebsiteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openWebsite();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		// Separating space
		new Label(functionsGroup, SWT.NONE);

		eventProgrammePointTypeConfigButton = new Button(functionsGroup, SWT.PUSH);
		eventProgrammePointTypeConfigButton.setText(ParticipantLabel.ProgrammePointTypes.getString());
		eventProgrammePointTypeConfigButton.setToolTipText(OnlineFormI18N.ConfigureOrderAndBookingRequirementsForProgrammePointsPerTypes);
		eventProgrammePointTypeConfigButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		eventProgrammePointTypeConfigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					editProgrammePointTypeConfigs();

				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		{
    		final String elements = ParticipantLabel.ParticipantTypes.getString();
    		final String message = OnlineFormI18N.SelectWhichElementsToHideInOnlineForm.replace("<elements>", elements);


    		hiddenParticipantTypesButton = new Button(functionsGroup, SWT.PUSH);
    		hiddenParticipantTypesButton.setText(elements);
    		hiddenParticipantTypesButton.setToolTipText(message);
    		hiddenParticipantTypesButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    		hiddenParticipantTypesButton.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				try {
    					configureHiddenParticipantTypes(elements, message);
    				}
    				catch (Exception ex) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
    				}
    			}
    		});

    		new Label(functionsGroup, SWT.NONE).setText(UtilI18N.Default+":");
    		try {
    			participantTypeCombo = new ParticipantTypeCombo(functionsGroup, SWT.NONE);
    			participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    		}
    		catch (Exception ex) {
    			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), ex);
    		}
		}

		// Separating space
		new Label(functionsGroup, SWT.NONE);

		{
    		final String elements = ContactLabel.CustomFields.getString();
    		final String message = OnlineFormI18N.SelectWhichElementsToHideInOnlineForm.replace("<elements>", elements);

    		hiddenCustomFieldsButton = new Button(functionsGroup, SWT.PUSH);
    		hiddenCustomFieldsButton.setText(elements);
    		hiddenCustomFieldsButton.setToolTipText(message);
    		hiddenCustomFieldsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    		hiddenCustomFieldsButton.addSelectionListener(new SelectionAdapter() {
    			@Override
    			public void widgetSelected(SelectionEvent e) {
    				try {
    					configureHiddenCustomFields(elements, message);
    				}
    				catch (Exception ex) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
    				}
    			}
    		});
		}

		{
			final String elements = de.regasus.core.ui.CoreI18N.Config_TypedCustomFields;
			final String message = OnlineFormI18N.SelectWhichElementsToHideInOnlineForm.replace("<elements>", elements);

			hiddenParticipantCustomFieldsButton = new Button(functionsGroup, SWT.PUSH);
			hiddenParticipantCustomFieldsButton.setText(elements);
			hiddenParticipantCustomFieldsButton.setToolTipText(message);
			hiddenParticipantCustomFieldsButton.setText(de.regasus.core.ui.CoreI18N.Config_TypedCustomFields);
			hiddenParticipantCustomFieldsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			hiddenParticipantCustomFieldsButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						configureHiddenParticipantCustomFields(elements, message);
					}
					catch (Exception ex) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
					}
				}
			});
		}

		confirmationMailsButton = new Button(functionsGroup, SWT.PUSH);
		confirmationMailsButton.setText(OnlineFormI18N.ConfirmationMails);
		confirmationMailsButton.setToolTipText(OnlineFormI18N.SelectWhichToUseAsConfirmationMails);
		confirmationMailsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		confirmationMailsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					configureSystemMails(CONFIRMATION, OnlineFormI18N.SelectWhichToUseAsConfirmationMails);
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		confirmationCallationMailsButton = new Button(functionsGroup, SWT.PUSH | SWT.WRAP);
		confirmationCallationMailsButton.setText(OnlineFormI18N.ConfirmationCancellationMails);
		confirmationCallationMailsButton.setToolTipText(OnlineFormI18N.SelectWhichToUseAsCancellationConfirmationMails);
		confirmationCallationMailsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		confirmationCallationMailsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					configureSystemMails(CONFIRM_CANCEL, OnlineFormI18N.SelectWhichToUseAsCancellationConfirmationMails);
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		exportLinksButton = new Button(functionsGroup, SWT.PUSH);
		exportLinksButton.setText(OnlineFormI18N.ExportLinks);
		exportLinksButton.setToolTipText(OnlineFormI18N.ExportRegistrationsLinksForParticipantsToCSVFile);
		exportLinksButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		exportLinksButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					exportLinks();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		setCcPaymentFeeButton = new Button(functionsGroup, SWT.PUSH);
		setCcPaymentFeeButton.setText(OnlineFormI18N.CcPaymentFee);
		setCcPaymentFeeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setCcPaymentFeeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					configureCcPaymentFee();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});

		// ******************************************
		// Configurations group

		Group configurationGroup = new Group(this, SWT.NONE);
		configurationGroup.setLayout(new GridLayout(2, false));
		configurationGroup.setText(OnlineFormI18N.Configuration);
		configurationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.StartRegistrationPeriod);
		startTime = new DateComposite(configurationGroup, SWT.BORDER);
		WidgetSizer.setWidth(startTime);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.EndRegistrationPeriod);
		endTime = new DateComposite(configurationGroup, SWT.BORDER);
		WidgetSizer.setWidth(endTime);

		SWTHelper.createLabel(configurationGroup, OnlineFormI18N.AlternativeDomain);
		alternativeDomain = new Text(configurationGroup, SWT.BORDER);
		alternativeDomain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(configurationGroup, "");
		redirectToDigitalEventButton = new Button(configurationGroup, SWT.CHECK);
		redirectToDigitalEventButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		redirectToDigitalEventButton.setText(OnlineFormI18N.RedirectToDigitalEvent);
		redirectToDigitalEventButton.setToolTipText(OnlineFormI18N.RedirectToDigitalEvent_desc);


		firstPagePasswordLabel = new Label(configurationGroup, SWT.RIGHT);
		firstPagePasswordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		firstPagePasswordLabel.setText(OnlineFormI18N.FirstPagePassword);
		firstPagePasswordLabel.setVisible(registrationFormConfig != null ? registrationFormConfig.isPasswordProtectedFirstPageEnable() : false);
		firstPagePassword = new Text(configurationGroup, SWT.BORDER);
		firstPagePassword.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		firstPagePassword.setVisible(registrationFormConfig != null ? registrationFormConfig.isPasswordProtectedFirstPageEnable() : false);

		// ******************************************
		// Options group

		Group optionsGroup = new Group(this, SWT.NONE);
		optionsGroup.setLayout(new GridLayout(2, false));
		optionsGroup.setText(OnlineFormI18N.Options);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));

		SWTHelper.createLabel(optionsGroup, OnlineFormI18N.AllowOverlappingBookings);
		allowOverlappingBookingsButton = new Button(optionsGroup, SWT.CHECK);

		SWTHelper.createLabel(optionsGroup, OnlineFormI18N.AllowInputOfBookingInfo);
		allowInputOfBookingInfoButton = new Button(optionsGroup, SWT.CHECK);

		SWTHelper.createLabel(optionsGroup, OnlineFormI18N.ShowAvailablePlaces);
		showAvailablePlacesButton = new Button(optionsGroup, SWT.CHECK);

		// ******************************************
		// Layout group

		Group layoutGroup = new Group(this, SWT.NONE);
		layoutGroup.setLayout(new GridLayout(2, false));
		layoutGroup.setText(OnlineFormI18N.Layout);
		layoutGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.BackgroundColor);
		backgroundColorChooser = new ColorChooser(layoutGroup);
		backgroundColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ForegroundColor);
		foregroundColorChooser = new ColorChooser(layoutGroup);
		foregroundColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ColorPanel);
		panelColorChooser = new ColorChooser(layoutGroup);
		panelColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ColorPanelHeader);
		panelHeaderColorChooser = new ColorChooser(layoutGroup);
		panelHeaderColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ColorFontPanelHeader);
		fontPanelHeaderColorChooser = new ColorChooser(layoutGroup);
		fontPanelHeaderColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ColorOutside);
		outsideColorChooser = new ColorChooser(layoutGroup);
		outsideColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ButtonBackgroundColor);
		buttonBackgroundColorChooser = new ColorChooser(layoutGroup);
		buttonBackgroundColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ButtonBorderColor);
		buttonBorderColorChooser = new ColorChooser(layoutGroup);
		buttonBorderColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ButtonFontColor);
		buttonFontColorChooser = new ColorChooser(layoutGroup);
		buttonFontColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.LinksColor);
		linksColorChooser = new ColorChooser(layoutGroup);
		linksColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.BannerAlignment);
		leftCenterRightComposite = new LeftCenterRightComposite(layoutGroup, SWT.NONE);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.LogoutButtonAlignment);
		logoutButtonAlignmentComposite = new LeftCenterRightComposite(layoutGroup, SWT.NONE, false /*withResponsive*/);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.PrintButtonAlignment);
		printButtonAlignmentComposite = new LeftCenterRightComposite(layoutGroup, SWT.NONE, false /*withResponsive*/);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ShowGeneratedBanner);
		showGeneratedBannerButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ColorGeneratedBanner);
		generatedBannerColorChooser = new ColorChooser(layoutGroup);
		generatedBannerColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ColorFontGeneratedBanner);
		generatedBannerFontColorChooser = new ColorChooser(layoutGroup);
		generatedBannerFontColorChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.HideNavbar);
		hideNavbarButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.HideBorders);
		hideBordersButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.HidePageHeaders);
		hidePageHeadersButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.HideSmokerOption);
		hideSmokerOptionButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.HideConstraintsWhenNoError);
		hideConstraintsWhenNoErrorButton= new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ShowInputRequiredHint);
		showInputRequiredHintButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.ShowLinkNewRegistration);
		showLinkNewRegistrationButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.AutoCloseLastPage);
		autoCloseLastPageButton = new Button(layoutGroup, SWT.CHECK);

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.UrlAfterLogout);
		logoutUrlText = new Text(layoutGroup, SWT.BORDER);
		logoutUrlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SWTHelper.createLabel(layoutGroup, OnlineFormI18N.HideLogout);
		hideLogoutButton = new Button(layoutGroup, SWT.CHECK);

		startTime.addModifyListener(modifySupport);
		endTime.addModifyListener(modifySupport);
		alternativeDomain.addModifyListener(modifySupport);
		redirectToDigitalEventButton.addSelectionListener(modifySupport);
		firstPagePassword.addModifyListener(modifySupport);

		allowInputOfBookingInfoButton.addSelectionListener(modifySupport);
		showAvailablePlacesButton.addSelectionListener(modifySupport);
		allowOverlappingBookingsButton.addSelectionListener(modifySupport);
		showGeneratedBannerButton.addSelectionListener(modifySupport);
		hideBordersButton.addSelectionListener(modifySupport);
		hideNavbarButton.addSelectionListener(modifySupport);
		hideConstraintsWhenNoErrorButton.addSelectionListener(modifySupport);
		showInputRequiredHintButton.addSelectionListener(modifySupport);
		hidePageHeadersButton.addSelectionListener(modifySupport);
		hideSmokerOptionButton.addSelectionListener(modifySupport);
		showLinkNewRegistrationButton.addSelectionListener(modifySupport);
		autoCloseLastPageButton.addSelectionListener(modifySupport);
		hideLogoutButton.addSelectionListener(modifySupport);
		generatedBannerColorChooser.addModifyListener(modifySupport);
		generatedBannerFontColorChooser.addModifyListener(modifySupport);
		buttonBackgroundColorChooser.addModifyListener(modifySupport);
		buttonBorderColorChooser.addModifyListener(modifySupport);
		buttonFontColorChooser.addModifyListener(modifySupport);
		linksColorChooser.addModifyListener(modifySupport);
		panelHeaderColorChooser.addModifyListener(modifySupport);
		fontPanelHeaderColorChooser.addModifyListener(modifySupport);
		panelColorChooser.addModifyListener(modifySupport);
		outsideColorChooser.addModifyListener(modifySupport);
		backgroundColorChooser.addModifyListener(modifySupport);
		foregroundColorChooser.addModifyListener(modifySupport);
		leftCenterRightComposite.addSelectionListener(modifySupport);
		logoutButtonAlignmentComposite.addSelectionListener(modifySupport);
		printButtonAlignmentComposite.addSelectionListener(modifySupport);
		participantTypeCombo.addModifyListener(modifySupport);
		logoutUrlText.addModifyListener(modifySupport);
	}


	protected void configureCcPaymentFee() throws Exception {
		System.out.println("DesignTabComposite.setCcPaymentFee()");

		List<ProgrammePointVO> programmePointVOs = ppModel.getProgrammePointVOsByEventPK(registrationFormConfig.getEventPK());

		if (programmePointVOs != null) {

			// Zeige Liste aller Programmpunkte
			ListSelectionDialog listDialog = new ListSelectionDialog(
				getShell(),
				programmePointVOs,
				ArrayContentProvider.getInstance(),
				new ProgrammePointLabelProvider(), OnlineFormI18N.ChooseProgrammePointUsedAsFeeForCreditCardPayment
			);

			// Find out which of the PPs is to be shown as selected, if any
			Long creditCardPaymentFeeProgrammePointID = registrationFormConfig.getCreditCardPaymentFeeProgrammeOfferingID();
			if (creditCardPaymentFeeProgrammePointID != null) {
				ProgrammeOfferingVO programmeOfferingVO = poModel.getProgrammeOfferingVO(creditCardPaymentFeeProgrammePointID);
				Long feeProgrammePointPK = programmeOfferingVO.getProgrammePointPK();

				for (ProgrammePointVO programmePointVO : programmePointVOs) {
					if (programmePointVO.getPK().equals(feeProgrammePointPK)) {
						listDialog.setInitialElementSelections(Collections.singletonList(programmePointVO));
						break;
					}
				}
			}
			int code = listDialog.open();
			if (code == Window.OK) {
				Object[] result = listDialog.getResult();
				ProgrammePointVO selectedProgrammePointVO = null;

				if (result != null && result.length > 1) {
					// Falls mehrere PP : Fehler
					MessageDialog.openError(getShell(), UtilI18N.Error, OnlineFormI18N.ProgrammePointUsedAsFeeForCreditCardPaymentMustHavePreciselyOneOffering);
					return;
				}
				else if (result != null && result.length == 1) {
					selectedProgrammePointVO = (ProgrammePointVO) result[0];
				}

				if (selectedProgrammePointVO != null) {
					List<ProgrammeOfferingVO> programmeOfferingVOs = poModel.getProgrammeOfferingVOsByProgrammePointPK(selectedProgrammePointVO.getPK());
					if (CollectionsHelper.empty(programmeOfferingVOs) || programmeOfferingVOs.size() != 1) {
						// Falls ausgewählter PP mehrere oder keine PO: Fehler
						MessageDialog.openError(getShell(), UtilI18N.Error, OnlineFormI18N.ProgrammePointUsedAsFeeForCreditCardPaymentMustHavePreciselyOneOffering);
						return;
					}
					ProgrammeOfferingVO programmeOfferingVO = programmeOfferingVOs.get(0);
					if (programmeOfferingVO.getParticipantTypePK() != null) {
						// Falls PO hat einschränkung für Teilnehmerart: Fehler
						MessageDialog.openError(getShell(), UtilI18N.Error, OnlineFormI18N.ProgrammePointUsedAsFeeForCreditCardPaymentMustHavePreciselyOneOffering);
						return;
					}
					// Hat gewählter PO einen Rechnungsnummernkreis

					if (programmeOfferingVO.getMainPriceVO().getInvoiceNoRangePK() == null) {
						// Falls PO keinen RNKR hat: Fehler
						MessageDialog.openError(getShell(), UtilI18N.Error, OnlineFormI18N.ProgrammePointUsedAsFeeForCreditCardPaymentMustHavePreciselyOneOffering);
						return;
					}


					// Andernfalls in Attribut übernehmen und PO-Preis auf Button zur Info ergänzen
					Long id = programmeOfferingVO.getPK();
					registrationFormConfig.setCreditCardPaymentFeeProgrammeOfferingID(id);
					updateButtonStates();
					modifySupport.fire();

				}
				else {
					registrationFormConfig.setCreditCardPaymentFeeProgrammeOfferingID(null);
					updateButtonStates();
					modifySupport.fire();
				}
			}
		}
	}


	private void updateButtonStates() throws Exception {
		String baseText = OnlineFormI18N.CcPaymentFee;
		Long creditCardPaymentFeeProgrammePointID = registrationFormConfig.getCreditCardPaymentFeeProgrammeOfferingID();
		if (creditCardPaymentFeeProgrammePointID != null) {
			ProgrammeOfferingVO programmeOfferingVO = poModel.getProgrammeOfferingVO(creditCardPaymentFeeProgrammePointID);
			CurrencyAmount currencyAmount = programmeOfferingVO.getCurrencyAmountGross();
			baseText = baseText + " (" + currencyAmount.format() + ")";
		}
		else {
			baseText = baseText + " (" + UtilI18N.None + ")";
		}

		setCcPaymentFeeButton.setText(baseText);
		setCcPaymentFeeButton.setEnabled(registrationFormConfig.isCcPaymentEngineEnabled());

		confirmationCallationMailsButton.setEnabled(registrationFormConfig.isSendCancellationConfirmationMail());
	}


	protected void configureSystemMails(EmailTemplateSystemRole systemRole, String message) throws Exception {
		EmailTemplateModel etsModel = EmailTemplateModel.getInstance();
		List<EmailTemplate> emailTemplateList = etsModel.getEmailTemplateSearchDataByEvent(
			registrationFormConfig.getEventPK(),
			systemRole
		);

		// sort EmailTemplateSearchData by language and name
		emailTemplateList = CollectionsHelper.createArrayList(emailTemplateList);
		Collections.sort(emailTemplateList, EmailTemplateComparator.getInstance());

		List<EmailTemplate> oldSystemEmailTemplateList = new ArrayList<>();
		for (String language : registrationFormConfig.getLanguageCodesList()) {
			EmailTemplate emailTemplate = EmailTemplateRegistrationFormHelper.pickEmailTemplate(systemRole, registrationFormConfig, language, emailTemplateList);
			if (emailTemplate != null) {
				oldSystemEmailTemplateList.add(emailTemplate);
			}
		}

		LabelProvider labelProvider = new EmailTemplateLabelProvider();

		ListSelectionDialog listDialog = new ListSelectionDialog(
			getShell(),
			emailTemplateList,
			ArrayContentProvider.getInstance(),
			labelProvider,
			EmailLabel.EmailTemplates.getString()
		);

		listDialog.setMessage(message);
		listDialog.setInitialElementSelections(oldSystemEmailTemplateList);

		int code = listDialog.open();
		if (code == Window.OK) {
			Object[] result = listDialog.getResult();

			// Validate selection: for each language there must be one confirmation mail, and not two per language
			List<EmailTemplate> newSystemEmailTemplateList = new ArrayList<>();
			for (Object object : result) {
				newSystemEmailTemplateList.add((EmailTemplate) object);
			}

			// Update on server
			EmailTemplateModel emailTemplateModel = EmailTemplateModel.getInstance();
			for (EmailTemplate etsd : emailTemplateList) {
				if (newSystemEmailTemplateList.contains(etsd) && ! oldSystemEmailTemplateList.contains(etsd)) {
					EmailTemplate emailTemplate = emailTemplateModel.getEmailTemplate(etsd.getID());
					emailTemplate.setRegistrationFormConfigPK(registrationFormConfig.getId());
					emailTemplateModel.update(emailTemplate);
				}
				else if (! newSystemEmailTemplateList.contains(etsd) && oldSystemEmailTemplateList.contains(etsd)) {
					EmailTemplate emailTemplate = emailTemplateModel.getEmailTemplate(etsd.getID());
					emailTemplate.setRegistrationFormConfigPK(null);
					emailTemplateModel.update(emailTemplate);
				}
			}

			Long eventPK = registrationFormConfig.getEventPK();
			List<EmailTemplate> allEmailTemplatesForThisEvent = emailTemplateModel.getEmailTemplateSearchDataByEvent(eventPK);
			List<RegistrationFormConfig> allRegistrationFormConfigsForThisEvent = registrationFormConfigModel.getRegistrationFormConfigsByEventPK(eventPK);
			Collection<Language> languages = LanguageModel.getInstance().getAllUndeletedLanguages();
			List<String> warnings = EmailTemplateRegistrationFormHelper.validateEmailTemplateConfiguration(allEmailTemplatesForThisEvent, allRegistrationFormConfigsForThisEvent, languages);
			if (warnings.size() > 0) {
				String warning = StringHelper.concatenateLines(warnings);
				String warningWithoutHTMLAchorsForLink = warning.replace("<A>", "").replace("</A>", "");
				MessageDialog.openWarning(getShell(), UtilI18N.Warning, warningWithoutHTMLAchorsForLink);
			}
		}
	}


	protected void exportLinks() {

		FileDialog dialog = new FileDialog (getShell(), SWT.SAVE);
		String [] filterNames = new String [] {"CSV-Dateien", "Alle Dateien (*)"};
		String [] filterExtensions = new String [] {"*.csv", "*"};
		String filterPath = "/";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterNames = new String [] {"CSV-Dateien", "Alle Dateien (*.*)"};
			filterExtensions = new String [] {"*.csv", "*.*"};
		}
		dialog.setFilterNames (filterNames);
		dialog.setFilterExtensions (filterExtensions);
		dialog.setFilterPath (filterPath);
		dialog.setFileName (registrationFormConfig.getWebId()+ "_Links.csv");
		String path = dialog.open();
		if (path != null) {

			try {
				File file = new File(path);
				PrintWriter pw = new PrintWriter(file);
				int count = 0;

				ParticipantStateModel participantStateModel = ParticipantStateModel.getInstance();
				ParticipantSearchModel searchModel = ParticipantSearchModel.getDetachedInstance();
				searchModel.setEventPK(eventVO.getPK());
				searchModel.setSqlParameterList(new ArrayList<SQLParameter>());

				List<ParticipantSearchData> list = searchModel.getModelData();


				String webId = registrationFormConfig.getWebId();
				String url = registrationFormConfigModel.getOnlineWebappUrl(webId);
				boolean urlContainsQueryParameter = url.contains("?");

				StringBuilder line = new StringBuilder(512);

				for (ParticipantSearchData psd : list) {
					Integer number = psd.getNumber();
					String lastName = StringHelper.avoidNull(psd.getLastName());
					String firstName = StringHelper.avoidNull(psd.getFirstName());

					ParticipantState participantState = participantStateModel.getParticipantState(psd.getStatePK());
					String stateName = participantState.getName().getString();

					String vigenere2 = Vigenere2.toVigenereString(psd.getPK());


					Charset cs = Charset.forName("ISO-8859-1");
					String vigenere2Hex = StringHelper.bytesToHexString(vigenere2.getBytes(cs));

					line.setLength(0);
					line.append(number);
					line.append(";").append(lastName);
					line.append(";").append(firstName);
					line.append(";").append(stateName);
					line.append(";").append(url);
					if (urlContainsQueryParameter) {
						line.append("&");
					}
					else {
						line.append("?");
					}
					line.append(OnlineFormConstants.CODE).append("=").append(vigenere2Hex);

					pw.println(line);
					count++;
				}
				pw.close();

				String message = count + " " + OnlineFormI18N.CountLinksExportedShouldOpen;

				boolean shouldOpen = MessageDialog.openConfirm(getShell(), UtilI18N.Info,  message);
				if (shouldOpen) {
					/* save and open generated CSV file
					 * This code is referenced by
					 * https://lambdalogic.atlassian.net/wiki/pages/createpage.action?spaceKey=REGASUS&fromPageId=21987353
					 * Adapt the wiki document if this code is moved to another class or method.
					 */
					FileHelper.open(file);
				}

			} catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	void syncWidgetsToEntity() {
		syncWidgetsToEntityInternal(registrationFormConfig, true);
	}


	private void syncWidgetsToEntityInternal(
		final RegistrationFormConfig rfConfig,
		final boolean avoidEvents) {
		if (rfConfig != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (avoidEvents) {
							modifySupport.setEnabled(false);
						}

						allowInputOfBookingInfoButton.setSelection(rfConfig.isAllowInputOfBookingInfo());
						showAvailablePlacesButton.setSelection(rfConfig.isShowAvailablePlaces());
						allowOverlappingBookingsButton.setSelection(rfConfig.isNoOverlappingCheck());
						foregroundColorChooser.setColorAsInteger(rfConfig.getForegroundColor());
						backgroundColorChooser.setColorAsInteger(rfConfig.getBackgroundColor());
						leftCenterRightComposite.setAlignment(rfConfig.getBannerAlignment());
						logoutButtonAlignmentComposite.setAlignment(rfConfig.getLogoutButtonAlignment());
						printButtonAlignmentComposite.setAlignment(rfConfig.getPrintButtonAlignment());
						showGeneratedBannerButton.setSelection(rfConfig.isShowGeneratedBanner());
						hideBordersButton.setSelection(rfConfig.isHideBorders());
						hideNavbarButton.setSelection(rfConfig.isHideNavbar());
						if(rfConfig.isHotelEnabled()){
							hideSmokerOptionButton.setEnabled(true);
							hideSmokerOptionButton.setSelection(rfConfig.isHideSmokerOption());
						} else {
							hideSmokerOptionButton.setEnabled(false);
							hideSmokerOptionButton.setSelection(false);
						}
						hideConstraintsWhenNoErrorButton.setSelection(rfConfig.isHideConstraintsWhenNoError());
						showInputRequiredHintButton.setSelection(rfConfig.isShowInputRequiredHint());
						hidePageHeadersButton.setSelection(rfConfig.isHidePageHeaders());
						showLinkNewRegistrationButton.setSelection(rfConfig.isShowLinkNewRegistration());
						autoCloseLastPageButton.setSelection(rfConfig.isAutocloseLastPage());
						hideLogoutButton.setSelection(rfConfig.isHideLogout());
						logoutUrlText.setText(StringHelper.avoidNull(rfConfig.getLogoutUrl()));
						generatedBannerColorChooser.setColorAsInteger(rfConfig.getColorGeneratedBanner());
						generatedBannerFontColorChooser.setColorAsInteger(rfConfig.getColorFontGeneratedBanner());
						buttonBackgroundColorChooser.setColorAsInteger(rfConfig.getButtonBackgroundColor());
						buttonBorderColorChooser.setColorAsInteger(rfConfig.getButtonBorderColor());
						buttonFontColorChooser.setColorAsInteger(rfConfig.getButtonFontColor());
						linksColorChooser.setColorAsInteger(rfConfig.getLinksColor());
						panelHeaderColorChooser.setColorAsInteger(rfConfig.getColorPanelHeader());
						fontPanelHeaderColorChooser.setColorAsInteger(rfConfig.getColorFontPanelHeader());
						panelColorChooser.setColorAsInteger(rfConfig.getColorPanel());
						outsideColorChooser.setColorAsInteger(rfConfig.getColorOutside());

						startTime.setLocalDate( TypeHelper.toLocalDate(rfConfig.getStartRegistrationPeriod()) );
						endTime.setLocalDate( TypeHelper.toLocalDate(rfConfig.getEndRegistrationPeriod()) );

						alternativeDomain.setText(StringHelper.avoidNull(rfConfig.getAlternativeDomain()));
						redirectToDigitalEventButton.setSelection( rfConfig.isRedirectToDigitalEvent() );

						firstPagePasswordLabel.setVisible(registrationFormConfig.isPasswordProtectedFirstPageEnable());
						firstPagePassword.setVisible(registrationFormConfig.isPasswordProtectedFirstPageEnable());
						firstPagePassword.setText(StringHelper.avoidNull(rfConfig.getFirstPagePassword()));

						participantTypeCombo.setEventID(eventVO.getID());
						participantTypeCombo.setParticipantTypePK( rfConfig.getDefaultParticipantTypePK() );

						exportLinksButton.setVisible(rfConfig.isLoginPersonalizedLinkEnabled());
						eventProgrammePointTypeConfigButton.setEnabled(rfConfig.isGroupProgrammePointsByType());

						updateButtonStates();

					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						if (avoidEvents) {
							modifySupport.setEnabled(true);
						}
					}
				}
			});

		}
	}

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	public void syncEntityToWidgets() {
		registrationFormConfig.setStartRegistrationPeriod(startTime.getDate());
		registrationFormConfig.setEndRegistrationPeriod(endTime.getDate());
		registrationFormConfig.setAlternativeDomain(StringHelper.trim(alternativeDomain.getText()));
		registrationFormConfig.setRedirectToDigitalEvent( redirectToDigitalEventButton.getSelection() );

		registrationFormConfig.setAllowInputOfBookingInfo(allowInputOfBookingInfoButton.getSelection());
		registrationFormConfig.setShowAvailablePlaces(showAvailablePlacesButton.getSelection());
		registrationFormConfig.setNoOverlappingCheck(allowOverlappingBookingsButton.getSelection());
		registrationFormConfig.setBackgroundColor(backgroundColorChooser.getColorAsInteger());
		registrationFormConfig.setForegroundColor(foregroundColorChooser.getColorAsInteger());
		registrationFormConfig.setBannerAlignment(leftCenterRightComposite.getAlignment());
		registrationFormConfig.setLogoutButtonAlignment(logoutButtonAlignmentComposite.getAlignment());
		registrationFormConfig.setPrintButtonAlignment(printButtonAlignmentComposite.getAlignment());
		registrationFormConfig.setShowGeneratedBanner(showGeneratedBannerButton.getSelection());
		registrationFormConfig.setHideBorders(hideBordersButton.getSelection());
		registrationFormConfig.setHideNavbar(hideNavbarButton.getSelection());
		registrationFormConfig.setHideSmokerOption(hideSmokerOptionButton.getSelection());
		registrationFormConfig.setHideConstraintsWhenNoError(hideConstraintsWhenNoErrorButton.getSelection());
		registrationFormConfig.setShowInputRequiredHint(showInputRequiredHintButton.getSelection());
		registrationFormConfig.setHidePageHeaders(hidePageHeadersButton.getSelection());
		registrationFormConfig.setShowLinkNewRegistration(showLinkNewRegistrationButton.getSelection());
		registrationFormConfig.setAutocloseLastPage(autoCloseLastPageButton.getSelection());
		registrationFormConfig.setHideLogout(hideLogoutButton.getSelection());
		registrationFormConfig.setLogoutUrl(StringHelper.trim(logoutUrlText.getText()));
		registrationFormConfig.setColorGeneratedBanner(generatedBannerColorChooser.getColorAsInteger());
		registrationFormConfig.setColorFontGeneratedBanner(generatedBannerFontColorChooser.getColorAsInteger());
		registrationFormConfig.setButtonBackgroundColor(buttonBackgroundColorChooser.getColorAsInteger());
		registrationFormConfig.setButtonBorderColor(buttonBorderColorChooser.getColorAsInteger());
		registrationFormConfig.setButtonFontColor(buttonFontColorChooser.getColorAsInteger());
		registrationFormConfig.setLinksColor(linksColorChooser.getColorAsInteger());
		registrationFormConfig.setColorPanelHeader(panelHeaderColorChooser.getColorAsInteger());
		registrationFormConfig.setColorFontPanelHeader(fontPanelHeaderColorChooser.getColorAsInteger());
		registrationFormConfig.setColorPanel(panelColorChooser.getColorAsInteger());
		registrationFormConfig.setColorOutside(outsideColorChooser.getColorAsInteger());
		registrationFormConfig.setDefaultParticipantTypePK(participantTypeCombo.getParticipantTypePK());
		registrationFormConfig.setFirstPagePassword(firstPagePassword.getText());
	}


	public void setRegistrationFormConfig(RegistrationFormConfig registrationFormConfig, EventVO eventVO) {
		this.registrationFormConfig = registrationFormConfig;
		this.eventVO = eventVO;

		syncWidgetsToEntity();
	}


	protected void configureHiddenCustomFields(String elements, String message) {

		// Collect the indices of all custom fields from 1 to 40 that have a name.
		// Those from 11 to 30 only if travel page is enabled.
		boolean travelEnabled = registrationFormConfig.isTravelEnabled();
		List<Integer> customFieldIdxs = new ArrayList<>();
		for (int idx = 1; idx <= 40; idx++) {

			// Skip fields 11 to 30 if travel page is not visible
			if (idx >= 11 && idx <=30 && ! travelEnabled) {
				continue;
			}

			// Add index to list if there is a named custom field for it
			if (isNotEmpty(eventVO.getCustomFieldName(idx))) {
				customFieldIdxs.add(idx);
			}
		}

		if (empty(customFieldIdxs)) {
			// If there is no custom field, show a short message dialog, and that's it.
			MessageDialog.openInformation(getShell(), UtilI18N.Info, OnlineFormI18N.NoCustomFieldsIn1To40);
		}
		else {
			// If there are custom fields, show a dialog to select their visibility
			List<Integer> hiddenCustomFieldsList = registrationFormConfig.getHiddenCustomFieldsList();
			HiddenCustomFieldsDialog dialog = new HiddenCustomFieldsDialog(getShell(), eventVO, customFieldIdxs, hiddenCustomFieldsList, travelEnabled);
			int code = dialog.open();

			if (code == Window.OK) {
				List<Integer> hiddenCustomFields = dialog.getHiddenCustomFields();
				registrationFormConfig.setHiddenCustomFieldsList(hiddenCustomFields);
				modifySupport.fire();
			}
		}

	}


	protected void configureHiddenParticipantCustomFields(String elements, String message) throws Exception {
		List<Long> hiddenCustomFieldsList = registrationFormConfig.getHiddenParticipantCustomFieldsList();

		List<ParticipantCustomField> customFields = new ArrayList<>();

		List<ParticipantCustomFieldGroup> pcfGroups = pcfgModel.getParticipantCustomFieldGroupsByEventPK(eventVO.getID());
		if (CollectionsHelper.notEmpty(pcfGroups)) {
			// sort pcfGroups by location first, then by position
			// need to create new array list because the pcfGroups is an unmodifiable list
			List<ParticipantCustomFieldGroup> groups = CollectionsHelper.createArrayList(pcfGroups);
			Collections.sort(groups, ParticipantCustomFieldGroup_Location_Position_Comparator.getInstance());

			for (ParticipantCustomFieldGroup group : groups) {
				List<ParticipantCustomField> pcfList = getParticipantCustomFieldMgr().getParticipantCustomFieldsByGroupPK(group.getID());
				if (CollectionsHelper.notEmpty(pcfList)) {
					customFields.addAll(pcfList);
				}
			}
		}


		List<Long> definedParticipantCustomFieldIDs = AbstractEntity2.getPrimaryKeyList(customFields);


		LabelProvider labelProvider = new ParticipantCustomFieldPKLabelProvider(customFields);

		ListSelectionDialog listDialog =
			new ListSelectionDialog(
				getShell(),
				definedParticipantCustomFieldIDs,
				ArrayContentProvider.getInstance(),
				labelProvider,
				elements);
		listDialog.setMessage(message);
		listDialog.setInitialElementSelections(hiddenCustomFieldsList);

		int code = listDialog.open();
		if (code == Window.OK) {
			modifySupport.fire();

			Object[] result = listDialog.getResult();
			List<Long> hiddenParticipantTypes2 = new ArrayList<>(result.length);
			for (int i = 0; i < result.length; i++) {
				hiddenParticipantTypes2.add((Long) result[i]);
			}
			registrationFormConfig.setHiddenParticipantCustomFieldsList(hiddenParticipantTypes2);

			modifySupport.fire();
		}
	}


	protected void configureHiddenParticipantTypes(String elements, String message) throws Exception {
		List<Long> hiddenParticipantTypes = registrationFormConfig.getHiddenParticipantTypesList();

		List<ParticipantType> participantTypes = getParticipantTypeMgr().readByEvent(eventVO.getPK());
		List<Long> definedParticipantTypes = ParticipantType.getPKs(participantTypes);

		LabelProvider labelProvider = new ParticipantTypePKLabelProvider(participantTypes);

		ListSelectionDialog listDialog = new ListSelectionDialog(
			getShell(),
			definedParticipantTypes,
			ArrayContentProvider.getInstance(),
			labelProvider,
			elements
		);

		listDialog.setMessage(message);
		listDialog.setInitialElementSelections(hiddenParticipantTypes);

		int code = listDialog.open();
		if (code == Window.OK) {
			modifySupport.fire();

			Object[] result = listDialog.getResult();
			List<Long> hiddenParticipantTypes2 = new ArrayList<>(result.length);
			for (int i = 0; i < result.length; i++) {
				hiddenParticipantTypes2.add((Long) result[i]);
			}
			registrationFormConfig.setHiddenParticipantTypesList(hiddenParticipantTypes2);

			modifySupport.fire();
		}
	}


	protected void editProgrammePointTypeConfigs() {
		try {
			IFormProgrammePointTypeConfigManager mgr = getFormProgrammePointTypeConfigMgr();

			mgr.createMissingFormProgrammePointTypeConfigVOs(registrationFormConfig.getEventPK(), registrationFormConfig.getId());
			List<FormProgrammePointTypeConfigVO> configVOs = mgr.getFormProgrammePointTypeConfigVOsByForm(registrationFormConfig.getId());
			Collections.sort(configVOs, FormProgrammePointTypeConfigVO_Position_Comparator.getInstance());

			boolean useProgrammePointTypeNamesAsHeaders = registrationFormConfig.isUseProgrammePointTypeNamesAsHeaders();
			FormProgrammePointTypeConfigDialog dialog =
				new FormProgrammePointTypeConfigDialog(getShell(), configVOs, useProgrammePointTypeNamesAsHeaders);
			int code = dialog.open();
			if (code == Window.OK) {

				boolean newValue = dialog.isUseProgrammePointTypeNamesAsHeaders();
				if (useProgrammePointTypeNamesAsHeaders != newValue) {
					registrationFormConfig.setUseProgrammePointTypeNamesAsHeaders(newValue);
					modifySupport.fire();
				}

				mgr.updateFormProgrammePointTypeConfigList(configVOs);
			}

		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

	}


	protected void openWebsite() {
		String webId = registrationFormConfig.getWebId();
		String url = registrationFormConfigModel.getOnlineWebappUrl(webId);

		BrowserHelper.openBrowser(url);
	}


	void setEditorIsDirty(boolean dirty) {
		openWebsiteButton.setEnabled(!dirty && registrationFormConfig.isOnlineFormEnabled());
//		eventProgrammePointTypeConfigButton.setEnabled(!dirty);
	}


	public void setEventVO(EventVO eventVO) {
		this.eventVO = eventVO;
	}

}

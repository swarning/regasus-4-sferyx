package de.regasus.impex.db.dialog;

import static de.regasus.impex.Constants.CHARSET;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.kernel.interfaces.ImportMetadata;
import com.lambdalogic.messeinfo.kernel.interfaces.ImportSettings;
import com.lambdalogic.messeinfo.kernel.interfaces.ServerRole;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.report.ReportLabel;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.impex.Constants;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.ui.Activator;


public class ImportDialog extends TitleAreaDialog {

	/**
	 * The File which was chosen by the user when this dialog was used the last time (during the current application session)
	 */
	private static File selectedFile;


	/**
	 * Last selected value of {@link ignoreSecondaryDataButton}.
	 */
	private static Boolean ignoreSecondaryData;


	private Button okButton;
	private FileSelectionComposite fileSelectionComposite;

	private Button eventButton;
	private Button ignoreSecondaryDataButton;

	private Button reportButton;

	// one Button (check box) for each master data domain
	private Button countryButton;
	private Button languageButton;
	private Button currencyButton;
	private Button creditCardTypeButton;
	private Button paymentSystemSetupButton;
	private Button accountancyButton;
	private Button gateDeviceButton;
	private Button programmePointTypeButton;
	private Button participantStateButton;
	private Button participantTypeButton;
	private Button hotelButton;
	private Button userButton;


	// values reflecting metadata.csv of the last opened ZIP file
	private ImportMetadata importMetadata = new ImportMetadata();


	// result value of this Dialog
	private ImportSettings importSettings;


	private ModifyListener fileSelectionModifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			selectedFile = fileSelectionComposite.getFile();

			updateImportButtonsStatus();
			updateButtonStatus();
		}
	};


	private SelectionAdapter importButtonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateButtonStatus();
		}
	};


	public ImportDialog(Shell parentShell) throws Exception {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE );
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(ImpexI18N.ImportDialog_Title);
		setMessage(ImpexI18N.ImportDialog_Message);

		// create dialog area that is a Composite with a single column
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		// create area as parent for the widgets of this Dialog with individual layout
		Composite area = new Composite(dialogArea, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(new GridLayout(2, true));

		try {

			/*
			 * file selection
			 */
			fileSelectionComposite = new FileSelectionComposite(area, SWT.OPEN);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			fileSelectionComposite.setFilterExtensions(new String[]{"*.zip", "*.*"});


			// CheckBox: import Event
			eventButton = new Button(area, SWT.CHECK);
			eventButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			eventButton.setText(ParticipantLabel.Event.getString());
			//importEventButton.setToolTipText(EmailI18N.ImportDialog_ImportEventBtn_tooltip);
			eventButton.addSelectionListener(importButtonSelectionListener);


			ignoreSecondaryDataButton = new Button(area, SWT.CHECK);
			ignoreSecondaryDataButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			ignoreSecondaryDataButton.setText(ImpexI18N.ImportDialog_IgnoreSecondaryDataBtn);
			ignoreSecondaryDataButton.setToolTipText(ImpexI18N.ImportDialog_IgnoreSecondaryDataBtn_tooltip);

			if (ignoreSecondaryData == null) {
				// ignoreSecondaryDataButton is checked by default if the current server is a MAIN system
				ServerRole serverRole = PropertyModel.getInstance().getServerRole();
				ignoreSecondaryData = Boolean.valueOf(serverRole == ServerRole.MAIN);
			}
			ignoreSecondaryDataButton.setSelection(ignoreSecondaryData);


			// CheckBox: import Report data
			reportButton = new Button(area, SWT.CHECK);
			reportButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			reportButton.setText(ReportLabel.userReports.getString());
			reportButton.addSelectionListener(importButtonSelectionListener);

			// placeholder
			new Label(area, SWT.NONE);


			// *********************************************************************************************************
			// * One Button (check box) for each master data domain
			// *

			Group masterDataGroup = new Group(area, SWT.NONE);
			masterDataGroup.setText(ImpexI18N.ImportDialog_MasterDataGroup);
			masterDataGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			masterDataGroup.setLayout(new GridLayout(2, true));


			// CheckBox: import Country
			countryButton = new Button(masterDataGroup, SWT.CHECK);
			countryButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			countryButton.setText(ContactLabel.countries.getString());
			countryButton.setToolTipText(ImpexI18N.ImportDialog_countryButton_tooltip);
			countryButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import Language
			languageButton = new Button(masterDataGroup, SWT.CHECK);
			languageButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			languageButton.setText(ContactLabel.languages.getString());
			languageButton.setToolTipText(ImpexI18N.ImportDialog_languageButton_tooltip);
			languageButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import Currency
			currencyButton = new Button(masterDataGroup, SWT.CHECK);
			currencyButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			currencyButton.setText(InvoiceLabel.Currencies.getString());
			currencyButton.setToolTipText(ImpexI18N.ImportDialog_currencyButton_tooltip);
			currencyButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import CreditCardType
			creditCardTypeButton = new Button(masterDataGroup, SWT.CHECK);
			creditCardTypeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			creditCardTypeButton.setText(ContactLabel.creditCardTypes.getString());
			creditCardTypeButton.setToolTipText(ImpexI18N.ImportDialog_creditCardTypeButton_tooltip);
			creditCardTypeButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import PayEngineSetup
			paymentSystemSetupButton = new Button(masterDataGroup, SWT.CHECK);
			paymentSystemSetupButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			paymentSystemSetupButton.setText(InvoiceLabel.PaymentSystemSetup.getString());
			paymentSystemSetupButton.setToolTipText(ImpexI18N.ImportDialog_payEngineSetupButton_tooltip);
			paymentSystemSetupButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import Accountancy
			accountancyButton = new Button(masterDataGroup, SWT.CHECK);
			accountancyButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			accountancyButton.setText(InvoiceLabel.Accountancy.getString());
			accountancyButton.setToolTipText(ImpexI18N.ImportDialog_accountancyButton_tooltip);
			accountancyButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import GateDevice
			gateDeviceButton = new Button(masterDataGroup, SWT.CHECK);
			gateDeviceButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			gateDeviceButton.setText(ParticipantLabel.GateDevices.getString());
			gateDeviceButton.setToolTipText(ImpexI18N.ImportDialog_gateDeviceButton_tooltip);
			gateDeviceButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import ProgrammePointType
			programmePointTypeButton = new Button(masterDataGroup, SWT.CHECK);
			programmePointTypeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			programmePointTypeButton.setText(ParticipantLabel.ProgrammePointTypes.getString());
			programmePointTypeButton.setToolTipText(ImpexI18N.ImportDialog_programmePointTypeButton_tooltip);
			programmePointTypeButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import ParticipantState
			participantStateButton = new Button(masterDataGroup, SWT.CHECK);
			participantStateButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			participantStateButton.setText(ParticipantLabel.ParticipantStates.getString());
			participantStateButton.setToolTipText(ImpexI18N.ImportDialog_participantStateButton_tooltip);
			participantStateButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import ParticipantType
			participantTypeButton = new Button(masterDataGroup, SWT.CHECK);
			participantTypeButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			participantTypeButton.setText(ParticipantLabel.ParticipantTypes.getString());
			participantTypeButton.setToolTipText(ImpexI18N.ImportDialog_participantTypeButton_tooltip);
			participantTypeButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import Hotel
			hotelButton = new Button(masterDataGroup, SWT.CHECK);
			hotelButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			hotelButton.setText(HotelLabel.Hotels.getString());
			hotelButton.setToolTipText(ImpexI18N.ImportDialog_hotelButton_tooltip);
			hotelButton.addSelectionListener(importButtonSelectionListener);

			// CheckBox: import User
			userButton = new Button(masterDataGroup, SWT.CHECK);
			userButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
			userButton.setText(AccountLabel.User.getString());
			userButton.setToolTipText(ImpexI18N.ImportDialog_userButton_tooltip);
			userButton.addSelectionListener(importButtonSelectionListener);

			// *
			// * One Button (check box) for each master data domain
			// *********************************************************************************************************


			/* set initial File
			 * Do this after observing fileSelectionComposite! Otherwise setting the initial File won't be recognized
			 * and selectedFile remains empty.
			 */
			fileSelectionComposite.addModifyListener(fileSelectionModifyListener);

			// restore previous selected file
			if (selectedFile != null) {
				fileSelectionComposite.setFile(selectedFile);
			}


			// update Buttons after initialization
			updateImportButtonsStatus();
			updateButtonStatus();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return area;
	}


	/**
	 * The Window Icon and Title are made identical to the icon and the tooltip of the button that opens this dialog.
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
//		shell.setText(UtilI18N.Question);
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, ImpexI18N.ImportBtn, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		updateButtonStatus();
	}


	@Override
	protected void okPressed() {
		// store current setting and selections
		selectedFile = fileSelectionComposite.getFile();

		ignoreSecondaryData = ignoreSecondaryDataButton.getSelection();

		importSettings = new ImportSettings();
		importSettings.setIgnoreSecondaryData(ignoreSecondaryData);
		importSettings.setIgnoreEvent(              eventButton.isEnabled() &&              !eventButton.getSelection());

		importSettings.setIgnoreCountry(            countryButton.isEnabled() &&            !countryButton.getSelection());
		importSettings.setIgnoreLanguage(           languageButton.isEnabled() &&           !languageButton.getSelection());
		importSettings.setIgnoreCurrency(           currencyButton.isEnabled() &&           !currencyButton.getSelection());
		importSettings.setIgnoreCreditCardType(     creditCardTypeButton.isEnabled() &&     !creditCardTypeButton.getSelection());
		importSettings.setIgnorePaymentSystemSetup( paymentSystemSetupButton.isEnabled() && !paymentSystemSetupButton.getSelection());
		importSettings.setIgnoreAccountancy(        accountancyButton.isEnabled() &&        !accountancyButton.getSelection());
		importSettings.setIgnoreGateDevice(         gateDeviceButton.isEnabled() &&         !gateDeviceButton.getSelection());
		importSettings.setIgnoreProgrammePointType( programmePointTypeButton.isEnabled() && !programmePointTypeButton.getSelection());
		importSettings.setIgnoreParticipantState(   participantStateButton.isEnabled() &&   !participantStateButton.getSelection());
		importSettings.setIgnoreParticipantType(    participantTypeButton.isEnabled() &&    !participantTypeButton.getSelection());
		importSettings.setIgnoreHotel(              hotelButton.isEnabled() &&              !hotelButton.getSelection());
		importSettings.setIgnoreUser(               userButton.isEnabled() &&               !userButton.getSelection());

		importSettings.setIgnoreReport(             reportButton.isEnabled() &&             !reportButton.getSelection());

		super.okPressed();
	}


	private void updateButtonStatus() {
		if (okButton != null) {
			boolean enabled =
				selectedFile != null
				&& (   eventButton.getSelection()
					|| countryButton.getSelection()
					|| languageButton.getSelection()
					|| currencyButton.getSelection()
					|| creditCardTypeButton.getSelection()
					|| paymentSystemSetupButton.getSelection()
					|| accountancyButton.getSelection()
					|| gateDeviceButton.getSelection()
					|| programmePointTypeButton.getSelection()
					|| participantStateButton.getSelection()
					|| participantTypeButton.getSelection()
					|| hotelButton.getSelection()
					|| userButton.getSelection()
					|| reportButton.getSelection()
				);

			okButton.setEnabled(enabled);
		}
	}


	private void updateImportButtonsStatus() {
		/*
		 * read metadata.csv and set show variables according to the tags found there
		 */

		// set all show variables to false
		importMetadata.init();

		if (selectedFile != null) {
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(selectedFile);
				ZipEntry metadataZipEntry = zipFile.getEntry(Constants.METADATA_FILE_NAME);
				if (metadataZipEntry != null) {
					InputStream inputStream = zipFile.getInputStream(metadataZipEntry);
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream, CHARSET);
					BufferedReader reader = new BufferedReader(inputStreamReader);

					String line = reader.readLine();
					while (line != null) { // line == null --> end of the stream

						if (line.equals(Constants.EVENT_TAG)) {
							importMetadata.setEventAvailable(true);
						}
						else if (line.equals(Constants.COUNTRY_TAG)) {
							importMetadata.setCountryAvailable(true);
						}
						else if (line.equals(Constants.LANGUAGE_TAG)) {
							importMetadata.setLanguageAvailable(true);
						}
						else if (line.equals(Constants.CURRENCY_TAG)) {
							importMetadata.setCurrencyAvailable(true);
						}
						else if (line.equals(Constants.CREDIT_CARD_TYPE_TAG)) {
							importMetadata.setCreditCardTypeAvailable(true);
						}
						else if (line.equals(Constants.PAYMENT_SYSTEM_SETUP_TAG)) {
							importMetadata.setPaymentSystemSetupAvailable(true);
						}
						else if (line.equals(Constants.ACCOUNTANCY_TAG)) {
							importMetadata.setAccountancyAvailable(true);
						}
						else if (line.equals(Constants.GATE_DEVICE_TAG)) {
							importMetadata.setGateDeviceAvailable(true);
						}
						else if (line.equals(Constants.PROGRAMME_POINT_TYPE_TAG)) {
							importMetadata.setProgrammePointTypeAvailable(true);
						}
						else if (line.equals(Constants.PARTICIPANT_STATE_TAG)) {
							importMetadata.setParticipantStateAvailable(true);
						}
						else if (line.equals(Constants.PARTICIPANT_TYPE_TAG)) {
							importMetadata.setParticipantTypeAvailable(true);
						}
						else if (line.equals(Constants.HOTEL_TAG)) {
							importMetadata.setHotelAvailable(true);
						}
						else if (line.equals(Constants.USER_TAG)) {
							importMetadata.setUserAvailable(true);
						}
						else if (line.equals(Constants.REPORT_TAG)) {
							importMetadata.setReportAvailable(true);
						}

						// read next line
						line = reader.readLine();
					}
				}
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
			finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					}
					catch (IOException e) {
						// ignore
					}
				}
			}
		}

		eventButton.setEnabled( importMetadata.isEventAvailable() );
		eventButton.setSelection( importMetadata.isEventAvailable() );

//		ignoreSecondaryDataButton.setEnabled( importMetadata.isEventAvailable() );

		countryButton.setEnabled( importMetadata.isCountryAvailable() );
		countryButton.setSelection( importMetadata.isCountryAvailable() );

		languageButton.setEnabled( importMetadata.isLanguageAvailable() );
		languageButton.setSelection( importMetadata.isLanguageAvailable() );

		currencyButton.setEnabled( importMetadata.isCurrencyAvailable() );
		currencyButton.setSelection( importMetadata.isCurrencyAvailable() );

		creditCardTypeButton.setEnabled( importMetadata.isCreditCardTypeAvailable() );
		creditCardTypeButton.setSelection( importMetadata.isCreditCardTypeAvailable() );

		paymentSystemSetupButton.setEnabled( importMetadata.isPaymentSystemSetupAvailable() );
		paymentSystemSetupButton.setSelection( importMetadata.isPaymentSystemSetupAvailable() );

		accountancyButton.setEnabled( importMetadata.isAccountancyAvailable() );
		accountancyButton.setSelection( importMetadata.isAccountancyAvailable() );

		gateDeviceButton.setEnabled( importMetadata.isGateDeviceAvailable() );
		gateDeviceButton.setSelection( importMetadata.isGateDeviceAvailable() );

		programmePointTypeButton.setEnabled( importMetadata.isProgrammePointTypeAvailable() );
		programmePointTypeButton.setSelection( importMetadata.isProgrammePointTypeAvailable() );

		participantStateButton.setEnabled( importMetadata.isParticipantStateAvailable() );
		participantStateButton.setSelection( importMetadata.isParticipantStateAvailable() );

		participantTypeButton.setEnabled( importMetadata.isParticipantTypeAvailable() );
		participantTypeButton.setSelection( importMetadata.isParticipantTypeAvailable() );

		hotelButton.setEnabled( importMetadata.isHotelAvailable() );
		hotelButton.setSelection( importMetadata.isHotelAvailable() );

		userButton.setEnabled( importMetadata.isUserAvailable() );
		userButton.setSelection( importMetadata.isUserAvailable() );

		reportButton.setEnabled( importMetadata.isReportAvailable() );
		reportButton.setSelection( importMetadata.isReportAvailable() );
	}


	public File getFile() {
		return selectedFile;
	}


	public ImportSettings getImportSettings() {
		return importSettings;
	}


	public ImportMetadata getImportMetadata() {
		return importMetadata;
	}

}

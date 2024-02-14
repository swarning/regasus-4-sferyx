package de.regasus.impex.db.dialog;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.lambdalogic.messeinfo.account.AccountLabel;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.widget.FileSelectionComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.impex.ImpexI18N;
import de.regasus.impex.MasterDataExportSettings;
import de.regasus.impex.ui.Activator;


public class MasterDataExportDialog extends TitleAreaDialog {

	/**
	 * The File which was chosen by the user when this dialog was used the last time (during the current application session)
	 */
	private static File previousFile = null;

	private static MasterDataExportSettings settings = new MasterDataExportSettings();

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


	private Button okButton;
	private FileSelectionComposite fileSelectionComposite;


	private File selectedFile;


	public MasterDataExportDialog(Shell parentShell) throws Exception {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE );
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(ImpexI18N.MasterDataExportDialog_Title);
		setMessage(ImpexI18N.MasterDataExportDialog_Message);

		// create dialog area that is a Composite with a single column
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		// create area as parent for the widgets of this Dialog with individual layout
		Composite area = new Composite(dialogArea, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(new GridLayout(2, true));


		try {

			SelectionListener buttonSelectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateButtonStatus();
				}
			};

			// *********************************************************************************************************
			// * One Button (check box) for each master data domain
			// *

			// left
			countryButton = new Button(area, SWT.CHECK);
			countryButton.setText(ContactLabel.countries.getString());
			countryButton.addSelectionListener(buttonSelectionListener);
			countryButton.setSelection(settings.isIncludeCountry());

			// right
			languageButton = new Button(area, SWT.CHECK);
			languageButton.setText(ContactLabel.languages.getString());
			languageButton.addSelectionListener(buttonSelectionListener);
			languageButton.setSelection(settings.isIncludeLanguage());

			// left
			currencyButton = new Button(area, SWT.CHECK);
			currencyButton.setText(InvoiceLabel.Currencies.getString());
			currencyButton.addSelectionListener(buttonSelectionListener);
			currencyButton.setSelection(settings.isIncludeCurrency());

			// right
			creditCardTypeButton = new Button(area, SWT.CHECK);
			creditCardTypeButton.setText(ContactLabel.creditCardTypes.getString());
			creditCardTypeButton.addSelectionListener(buttonSelectionListener);
			creditCardTypeButton.setSelection(settings.isIncludeCreditCardType());

			// left
			paymentSystemSetupButton = new Button(area, SWT.CHECK);
			paymentSystemSetupButton.setText(InvoiceLabel.PaymentSystemSetup.getString());
			paymentSystemSetupButton.addSelectionListener(buttonSelectionListener);
			paymentSystemSetupButton.setSelection(settings.isIncludePaymentSystemSetup());

			// right
			accountancyButton = new Button(area, SWT.CHECK);
			accountancyButton.setText(InvoiceLabel.Accountancy.getString());
			accountancyButton.addSelectionListener(buttonSelectionListener);
			accountancyButton.setSelection(settings.isIncludeAccountancy());

			// left
			gateDeviceButton = new Button(area, SWT.CHECK);
			gateDeviceButton.setText(ParticipantLabel.GateDevices.getString());
			gateDeviceButton.addSelectionListener(buttonSelectionListener);
			gateDeviceButton.setSelection(settings.isIncludeGateDevice());

			// right
			programmePointTypeButton = new Button(area, SWT.CHECK);
			programmePointTypeButton.setText(ParticipantLabel.ProgrammePointTypes.getString());
			programmePointTypeButton.addSelectionListener(buttonSelectionListener);
			programmePointTypeButton.setSelection(settings.isIncludeProgrammePointType());

			// left
			participantStateButton = new Button(area, SWT.CHECK);
			participantStateButton.setText(ParticipantLabel.ParticipantStates.getString());
			participantStateButton.addSelectionListener(buttonSelectionListener);
			participantStateButton.setSelection(settings.isIncludeParticipantState());

			// right
			participantTypeButton = new Button(area, SWT.CHECK);
			participantTypeButton.setText(ParticipantLabel.ParticipantTypes.getString());
			participantTypeButton.addSelectionListener(buttonSelectionListener);
			participantTypeButton.setSelection(settings.isIncludeParticipantType());

			// left
			hotelButton = new Button(area, SWT.CHECK);
			hotelButton.setText(HotelLabel.Hotels.getString());
			hotelButton.addSelectionListener(buttonSelectionListener);
			hotelButton.setSelection(settings.isIncludeHotel());

			// right
			userButton = new Button(area, SWT.CHECK);
			userButton.setText(AccountLabel.User.getString());
			userButton.addSelectionListener(buttonSelectionListener);
			userButton.setSelection(settings.isIncludeHotel());

			// *
			// * One Button (check box) for each master data domain
			// *********************************************************************************************************

			/*
			 * file selection
			 */
			fileSelectionComposite = new FileSelectionComposite(area, SWT.SAVE);
			fileSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			fileSelectionComposite.setFilterExtensions(new String[]{"*.zip", "*.*"});

			/* set initial File
			 * Do this after observing fileSelectionComposite! Otherwise setting the initial File won't be recognized
			 * and selectedFile remains empty.
			 */
			fileSelectionComposite.addModifyListener(new ModifyListener(){
				@Override
				public void modifyText(ModifyEvent e) {
					selectedFile = fileSelectionComposite.getFile();
					updateButtonStatus();
				}
			});

			if (previousFile != null) {
				fileSelectionComposite.setFile(previousFile);
			}
			else {
				File fileProposal = new File("regasus-master-data.zip");
				fileSelectionComposite.setFile(fileProposal);
			}


			// update OK Button after initializing the table and the file widget
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
		okButton = createButton(parent, IDialogConstants.OK_ID, ImpexI18N.ExportBtn, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		updateButtonStatus();
	}


	@Override
	protected void okPressed() {
		// save file selection
		previousFile = selectedFile;

		// copy Button selections to settings
		settings.setIncludeCountry(countryButton.getSelection());
		settings.setIncludeLanguage(languageButton.getSelection());
		settings.setIncludeCurrency(currencyButton.getSelection());
		settings.setIncludeCreditCardType(creditCardTypeButton.getSelection());
		settings.setIncludePaymentSystemSetup(paymentSystemSetupButton.getSelection());
		settings.setIncludeAccountancy(accountancyButton.getSelection());
		settings.setIncludeGateDevice(gateDeviceButton.getSelection());
		settings.setIncludeProgrammePointType(programmePointTypeButton.getSelection());
		settings.setIncludeParticipantState(participantStateButton.getSelection());
		settings.setIncludeParticipantType(participantTypeButton.getSelection());
		settings.setIncludeHotel(hotelButton.getSelection());
		settings.setIncludeUser(userButton.getSelection());

		super.okPressed();
	}


	private void updateButtonStatus() {
		if (okButton != null) {
			boolean enabled = countryButton.getSelection()
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
				|| userButton.getSelection();

			okButton.setEnabled(enabled);
		}
	}


	public MasterDataExportSettings getMasterDataSettings() {
		return settings.clone();
	}


	public File getFile() {
		return selectedFile;
	}

}

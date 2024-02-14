package de.regasus.portal.page.editor;

import java.util.List;
import java.util.Objects;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.regasus.I18N;
import de.regasus.portal.PortalI18N;
import de.regasus.portal.PortalType;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.DigitalEventComponent;
import de.regasus.portal.component.EmailComponent;
import de.regasus.portal.component.FileComponent;
import de.regasus.portal.component.OpenAmountComponent;
import de.regasus.portal.component.ParticipantFieldComponent;
import de.regasus.portal.component.PaymentComponent;
import de.regasus.portal.component.PaymentWithFeeComponent;
import de.regasus.portal.component.PrintComponent;
import de.regasus.portal.component.ProfileFieldComponent;
import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.portal.component.SendLetterOfInvitationComponent;
import de.regasus.portal.component.StreamComponent;
import de.regasus.portal.component.SummaryComponent;
import de.regasus.portal.component.TextComponent;
import de.regasus.portal.component.TotalAmountComponent;
import de.regasus.portal.component.UploadComponent;


public class CreatePageContentDialog extends TitleAreaDialog {

	private List<Class<? extends Component>> availableComponentClassList;


	private boolean isSection = false;
	private boolean isTextComponent = false;
	private boolean isProfileFieldComponent = false;
	private boolean isParticipantFieldComponent = false;
	private boolean isEmailComponent = false;
	private boolean isFileComponent = false;
	private boolean isUploadComponent = false;
	private boolean isProgrammeBookingComponent = false;
	private boolean isOpenAmountComponent = false;
	private boolean isTotalAmountComponent = false;
	private boolean isStreamComponent = false;
	private boolean isPaymentComponent = false;
	private boolean isPaymentWithFeeComponent = false;
	private boolean isDigitalEventComponent = false;
	private boolean isPrintComponent = false;
	private boolean isSendLetterOfInvitationComponent = false;
	private boolean isSummaryComponent = false;

	// widgets
	private Button okButton;
	private Button sectionButton;
	private Button textComponentButton;
	private Button profileFieldComponentButton;
	private Button participantFieldComponentButton;
	private Button emailComponentButton;
	private Button fileComponentButton;
	private Button uploadComponentButton;
	private Button programmeBookingComponentButton;
	private Button openAmountComponentButton;
	private Button totalAmountComponentButton;
	private Button streamComponentButton;
	private Button paymentComponentButton;
	private Button paymentWithFeeComponentButton;
	private Button digitalEventComponentButton;
	private Button printComponentButton;
	private Button sendLetterOfInvitationComponentButton;
	private Button summaryComponentButton;


	public CreatePageContentDialog(Shell shell, PortalType portalType) {
		super(shell);

		setShellStyle(getShellStyle() |  SWT.RESIZE);

		Objects.requireNonNull(portalType);
		availableComponentClassList = portalType.getComponentClassList();
	}


	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(I18N.CreatePageContentDialog_Title);
		setMessage(I18N.CreatePageContentDialog_Message);

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Composite centerComposite = new Composite(dialogArea, SWT.FILL);
		centerComposite.setLayoutData( new GridData(SWT.CENTER, SWT.CENTER, true, true) );
		centerComposite.setLayout( new GridLayout() );

		sectionButton = new Button(centerComposite, SWT.RADIO);
		sectionButton.setText( PortalI18N.Section.getString() );
		sectionButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));

		if (availableComponentClassList.contains(TextComponent.class)) {
    		textComponentButton = new Button(centerComposite, SWT.RADIO);
    		textComponentButton.setText( PortalI18N.TextComponent.getString() );
    		textComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(ProfileFieldComponent.class)) {
    		profileFieldComponentButton = new Button(centerComposite, SWT.RADIO);
    		// same label as participantFieldComponentButton cause they never appear at the same time
    		profileFieldComponentButton.setText( PortalI18N.FieldComponent.getString() );
    		profileFieldComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(ParticipantFieldComponent.class)) {
			participantFieldComponentButton = new Button(centerComposite, SWT.RADIO);
    		// same label as profileFieldComponentButton cause they never appear at the same time
			participantFieldComponentButton.setText( PortalI18N.FieldComponent.getString() );
			participantFieldComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}
		
		if (availableComponentClassList.contains(EmailComponent.class)) {
			emailComponentButton = new Button(centerComposite, SWT.RADIO);
			emailComponentButton.setText( PortalI18N.EmailComponent.getString());
			emailComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(FileComponent.class)) {
    		fileComponentButton = new Button(centerComposite, SWT.RADIO);
    		fileComponentButton.setText( PortalI18N.FileComponent.getString() );
    		fileComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(UploadComponent.class)) {
			uploadComponentButton = new Button(centerComposite, SWT.RADIO);
			uploadComponentButton.setText( PortalI18N.UploadComponent.getString() );
			uploadComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(ProgrammeBookingComponent.class)) {
    		programmeBookingComponentButton = new Button(centerComposite, SWT.RADIO);
    		programmeBookingComponentButton.setText( PortalI18N.ProgrammeBookingComponent.getString() );
    		programmeBookingComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(OpenAmountComponent.class)) {
			openAmountComponentButton = new Button(centerComposite, SWT.RADIO);
    		openAmountComponentButton.setText( PortalI18N.OpenAmountComponent.getString() );
    		openAmountComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(TotalAmountComponent.class)) {
			totalAmountComponentButton = new Button(centerComposite, SWT.RADIO);
			totalAmountComponentButton.setText( PortalI18N.TotalAmountComponent.getString() );
			totalAmountComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(StreamComponent.class)) {
    		streamComponentButton = new Button(centerComposite, SWT.RADIO);
    		streamComponentButton.setText( PortalI18N.StreamComponent.getString() );
    		streamComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(PaymentComponent.class)) {
    		paymentComponentButton = new Button(centerComposite, SWT.RADIO);
    		paymentComponentButton.setText( PortalI18N.PaymentComponent.getString() );
    		paymentComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(PaymentWithFeeComponent.class)) {
			paymentWithFeeComponentButton = new Button(centerComposite, SWT.RADIO);
			paymentWithFeeComponentButton.setText( PortalI18N.PaymentWithFeeComponent.getString() );
			paymentWithFeeComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(DigitalEventComponent.class)) {
    		digitalEventComponentButton = new Button(centerComposite, SWT.RADIO);
    		digitalEventComponentButton.setText( PortalI18N.DigitalEventComponent.getString() );
    		digitalEventComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(PrintComponent.class)) {
			printComponentButton = new Button(centerComposite, SWT.RADIO);
			printComponentButton.setText( PortalI18N.PrintComponent.getString() );
			printComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}
		
		if (availableComponentClassList.contains(SendLetterOfInvitationComponent.class)) {
    		sendLetterOfInvitationComponentButton = new Button(centerComposite, SWT.RADIO);
    		sendLetterOfInvitationComponentButton.setText( PortalI18N.SendLetterOfInvitationComponent.getString() );
    		sendLetterOfInvitationComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		if (availableComponentClassList.contains(SummaryComponent.class)) {
    		summaryComponentButton = new Button(centerComposite, SWT.RADIO);
    		summaryComponentButton.setText( PortalI18N.SummaryComponent.getString() );
    		summaryComponentButton.addListener(SWT.Selection, e -> okButton.setEnabled(true));
		}

		return dialogArea;
	}


	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}


	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}


//	@Override
//	protected void configureShell(Shell newShell) {
//		super.configureShell(newShell);
//		newShell.setText(EmailI18N.CitySelectionDialog_ShellText);
//	}


	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == OK) {
			isSection = sectionButton.getSelection();

			isTextComponent = 					textComponentButton != null						&& textComponentButton.getSelection();
			isProfileFieldComponent = 			profileFieldComponentButton != null				&& profileFieldComponentButton.getSelection();
			isEmailComponent = 					emailComponentButton != null					&& emailComponentButton.getSelection();
			isParticipantFieldComponent = 		participantFieldComponentButton != null			&& participantFieldComponentButton.getSelection();
			isFileComponent = 					fileComponentButton != null						&& fileComponentButton.getSelection();
			isUploadComponent = 				uploadComponentButton != null					&& uploadComponentButton.getSelection();
			isProgrammeBookingComponent =		programmeBookingComponentButton != null			&& programmeBookingComponentButton.getSelection();
			isOpenAmountComponent = 			openAmountComponentButton != null				&& openAmountComponentButton.getSelection();
			isTotalAmountComponent = 			totalAmountComponentButton != null				&& totalAmountComponentButton.getSelection();
			isStreamComponent = 				streamComponentButton != null					&& streamComponentButton.getSelection();
			isPaymentComponent = 				paymentComponentButton != null					&& paymentComponentButton.getSelection();
			isPaymentWithFeeComponent =			paymentWithFeeComponentButton != null			&& paymentWithFeeComponentButton.getSelection();
			isDigitalEventComponent = 			digitalEventComponentButton != null				&& digitalEventComponentButton.getSelection();
			isPrintComponent = 					printComponentButton != null					&& printComponentButton.getSelection();
			isSendLetterOfInvitationComponent = sendLetterOfInvitationComponentButton != null 	&& sendLetterOfInvitationComponentButton.getSelection();
			isSummaryComponent = 				summaryComponentButton != null					&& summaryComponentButton.getSelection();
		}
		super.buttonPressed(buttonId);
	}


	public boolean isSection() {
		return isSection;
	}


	public boolean isTextComponent() {
		return isTextComponent;
	}


	public boolean isProfileFieldComponent() {
		return isProfileFieldComponent;
	}


	public boolean isParticipantFieldComponent() {
		return isParticipantFieldComponent;
	}

	
	public boolean isEmailComponent() {
		return isEmailComponent;
	}


	public boolean isFileComponent() {
		return isFileComponent;
	}


	public boolean isUploadComponent() {
		return isUploadComponent;
	}


	public boolean isProgrammeBookingComponent() {
		return isProgrammeBookingComponent;
	}


	public boolean isOpenAmountComponent() {
		return isOpenAmountComponent;
	}


	public boolean isTotalAmountComponent() {
		return isTotalAmountComponent;
	}


	public boolean isStreamComponent() {
		return isStreamComponent;
	}


	public boolean isPaymentComponent() {
		return isPaymentComponent;
	}


	public boolean isPaymentWithFeeComponent() {
		return isPaymentWithFeeComponent;
	}


	public boolean isDigitalEventComponent() {
		return isDigitalEventComponent;
	}


	public boolean isPrintComponent() {
		return isPrintComponent;
	}
	

	public boolean isSendLetterOfInvitationComponent() {
		return isSendLetterOfInvitationComponent;
	}


	public boolean isSummaryComponent() {
		return isSummaryComponent;
	}


	public void setSectionEnabled(boolean enabled) {
		sectionButton.setEnabled(enabled);
	}


	public void setTextComponentEnabled(boolean enabled) {
		if (textComponentButton != null) {
			textComponentButton.setEnabled(enabled);
		}
	}


	public void setProfileFieldComponentEnabled(boolean enabled) {
		if (profileFieldComponentButton != null) {
			profileFieldComponentButton.setEnabled(enabled);
		}
	}


	public void setParticipantFieldComponentEnabled(boolean enabled) {
		if (participantFieldComponentButton != null) {
			participantFieldComponentButton.setEnabled(enabled);
		}
	}
	
	
	public void setEmailComponentEnabled(boolean enabled) {
		if (emailComponentButton != null) {
			emailComponentButton.setEnabled(enabled);
		}		
	}


	public void setFileComponentEnabled(boolean enabled) {
		if (fileComponentButton != null) {
			fileComponentButton.setEnabled(enabled);
		}
	}


	public void setUploadComponentEnabled(boolean enabled) {
		if (uploadComponentButton != null) {
			uploadComponentButton.setEnabled(enabled);
		}
	}


	public void setProgrammeBookingComponentEnabled(boolean enabled) {
		if (programmeBookingComponentButton != null) {
			programmeBookingComponentButton.setEnabled(enabled);
		}
	}


	public void setOpenAmountComponentEnabled(boolean enabled) {
		if (openAmountComponentButton != null) {
			openAmountComponentButton.setEnabled(enabled);
		}
	}


	public void setTotalAmountComponentEnabled(boolean enabled) {
		if (totalAmountComponentButton != null) {
			totalAmountComponentButton.setEnabled(enabled);
		}
	}


	public void setStreamComponentEnabled(boolean enabled) {
		if (streamComponentButton != null) {
			streamComponentButton.setEnabled(enabled);
		}
	}


	public void setPaymentComponentEnabled(boolean enabled) {
		if (paymentComponentButton != null) {
			paymentComponentButton.setEnabled(enabled);
		}
	}


	public void setPaymentWithFeeComponentEnabled(boolean enabled) {
		if (paymentWithFeeComponentButton != null) {
			paymentWithFeeComponentButton.setEnabled(enabled);
		}
	}


	public void setDigitalEventComponentEnabled(boolean enabled) {
		if (digitalEventComponentButton != null) {
			digitalEventComponentButton.setEnabled(enabled);
		}
	}


	public void setPrintComponentEnabled(boolean enabled) {
		if (printComponentButton != null) {
			printComponentButton.setEnabled(enabled);
		}
	}
	
	
	public void setSendLetterOfInvitationComponentEnabled(boolean enabled) {
		if (sendLetterOfInvitationComponentButton != null) {
			sendLetterOfInvitationComponentButton.setEnabled(enabled);
		}
	}


	public void setSummaryComponentEnabled(boolean enabled) {
		if (summaryComponentButton != null) {
			summaryComponentButton.setEnabled(enabled);
		}
	}

}

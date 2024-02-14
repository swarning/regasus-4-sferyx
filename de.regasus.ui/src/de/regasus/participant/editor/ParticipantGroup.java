package de.regasus.participant.editor;

import java.util.Date;
import java.util.Locale;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.i18n.LanguageString;
import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.EventModel;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.participant.state.combo.ParticipantStateCombo;
import de.regasus.participant.type.combo.ParticipantTypeCombo;
import de.regasus.ui.Activator;

public class ParticipantGroup extends Group {

	// *************************************************************************
	// * Entity
	// *

	private Participant participant;
	private Long originalParticipantTypePK;

	// *************************************************************************
	// * Widgets
	// *

	private Composite eventComposite;
	private Text eventText;

	private Text participantNoText;
	private ParticipantStateCombo participantStateCombo;
	private ParticipantTypeCombo participantTypeCombo;
	private DateTimeComposite registerDate;
	private DateTimeComposite certificatePrint;
	private DateTimeComposite programmeNoteTime;
	private DateTimeComposite hotelNoteTime;

	private Button proofProvidedButton;
	private Button anonymousButton;
	private Button vipButton;
	private Button wwwRegistrationButton;

	// *************************************************************************
	// * Other Attributes
	// *

	private FormatHelper formatHelper;

	private ModifySupport modifySupport = new ModifySupport(this);


	// *************************************************************************
	// * Constructor
	// *

	public ParticipantGroup(
		Composite parent,
		int style,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(parent, style);

		ParticipantConfigParameterSet participantConfigParameterSet = null;
		if (configParameterSet != null) {
			participantConfigParameterSet = configParameterSet.getEvent().getParticipant();
		}
		else {
			participantConfigParameterSet = new ParticipantConfigParameterSet();
		}

		formatHelper = new FormatHelper();

		setText(ParticipantLabel.Participant.getString());

		GridLayout layout = new GridLayout(2, false);
		setLayout(layout);

		// Event
		Label eventLabel = new Label(this, SWT.NONE);
		eventLabel.setText( Participant.EVENT.getLabel() );
		eventLabel.setToolTipText( Participant.EVENT.getDescription() );
		eventLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		eventComposite = new Composite(this, SWT.NONE);
		eventComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		eventComposite.setLayout(new FillLayout());


		// Participant Number
		Label participantNoLabel = new Label(this, SWT.NONE);
		participantNoLabel.setText( Participant.NUMBER.getString() );
		participantNoLabel.setToolTipText( Participant.NUMBER.getDescription() );
		participantNoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		participantNoText = new Text(this, SWT.BORDER);
		participantNoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		SWTHelper.disableTextWidget(participantNoText);

		participantNoText.addModifyListener(modifySupport);

		// Participant State / Teilnehmerstatus
		if ( participantConfigParameterSet.getParticipantState().isVisible() ) {
    		Label participantStateLabel = new Label(this, SWT.NONE);
    		participantStateLabel.setText( Participant.PARTICIPANT_STATE.getLabel() );
    		participantStateLabel.setToolTipText( Participant.PARTICIPANT_STATE.getDescription() );
    		participantStateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		participantStateCombo = new ParticipantStateCombo(this, SWT.BORDER);
    		participantStateCombo.setWithEmptyElement(false);

    		participantStateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    		participantStateCombo.addModifyListener(modifySupport);
		}

		// Participant Type / Teilnehmerart
		Label participantTypeLabel = new Label(this, SWT.NONE);
		participantTypeLabel.setText( Participant.PARTICIPANT_TYPE.getLabel() );
		participantTypeLabel.setToolTipText( Participant.PARTICIPANT_TYPE.getDescription() );
		participantTypeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		SWTHelper.makeBold(participantTypeLabel);

		participantTypeCombo = new ParticipantTypeCombo(this, SWT.READ_ONLY);
		participantTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		participantTypeCombo.addModifyListener(modifySupport);


		// Checkboxes
		if (participantConfigParameterSet.getProofProvided().isVisible() ||
			participantConfigParameterSet.getVIP().isVisible() ||
			participantConfigParameterSet.getAnonym().isVisible() ||
			participantConfigParameterSet.getWWWRegistration().isVisible()
		) {
    		new Label(this, SWT.NONE); 		// Dummy as placeholder

    		Composite buttonComposite = new Composite(this, SWT.NONE);
    		GridLayout gridLayout = new GridLayout(2, false);
    		gridLayout.marginWidth = 0;
    		buttonComposite.setLayout(gridLayout);
    		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


    		GridDataFactory buttonGridDataFactory = GridDataFactory.swtDefaults()
    			.align(SWT.LEFT, SWT.CENTER)
    			.grab(true, false);

    		if (participantConfigParameterSet.getProofProvided().isVisible()) {
    			proofProvidedButton = new Button(buttonComposite, SWT.CHECK | SWT.RIGHT);
    			proofProvidedButton.setText( Participant.PROOF_PROVIDED.getString() );
    			buttonGridDataFactory.applyTo(proofProvidedButton);

    			proofProvidedButton.addSelectionListener(modifySupport);
    		}

    		if (participantConfigParameterSet.getAnonym().isVisible()) {
        		anonymousButton = new Button(buttonComposite, SWT.CHECK | SWT.RIGHT);
        		anonymousButton.setText( Participant.ANONYM.getString() );
        		buttonGridDataFactory.applyTo(anonymousButton);

        		anonymousButton.addSelectionListener(modifySupport);
    		}

    		if (participantConfigParameterSet.getVIP().isVisible()) {
        		vipButton = new Button(buttonComposite, SWT.CHECK | SWT.RIGHT);
        		vipButton.setText( Participant.VIP.getString() );
        		buttonGridDataFactory.applyTo(vipButton);

        		vipButton.addSelectionListener(modifySupport);
    		}

    		if (participantConfigParameterSet.getWWWRegistration().isVisible()) {
        		wwwRegistrationButton = new Button(buttonComposite, SWT.CHECK | SWT.RIGHT);
        		wwwRegistrationButton.setText( Participant.WWW_REGISTRATION.getString() );
        		buttonGridDataFactory.applyTo(wwwRegistrationButton);

        		wwwRegistrationButton.addSelectionListener(modifySupport);
    		}
		}

		// RegisterDate / Registrierdatum
		if (participantConfigParameterSet.getRegisterDate().isVisible()) {
    		Label registerDateLabel = new Label(this, SWT.NONE);
    		registerDateLabel.setText( Participant.REGISTER_DATE.getString() );
    		registerDateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		registerDate = new DateTimeComposite(this, SWT.BORDER);
    		registerDate.setTimeVisible(false);
    		// Registration date should default to today
    		registerDate.setDate(new Date());
    		registerDate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    		registerDate.addModifyListener(modifySupport);
		}

		// CertificatePrint / Zeitpunkt des Zertifikatsdrucks
		if (participantConfigParameterSet.getCertificatePrint().isVisible()) {
    		Label certificatePrintLabel = new Label(this, SWT.NONE);
    		certificatePrintLabel.setText( Participant.CERTIFICATE_PRINT.getString() );
    		certificatePrintLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		certificatePrint = new DateTimeComposite(this, SWT.BORDER);
    		certificatePrint.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    		certificatePrint.addModifyListener(modifySupport);
		}

		// Programm Note Time
		if (participantConfigParameterSet.getProgrammeNoteTime().isVisible()) {
    		Label programmeNoteTimeLabel = new Label(this, SWT.NONE);
    		programmeNoteTimeLabel.setText( Participant.PROGRAMME_NOTE_TIME.getAbbreviation() );
    		programmeNoteTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		programmeNoteTime = new DateTimeComposite(this, SWT.BORDER);
    		programmeNoteTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    		programmeNoteTime.addModifyListener(modifySupport);
		}

		// Hotel Note Time
		if (participantConfigParameterSet.getHotelNoteTime().isVisible()) {
    		Label hotelNoteTimeLabel = new Label(this, SWT.NONE);
    		hotelNoteTimeLabel.setText( Participant.HOTEL_NOTE_TIME.getAbbreviation() );
    		hotelNoteTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

    		hotelNoteTime = new DateTimeComposite(this, SWT.BORDER);
    		hotelNoteTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    		hotelNoteTime.addModifyListener(modifySupport);
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	private void syncWidgetsToEntity() {
		if (participant != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (eventText == null) {
							// create the Text for eventText
							eventText = new Text(eventComposite, SWT.BORDER);
							SWTHelper.disableTextWidget(eventText);
							eventComposite.layout();
						}
						EventVO eventVO = EventModel.getInstance().getEventVO(participant.getEventId());
						eventText.setText(eventVO.getLabel(Locale.getDefault()));

						participantNoText.setText(formatHelper.format(participant.getNumber()));

						if (participantStateCombo != null) {
							participantStateCombo.setParticipantStateID(participant.getParticipantStatePK());
						}

						// keep original Participant Type
						originalParticipantTypePK = participant.getParticipantTypePK();

						// To initialize the ParticipantTypeCombo we have to set the eventPK, too
						participantTypeCombo.setEventID(participant.getEventId());
						participantTypeCombo.setParticipantTypePK( participant.getParticipantTypePK() );
						participantTypeCombo.addModifyListener(participantTypeListener);
						updateProofRequiredButtonStatus();

						if (registerDate != null) {
							registerDate.setDate(participant.getRegisterDate());
						}

						if (certificatePrint != null) {
							certificatePrint.setDate(participant.getCertificatePrint());
						}

						if (programmeNoteTime != null) {
							programmeNoteTime.setDate(participant.getProgrammeNoteTime());
						}

						if (hotelNoteTime != null) {
							hotelNoteTime.setDate(participant.getHotelNoteTime());
						}

						if (proofProvidedButton != null) {
							proofProvidedButton.setSelection(participant.isProofProvided());
						}

						if (anonymousButton != null) {
							anonymousButton.setSelection(participant.isAnonym());
						}

						if (vipButton != null) {
							vipButton.setSelection(participant.isVIP());
						}

						if (wwwRegistrationButton != null) {
							wwwRegistrationButton.setSelection(participant.isWwwRegistration());
						}
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (participant != null) {
			if (participantStateCombo != null) {
				participant.setParticipantStatePK(participantStateCombo.getParticipantStateID());
			}

			participant.setParticipantTypePK(participantTypeCombo.getParticipantTypePK());

			if (proofProvidedButton != null) {
				participant.setProofProvided(proofProvidedButton.getSelection());
			}

			if (anonymousButton != null) {
				participant.setAnonym(anonymousButton.getSelection());
			}

			if (vipButton != null) {
				participant.setVIP(vipButton.getSelection());
			}

			if (wwwRegistrationButton != null) {
				participant.setWwwRegistration(wwwRegistrationButton.getSelection());
			}

			if (registerDate != null) {
				participant.setRegisterDate(registerDate.getDate());
			}

			if (certificatePrint != null) {
				participant.setCertificatePrint(certificatePrint.getDate());
			}

			if (programmeNoteTime != null) {
				participant.setProgrammeNoteTime(programmeNoteTime.getDate());
			}

			if (hotelNoteTime != null) {
				participant.setHotelNoteTime(hotelNoteTime.getDate());
			}
		}
	}


	public Participant getParticipant() {
		return participant;
	}


	public void setParticipant(Participant participant) {
		this.participant = participant;
		syncWidgetsToEntity();
	}


	public boolean isComplete() {
		return
			participantStateCombo.getEntity() != null &&
			participantTypeCombo.getEntity() != null;
	}

	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

	public void setEvent(EventVO event) {
		LanguageString name = event.getName();
		if (name != null) {
			eventText.setText(name.getString());
		}
		try {
			participantTypeCombo.setEventID(event.getID());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private ModifyListener participantTypeListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			if (proofProvidedButton != null && proofProvidedButton.getSelection() ) {
				proofProvidedButton.setSelection(false);

				ParticipantType originalParticipantType = getParticipantType(originalParticipantTypePK);
				String ptName = originalParticipantType.getName().getString();
				String message = I18N.ProofProvidedSetToFalseMessage.replace("<ParticipantType>", ptName);

				MessageDialog.openInformation(
					getShell(),
					KernelLabel.Info.getString(),
					message
				);
			}

			updateProofRequiredButtonStatus();
		}
	};


	private void updateProofRequiredButtonStatus() {
		if (proofProvidedButton != null) {
			boolean proofRequired = false;

			Long participantTypePK = participantTypeCombo.getParticipantTypePK();
			if (participantTypePK != null) {
				ParticipantType participantType = getParticipantType(participantTypePK);
				proofRequired = participantType.isProofRequired();
			}

			proofProvidedButton.setVisible(proofRequired);
		}
	}


	private ParticipantType getParticipantType(Long participantTypeId) {
		try {
			ParticipantType participantType = ParticipantTypeModel.getInstance().getParticipantType(participantTypeId);
			return participantType;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}

}

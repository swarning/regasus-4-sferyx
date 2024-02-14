package de.regasus.core.ui.editor.config;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.lambdalogic.messeinfo.config.parameter.ParticipantConfigParameter;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;

public class ParticipantConfigGroup extends Group {

	// the entity
	private ParticipantConfigParameter participantConfigParameter;

	// corresponding admin Config that controls which settings are enabled
	private ParticipantConfigParameter adminConfigParameter;

	// Widgets
	private FieldConfigWidgets badgeWidgets;
	private FieldConfigWidgets leadWidgets;
	private FieldConfigWidgets correspondenceWidgets;
	private FieldConfigWidgets documentWidgets;
	private FieldConfigWidgets historyWidgets;
	private FieldConfigWidgets proofProvidedWidgets;
	private FieldConfigWidgets vipWidgets;
	private FieldConfigWidgets wwwRegistrationWidgets;
	private FieldConfigWidgets participantStateWidgets;
	private FieldConfigWidgets registerDateWidgets;
	private FieldConfigWidgets programmeNoteTimeWidgets;
	private FieldConfigWidgets hotelNoteTimeWidgets;
	private FieldConfigWidgets certificatePrintWidgets;
	private FieldConfigWidgets anonymWidgets;
	private FieldConfigWidgets secondPersonWidgets;

	/**
	 * Array with all widgets to make some methods shorter.
	 */
	private FieldConfigWidgets[] fieldConfigWidgets;


	public ParticipantConfigGroup(Composite parent, int style) {
		super(parent, style);

		setLayout( new GridLayout(FieldConfigWidgets.NUM_COLUMNS, false) );
		setText(ParticipantLabel.Participant.getString());

		// Widgets
		badgeWidgets = new FieldConfigWidgets(this, ParticipantLabel.Badge.getString());
		leadWidgets = new FieldConfigWidgets(this, ParticipantLabel.Leads.getString());
		correspondenceWidgets = new FieldConfigWidgets(this, ContactLabel.Correspondence.getString());
		documentWidgets = new FieldConfigWidgets(this, ContactLabel.Files.getString());
		historyWidgets = new FieldConfigWidgets(this, CoreI18N.Config_ParticipantHistory);

		proofProvidedWidgets = new FieldConfigWidgets(this, Participant.PROOF_PROVIDED.getString());
		vipWidgets = new FieldConfigWidgets(this, Participant.VIP.getString());
		wwwRegistrationWidgets = new FieldConfigWidgets(this, Participant.WWW_REGISTRATION.getString());
		participantStateWidgets = new FieldConfigWidgets(this, Participant.PARTICIPANT_STATE.getString());
		registerDateWidgets = new FieldConfigWidgets(this, Participant.REGISTER_DATE.getString());
		programmeNoteTimeWidgets = new FieldConfigWidgets(this, Participant.PROGRAMME_NOTE_TIME.getString());
		hotelNoteTimeWidgets = new FieldConfigWidgets(this, Participant.HOTEL_NOTE_TIME.getString());
		certificatePrintWidgets = new FieldConfigWidgets(this, Participant.CERTIFICATE_PRINT.getString());
		anonymWidgets = new FieldConfigWidgets(this, Participant.ANONYM.getString());
		secondPersonWidgets = new FieldConfigWidgets(this, Participant.PERSON_2.getString());


		fieldConfigWidgets = new FieldConfigWidgets[] {
			badgeWidgets,
			leadWidgets,
			correspondenceWidgets,
			documentWidgets,
			historyWidgets,
			proofProvidedWidgets,
			vipWidgets,
			wwwRegistrationWidgets,
			participantStateWidgets,
			registerDateWidgets,
			programmeNoteTimeWidgets,
			hotelNoteTimeWidgets,
			certificatePrintWidgets,
			anonymWidgets,
			secondPersonWidgets
		};

	}


	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		updateEnabledStatus();
	}


	public void setAdminConfigParameter(ParticipantConfigParameter adminConfigParameter) {
		this.adminConfigParameter = adminConfigParameter;

		updateEnabledStatus();
	}


	private void updateEnabledStatus() {
		/* visibility of widgets depends on
		 * - enable-state of the Group
		 * - the setting of corresponding Admin-Config
		 */
		boolean enabled = getEnabled();

		badgeWidgets.setEnabled(enabled && adminConfigParameter.getBadgeConfigParameter().isVisible());
		leadWidgets.setEnabled(enabled && adminConfigParameter.getLeadConfigParameter().isVisible());
		correspondenceWidgets.setEnabled(enabled && adminConfigParameter.getCorrespondenceConfigParameter().isVisible());
		documentWidgets.setEnabled(enabled && adminConfigParameter.getDocumentConfigParameter().isVisible());
		historyWidgets.setEnabled(enabled && adminConfigParameter.getHistoryConfigParameter().isVisible());
		proofProvidedWidgets.setEnabled(enabled && adminConfigParameter.getProofProvidedConfigParameter().isVisible());
		vipWidgets.setEnabled(enabled && adminConfigParameter.getVipConfigParameter().isVisible());
		wwwRegistrationWidgets.setEnabled(enabled && adminConfigParameter.getWwwRegistrationConfigParameter().isVisible());
		participantStateWidgets.setEnabled(enabled && adminConfigParameter.getParticipantStateConfigParameter().isVisible());
		registerDateWidgets.setEnabled(enabled && adminConfigParameter.getRegisterDateConfigParameter().isVisible());
		programmeNoteTimeWidgets.setEnabled(enabled && adminConfigParameter.getProgrammeNoteTimeConfigParameter().isVisible());
		hotelNoteTimeWidgets.setEnabled(enabled && adminConfigParameter.getHotelNoteTimeConfigParameter().isVisible());
		certificatePrintWidgets.setEnabled(enabled && adminConfigParameter.getCertificatePrintConfigParameter().isVisible());
		anonymWidgets.setEnabled(enabled && adminConfigParameter.getAnonymConfigParameter().isVisible());
		secondPersonWidgets.setEnabled(enabled && adminConfigParameter.getSecondPersonConfigParameter().isVisible());
	}


	public void addModifyListener(ModifyListener modifyListener) {
		for (FieldConfigWidgets fcw : fieldConfigWidgets) {
			fcw.addModifyListener(modifyListener);
		}
	}


	public void syncWidgetsToEntity() {
		if (participantConfigParameter != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {

				@Override
				public void run() {
					try {
						for (FieldConfigWidgets fcw : fieldConfigWidgets) {
							fcw.syncWidgetsToEntity();
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
		if (participantConfigParameter != null) {
			for (FieldConfigWidgets fcw : fieldConfigWidgets) {
				fcw.syncEntityToWidgets();
			}
		}
	}


	public void setParticipantConfigParameter(ParticipantConfigParameter participantConfigParameter) {
		this.participantConfigParameter = participantConfigParameter;

		badgeWidgets.setFieldConfigParameter(participantConfigParameter.getBadgeConfigParameter());
		leadWidgets.setFieldConfigParameter(participantConfigParameter.getLeadConfigParameter());
		correspondenceWidgets.setFieldConfigParameter(participantConfigParameter.getCorrespondenceConfigParameter());
		documentWidgets.setFieldConfigParameter(participantConfigParameter.getDocumentConfigParameter());
		historyWidgets.setFieldConfigParameter(participantConfigParameter.getHistoryConfigParameter());
		proofProvidedWidgets.setFieldConfigParameter(participantConfigParameter.getProofProvidedConfigParameter());
		vipWidgets.setFieldConfigParameter(participantConfigParameter.getVipConfigParameter());
		wwwRegistrationWidgets.setFieldConfigParameter(participantConfigParameter.getWwwRegistrationConfigParameter());
		participantStateWidgets.setFieldConfigParameter(participantConfigParameter.getParticipantStateConfigParameter());
		registerDateWidgets.setFieldConfigParameter(participantConfigParameter.getRegisterDateConfigParameter());
		programmeNoteTimeWidgets.setFieldConfigParameter(participantConfigParameter.getProgrammeNoteTimeConfigParameter());
		hotelNoteTimeWidgets.setFieldConfigParameter(participantConfigParameter.getHotelNoteTimeConfigParameter());
		certificatePrintWidgets.setFieldConfigParameter(participantConfigParameter.getCertificatePrintConfigParameter());
		anonymWidgets.setFieldConfigParameter(participantConfigParameter.getAnonymConfigParameter());
		secondPersonWidgets.setFieldConfigParameter(participantConfigParameter.getSecondPersonConfigParameter());
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

/**
 * ParticipantSectionContainer.java
 * created on 17.07.2013 11:16:20
 */
package de.regasus.participant.editor.overview;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.ParticipantConfigParameterSet;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantCorrespondence;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.event.EventModel;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantCorrespondenceModel;
import de.regasus.participant.ParticipantFileModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.ParticipantTypeModel;
import de.regasus.ui.Activator;

public class ParticipantSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;
	private Long eventPK;
	private Long participantStatePK;
	private Long participantTypePK;

	private ParticipantModel participantModel;
	private EventModel eventModel;
	private ParticipantStateModel participantStateModel;
	private ParticipantTypeModel participantTypeModel;
	private ParticipantCorrespondenceModel participantCorrespondenceModel;
	private ParticipantFileModel participantFileModel;

	private ParticipantConfigParameterSet partConfigParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public ParticipantSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		ParticipantConfigParameterSet participantConfigParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.participantID = participantID;
		this.partConfigParameterSet = participantConfigParameterSet;

		addDisposeListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this, participantID);

		Participant participant = participantModel.getParticipant(participantID);

		eventModel = EventModel.getInstance();
		// observe event here, because it cannot change
		eventPK = participant.getEventId();
		eventModel.addListener(this, eventPK);

		participantStateModel = ParticipantStateModel.getInstance();
		participantTypeModel = ParticipantTypeModel.getInstance();
		participantCorrespondenceModel = ParticipantCorrespondenceModel.getInstance();

		participantFileModel = ParticipantFileModel.getInstance();
		participantFileModel.addForeignKeyListener(this, participantID);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return ParticipantLabel.Participant.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;


    		// get data
    		Participant participant = participantModel.getParticipant(participantID);

    		// event
    		EventVO eventVO = eventModel.getEventVO(eventPK);
    		addIfNotEmpty(Participant.EVENT.getString(), eventVO.getLabel(Locale.getDefault()));

    		// participant number
    		addIfNotEmpty(UtilI18N.NumberAbreviation, participant.getNumber());

    		// participant state
    		if (partConfigParameterSet == null || partConfigParameterSet.getParticipantState().isVisible()) {
        		Long newParticipantStatePK = participant.getParticipantStatePK();
        		if (!newParticipantStatePK.equals(participantStatePK)) {
        			participantStateModel.removeListener(this, participantStatePK);
        			participantStateModel.addListener(this, newParticipantStatePK);
        			participantStatePK = newParticipantStatePK;
        		}
        		ParticipantState participantState = participantStateModel.getParticipantState(participantStatePK);
        		addIfNotEmpty(Participant.PARTICIPANT_STATE.getString(), participantState.getString());
    		}

    		// participant type
    		Long newParticipantTypePK = participant.getParticipantTypePK();
    		if (!newParticipantTypePK.equals(participantTypePK)) {
    			participantTypeModel.removeListener(this, participantTypePK);
    			participantTypeModel.addListener(this, newParticipantTypePK);
    			participantTypePK = newParticipantTypePK;
    		}
    		ParticipantType participantType = participantTypeModel.getParticipantType(participantTypePK);
    		addIfNotEmpty(Participant.PARTICIPANT_TYPE.getString(), participantType.getName().getString());

    		// vip
    		if (partConfigParameterSet == null || partConfigParameterSet.getVIP().isVisible()) {
    			addIfYes(Participant.VIP.getString(), participant.isVIP());
    		}

    		// anonym
    		if (partConfigParameterSet == null || partConfigParameterSet.getAnonym().isVisible()) {
    			addIfYes(Participant.ANONYM.getString(), participant.isAnonym());
    		}

    		// wwwRegistration
    		if (partConfigParameterSet == null || partConfigParameterSet.getWWWRegistration().isVisible()) {
    			addIfYes(Participant.WWW_REGISTRATION.getString(), participant.isWwwRegistration());
    		}

    		// registerDate
    		if (partConfigParameterSet == null || partConfigParameterSet.getRegisterDate().isVisible()) {
    			Date registerDate = participant.getRegisterDate();
    			if (registerDate != null) {
    				addIfNotEmpty(Participant.REGISTER_DATE.getString(), formatHelper.formatDate(registerDate));
    			}
    		}

    		// certificatePrint
    		if (partConfigParameterSet == null || partConfigParameterSet.getCertificatePrint().isVisible()) {
    			Date certificatePrint = participant.getCertificatePrint();
    			if (certificatePrint != null) {
    				addIfNotEmpty(Participant.CERTIFICATE_PRINT.getString(), formatHelper.formatDate(certificatePrint));
    			}
    		}

    		// hasBadge
    		if (partConfigParameterSet == null || partConfigParameterSet.getBadge().isVisible()) {
    			addIfYes(ParticipantLabel.HasBadge_Enabled.getString(), Boolean.TRUE.equals(participant.hasEnabledBadge()));
    		}


    		// correspondence
    		/* It's not necessary to observe CorrespondenceModel, because correspondence is always saved
    		 * with Participant together.
    		 */
    		if (partConfigParameterSet == null || partConfigParameterSet.getCorrespondence().isVisible()) {
        		int correspondenceCount = 0;
        		List<ParticipantCorrespondence> correspondenceList = participantCorrespondenceModel.getCorrespondenceListByParticipantId(participantID);
        		if (correspondenceList != null && !correspondenceList.isEmpty()) {
        			correspondenceCount = correspondenceList.size();
        		}
        		addIfNotEmpty(ContactLabel.Correspondence.getString(), correspondenceCount);
    		}

    		// documents
    		if (partConfigParameterSet == null || partConfigParameterSet.getDocument().isVisible()) {
    			int documentCount = 0;
    			List<FileSummary> participantFileList = participantFileModel.getParticipantFileSummaryListByParticipantId(participantID);
    			if (participantFileList != null) {
    				documentCount = participantFileList.size();
    			}
        		addIfNotEmpty(ContactLabel.Files.getString(), documentCount);
    		}
		}
		finally {
			ignoreCacheModelEvents = false;
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			if ( ! ignoreCacheModelEvents) {
				refreshSection();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent event) {
		if (participantModel != null && participantID != null) {
			try {
				participantModel.removeListener(this, participantID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (eventModel != null && eventPK != null) {
			try {
				eventModel.removeListener(this, eventPK);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (participantStateModel != null && participantStatePK != null) {
			try {
				participantStateModel.removeListener(this, participantStatePK);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (participantTypeModel != null && participantTypePK != null) {
			try {
				participantTypeModel.removeListener(this, participantTypePK);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}

		if (participantFileModel != null && participantID != null) {
			try {
				participantFileModel.removeForeignKeyListener(this, participantID);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}

}

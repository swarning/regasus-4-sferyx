package de.regasus.participant.editor.overview;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.PreferredPaymentTypeConfigParameterSet;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.PreferredPaymentType;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class PreferredPaymentTypeContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;

	private ParticipantModel participantModel;

	private PreferredPaymentTypeConfigParameterSet configParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public PreferredPaymentTypeContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		PreferredPaymentTypeConfigParameterSet configParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.participantID = participantID;
		this.configParameterSet = configParameterSet;

		addDisposeListener(this);

		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this, participantID);

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return ParticipantLabel.PreferredPaymentTypes.getString();
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
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

    		// get data
    		Participant participant = participantModel.getParticipant(participantID);
    		PreferredPaymentType preferredProgrammePaymentType = participant.getPreferredProgrammePaymentType();
    		PreferredPaymentType preferredHotelPaymentType = participant.getPreferredHotelPaymentType();

    		// set visible
    		boolean visible =
    			   (configParameterSet == null || configParameterSet.isVisible())
    			&& (preferredProgrammePaymentType != null || preferredHotelPaymentType != null);

    		setVisible(visible);

    		if (visible) {
    			// add entries
    			if (configParameterSet != null && configParameterSet.getProgrammeBooking().isVisible()) {
    				addIfNotEmpty(
    					ParticipantLabel.ProgrammeBookings.getString(),
						preferredProgrammePaymentType != null ? preferredProgrammePaymentType.getString() : null
					);
    			}

    			if (configParameterSet != null && configParameterSet.getHotelBooking().isVisible()) {
    				addIfNotEmpty(
    					ParticipantLabel.HotelBookings.getString(),
						preferredHotelPaymentType != null ? preferredHotelPaymentType.getString() : null
					);
    			}
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

}

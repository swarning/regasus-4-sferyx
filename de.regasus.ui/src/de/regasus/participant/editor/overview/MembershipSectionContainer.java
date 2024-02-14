package de.regasus.participant.editor.overview;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.lambdalogic.messeinfo.config.parameterset.MembershipConfigParameterSet;
import com.lambdalogic.messeinfo.contact.data.Membership;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.participant.ParticipantModel;
import de.regasus.ui.Activator;

public class MembershipSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long participantID;

	private ParticipantModel participantModel;

	private MembershipConfigParameterSet configParameterSet;

	private boolean ignoreCacheModelEvents = false;


	public MembershipSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long participantID,
		MembershipConfigParameterSet configParameterSet
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
		return Participant.MEMBERSHIP.getString();
	}


	@Override
	protected void createSectionElements() throws Exception {
		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

    		// get data
    		Participant participant = participantModel.getParticipant(participantID);
    		Membership membership = participant.getMembership();

    		// set visible
    		boolean visible =
    			   (configParameterSet == null || configParameterSet.isVisible())
    			&& membership != null
    			&& !membership.isEmpty();

    		setVisible(visible);

    		if (visible) {
    			// add entries
    			if (configParameterSet != null && configParameterSet.getStatus().isVisible()) {
    				addIfNotEmpty(Membership.STATUS.getLabel(), membership.getStatus());
    			}

    			if (configParameterSet != null && configParameterSet.getType().isVisible()) {
    				addIfNotEmpty(Membership.TYPE.getLabel(), membership.getType());
    			}

    			if (configParameterSet != null && configParameterSet.getBegin().isVisible()) {
    				addIfNotEmpty(Membership.BEGIN.getLabel(), membership.getBegin());
    			}

    			if (configParameterSet != null && configParameterSet.getEnd().isVisible()) {
    				addIfNotEmpty(Membership.END.getLabel(), membership.getEnd());
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

}

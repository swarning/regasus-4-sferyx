/**
 * ConnectionSectionContainer.java
 * created on 06.08.2013 15:22:18
 */
package de.regasus.profile.editor.overview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.messeinfo.profile.PersonLinkData;
import com.lambdalogic.messeinfo.profile.Profile;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.I18N;
import de.regasus.auth.AuthorizationException;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.event.AbstractSectionContainer;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantModel;
import de.regasus.participant.ParticipantStateModel;
import de.regasus.participant.editor.ParticipantEditor;
import de.regasus.participant.editor.ParticipantEditorInput;
import de.regasus.person.PersonLinkModel;
import de.regasus.profile.ProfileModel;
import de.regasus.ui.Activator;

public class ConnectionSectionContainer
extends AbstractSectionContainer
implements CacheModelListener<Long>, DisposeListener {

	private Long profileID;
	private Long personLink;
	private Collection<Long> participantIDs;
	private Collection<Long> eventPKs;

	private ConfigParameterSet configParameterSet;

	private EventModel eventModel;
	private ParticipantStateModel participantStateModel;
	private ParticipantModel participantModel;
	private ProfileModel profileModel;
	private PersonLinkModel personLinkModel;

	private boolean ignoreCacheModelEvents = false;


	public ConnectionSectionContainer(
		FormToolkit formToolkit,
		Composite body,
		Long profileID,
		ConfigParameterSet configParameterSet
	)
	throws Exception {
		super(formToolkit, body);

		this.profileID = profileID;

		this.configParameterSet = configParameterSet;

		addDisposeListener(this);

		eventModel = EventModel.getInstance();
		eventPKs = new ArrayList<>();

		participantStateModel = ParticipantStateModel.getInstance();

		participantModel = ParticipantModel.getInstance();

		profileModel = ProfileModel.getInstance();
		// Do not add as listener for ProfileModel here, but later in createSectionElements().

		personLinkModel = PersonLinkModel.getInstance();

		refreshSection();
	}


	@Override
	protected String getTitle() {
		return I18N.ParticipantsOfEvent;
	}


	@Override
	protected void createSectionElements() throws Exception {
		// remove listeners

		// remove as listener from ProfileModel to avoid calls of dataChange() while creating the section elements
		profileModel.removeListener(this, profileID);

		if (participantIDs != null && !participantIDs.isEmpty()) {
			for (Long participantID : participantIDs) {
				participantModel.removeListener(this, participantID);
			}
		}
		if (eventPKs != null && !eventPKs.isEmpty()) {
			for (Long eventPK : eventPKs) {
				eventModel.removeListener(this, eventPK);
			}
			eventPKs.clear();
		}
		if (personLink != null) {
			personLinkModel.removeListener(this, personLink);
		}

		try {
			// ignore CacheModelEvents created indirectly by getting data from Models
			ignoreCacheModelEvents = true;

    		// load data
    		Profile profile = profileModel.getProfile(profileID);

    		personLink = profile.getPersonLink();

    		PersonLinkData personLinkData = personLinkModel.getPersonLinkData(personLink);

    		try {
    			if (configParameterSet == null ||
    				(configParameterSet.getEvent().isVisible() && configParameterSet.getEvent().getParticipant().isVisible())
    			) {

    				boolean visible = personLinkData != null && !personLinkData.getParticipantIDs().isEmpty();
    				setVisible(visible);

    				if (visible) {
    					participantIDs = personLinkData.getParticipantIDs();

    					// load all participants
    					List<Participant> participants = participantModel.getParticipants(participantIDs);
    					Map<Long, Participant> participantMap = Participant.abstractEntities2Map(participants);

    					/* Create list of ComparableProfileParticipantLink.
    					 * Add EventVOs and Participants.
    					 * Order list of ComparableProfileParticipantLink.
    					 * Create entries into section.
    					 */
    					List<ComparableProfileParticipantLink> linkList = CollectionsHelper.createArrayList(participantIDs.size());
    					Set<Long> eventIDs = CollectionsHelper.createHashSet(participantIDs.size());


    					// load all events
    					for (Participant participant : participants) {
    						eventIDs.add(participant.getEventId());
    					}
    					List<EventVO> eventVOs = eventModel.getEventVOs(eventIDs);
    					Map<Long, EventVO> eventMap = new HashMap<>(MapHelper.calcCapacity(eventVOs.size()));
    					for (EventVO eventVO : eventVOs) {
    						eventMap.put(eventVO.getID(), eventVO);
    						Long eventPK = eventVO.getID();
    						eventPKs.add(eventPK);
    					}


    					// create list of ComparableProfileParticipantLink
    					for (Participant participant : participants) {
    						EventVO eventVO = eventMap.get(participant.getEventId());

    						ComparableProfileParticipantLink comparableLink = new ComparableProfileParticipantLink();
    						comparableLink.setEventID(eventVO.getID());
    						comparableLink.setEventVO(eventVO);
    						comparableLink.setParticipantID(participant.getID());
    						comparableLink.setParticipant(participant);

    						linkList.add(comparableLink);
    					}


    					// put EventVOs and Participants into ComparableProfileParticipantLinks
    					for (ComparableProfileParticipantLink comparableLink : linkList) {
    						EventVO eventVO = eventMap.get(comparableLink.getEventID());
    						comparableLink.setEventVO(eventVO);

    						Participant participant = participantMap.get(comparableLink.getParticipantID());
    						comparableLink.setParticipant(participant);
    					}


    					// sort linkList
    					Collections.sort(linkList, ComparableProfileParticipantLinkComparator.getInstance());

    					for (ComparableProfileParticipantLink comparableLink : linkList) {
    						try {
    							// Event name as link in 1st column of section
    							String text = comparableLink.getEventVO().getLabel(Locale.getDefault());
    							Hyperlink hyperlink = formToolkit.createHyperlink(sectionComposite, text, SWT.NONE);
    							hyperlink.setHref(comparableLink.getParticipantID());

    							hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
    								@Override
									public void linkActivated(HyperlinkEvent e) {
    									Long participantID = (Long) e.data;
    									openParticipantEditor(participantID);
    								}
    							});


    							// ParticipantState in 2nd column of section
    							Long participantStatePK = comparableLink.getParticipant().getParticipantStatePK();
    							ParticipantState participantState = participantStateModel.getParticipantState(participantStatePK);

    							Label rightLabel = formToolkit.createLabel(
    								sectionComposite,
    								SWTHelper.prepareLabelText("(" + participantState.getString() + ")"),
    								SWT.LEFT
    							);
    							rightLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    						}
    						catch (AuthorizationException miSecurityException) {
    							// skip event the user is not allowed to read
    							System.err.println(miSecurityException);
    							continue;
    						}
    					}
    				}

    			}
    		}
    		catch (Throwable t) {
    			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
    		}
		}
		finally {
			ignoreCacheModelEvents = false;
		}


		// add as listener to models
		profileModel.addListener(this, profileID);

		if (personLink != null) {
			personLinkModel.addListener(this, personLink);
		}

		if (participantIDs != null) {
    		for (Long participantID : participantIDs) {
    			participantModel.addListener(this, participantID);
    		}
		}

		if (eventPKs != null) {
			for (Long eventPK : eventPKs) {
				eventModel.addListener(this, eventPK);
			}
		}
	}


	protected void openParticipantEditor(Long participantID) {
		try {
			ParticipantEditorInput participantEditorInput = ParticipantEditorInput.getEditInstance(participantID);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.openEditor(participantEditorInput, ParticipantEditor.ID);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
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
		try {
			if (profileModel != null && profileID != null) {
				profileModel.removeListener(this, profileID);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			if (personLinkModel != null && personLink != null) {
				personLinkModel.removeListener(this, personLink);
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			if (participantModel != null && participantIDs != null && !participantIDs.isEmpty()) {
				for (Long participantID : participantIDs) {
					participantModel.removeListener(this, participantID);
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			if (eventModel != null && eventPKs != null && !eventPKs.isEmpty()) {
				for (Long eventPK : eventPKs) {
					eventModel.removeListener(this, eventPK);
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}

}

package de.regasus.email.template;

import static de.regasus.LookupService.getParticipantMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.ui.IMemento;

import com.lambdalogic.messeinfo.contact.data.SimplePersonSearchData;
import com.lambdalogic.messeinfo.email.EmailDispatchService;
import com.lambdalogic.messeinfo.participant.data.ParticipantCVO;

import de.regasus.email.SampleRecipientListener;
import de.regasus.email.template.search.view.EmailTemplateSearchView;
import de.regasus.profile.ProfileModel;

/**
 * A model that stores (at the client site) what different persons are to be used for the previews of the email
 * templates per event.
 * <p>
 * The {@link EmailTemplateSearchView} stores the data of this Model as {@link IMemento}s.
 *
 * @author manfred
 *
 */
public enum SampleRecipientModel {
	INSTANCE;

	private Map<Long, SimplePersonSearchData> eventPersonMap = new HashMap<>();


	public void put(Long eventPK, SimplePersonSearchData psd) {
		eventPersonMap.put(eventPK, psd);

		Iterator<SampleRecipientListener> iterator = sampleRecipentListenerList.iterator();
		while (iterator.hasNext()) {
			try {
				SampleRecipientListener listener = iterator.next();
				listener.changed(eventPK, psd);
			}
			catch (Exception e) {
				iterator.remove();
			}
		}
	}


	public Set<Entry<Long, SimplePersonSearchData>> getEntries() {
		return eventPersonMap.entrySet();
	}


	public SimplePersonSearchData getSimplePersonSearchData(Long eventID) {
		return eventPersonMap.get(eventID);
	}

	// *************************************************************************
	// * Listener Infrastructure
	// *

	private List<SampleRecipientListener> sampleRecipentListenerList = new ArrayList<>();


	public void addSampleRecipientListener(SampleRecipientListener listener) {
		sampleRecipentListenerList.add(listener);
	}


	public void removeSampleRecipientListener(SampleRecipientListener listener) {
		sampleRecipentListenerList.remove(listener);
	}

	// *************************************************************************
	// * Accessing the other models for actually providing the sample recipients
	// *


	/**
	 * Returns either a Profile or a ParticipantCVO.
	 *
	 * @param eventPK
	 * @return
	 * @throws Exception
	 */
	public Object getSampleRecipient(Long eventPK) throws Exception {
		SimplePersonSearchData simplePersonSearchData = getSimplePersonSearchData(eventPK);
		if (simplePersonSearchData == null) {
			return null;
		}

		Long profileOrParticipantId = simplePersonSearchData.getId();
		if (eventPK == null) {
			return ProfileModel.getInstance().getProfile(profileOrParticipantId);
		}
		else {
			ParticipantCVO participantCVO = getParticipantMgr().getParticipantCVO(
				profileOrParticipantId,
				EmailDispatchService.PARTICIPANT_SETTINGS
			);
			// Take the CVO, to have more information than in the JPA entity
			return participantCVO;
		}
	}


	/**
	 * Returns either the Long of a Profile or a Participant.
	 *
	 * @param eventPK
	 */
	public Long getSampleRecipientPK(Long eventPK) throws Exception {
		Long sampleRecipientPK = null;

		SimplePersonSearchData simplePersonSearchData = getSimplePersonSearchData(eventPK);
		if (simplePersonSearchData != null) {
			sampleRecipientPK = simplePersonSearchData.getId();
		}

		return sampleRecipientPK;
	}


	public void removeSampleRecipient(Long eventPK) {
		eventPersonMap.remove(eventPK);
	}

}

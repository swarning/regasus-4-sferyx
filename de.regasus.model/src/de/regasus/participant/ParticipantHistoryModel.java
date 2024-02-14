package de.regasus.participant;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldValue;
import com.lambdalogic.messeinfo.participant.data.ParticipantVO;
import com.lambdalogic.util.CollectionsHelper;
import com.lambdalogic.util.MapHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.core.model.MICacheModel;

public class ParticipantHistoryModel extends MICacheModel<Long, ParticipantHistoryContainer> {

	private static ParticipantHistoryModel singleton;


	// models
	private ParticipantModel participantModel;


	private ParticipantHistoryModel() {
	}


	public static ParticipantHistoryModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantHistoryModel();
			singleton.initModels();
		}
		return singleton;
	}


	private CacheModelListener<Long> participantModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (   event.getOperation() == CacheModelOperation.REFRESH
					|| event.getOperation() == CacheModelOperation.UPDATE
				) {
					refresh( event.getKeyList() );
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private void initModels() {
		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(participantModelListener);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(ParticipantHistoryContainer entity) {
		return entity.getParticipantPK();
	}


	@Override
	protected ParticipantHistoryContainer getEntityFromServer(Long participantID) throws Exception {
		// get historical records of participant from server
		List<ParticipantVO> participantHistoryVOs = getParticipantMgr().getParticipantHistoryVOs(participantID);

		// get historical ParticipantCustomFieldValues from server
		List<ParticipantCustomFieldValue> customFieldValueHistoryList = getParticipantCustomFieldValueMgr().getHistoryByParticipant(participantID);

		ParticipantHistoryContainer container = new ParticipantHistoryContainer(
			participantID,
			participantHistoryVOs,
			customFieldValueHistoryList
		);

		return container;
	}


	@Override
	protected List<ParticipantHistoryContainer> getEntitiesFromServer(Collection<Long> participantIDs)
	throws Exception {

		// create Map from participantID to ParticipantHistoryContainer with entries for every participantID
		Map<Long, ParticipantHistoryContainer> containerMap = MapHelper.createHashMap(participantIDs.size());
		for (Long participantID : participantIDs) {
			ParticipantHistoryContainer container = new ParticipantHistoryContainer(
				participantID,
				new ArrayList<ParticipantVO>(),
				new ArrayList<ParticipantCustomFieldValue>()
			);
			containerMap.put(participantID, container);
		}

		// get historical participant records from server
		List<ParticipantVO> participantHistoryVOs = getParticipantMgr().getParticipantHistoryVOs(participantIDs);

		for (ParticipantVO participantHistoryVO : participantHistoryVOs) {
			// get ParticipantHistoryContainer of participant
			Long participantID = participantHistoryVO.getID();
			ParticipantHistoryContainer container = containerMap.get(participantID);

			// add current historical ParticipantVO to ParticipantHistoryContainer
			container.getParticipantHistoryVOs().add(participantHistoryVO);
		}

		// create List<ParticipantHistoryContainer>
		List<ParticipantHistoryContainer> containerList = CollectionsHelper.createArrayList(containerMap.values());

		// enrich ParticipantHistoryContainers with historical ParticipantCustomFieldValues
		// TODO: avoid multiple server calls to improve performance
		for (ParticipantHistoryContainer container : containerList) {
			Long participantID = container.getParticipantPK();

			// get historical ParticipantCustomFieldValues from server
			List<ParticipantCustomFieldValue> customFieldValueHistoryList =
				getParticipantCustomFieldValueMgr().getHistoryByParticipant(participantID);

			container.setCustomFieldValueHistoryList(customFieldValueHistoryList);
		}

		return containerList;
	}


	public ParticipantHistoryContainer getParticipantHistory(Long participantID) throws Exception {
		return super.getEntity(participantID);
	}

}

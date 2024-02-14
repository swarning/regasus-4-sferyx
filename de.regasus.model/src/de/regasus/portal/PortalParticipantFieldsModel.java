package de.regasus.portal;

import static de.regasus.LookupService.getPortalParticipantFieldsMgr;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.participant.ParticipantCustomField;
import com.lambdalogic.messeinfo.participant.ParticipantCustomFieldGroup;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;
import de.regasus.portal.participant.PortalParticipantFields;

public class PortalParticipantFieldsModel extends MICacheModel<Long, PortalParticipantFields> {
	private static PortalParticipantFieldsModel singleton;

	private ParticipantCustomFieldGroupModel paCFGrpModel;

	private ParticipantCustomFieldModel paCFModel;


	public static PortalParticipantFieldsModel getInstance() {
		if (singleton == null) {
			singleton = new PortalParticipantFieldsModel();
		}
		return singleton;
	}


	private PortalParticipantFieldsModel() {
		super();

		EventModel.getInstance().addListener(eventModelListener);

		paCFGrpModel = ParticipantCustomFieldGroupModel.getInstance();
		paCFGrpModel.addListener(groupModelListener);

		paCFModel = ParticipantCustomFieldModel.getInstance();
		paCFModel.addListener(customFieldModelListener);
	}


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			CacheModelOperation op = event.getOperation();

			if (op == CacheModelOperation.REFRESH || op == CacheModelOperation.UPDATE) {
				refresh( event.getKeyList() );
			}
			else if (op == CacheModelOperation.DELETE) {
				removeEntities( event.getKeyList() );
			}
		}
	};


	private CacheModelListener<Long> groupModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			// determine PKs of affected Events
			List<Long> groupIDs = event.getKeyList();
			List<ParticipantCustomFieldGroup> groups = paCFGrpModel.getParticipantCustomFieldGroups(groupIDs);
			Set<Long> eventPKs = new HashSet<>();
			for (ParticipantCustomFieldGroup group : groups) {
				eventPKs.add( group.getEventPK() );
			}

			refresh(eventPKs);
		}
	};


	private CacheModelListener<Long> customFieldModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			// determine PKs of affected Events
			List<Long> customFieldIDs = event.getKeyList();
			List<ParticipantCustomField> customFields = paCFModel.getParticipantCustomFields(customFieldIDs);
			Set<Long> eventPKs = new HashSet<>();
			for (ParticipantCustomField customField : customFields) {
				eventPKs.add( customField.getEventPK() );
			}

			refresh(eventPKs);
		}
	};


	@Override
	protected Long getKey(PortalParticipantFields fields) {
		return fields.getEventPK();
	}


	@Override
	protected PortalParticipantFields getEntityFromServer(Long eventPK) throws Exception {
		PortalParticipantFields fields = getPortalParticipantFieldsMgr().getPortalParticipantFieldsByEvent(eventPK);
		return fields;
	}


	public PortalParticipantFields getPortalParticipantFields(Long eventPK) throws Exception {
		return super.getEntity(eventPK);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}

}

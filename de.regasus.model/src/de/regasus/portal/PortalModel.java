package de.regasus.portal;

import static de.regasus.LookupService.getPortalMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.event.EventModel;
import de.regasus.model.Activator;
import de.regasus.participant.ParticipantCustomFieldGroupModel;
import de.regasus.participant.ParticipantCustomFieldModel;


public class PortalModel extends MICacheModel<Long, Portal> {

	public static final PortalSettings DEFAULT_SETTINGS = new PortalSettings();
	static {
		DEFAULT_SETTINGS.withParticipantTypeIds = true;
		DEFAULT_SETTINGS.withEmailTemplateIds = true;
	}

	private static Long NULL_FOREIGN_KEY = 0L;

	private static PortalModel singleton;

	private EventModel eventModel;


	private CacheModelListener<Long> eventModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getSource() == eventModel && event.getOperation() == CacheModelOperation.DELETE) {

					Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

					for (Long eventPK : event.getKeyList()) {
						for (Portal portal : getLoadedAndCachedEntities()) {
							if (eventPK.equals(portal.getEventId())) {
								deletedPKs.add(portal.getId());
							}
						}

						/* Remove the foreign key whose entity has been deleted from the model before firing the
						 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
						 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
						 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
						 * shall get an empty list.
						 */
						removeForeignKeyData(eventPK);
					}

					if (!deletedPKs.isEmpty()) {
						fireDelete(deletedPKs);
						removeEntities(deletedPKs);
					}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private PortalModel() {
		eventModel = EventModel.getInstance();
		eventModel.addListener(eventModelListener);
	}


	public static PortalModel getInstance() {
		if (singleton == null) {
			singleton = new PortalModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(Portal entity) {
		return entity.getId();
	}


	@Override
	protected Portal getEntityFromServer(Long id) throws Exception {
		Portal portal = getPortalMgr().read(id, DEFAULT_SETTINGS);
		return portal;
	}


	public Portal getPortal(Long id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<Portal> getEntitiesFromServer(Collection<Long> portalPKs) throws Exception {
		List<Portal> portalList = getPortalMgr().read(portalPKs, DEFAULT_SETTINGS);
		return portalList;
	}


	public List<Portal> getPortals(List<Long> portalPKs) throws Exception {
		return super.getEntities(portalPKs);
	}


	@Override
	protected Portal createEntityOnServer(Portal portal) throws Exception {
		/* Do not validate, parameter Portal is only a container for the 5 values that are necessary to
		 * create a valid Portal according to its type.
		 */
		portal = getPortalMgr().create(
			portal.getEventId(),
			portal.getPortalType(),
			portal.getMnemonic(),
			portal.getName(),
			portal.getLanguageList()
		);

		// read Portal anew to get an instance according to DEFAULT_SETTINGS
		portal = getPortal( portal.getId() );

		return portal;
	}


	public Portal create(
		Long eventId,
		PortalType portalType,
		String mnemonic,
		String name,
		List<String> languageIds
	)
	throws Exception {
		/* Use Portal as a container for the 5 values, cause the Model architecture requires
		 * an entity of the correct type (here Portal).
		 */
		Portal portal = new Portal(
			eventId,
			portalType,
			mnemonic,
			name,
			languageIds
		);

		portal = super.create(portal);


		// refresh Participant Custom Fields if case of a Group Portal
		if (   portalType.getId().equals(PortalType.PORTAL_TYPE_STANDARD_GROUP_ID)
			|| portalType.getId().equals(PortalType.PORTAL_TYPE_REACT_GROUP_ID)
		) {
    		ParticipantCustomFieldGroupModel.getInstance().refreshForeignKey(eventId);
    		ParticipantCustomFieldModel.getInstance().refreshForeignKey(eventId);
		}


		return portal;
	}


	public Portal copy(
		Long sourcePortalId,
		Long targetEventId,
		String newPortalMnemonic,
		boolean copyPhotos,
		CopyPortalMissingParticipantTypeBehaviour missingParticipantTypeBehaviour,
		CopyPortalMissingCustomFieldBehaviour missingCustomFieldBehaviour,
		CopyPortalMissingProgrammePointBehaviour missingProgrammePointBehaviour
	)
	throws Exception {
		Portal copyPortal = getPortalMgr().copyPortal(
			sourcePortalId,
			targetEventId,
			newPortalMnemonic,
			copyPhotos,
			missingParticipantTypeBehaviour,
			missingCustomFieldBehaviour,
			missingProgrammePointBehaviour
		);

		// read Portal anew to get an instance according to DEFAULT_SETTINGS
		copyPortal = getPortal( copyPortal.getId() );

		handleCreate(copyPortal);

		return copyPortal;
	}


	@Override
	protected Portal updateEntityOnServer(Portal portal) throws Exception {
		portal.validate();
		portal = getPortalMgr().update(portal);
		return portal;
	}


	@Override
	public Portal update(Portal portal) throws Exception {
		return super.update(portal);
	}


	@Override
	protected void deleteEntityOnServer(Portal portal) throws Exception {
		if (portal != null) {
			getPortalMgr().delete(portal.getId());
		}
	}


	@Override
	public void delete(Portal portal) throws Exception {
		super.delete(portal);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Long getForeignKey(Portal portal) {
		Long fk = null;

		if (portal != null) {
			fk = portal.getEventId();
			if (fk == null) {
				fk = NULL_FOREIGN_KEY;
			}
		}

		return fk;
	}


	@Override
	protected List<Portal> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long eventPK = (Long) foreignKey;
		if (eventPK == NULL_FOREIGN_KEY) {
			eventPK = null;
		}

		// load data from server
		List<Portal> portalList = getPortalMgr().readByEvent(eventPK, DEFAULT_SETTINGS);

		return portalList;
	}


	public List<Portal> getPortalsByEvent(Long eventPK) throws Exception {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}

		return super.getEntityListByForeignKey(eventPK);
	}


	public void refreshForeignKey(Long eventPK) throws Exception {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}
		super.refreshForeignKey(eventPK);
	}


	public void refreshEntitiesOfForeignKey(Long eventPK) throws Exception {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}
		super.refreshEntitiesOfForeignKey(eventPK);
	}


	@Override
	public void addForeignKeyListener(CacheModelListener<?> listener, Object eventPK) {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}
		super.addForeignKeyListener(listener, eventPK);
	}


	@Override
	public void removeForeignKeyListener(CacheModelListener<?> listener, Object eventPK) {
		if (eventPK == null) {
			eventPK = NULL_FOREIGN_KEY;
		}
		super.removeForeignKeyListener(listener, eventPK);
	}


	public String getPortalUrl(Long portalId) throws Exception {
		// determine base URL of Portals
		String portalUrl = ServerModel.getInstance().getPortalUrl();
		if (portalUrl == null) {
			portalUrl = "";
		}

		// determine Portal mnemonic
		String mnemonic = getPortal(portalId).getMnemonic();

		StringBuilder url = new StringBuilder(256);
		url.append(portalUrl);
		url.append("/init?mnemonic=").append(mnemonic);

		return url.toString();
	}


	// TODO: [MIRCP-2788] Copy Portal
//	public Portal copyPortal(
//		Long sourcePortalID,
//		Long destEventPK
//	)
//	throws Exception {
//		Portal portal = getPortalMgr().copyPortal(sourcePortalID, destEventPK);
//
//		put(portal);
//
//		List<Long> primaryKeyList = Collections.singletonList(portal.getId());
//
//		try {
//			fireCreate(primaryKeyList);
//		}
//		catch (Exception e) {
//			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
//		}
//
//		return portal;
//	}

}

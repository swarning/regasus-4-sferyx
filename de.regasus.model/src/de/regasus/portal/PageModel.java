package de.regasus.portal;

import static de.regasus.LookupService.getPageMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.OrderPosition;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;


public class PageModel extends MICacheModel<Long, Page> implements CacheModelListener<Long> {

	public static final PageSettings DEFAULT_SETTINGS = new PageSettings();

	private static PageModel singleton;

	private PortalModel portalModel;


	private PageModel() {
		portalModel = PortalModel.getInstance();
		portalModel.addListener(this);
	}


	public static PageModel getInstance() {
		if (singleton == null) {
			singleton = new PageModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(Page entity) {
		return entity.getId();
	}


	@Override
	protected Page getEntityFromServer(Long id) throws Exception {
		Page page = getPageMgr().read(id, DEFAULT_SETTINGS);
		return page;
	}


	public Page getPage(Long id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<Page> getEntitiesFromServer(Collection<Long> pagePKs) throws Exception {
		List<Page> pageList = getPageMgr().read(pagePKs, DEFAULT_SETTINGS);
		return pageList;
	}


	public List<Page> getPages(List<Long> pagePKs) throws Exception {
		return super.getEntities(pagePKs);
	}


	@Override
	protected Page updateEntityOnServer(Page page) throws Exception {
		page.validate();
		page = getPageMgr().update(page);
		return page;
	}


	@Override
	public Page update(Page page) throws Exception {
		return super.update(page);
	}


	/**
	 * Move a {@link Page} before or after another one.
	 * The target Page must belong to the same Portal.
	 *
	 * @param movedPageId
	 * @param orderPosition
	 * @param targetPageId
	 * @return List of Pages that have changed
	 * @throws Exception
	 */
	public void move(Long movedPageId, OrderPosition orderPosition, Long targetPageId)
	throws Exception {
		List<Page> pages = getPageMgr().move(
			movedPageId,
			orderPosition,
			targetPageId
		);

		put(pages);

		fireDataChange(CacheModelOperation.UPDATE, Page.getPKs(pages));
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Long getForeignKey(Page page) {
		Long fk = null;
		if (page != null) {
			fk = page.getPortalId();
		}
		return fk;
	}


	@Override
	protected List<Page> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long portalPK = (Long) foreignKey;

		// load data from server
		List<Page> pageList = getPageMgr().readByPortal(portalPK, DEFAULT_SETTINGS);

		Collections.sort(pageList, PageComparator.getInstance());

		return pageList;
	}


	public List<Page> getPagesByPortal(Long portalPK) throws Exception {
		return getEntityListByForeignKey(portalPK);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() == portalModel && event.getOperation() == CacheModelOperation.DELETE) {

				Collection<Long> deletedPKs = new ArrayList<>(event.getKeyList().size());

				for (Long portalPK : event.getKeyList()) {
					for (Page page : getLoadedAndCachedEntities()) {
						if (portalPK.equals(page.getPortalId())) {
							deletedPKs.add(page.getId());
						}
					}

					/* Remove the foreign key whose entity has been deleted from the model before firing the
					 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
					 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
					 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
					 * shall get an empty list.
					 */
					removeForeignKeyData(portalPK);
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

}

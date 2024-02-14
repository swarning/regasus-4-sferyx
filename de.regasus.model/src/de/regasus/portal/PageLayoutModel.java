package de.regasus.portal;

import static de.regasus.LookupService.getPageLayoutMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;


public class PageLayoutModel extends MICacheModel<Long, PageLayout> implements CacheModelListener<Long> {

	public static final PageLayoutSettings DEFAULT_SETTINGS = new PageLayoutSettings();

	private static PageLayoutModel singleton;

	private PortalModel portalModel;


	private PageLayoutModel() {
		portalModel = PortalModel.getInstance();
		portalModel.addListener(this);
	}


	public static PageLayoutModel getInstance() {
		if (singleton == null) {
			singleton = new PageLayoutModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(PageLayout entity) {
		return entity.getId();
	}


	@Override
	protected PageLayout getEntityFromServer(Long id) throws Exception {
		PageLayout pageLayout = getPageLayoutMgr().read(id, DEFAULT_SETTINGS);
		return pageLayout;
	}


	public PageLayout getPageLayout(Long id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<PageLayout> getEntitiesFromServer(Collection<Long> pageLayoutPKs) throws Exception {
		List<PageLayout> pageLayoutList = getPageLayoutMgr().read(pageLayoutPKs, DEFAULT_SETTINGS);
		return pageLayoutList;
	}


	public List<PageLayout> getPageLayouts(List<Long> pageLayoutPKs) throws Exception {
		return super.getEntities(pageLayoutPKs);
	}


	@Override
	protected PageLayout createEntityOnServer(PageLayout pageLayout) throws Exception {
		pageLayout.validate();
		pageLayout = getPageLayoutMgr().create(pageLayout);
		return pageLayout;
	}


	@Override
	public PageLayout create(PageLayout pageLayout) throws Exception {
		return super.create(pageLayout);
	}


	@Override
	protected PageLayout updateEntityOnServer(PageLayout pageLayout) throws Exception {
		pageLayout.validate();
		pageLayout = getPageLayoutMgr().update(pageLayout);
		return pageLayout;
	}


	@Override
	public PageLayout update(PageLayout pageLayout) throws Exception {
		return super.update(pageLayout);
	}


	@Override
	protected void deleteEntityOnServer(PageLayout pageLayout) throws Exception {
		if (pageLayout != null) {
			getPageLayoutMgr().delete(pageLayout.getId());
		}
	}


	@Override
	public void delete(PageLayout pageLayout) throws Exception {
		super.delete(pageLayout);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Long getForeignKey(PageLayout pageLayout) {
		Long fk = null;
		if (pageLayout != null) {
			fk = pageLayout.getPortalId();
		}
		return fk;
	}


	@Override
	protected List<PageLayout> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long portalPK = (Long) foreignKey;

		// load data from server
		List<PageLayout> pageLayoutList = getPageLayoutMgr().readByPortal(portalPK, DEFAULT_SETTINGS);

		return pageLayoutList;
	}


	public List<PageLayout> getPageLayoutsByPortal(Long portalPK) throws Exception {
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
					for (PageLayout pageLayout : getLoadedAndCachedEntities()) {
						if (portalPK.equals(pageLayout.getPortalId())) {
							deletedPKs.add(pageLayout.getId());
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

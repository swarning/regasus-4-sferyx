package de.regasus.programme;

import static de.regasus.LookupService.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.lambdalogic.messeinfo.invoice.data.PriceVO;
import com.lambdalogic.messeinfo.kernel.data.AbstractVO;
import com.lambdalogic.messeinfo.participant.data.CreateProgrammeCancelationTermResult;
import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeOfferingVO;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;

public class ProgrammeCancelationTermModel  extends MICacheModel<Long, ProgrammeCancelationTermVO> {

	private static ProgrammeCancelationTermModel instance;

	private ProgrammeOfferingModel programmeOfferingModel;


	private ProgrammeCancelationTermModel() {
		super();

		programmeOfferingModel = ProgrammeOfferingModel.getInstance();
		programmeOfferingModel.addListener(programmeOfferingModelListener);
	}


	public static ProgrammeCancelationTermModel getInstance() {
		if (instance == null) {
			instance = new ProgrammeCancelationTermModel();
		}
		return instance;
	}


	@Override
	protected Long getKey(ProgrammeCancelationTermVO data) {
		return data.getID();
	}


	@Override
	protected ProgrammeCancelationTermVO getEntityFromServer(Long pk) throws Exception {
		ProgrammeCancelationTermVO vo = getProgrammeCancelationTermMgr().getProgrammeCancelationTermVO(pk);
		return vo;
	}


	public ProgrammeCancelationTermVO getProgrammeCancelationTermVO(Long pk) throws Exception {
		return super.getEntity(pk);
	}


	@Override
	protected List<ProgrammeCancelationTermVO> getEntitiesFromServer(Collection<Long> keyList) throws Exception {
		List<ProgrammeCancelationTermVO> entities = getProgrammeCancelationTermMgr().getProgrammeCancelationTermVOs(keyList);
		return entities;
	}


	public List<ProgrammeCancelationTermVO> getProgrammeCancelationTermVOs(List<Long> pkList) throws Exception {
		return super.getEntities(pkList);
	}


	@Override
	protected ProgrammeCancelationTermVO createEntityOnServer(ProgrammeCancelationTermVO vo) throws Exception {
		vo.validate();
		vo = getProgrammeCancelationTermMgr().createProgrammeCancelationTerm(vo);
		return vo;
	}


	@Override
	public ProgrammeCancelationTermVO create(ProgrammeCancelationTermVO vo) throws Exception {
		return super.create(vo);
	}


	@Override
	protected ProgrammeCancelationTermVO updateEntityOnServer(ProgrammeCancelationTermVO vo) throws Exception {
		vo.validate();
		vo = getProgrammeCancelationTermMgr().updateProgrammeCancelationTerm(vo);
		return vo;
	}


	@Override
	public ProgrammeCancelationTermVO update(ProgrammeCancelationTermVO vo) throws Exception {
		return super.update(vo);
	}


	@Override
	protected void deleteEntityOnServer(ProgrammeCancelationTermVO vo) throws Exception {
		if (vo != null) {
			Long id = vo.getID();
			getProgrammeCancelationTermMgr().deleteProgrammeCancelationTerm(id);
		}
	}


	@Override
	public void delete(ProgrammeCancelationTermVO programmeCancelationTermVO) throws Exception {
		super.delete(programmeCancelationTermVO);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(ProgrammeCancelationTermVO programmeCancelationTermVO) {
		Long fk = null;
		if (programmeCancelationTermVO != null) {
			fk= programmeCancelationTermVO.getOfferingPK();
		}
		return fk;
	}


	@Override
	protected List<ProgrammeCancelationTermVO> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long programmeOfferingPK = (Long) foreignKey;

		List<ProgrammeCancelationTermVO> voList = getProgrammeCancelationTermMgr().getProgrammeCancelationTermVOsByProgrammeOfferingPK(
			programmeOfferingPK,
			null	// referendeDate
		);

		return voList;
	}


	public List<ProgrammeCancelationTermVO> getProgrammeCancelationTermVOsByProgrammeOfferingPK(Long programmeOfferingPK)
	throws Exception {
		return getEntityListByForeignKey(programmeOfferingPK);
	}


	// *************************************************************************
    // * Block of methods used for creation of multiple cancelation terms
	// * see https://lambdalogic.atlassian.net/browse/MIRCP-31

	public CreateProgrammeCancelationTermResult createProgrammeCancelationTermsByProgrammePointPK(
		Long programmePointPK,
		Date startDate,
		Date endDate,
		BigDecimal percentValue,
		PriceVO priceVO,
		boolean forceInterval
	)
	throws Exception {
		CreateProgrammeCancelationTermResult result = getProgrammeCancelationTermMgr().createProgrammeCancelationTermsByProgrammePointPK(
			programmePointPK,
			startDate,
			endDate,
			percentValue,
			priceVO,
			forceInterval
		);

		// assure that the cache is big enough
		int formerCacheSize = assureCacheSize(result.getSuccessCount());

		List<ProgrammeCancelationTermVO> cancelationTermVOs = result.getProgrammeCancelationTermVOs();
		put(cancelationTermVOs);

		List<Long> cancelationTermPKs = AbstractVO.getPKs(cancelationTermVOs);
		fireCreate(cancelationTermPKs);

		// reset the initial cache size
		setCacheSize(formerCacheSize);

		return result;
	}


	public CreateProgrammeCancelationTermResult createProgrammeCancelationTermsByEvent(
		Long eventPK,
		Date startDate,
		Date endDate,
		BigDecimal percentValue,
		PriceVO amount,
		boolean forceInterval
	)
	throws Exception {
		CreateProgrammeCancelationTermResult result = getProgrammeCancelationTermMgr().createProgrammeCancelationTermsByEvent(
			eventPK,
			startDate,
			endDate,
			percentValue,
			amount,
			forceInterval
		);

		// assure that the cache is big enough
		int formerCacheSize = assureCacheSize(result.getSuccessCount());

		List<ProgrammeCancelationTermVO> cancelationTermVOs = result.getProgrammeCancelationTermVOs();
		put(cancelationTermVOs);

		List<Long> cancelationTermPKs = AbstractVO.getPKs(cancelationTermVOs);
		fireCreate(cancelationTermPKs);

		// reset the initial cache size
		setCacheSize(formerCacheSize);

		return result;
	}

    // * Block of methods used for creation of multiple cancelation terms
	// * see https://lambdalogic.atlassian.net/browse/MIRCP-31
	// *************************************************************************

	public ProgrammeCancelationTermVO copyProgrammeCancelationTerm(
		Long sourceProgCancelTermPK,
		Long destProgOfferingPK
	)
	throws ErrorMessageException {
		ProgrammeCancelationTermVO pctVO = getProgrammeCancelationTermMgr().copyProgrammeCancelationTerm(
			sourceProgCancelTermPK,
			destProgOfferingPK,
			null	// dayShift
		);

		put(pctVO);

		List<Long> primaryKeyList = Collections.singletonList(pctVO.getID());
		try {
			fireCreate(primaryKeyList);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return pctVO;
	}


	private CacheModelListener<Long> programmeOfferingModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
    			if (   event.getOperation() == CacheModelOperation.UPDATE
    				|| event.getOperation() == CacheModelOperation.REFRESH
    			) {
					Collection<Long> affectedPKs = new ArrayList<>();

    				for (Object poPK : event.getKeyList()) {
    					// load updated Programme Offering
    					ProgrammeOfferingVO poVO = programmeOfferingModel.getProgrammeOfferingVO((Long) poPK);

						/* Only the cancellation of a Programme Point affects its Work Groups.
						 * Therefore we ignore Programme Offerings that are not cancelled.
						 */
						if ( poVO.isCancelled() ) {
	    					for (ProgrammeCancelationTermVO pctVO : getLoadedAndCachedEntities()) {
	    						if (poPK.equals(pctVO.getOfferingPK())) {
    								affectedPKs.add( pctVO.getID() );
    							}
    						}
						}

	        			if (event.getOperation() == CacheModelOperation.UPDATE) {
        					// find Programme Cancelation Terms that belong to the updated Programme Offering
        					for (ProgrammeCancelationTermVO pctVO : getLoadedAndCachedEntities()) {
        						if (poPK.equals(pctVO.getOfferingPK())) {
        							/* Update the values of currency and brutto silently without
        							 * refreshing the entities and without firing an update event.
        							 *
        							 * This reflects the fact, that both fields are not persisted in
        							 * the table PROGRAMME_CANCELATION_TERM but taken from
        							 * PROGRAMME_OFFERING.
        							 *
        							 * Though the data of these Programme Cancelation Terms have changed,
        							 * no event is fired, because there might be editors with unsaved
        							 * data. If those editors receive a refresh or update event,
        							 * the changes would get lost. So we had to check if there are editors
        							 * with unsaved data and ask (annoy) the user to save them before
        							 * updating a Programme Offering.
        							 * The consequence that arises from this is that GUI components that
        							 * show the values of currency and brutto are responsible on their
        							 * own to keep them up to date. So they should observe
        							 * ProgrammeOfferingModel.
        							 */
        							pctVO.setCurrency(poVO.getCurrency());
        							pctVO.setBrutto(poVO.isGross());
        						}
        					}
    					}
    				}

					if (!affectedPKs.isEmpty()) {
        				refresh(affectedPKs);
					}
				}
				else if (event.getOperation() == CacheModelOperation.DELETE) {
    				// determine deleted entities
    				Collection<Long> affectedPKs = new ArrayList<>();
    				for (Object programmeOfferingPK : event.getKeyList()) {
    					for (ProgrammeCancelationTermVO pctVO : getLoadedAndCachedEntities()) {
    						if (programmeOfferingPK.equals(pctVO.getOfferingPK())) {
    							affectedPKs.add( pctVO.getPK() );
    						}
    					}
    				}

    				// inform listeners about deleted entities
    				if (!affectedPKs.isEmpty()) {
    					handleDeleteByKeyList(affectedPKs, true);
    				}
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};

}

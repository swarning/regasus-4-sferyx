package de.regasus.programme;

import static de.regasus.LookupService.*;
import static com.lambdalogic.util.CollectionsHelper.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.lambdalogic.messeinfo.kernel.ServerMessage;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingResult;
import com.lambdalogic.messeinfo.participant.data.ProgrammeBookingVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.CoModelEvent;
import com.lambdalogic.util.model.CoModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;
import de.regasus.model.Activator;
import de.regasus.participant.ParticipantModel;


public class WaitListModel 
extends MICacheModel<Long, ProgrammeBookingCVO> 
implements CacheModelListener<Long> {
	
	private static WaitListModel singleton;
	
	// models
	private ParticipantModel participantModel;
	
	
	
	private WaitListModel() {
	}
	
	
	public static WaitListModel getInstance() {
		if (singleton == null) {
			singleton = new WaitListModel();
			singleton.initModels();
		}
		return singleton;
	}


	private void initModels() {
		ProgrammeBookingModel.getInstance().addCoModelListener(this);
		
		participantModel = ParticipantModel.getInstance();
		participantModel.addListener(this);
	}


	@Override
	protected Long getKey(ProgrammeBookingCVO programmeBookingCVO) {
		return programmeBookingCVO.getPK();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}
	
	
	@Override
	protected Long getForeignKey(ProgrammeBookingCVO programmeBookingCVO) {
		/* Return the ProgrammeBookingCVOs programmePointPK as foreign key even if the 
		 * ProgrammeBookingCVO is no wait list booking (anymore). Considering ProgrammeBookingCVO
		 * that are no wait list booking as not belonging to the foreign key leads to problems.
		 * E.g. events won't be fired for them, because they won't be found.
		 */
		Long foreignKey = null;
		if (programmeBookingCVO != null) {
			foreignKey = programmeBookingCVO.getVO().getProgrammePointPK();
		}
		
		return foreignKey;
	}

	
	@Override
	protected ProgrammeBookingCVO getEntityFromServer(Long programmeBookingPK) throws Exception {
		ProgrammeBookingCVO programmeBookingCVO = getProgrammeBookingMgr().getProgrammeBookingCVO(
			programmeBookingPK, 
			ProgrammeBookingModel.PB_CVO_SETTINGS
		);
		return programmeBookingCVO;
	}
	
	
	@Override
	protected List<ProgrammeBookingCVO> getEntitiesFromServer(Collection<Long> programmeBookingPKs)
	throws Exception {
		List<ProgrammeBookingCVO> programmeBookingCVOs = getProgrammeBookingMgr().getProgrammeBookingCVOs(
			programmeBookingPKs, 
			ProgrammeBookingModel.PB_CVO_SETTINGS
		);
		return programmeBookingCVOs;
	}

	
	@Override
	protected List<ProgrammeBookingCVO> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long programmePointPK = (Long) foreignKey;
		List<ProgrammeBookingCVO> programmeBookingCVOs = getProgrammeBookingMgr().getWaitListProgrammeBookingCVOsByProgrammePointPK(
			programmePointPK, 
			ProgrammeBookingModel.PB_CVO_SETTINGS
		);
		return programmeBookingCVOs;
	}


	public WaitList getWaitList(Long programmePointPK) throws Exception {
		List<ProgrammeBookingCVO> programmeBookingCVOs = getEntityListByForeignKey(programmePointPK);
		
		WaitList waitList = new WaitList(programmePointPK);
		waitList.setProgrammeBookingCVOs(programmeBookingCVOs);
		return waitList;
	}
	
	
	/**
	 * Create ProgrammeBookings on basis of wait list bookings.
	 * For each wait list booking a new programme booking is created and the wait list booking is 
	 * deleted.
	 * 
	 * @param programmeBookingPKs the PKs of the programme bookings to book
	 * @return
	 * @throws Exception if for any wait list booking no programme booking can be created 
	 */
	public List<ServerMessage> bookWaitList(List<Long> programmeBookingPKs)
	throws Exception {
		List<ServerMessage> serverMessages = null;
		
		if (notEmpty(programmeBookingPKs)) {
			// create new programme bookings, delete wait list bookings
			ProgrammeBookingResult programmeBookingResult = getProgrammeBookingMgr().bookWaitList(programmeBookingPKs);
			serverMessages = programmeBookingResult.getServerMessages();
			
			/* At this point new non-waitlist-ProgrammeBookings have been created for each 
			 * Long in programmeBookingPKs.
			 * The PKs of the new ProgrammeBookings are stored in newProgrammeBookingPKs .
			 */
			
			// reload ProgrammeBookings
			List<ProgrammeBookingCVO> newProgrammeBookingCVOs = getEntitiesFromServer(programmeBookingPKs);
			
			// put new ProgrammeBookings into model cache
			put(newProgrammeBookingCVOs);

			// detect programmePointPKs
			Set<Long> programmePointPKs = new HashSet<Long>();
			for (ProgrammeBookingCVO newProgrammeBookingCVO : newProgrammeBookingCVOs) {
				Long programmePointPK = newProgrammeBookingCVO.getVO().getProgrammePointPK();
				programmePointPKs.add(programmePointPK);
			}
			
			
			/*************************************************************************************** 
			 * The following code is very fragile, so think twice, if you change it!
			 */
			
			/* Fire CacheModelEvents and CoModelEvents for ProgrammeBookings.
			 * This will inform the ProgrammeBookingModel (which is a CoModelListener).
			 * At this point the entities have to be part of the WaitListModel, though they are no
			 * wait list bookings anymore. Otherwise they won't be found and no CoModelEvents
			 * will be fired.
			 */
			fireUpdate(programmeBookingResult.getProgrammeBookingPKs());
			
			/* Remove the Programme Bookings that are no wait list booking anymore manually from the 
			 * WaitListModel. This won't fire any event. Otherwise CoModelEvents will be fired 
			 * during the next step when refreshEntitiesOfForeignKey(...) is called, because they
			 * would not be returned by the server and therefore handled as deleted entities.
			 * Finally, when the ProgrammeBookingModel gets a CoModelEvent with CoModelOperation.DELETE,
			 * the entity would be deleted from ProgrammeBookingModel, too.
			 */
			removeEntities(programmeBookingPKs);
			
			// refresh data of effected programmePointPKs
			for (Long programmePointPK : programmePointPKs) {
				/* Refresh the data of all entities that belong to the foreign key.
				 * This won't fire a CacheModelEvent if there is no entity left, because no entity
				 * has changed (from the models's point of view).
				 * Remember: We just removed entities manually! 
				 */
				refreshEntitiesOfForeignKey(programmePointPK);
				
				/* Fire an explicite refresh event to assure that an event is fired even if there
				 * are no more entities for this foreign key.
				 */
				fireRefreshForForeignKey(programmePointPK);
			}
			
			/**************************************************************************************/
		}
		
		return serverMessages;
	}
	
	
	public void updateWaitList(Long programmePointPK, List<Long> programmeBookingPKs) {
		try {
			getProgrammeBookingMgr().updateWaitList(programmePointPK, programmeBookingPKs);
			
			handleUpdate(programmeBookingPKs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dataChange(CoModelEvent<ProgrammeBookingCVO> event) throws Exception {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		/* Collect the programmePointPKs of the deleted PBs.
		 */
		Set<Long> programmePointPKs = null;
		if (event.getOperation() == CoModelOperation.DELETE) {
			Collection<Long> keys = (Collection<Long>) event.getKeys();
			
			// get entities (ProgrammeBookingCVOs) if loaded and collect their foreignKeys (programmePointPKs)
			List<ProgrammeBookingCVO> programmeBookingCVOs = getEntitiesIfAvailable(keys);
			
			programmePointPKs = new HashSet<Long>();
			for (ProgrammeBookingCVO programmeBookingCVO : programmeBookingCVOs) {
				Long programmePointPK = programmeBookingCVO.getVO().getProgrammePointPK();
				programmePointPKs.add(programmePointPK);
			}
		}

		
		super.dataChange(event);

		
		/* Refresh data of ProgrammePoints where a PB has been deleted.
		 */
		if (programmePointPKs != null) {
			for (Long programmePointPK : programmePointPKs) {
				refreshEntitiesOfForeignKey(programmePointPK);
			}
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}
		
		try {
			if (event.getSource() == participantModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					List<Long> participantPKs = event.getKeyList();
					
					Set<Long> programmePointPKs = new HashSet<Long>();
					
					Collection<ProgrammeBookingCVO> loadedProgrammeBookingCVOs = getLoadedEntities();
					for (ProgrammeBookingCVO programmeBookingCVO : loadedProgrammeBookingCVOs) {
						ProgrammeBookingVO programmeBookingVO = programmeBookingCVO.getVO();
						Long invoiceRecipientPK = programmeBookingVO.getInvoiceRecipientPK();
						Long benefitRecipientPK = programmeBookingVO.getBenefitRecipientPK();
						
						if (participantPKs.contains(invoiceRecipientPK) ||
							participantPKs.contains(benefitRecipientPK)
						) {
							// remove deleted PB from model
							removeEntity(programmeBookingCVO.getPK());

							// collect programmePointPK
							programmePointPKs.add(programmeBookingVO.getProgrammePointPK());
						}
					}

					
					Collection<ProgrammeBookingCVO> cachedProgrammeBookingCVOs = getCachedEntities();
					for (ProgrammeBookingCVO programmeBookingCVO : cachedProgrammeBookingCVOs) {
						ProgrammeBookingVO programmeBookingVO = programmeBookingCVO.getVO();
						Long invoiceRecipientPK = programmeBookingVO.getInvoiceRecipientPK();
						Long benefitRecipientPK = programmeBookingVO.getBenefitRecipientPK();
						
						if (participantPKs.contains(invoiceRecipientPK) ||
							participantPKs.contains(benefitRecipientPK)
						) {
							// remove deleted PB from model
							removeEntity(programmeBookingCVO.getPK());
						}
					}

					
					/* Refresh all ProgrammeBookings of affected WaitLists,  
					 * because their waitListPositions have changed.
					 */
					for (Long programmePointPK : programmePointPKs) {
						refreshForeignKey(programmePointPK);
					}

				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// **************************************************************************
	// * Co-Model Listener
	// *

	private List<ProgrammeBookingCVO> filterWaitList(Collection<ProgrammeBookingCVO> programmeBookingCVOs) {
		List<ProgrammeBookingCVO> waitListProgrammeBookingCVOs = createArrayList(programmeBookingCVOs.size());
		
		for (Iterator it = programmeBookingCVOs.iterator(); it.hasNext();) {
			ProgrammeBookingCVO programmeBookingCVO = (ProgrammeBookingCVO) it.next();
			if (programmeBookingCVO.isWaitList()) {
				waitListProgrammeBookingCVOs.add(programmeBookingCVO);
			}
		}
		
		return waitListProgrammeBookingCVOs;
	}
	
	
	@Override
	protected void handleCoModelRefresh(Collection<ProgrammeBookingCVO> entityCol) throws Exception {
		List<ProgrammeBookingCVO> waitListProgrammeBookingCVOs = filterWaitList(entityCol);
		super.handleCoModelRefresh(waitListProgrammeBookingCVOs);
	}


	@Override
	protected void handleCoModelCreate(Collection<ProgrammeBookingCVO> entityCol) throws Exception {
		List<ProgrammeBookingCVO> waitListProgrammeBookingCVOs = filterWaitList(entityCol);
		super.handleCoModelCreate(waitListProgrammeBookingCVOs);
	}


	@Override
	protected void handleCoModelUpdate(Collection<ProgrammeBookingCVO> entityCol) throws Exception {
		List<ProgrammeBookingCVO> waitListProgrammeBookingCVOs = filterWaitList(entityCol);
		super.handleCoModelUpdate(waitListProgrammeBookingCVOs);
	}


//	@Override
//	protected void handleCoModelDelete(Collection<Long> keyCol) throws Exception {
//		super.handleCoModelDelete(keyCol);
//	}

	// *
	// * Co-Model Listener
	// **************************************************************************

}

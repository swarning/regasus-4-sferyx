package de.regasus.report.model;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.report.data.UserReportDirVO;

import de.regasus.core.model.MICacheModel;

/**
 * A model containing a list of all available UserReportDirs.
 * 
 * @author Steffen Kluepfel
 *
 */
public final class UserReportDirListModel
extends MICacheModel<Long, UserReportDirVO> {
	
	private static UserReportDirListModel singleton = null;
	
	private UserReportDirVO root = new UserReportDirVO();
	
	
	private UserReportDirListModel() {
		super();
	}
	
	
	public static UserReportDirListModel getInstance() {
		if (singleton == null) {
			singleton = new UserReportDirListModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}

	
	@Override
	protected Long getKey(UserReportDirVO userReportDirVO) {
		return userReportDirVO.getPK();
	}

	@Override
	protected UserReportDirVO getEntityFromServer(Long userReportDirPK) throws Exception {
		throw new Exception("Not implemented.");
	}

	@Override
	protected List<UserReportDirVO> getEntitiesFromServer(Collection<Long> userReportDirPKs) throws Exception {
		throw new Exception("Not implemented.");
	}


	protected List<UserReportDirVO> getAllEntitiesFromServer() throws Exception {
		System.out.print(getClass().getName() + ".getAllEntitiesFromServer() --> ");
		List<UserReportDirVO> baseReportCVOs = null;
		
		if (serverModel.isLoggedIn()) {
			baseReportCVOs = getReportMgr().getUserReportDirVOs();
		}
		else {
			baseReportCVOs = Collections.emptyList();
		}
		System.out.println(baseReportCVOs.size());
		return baseReportCVOs;
	}

	
	public Collection<UserReportDirVO> getAllUserReportDirVOs() throws Exception {
		Collection<UserReportDirVO> allUserReportDirVOs = getAllEntities();
		return allUserReportDirVOs;
	}

	
	public UserReportDirVO getUserReportDirVO(Long userReportDirPK) throws Exception {
		return super.getEntity(userReportDirPK);
	}

	
	public UserReportDirVO getRoot() {
		return root;
	}
	
	
	public UserReportDirVO getVisibleRoot() throws Exception {
		UserReportDirVO result = null;
		
		for (UserReportDirVO userReportDirVO : getAllUserReportDirVOs()) {
			if (userReportDirVO.getParentID() == null) {
				result = userReportDirVO;
				break;
			}
		}
		
		return result;
	}

	
	@Override
	protected UserReportDirVO createEntityOnServer(UserReportDirVO userReportDirVO) throws Exception {
		userReportDirVO.validate();
		userReportDirVO = getReportMgr().createUserReportDir(userReportDirVO);
		return userReportDirVO;
	}

	
	public UserReportDirVO create(UserReportDirVO userReportDirVO)
	throws Exception {
		return super.create(userReportDirVO);
	}
	
	
	@Override
	protected UserReportDirVO updateEntityOnServer(UserReportDirVO userReportDirVO) throws Exception {
		userReportDirVO.validate();
		userReportDirVO = getReportMgr().updateUserReportDir(userReportDirVO);
		return userReportDirVO;
	}

	
	public UserReportDirVO update(UserReportDirVO userReportDirVO) throws Exception {
		return super.update(userReportDirVO);
	}

	
	@Override
	protected void deleteEntityOnServer(UserReportDirVO userReportDirVO) throws Exception {
		Long pk = userReportDirVO.getID();
		getReportMgr().deleteUserReportDir(pk, true /*recursive*/);
	}
	

	public void delete(UserReportDirVO userReportDirVO)
	throws Exception {
		/* This method has to be re-implemented because of the recursive deletion. 
		 * All steps of CacheModel.delete() are done, but not saving foreign key information, 
		 * because there are no foreign keys in this model.
		 * In addition all sub-directories of the one that is deleted are determined, because they
		 * are deleted indirectly.
		 * After deleting the userReportDirVO on the server, handleDelete() is called not only
		 * for the deleted userReportDirVO but for all of its sub-directories, too.
		 */
		if (userReportDirVO != null) {
    		Long userReportDirPK = userReportDirVO.getID();
    		
    		if (userReportDirPK == null) {
    			throw new Exception("The key of entities to delete must not be null.");
    		}


			// determine sub-directories
			List<Long> deletedUserReportDirPKs = new ArrayList<Long>();
			deletedUserReportDirPKs.add(userReportDirVO.getID());
			
			List<UserReportDirVO> deletedUserReportDirVOs = new ArrayList<UserReportDirVO>();
	    	deletedUserReportDirVOs.add(userReportDirVO);
	    	
	    	// search in model data 
	    	Collection<UserReportDirVO> loadedData = getLoadedAndCachedEntities();
	    	
	    	int i = 0;
	    	while (i < deletedUserReportDirVOs.size()) {
	    		UserReportDirVO currentUserReportDirVO = deletedUserReportDirVOs.get(i);
	    		Long currentUserReportDirPK = currentUserReportDirVO.getID();
	    		
	    		// get all subDirectories
	    		for (UserReportDirVO subUserReportDirVO : loadedData) {
	    			if (currentUserReportDirPK.equals(subUserReportDirVO.getParentID())) {
	    				deletedUserReportDirPKs.add(subUserReportDirVO.getID());
						deletedUserReportDirVOs.add(subUserReportDirVO);
					}
				}
	    		i++;
	    	}

    		
    		
			deleteEntityOnServer(userReportDirVO);
			
			
			
			handleDelete(
				deletedUserReportDirVOs, 
				null,	// oldKey2foreignKeysMap
				true	// fireCoModelEvent
			);
		}
	}

}

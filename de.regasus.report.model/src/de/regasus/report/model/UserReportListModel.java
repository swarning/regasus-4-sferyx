package de.regasus.report.model;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.report.data.UserReportVO;
import com.lambdalogic.report.DocumentContainer;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.MICacheModel;

/**
 * Model, dass eine Liste aller verfügbaren UserReports bereithält.
 *
 * @author sacha
 *
 */
public final class UserReportListModel
extends MICacheModel<Long, UserReportVO>
implements CacheModelListener<Long> {

	private static UserReportListModel singleton = null;


	private UserReportListModel() {
		super();
	}


	private void init() {
		UserReportDirListModel.getInstance().addListener(this);
	}


	public static UserReportListModel getInstance() {
		if (singleton == null) {
			singleton = new UserReportListModel();
			singleton.init();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(UserReportVO entity) {
		return entity.getID();
	}

	@Override
	protected UserReportVO getEntityFromServer(Long userReportPK) throws Exception {
		System.out.println(getClass().getName() + ".getEntityFromServer()");

		UserReportVO userReportVO = getReportMgr().getUserReportVO(userReportPK);
		return userReportVO;
	}

	@Override
	protected List<UserReportVO> getEntitiesFromServer(Collection<Long> userReportPKs) throws Exception {
		System.out.print(getClass().getName() + ".getEntitiesFromServer() --> ");

		List<UserReportVO> userReportVOs = getReportMgr().getUserReportVOs(userReportPKs);

		System.out.println(userReportVOs.size());

		return userReportVOs;
	}


	@Override
	protected List<UserReportVO> getAllEntitiesFromServer() throws Exception {
		System.out.print(getClass().getName() + ".getAllEntitiesFromServer() --> ");

		List<UserReportVO> userReportVOs = null;

		if (serverModel.isLoggedIn()) {
			userReportVOs = getReportMgr().getUserReportVOs();
		}
		else {
			userReportVOs = Collections.emptyList();
		}

		System.out.println(userReportVOs.size());

		return userReportVOs;
	}


	public Collection<UserReportVO> getAllUserReportVOs() throws Exception {
		Collection<UserReportVO> allUserReportVOs = getAllEntities();
		return allUserReportVOs;
	}


	public UserReportVO create(UserReportVO userReportVO, DocumentContainer template)
	throws Exception {
		userReportVO.validate();
		userReportVO = getReportMgr().createUserReport(userReportVO);
		Long userReportID = userReportVO.getID();

		// Create Report Template
		if (template != null) {
			byte[] content = template.getContent();
			if (content != null && content.length == 0) {
				content = null;
			}

			if (content != null) {
				getReportMgr().setUserReportTemplate(
					userReportID,
					content,
					userReportVO.getExtFilePath());

				// Daten neu laden, weil sich z.B. editTime und extFilePath geändert haben
				userReportVO = getReportMgr().getUserReportVO(userReportID);
			}
		}

		put(userReportVO);

		fireCreate(userReportID);

		return userReportVO;
	}


	@Override
	protected UserReportVO updateEntityOnServer(UserReportVO userReportVO) throws Exception {
		userReportVO.validate();
		UserReportVO newUserReportVO = getReportMgr().updateUserReport(userReportVO);
		return newUserReportVO;
	}


	@Override
	public UserReportVO update(UserReportVO userReportVO) throws Exception {
		return super.update(userReportVO);
	}

	@Override
	protected void deleteEntityOnServer(UserReportVO userReportVO) throws Exception {
		if (userReportVO != null) {
			Long userReportPK = userReportVO.getID();
			getReportMgr().deleteUserReport(userReportPK);
		}
	}

	@Override
	public void delete(UserReportVO userReportVO) throws Exception {
		super.delete(userReportVO);
	}


	public UserReportVO getUserReportVO(Long userReportPK) throws Exception {
		return super.getEntity(userReportPK);
	}


	public List<UserReportVO> getUserReportVOs(List<Long> userReportPKs) throws Exception {
		return super.getEntities(userReportPKs);
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		if (!serverModel.isLoggedIn()) {
			return;
		}

		try {
			if (event.getSource() instanceof UserReportDirListModel) {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					List<Long> deletedUserReportPKs = new ArrayList<>();

					List<Long> deletedUserReportDirPKs = event.getKeyList();

					// alle UserReports dieses Vezeichnisses entfernen
					for (UserReportVO userReportVO : getLoadedAndCachedEntities()) {
						if (deletedUserReportDirPKs.contains(userReportVO.getUserReportDirID())) {
							// add to list for fireDataChange
							deletedUserReportPKs.add(userReportVO.getID());
						}
					}

					if (!deletedUserReportPKs.isEmpty()) {
						fireDelete(deletedUserReportPKs);

						// remove UserReport from modelData
						removeEntities(deletedUserReportPKs);
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/**
	 * Update for the UserReport data shown in the editor (everything but tree position).
	 *
	 * template == null means that a special template won't be touched.
	 *
	 * template != null && template.getContent() != null means that a special template will be updated.
	 *
	 * template != null && template.getContent() == null means that a special template will be deleted.
	 *
	 * @param userReportVO
	 * @param templateFileData
	 * @return
	 * @throws Exception
	 */
	public UserReportVO updateEditorData(UserReportVO userReportVO, DocumentContainer template)
	throws Exception {
		final Long userReportID = userReportVO.getID();

		// Änderung am Directory unterbinden
		final UserReportVO currentUserReportVO = getUserReportVO(userReportID);
		userReportVO.setUserReportDirID(currentUserReportVO.getUserReportDirID());

		userReportVO.validate();
		userReportVO = getReportMgr().updateUserReport(userReportVO);

		/*
		 * Create/Update/Delete the special User Report Template.
		 */
		if (template != null) {
			byte[] templateContent = template.getContent();
			if (templateContent != null && templateContent.length == 0) {
				templateContent = null;
			}

			getReportMgr().setUserReportTemplate(
				userReportID,
				templateContent,
				userReportVO.getExtFilePath()
			);
		}

		// Daten neu laden, weil sich z.B. editTime und extFilePath geändert haben

		userReportVO = getReportMgr().getUserReportVO(userReportID);

		put(userReportVO);

		fireUpdate(userReportID);

		return userReportVO;
	}


	/**
	 * Update operation for the tree position.
	 *
	 * @param userReportPK
	 * @param userReportDirPK
	 * @return
	 * @throws Exception
	 */
	public UserReportVO moveUserReportToDir(Long userReportPK, Long userReportDirPK)
	throws Exception {
		UserReportVO updatedUserReportVO = null;

		// The VO which belongs to the dragged Id
		UserReportVO userReportVO = getUserReportVO(userReportPK);

		if (userReportDirPK.equals(userReportVO.getUserReportDirID())) {
			// wenn sich das Zielverzeichnis nicht ändert abbrechen
			updatedUserReportVO = userReportVO;
		}
		else {
			userReportVO.setUserReportDirID(userReportDirPK);

			updatedUserReportVO = getReportMgr().updateUserReport(userReportVO);

			put(updatedUserReportVO);

			fireUpdate(userReportPK);
		}
		return updatedUserReportVO;
	}


	/**
	 * This method is called when a Report is dragged with Strg+Key, so that the server makes a copy (in order to not
	 * communicate the template back and forth), returns the copied Report, which is put in the modelData, if present.
	 *
	 * @param sourceUserReportPK
	 *            The Long of the UserReport to be copied.
	 * @param targetUserReportDirPK
	 *            The Long of the Dir in which the UserReport is to be copied. May be <code>null</code>, in which case the
	 *            copy is created in the Dir of the given UserReport to be copied.
	 * @throws Exception
	 */
	public void copyUserReport(Long sourceUserReportPK, Long targetUserReportDirPK)
	throws Exception {
		UserReportVO copiedUserReport = getReportMgr().copyUserReport(
			sourceUserReportPK,
			targetUserReportDirPK
		);


		put(copiedUserReport);

		fireCreate(copiedUserReport.getID());
	}


	public void initTemplate(Long userReportID)
	throws Exception {
		getReportMgr().initTemplate(userReportID);
		refresh(userReportID);
	}

}

package de.regasus.report.model;

import static de.regasus.LookupService.getReportMgr;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.lambdalogic.messeinfo.report.data.BaseReportCVO;
import com.lambdalogic.messeinfo.report.data.BaseReportCVOSettings;

import de.regasus.core.model.MICacheModel;


public class BaseReportListModel
extends MICacheModel<Long, BaseReportCVO> {

	private static BaseReportListModel singleton = null;

	private BaseReportCVOSettings baseReportCVOsettings;


	private BaseReportListModel() {
		baseReportCVOsettings = new BaseReportCVOSettings();
		baseReportCVOsettings.withTemplateDataStoreVO = true;
	}


	public static BaseReportListModel getInstance() {
		if (singleton == null) {
			singleton = new BaseReportListModel();
		}
		return singleton;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	protected Long getKey(BaseReportCVO entity) {
		return entity.getPK();
	}


	@Override
	protected BaseReportCVO getEntityFromServer(Long baseReportPK) throws Exception {
		throw new Exception("Not implemented.");
	}


	@Override
	protected List<BaseReportCVO> getEntitiesFromServer(Collection<Long> baseReportPKs) throws Exception {
		throw new Exception("Not implemented.");
	}


	@Override
	protected List<BaseReportCVO> getAllEntitiesFromServer() throws Exception {
		List<BaseReportCVO> baseReportCVOs = null;

		if (serverModel.isLoggedIn()) {
			baseReportCVOs = getReportMgr().getBaseReportCVOs(baseReportCVOsettings);
			Collections.sort(baseReportCVOs, BaseReportCVOComparator.getInstance());
		}
		else {
			baseReportCVOs = Collections.emptyList();
		}

		return baseReportCVOs;
	}


	public Collection<BaseReportCVO> getAllBaseReportCVOs() throws Exception {
		Collection<BaseReportCVO> allBaseReportCVOs = getAllEntities();
		return allBaseReportCVOs;
	}


	public BaseReportCVO getBaseReportCVO(Long baseReportPK) throws Exception {
		return super.getEntity(baseReportPK);
	}

}

package de.regasus.participant;

import static de.regasus.LookupService.*;

import java.util.Collection;
import java.util.List;

import de.regasus.common.FileSummary;
import de.regasus.core.model.MICacheModel;

public class ParticipantFileModel extends MICacheModel<Long, FileSummary> {

	private static ParticipantFileModel singleton;


	private ParticipantFileModel() {
		super();
	}


	public static ParticipantFileModel getInstance() {
		if (singleton == null) {
			singleton = new ParticipantFileModel();
		}
		return singleton;
	}


	@Override
	protected Long getKey(FileSummary entity) {
		return entity.getId();
	}


	@Override
	protected FileSummary getEntityFromServer(Long id) throws Exception {
		FileSummary fileSummary = getFileMgr().readSummary(id);
		return fileSummary;
	}


	public FileSummary getParticipantFile(Long id) throws Exception {
		return super.getEntity(id);
	}


	@Override
	protected List<FileSummary> getEntitiesFromServer(Collection<Long> ids)
	throws Exception {
		List<FileSummary> fileSummaryList = getFileMgr().readSummary(ids);
		return fileSummaryList;
	}


	public List<FileSummary> getParticipantFileSummaryList(List<Long> ids) throws Exception {
		return super.getEntities(ids);
	}


	@Override
	protected FileSummary updateEntityOnServer(FileSummary fileSummary)
	throws Exception {
		fileSummary.validate();
		fileSummary = getFileMgr().update(fileSummary);
		return fileSummary;
	}


	@Override
	public FileSummary update(FileSummary fileSummary) throws Exception {
		return super.update(fileSummary);
	}


	@Override
	protected void deleteEntityOnServer(FileSummary fileSummary) throws Exception {
		if (fileSummary != null) {
			getFileMgr().delete( fileSummary.getId() );
		}
	}


	@Override
	public void delete(FileSummary fileSummary) throws Exception {
		super.delete(fileSummary);
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Object getForeignKey(FileSummary fileSummary) {
		Long fk = null;
		if (fileSummary != null) {
			fk = fileSummary.getRefId();
		}
		return fk;
	}


	@Override
	protected List<FileSummary> getEntitiesByForeignKeyFromServer(Object foreignKey)
	throws Exception {
		Long participantID = (Long) foreignKey;
		List<FileSummary> fileSummaryList = getParticipantMgr().readParticipantFiles(participantID);
		return fileSummaryList;
	}


	public List<FileSummary> getParticipantFileSummaryListByParticipantId(Long participantId)
	throws Exception {
		return getEntityListByForeignKey(participantId);
	}

}

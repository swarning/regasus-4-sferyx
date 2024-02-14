package de.regasus.common;

import static com.lambdalogic.util.CollectionsHelper.createArrayList;
import static de.regasus.LookupService.getFileMgr;

import java.util.Collection;
import java.util.List;

import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.core.ServerModel;
import de.regasus.core.model.MICacheModel;


/**
 * Base class for Model classes that manage File entities of a specific domain.
 * Though it offers methods to download and upload the content of these {@link File}s, the content itself is not
 * kept in the model's cache!
 */
public abstract class FileModel extends MICacheModel<Long, File> {

	public static String buildWebServiceUrl(BaseFile file) throws Exception {
		String webServiceUrl = ServerModel.getInstance().getWebServiceUrl();

		StringBuilder url = new StringBuilder(512);
		url.append(webServiceUrl);
		url.append("/content/");
		url.append( file.getId() );

		return url.toString();
	}


	@Override
	protected Long getKey(File entity) {
		return entity.getId();
	}


	@Override
	protected boolean isExtended(File file) {
		return file.getContent() != null;
	}


	@Override
	protected void copyExtendedValues(File from, File to) {
		to.setContent( from.getContent() );
	}


	@Override
	protected File getEntityFromServer(Long id) throws Exception {
		FileSummary fileSummary = getFileMgr().readSummary(id);
		return fileSummary.toFile();
	}


	@Override
	protected File getExtendedEntityFromServer(Long id) throws Exception {
		File file = getFileMgr().read(id);
		return file;
	}


	@Override
	protected List<File> getEntitiesFromServer(Collection<Long> filePKs) throws Exception {
		List<FileSummary> fileSummaryList = getFileMgr().readSummary(filePKs);
		return FileSummary.toFileList(fileSummaryList);
	}


	@Override
	protected List<File> getExtendedEntitiesFromServer(List<Long> filePKs) throws Exception {
		List<File> fileList = getFileMgr().read(filePKs);
		return fileList;
	}


	@Override
	protected boolean isForeignKeySupported() {
		return true;
	}


	@Override
	protected Long getForeignKey(File file) {
		Long fk = null;
		if (file != null) {
			fk = Long.valueOf( file.getRefId() );
		}
		return fk;
	}


	@Override
	protected List<File> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long refId = (Long) foreignKey;

		// load data from server
		List<FileSummary> fileSummaryList = getFileMgr().readSummaryByRefIds( createArrayList(refId) );
		return FileSummary.toFileList(fileSummaryList);
	}


	@Override
	protected void deleteEntityOnServer(File file) throws Exception {
		getFileMgr().delete( file.getId() );
	}


	@Override
	protected void deleteEntitiesOnServer(Collection<File> fileList) throws Exception {
		getFileMgr().delete( File.getPKs(fileList) );
	}


	protected File getFile(Long fileId) throws Exception {
		File file = getEntity(fileId);
		return file;
	}


	protected File getExtendedFile(Long fileId) throws Exception {
		File file = getExtendedEntity(fileId);
		return file;
	}


	protected File getExtendedFileWithoutCaching(Long fileId) throws Exception {
		File file = getExtendedEntityFromServer(fileId);
		return file;
	}


	/**
	 * Handle update locally instead of just calling {@link #handleUpdate(Long)} to avoid loading the content
	 * again from the server.
	 *
	 * @param file
	 * @throws Exception
	 */
	protected void handleUpdate(File file) throws Exception {
		put(file);
		fireDataChange(CacheModelOperation.UPDATE, file.getId());
	}


	protected void handleDelete(File file) throws Exception {
		handleDelete(
			file,
			createArrayList( getForeignKey(file) ),
			false // fireCoModelEvent
		);
	}


	public void removeContentFromCache(Long foreignKey) throws Exception {
		List<File> fileList = getEntityListByForeignKey(foreignKey);

		// delete content (make entities unextended)
		for (File file : fileList) {
			file.setContent(null);
		}

		/* alternative we could remove the foreign key information and the entities completely
		removeForeignKeyData(pageLayoutId);
		removeEntities( File.getPKs(fileList) );
		 */
	}

}

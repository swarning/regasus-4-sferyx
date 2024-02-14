package de.regasus.document;

import static de.regasus.LookupService.getFileMgr;

import java.util.List;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.model.CoModelEvent;

import de.regasus.common.File;
import de.regasus.common.FileSummary;
import de.regasus.common.GlobalFile;
import de.regasus.core.model.MICacheModel;


public class DocumentModel extends MICacheModel<String, FileSummary> {

	private GlobalFile globalFile;


	protected DocumentModel(GlobalFile globalFile) {
		super();
		this.globalFile = globalFile;
	}


	@Override
	protected String getKey(FileSummary fileSummary) {
		return fileSummary.getInternalPath();
	}


	@Override
	protected boolean isForeignKeySupported() {
		return false;
	}


	@Override
	public FileSummary getEntityFromServer(String internalPath) throws Exception {
		FileSummary fileSummary = getFileMgr().readSummaryWithPathEqualTo(internalPath);
		return fileSummary;
	}


	@Override
	protected List<FileSummary> getAllEntitiesFromServer() throws Exception {
		String rootPath = globalFile.getRootPath();
		List<FileSummary> fileSummaryList = getFileMgr().readSummaryWithPathLike(rootPath);
		return fileSummaryList;
	}


	@Override
	public List<FileSummary> getAllEntities() throws Exception {
		return super.getAllEntities();
	}


	@Override
	protected void deleteEntityOnServer(FileSummary file) throws Exception {
		if (file != null) {
			String path = file.getInternalPath();
			getFileMgr().deleteWithPathEqualTo(path);
		}
	}


	public File download(String internalPath) {
		File file = getFileMgr().readWithPathEqualTo(internalPath);
		return file;
	}


	public FileSummary upload(String filePath, String language, byte[] content) throws Exception {
		String extension = FileHelper.getExtension(filePath);
		String internalPath = globalFile.getInternalPath(extension, language);

		File file = new File(internalPath, content);
		file.setExternalPath(filePath);
		file.setFileType( globalFile.getFileType() );
		file.setLanguage(language);

		file.validate();

		getFileMgr().setDataByInternalPath(file);
		FileSummary fileSummary = getFileMgr().readSummaryWithPathEqualTo(internalPath);

		put(fileSummary);

		fireDataChange(CacheModelOperation.CREATE, internalPath);
		fireDataChange( CoModelEvent.createInstance(this, fileSummary) );

		return fileSummary;
	}


	@Override
	public void delete(FileSummary fileSummary) throws Exception {
		/*
		 * Do NOT delete if there is no ID, because it means that this File does not represent a record in table
		 * File but a file in the JAR.
		 */
		if (fileSummary.getId() != null) {
			super.delete(fileSummary);
		}
	}

}

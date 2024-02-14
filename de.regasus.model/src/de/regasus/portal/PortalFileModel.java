package de.regasus.portal;

import static de.regasus.LookupService.getPortalMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.common.File;
import de.regasus.common.FileModel;
import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.model.Activator;


/**
 * This model manages the {@link File}s of a {@link Portal}.
 * Though it offers methods to download and upload the content of these {@link File}s, the content itself is not
 * kept in the model's cache!
 */
public class PortalFileModel extends FileModel {

	private static PortalFileModel singleton;


	private PortalModel portalModel;


	private PortalFileModel() {
		portalModel = PortalModel.getInstance();
		portalModel.addListener(portalModelListener);
	}


	public static PortalFileModel getInstance() {
		if (singleton == null) {
			singleton = new PortalFileModel();
		}
		return singleton;
	}


	private CacheModelListener<Long> portalModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					Collection<Long> deletedPKs = new ArrayList<>( event.getKeyList().size() );

					for (Long portalPK : event.getKeyList()) {
						for (File file : getLoadedAndCachedEntities()) {
							if ( file.getRefId().equals(portalPK) ) {
								deletedPKs.add( file.getId() );
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
	};


	@Override
	protected List<File> getEntitiesByForeignKeyFromServer(Object foreignKey) throws Exception {
		// cast foreignKey
		Long portalPK = (Long) foreignKey;

		// load data from server
		List<FileSummary> fileSummaryList = getPortalMgr().readFileSummaries(portalPK);
		return FileSummary.toFileList(fileSummaryList);
	}


//	public File getPortalFile(Long portalPK, String fileMnemonic, String language) throws Exception {
//		File portalFile = null;
//
//		String internalPath = PortalFileHelper.buildInternalPath(portalPK, fileMnemonic, language);
//
//		List<File> portalFileList = getEntityListByForeignKey(portalPK);
//
//		for (File file : portalFileList) {
//			if ( file.getInternalPath().equals(internalPath) ) {
//				file = getExtendedEntity( file.getId() );
//				break;
//			}
//		}
//
//		return portalFile;
//	}


	@Override
	public File getFile(Long fileId) throws Exception {
		return super.getFile(fileId);
	}


	@Override
	public File getExtendedFile(Long fileId) throws Exception {
		return super.getExtendedFileWithoutCaching(fileId);
	}


	public Collection<File> getPortalFiles(Long portalId) throws Exception {
		return super.getEntityListByForeignKey(portalId);
	}


	public File upload(Long portalId, String mnemonic, String language, String externalPath, byte[] content)
	throws Exception {
		FileSummary fileSummary = getPortalMgr().uploadFile(portalId, mnemonic, language, externalPath, content);

		File file = fileSummary.toFile();
		// do not set content, because we don't want to keep it in memory

		handleUpdate(file);

		return file;
	}


	@Override
	public void delete(File file)
	throws Exception {
		super.delete(file);
	}


	@Override
	public void delete(Collection<File> files)
	throws Exception {
		super.delete(files);
	}

}

package de.regasus.programme;

import static de.regasus.LookupService.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointFile;
import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.common.File;
import de.regasus.common.FileModel;
import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.model.Activator;

/**
 * This model manages the {@link File} which are associated with a {@link ProgrammePoint}.
 * This is the image {@link File} for each language of the {@link Event}.
 *
 * For better performance, the model provides {@link File}s with and without content (a {@link File} with content
 * is extended. Because the model cannot load unextended {@link File}s (without content) from the server, it
 * internally loads {@link FileSummary}s instead and converts them to according {@link File} without content.
 *
 * However, the public interface of this model does not distinguish between extended and unextended {@link File}s.
 * The extended mechanism is only used internally.
 * All public methods that return a {@link File} return an extended {@link File} with content.
 */
public class ProgrammePointFileModel extends FileModel {

	private static ProgrammePointFileModel singleton;


	private ProgrammePointModel programmePointModel;


	private ProgrammePointFileModel() {
		super();

		programmePointModel = ProgrammePointModel.getInstance();
		programmePointModel.addListener(programmePointModelListener);
	}


	public static ProgrammePointFileModel getInstance() {
		if (singleton == null) {
			singleton = new ProgrammePointFileModel();
		}
		return singleton;
	}


	private CacheModelListener<Long> programmePointModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					Collection<Long> deletedPKs = new ArrayList<>( event.getKeyList().size() );

					for (Long programmePointPK : event.getKeyList()) {
						for (File file : getLoadedAndCachedEntities()) {
							if ( file.getRefId().equals(programmePointPK) ) {
								deletedPKs.add(file.getId());
							}
						}

						/* Remove the foreign key whose entity has been deleted from the model before firing the
						 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
						 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
						 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
						 * shall get an empty list.
						 */
						removeForeignKeyData(programmePointPK);
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


	private File getImageFile(Long programmePointPK, String language, ProgrammePointFile programmePointFile) throws Exception {
		File imageFile = null;

		List<File> programmePointFileList = getEntityListByForeignKey(programmePointPK);

		for (File file : programmePointFileList) {
			if (file.getFileType().equals(programmePointFile.getFileType())
				&&
				EqualsHelper.isEqual(file.getLanguage(), language)
			) {
				imageFile = getExtendedEntity( file.getId() );
				break;
			}
		}

		return imageFile;
	}


	public File getImageFile(Long programmePointPK, String language) throws Exception {
		return getImageFile(programmePointPK, language, ProgrammePointFile.IMAGE);
	}


	public File uploadImage(Long programmePointId, byte[] content, String language, String externalPath)
	throws Exception {
		FileSummary fileSummary = getProgrammePointMgr().uploadImage(programmePointId, content, language, externalPath);

		File file = fileSummary.toFile();
		// set content, because we want to keep it in memory
		file.setContent(content);

		handleUpdate(file);

		return file;
	}


	public void deleteImage(Long programmePointId, String language)
	throws Exception {
		/* Actually it is not efficient to load the File before it gets deleted.
		 * However, client might only delete Files that have been loaded already.
		 */
		File file = getImageFile(programmePointId, language);
		if (file != null) {
    		getProgrammePointMgr().deleteImage(programmePointId, language);
    		handleDelete(file);
    	}
	}

}

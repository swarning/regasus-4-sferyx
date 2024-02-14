package de.regasus.portal;

import static de.regasus.LookupService.getPageMgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
 * This model manages the {@link File}s which are associated with a {@link Page}.
 * This is the icon of the {@link Portal}.
 *
 * For better performance, the model provides {@link File}s with and without content (a {@link File} with content
 * is extended. Because the model cannot load unextended {@link File}s (without content) from the server, it
 * internally loads {@link FileSummary}s instead and converts them to according {@link File} without content.
 *
 * However, the public interface of this model does not distinguish between extended and unextended {@link File}s.
 * The extended mechanism is only used internally.
 * All public methods that return a {@link File} return an extended {@link File} with content.
 */
public class PageFileModel extends FileModel {

	private static PageFileModel singleton;


	private PageModel pageModel;


	private PageFileModel() {
		super();

		pageModel = PageModel.getInstance();
		pageModel.addListener(pageModelListener);
	}


	public static PageFileModel getInstance() {
		if (singleton == null) {
			singleton = new PageFileModel();
		}
		return singleton;
	}


	private CacheModelListener<Long> pageModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					Collection<Long> deletedPKs = new ArrayList<>( event.getKeyList().size() );

					for (Long pagePK : event.getKeyList()) {
						for (File file : getLoadedAndCachedEntities()) {
							if ( file.getRefId().equals(pagePK) ) {
								deletedPKs.add(file.getId());
							}
						}

						/* Remove the foreign key whose entity has been deleted from the model before firing the
						 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
						 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
						 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
						 * shall get an empty list.
						 */
						removeForeignKeyData(pagePK);
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


	private File getFile(Long pagePK, String language, PageFile pageFile) throws Exception {
		File file = null;

		List<File> fileList = getEntityListByForeignKey(pagePK);

		for (File f : fileList) {
			if (f.getFileType().equals(pageFile.getFileType())
				&&
				EqualsHelper.isEqual(f.getLanguage(), language)
			) {
				file = getExtendedEntity( f.getId() );
				break;
			}
		}

		return file;
	}


	public File getIconFile(Long pagePK) throws Exception {
		return getFile(pagePK, null /*language*/, PageFile.ICON);
	}


	public File uploadIcon(Long pageId, byte[] content, String externalPath)
	throws Exception {
		FileSummary fileSummary = getPageMgr().uploadIcon(pageId, content, externalPath);

		File file = fileSummary.toFile();
		// set content, because we want to keep it in memory
		file.setContent(content);

		handleUpdate(file);

		return file;
	}


	public void deleteIcon(Long pageId)
	throws Exception {
		/* Actually it is not efficient to load the File before it gets deleted.
		 * However, client might only delete Files that have been loaded already.
		 */
		File file = getIconFile(pageId);
		if (file != null) {
    		getPageMgr().deleteIcon(pageId);
    		handleDelete(file);
    	}
	}

}

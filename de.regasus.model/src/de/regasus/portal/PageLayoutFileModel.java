package de.regasus.portal;

import static de.regasus.LookupService.getPageLayoutMgr;

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
 * This model manages the {@link File}s which are associated with a {@link PageLayout}.
 * These are a header and a footer {@link File} for each language of the {@link Portal}.
 *
 * For better performance, the model provides {@link File}s with and without content (a {@link File} with content
 * is extended. Because the model cannot load unextended {@link File}s (without content) from the server, it
 * internally loads {@link FileSummary}s instead and converts them to according {@link File} without content.
 *
 * However, the public interface of this model does not distinguish between extended and unextended {@link File}s.
 * The extended mechanism is only used internally.
 * All public methods that return a {@link File} (which are {@link #getHeaderImageFile(Long, String)}
 * and {@link #getFooterImageFile(Long, String)}) return an extended {@link File} with content.
 */
public class PageLayoutFileModel extends FileModel {

	private static PageLayoutFileModel singleton;


	private PageLayoutModel pageLayoutModel;


	private PageLayoutFileModel() {
		pageLayoutModel = PageLayoutModel.getInstance();
		pageLayoutModel.addListener(pageLayoutModelListener);
	}


	public static PageLayoutFileModel getInstance() {
		if (singleton == null) {
			singleton = new PageLayoutFileModel();
		}
		return singleton;
	}


	private CacheModelListener<Long> pageLayoutModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (!serverModel.isLoggedIn()) {
				return;
			}

			try {
				if (event.getOperation() == CacheModelOperation.DELETE) {
					Collection<Long> deletedPKs = new ArrayList<>( event.getKeyList().size() );

					for (Long pageLayoutPK : event.getKeyList()) {
						for (File file : getLoadedAndCachedEntities()) {
							if ( file.getRefId().equals(pageLayoutPK) ) {
								deletedPKs.add(file.getId());
							}
						}

						/* Remove the foreign key whose entity has been deleted from the model before firing the
						 * corresponding CacheModelEvent. The entities shall exist in the model when firing the
						 * CacheModelEvent, but not the structural information about the foreign keys. If a listener gets
						 * the CacheModelEvent and consequently requests the list of all entities of the foreign key, it
						 * shall get an empty list.
						 */
						removeForeignKeyData(pageLayoutPK);
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


	private File getFile(Long pageLayoutPK, String language, PageLayoutFile pageLayoutFile) throws Exception {
		File file = null;

		List<File> fileList = getEntityListByForeignKey(pageLayoutPK);

		for (File f : fileList) {
			if (f.getFileType().equals(pageLayoutFile.getFileType())
				&&
				EqualsHelper.isEqual(f.getLanguage(), language)
			) {
				file = getExtendedEntity( f.getId() );
				break;
			}
		}

		return file;
	}


	public File getFaviconImageFile(Long pageLayoutPK) throws Exception {
		return getFile(pageLayoutPK, null /*language*/, PageLayoutFile.FAVICON_IMAGE);
	}


	public File getHeaderImageFile(Long pageLayoutPK, String language) throws Exception {
		return getFile(pageLayoutPK, language, PageLayoutFile.HEADER_IMAGE);
	}


	public File getFooterImageFile(Long pageLayoutPK, String language) throws Exception {
		return getFile(pageLayoutPK, language, PageLayoutFile.FOOTER_IMAGE);
	}


	public File uploadFaviconImage(Long pageLayoutId, byte[] content, String externalPath)
	throws Exception {
		FileSummary fileSummary = getPageLayoutMgr().uploadFaviconImage(pageLayoutId, content, externalPath);

		File file = fileSummary.toFile();
		// set content, because we want to keep it in memory
		file.setContent(content);

		handleUpdate(file);

		return file;
	}


	public File uploadHeaderImage(Long pageLayoutId, byte[] content, String language, String externalPath)
	throws Exception {
		FileSummary fileSummary = getPageLayoutMgr().uploadHeaderImage(pageLayoutId, content, language, externalPath);

		File file = fileSummary.toFile();
		// set content, because we want to keep it in memory
		file.setContent(content);

		handleUpdate(file);

		return file;
	}


	public File uploadFooterImage(Long pageLayoutId, byte[] content, String language, String externalPath)
	throws Exception {
		FileSummary fileSummary = getPageLayoutMgr().uploadFooterImage(pageLayoutId, content, language, externalPath);

		File file = fileSummary.toFile();
		// set content, because we want to keep it in memory
		file.setContent(content);

		handleUpdate(file);

		return file;
	}


	public void deleteFaviconImage(Long pageLayoutId)
	throws Exception {
		/* Actually it is not efficient to load the File before it gets deleted.
		 * However, client might only delete Files that have been loaded already.
		 */
		File file = getFaviconImageFile(pageLayoutId);
		if (file != null) {
    		getPageLayoutMgr().deleteFaviconImage(pageLayoutId);
    		handleDelete(file);
    	}
	}


	public void deleteHeaderImage(Long pageLayoutId, String language)
	throws Exception {
		/* Actually it is not efficient to load the File before it gets deleted.
		 * However, client might only delete Files that have been loaded already.
		 */
		File file = getHeaderImageFile(pageLayoutId, language);
		if (file != null) {
    		getPageLayoutMgr().deleteHeaderImage(pageLayoutId, language);
    		handleDelete(file);
    	}
	}


	public void deleteFooterImage(Long pageLayoutId, String language)
	throws Exception {
		/* Actually it is not efficient to load the File before it gets deleted.
		 * However, clients can only delete Files that have already been loaded.
		 */
		File file = getFooterImageFile(pageLayoutId, language);
		if (file != null) {
    		getPageLayoutMgr().deleteFooterImage(pageLayoutId, language);
    		handleDelete(file);
    	}
	}

}

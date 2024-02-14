package de.regasus.portal.pagelayout.editor;

import com.lambdalogic.util.EqualsHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.observer.DefaultEvent;
import com.lambdalogic.util.observer.Observer;
import com.lambdalogic.util.observer.ObserverSupport;
import com.lambdalogic.util.rcp.image.ImageFileGroupController;

import de.regasus.common.File;
import de.regasus.common.FileContentUrlHelper;
import de.regasus.core.ServerModel;
import de.regasus.portal.PageLayoutFile;
import de.regasus.portal.PageLayoutFileModel;

public class PageLayoutImageFileGroupController implements ImageFileGroupController {

	/**
	 * Define if this PageLayoutImageFileGroupController is handling the favicon, the header or footer of the PageLayout.
	 */
	private PageLayoutFile pageLayoutFile;

	private Long pageLayoutPK;
	private String language;

	private PageLayoutFileModel pageLayoutFileModel = PageLayoutFileModel.getInstance();

	protected ObserverSupport<DefaultEvent> observerSupport = new ObserverSupport<>(this);


	public PageLayoutImageFileGroupController(String language, PageLayoutFile pageLayoutFile) {
		// language can be null if the file is a favicon
//		Objects.requireNonNull(language);

		this.language = language;
		this.pageLayoutFile = pageLayoutFile;
	}


	private CacheModelListener<Long> pageLayoutFileModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			observerSupport.fire();
		}
	};


	public void setPageLayoutPK(Long pageLayoutPK) {
		if (this.pageLayoutPK == null && pageLayoutPK != null) {
			this.pageLayoutPK = pageLayoutPK;

			observerSupport.fire();
		}
		else if ( ! EqualsHelper.isEqual(this.pageLayoutPK, pageLayoutPK) ) {
			throw new RuntimeException("Setting different pageLayoutPK is not allowed "
				+ "(old value: " + this.pageLayoutPK + ", new value: " + pageLayoutPK + ").");
		}
	}


	@Override
	public void dispose() {
		pageLayoutFileModel.remove(pageLayoutFileModelListener);
	}


	@Override
	public File read() throws Exception {
		try {
			// temporarily disable observerSupport, because read operation might cause a CacheModelEvent
			observerSupport.setEnabled(false);

			File file = null;
			if (pageLayoutFile == PageLayoutFile.FAVICON_IMAGE) {
				file = pageLayoutFileModel.getFaviconImageFile(pageLayoutPK);
			}
			else if (pageLayoutFile == PageLayoutFile.HEADER_IMAGE) {
    			file = pageLayoutFileModel.getHeaderImageFile(pageLayoutPK, language);
    		}
    		else if (pageLayoutFile == PageLayoutFile.FOOTER_IMAGE) {
    			file = pageLayoutFileModel.getFooterImageFile(pageLayoutPK, language);
    		}

    		if (file != null) {
        		// observe after knowing the id
        		pageLayoutFileModel.addListener(pageLayoutFileModelListener, file.getId());
    		}

    		return file;
		}
		finally {
			observerSupport.setEnabled(true);
		}
	}


	@Override
	public void persist(byte[] content, String externalPath) throws Exception {
		/* Removing the listener before calling the model and adding it afterwards assures that no additional
		 * Event is fired if the File already exists, because we are observing it.
		 * Instead we fire an Event at the end.
		 */

		// stop observing old File
		pageLayoutFileModel.remove(pageLayoutFileModelListener);

		File file = null;
		if (pageLayoutFile == PageLayoutFile.FAVICON_IMAGE) {
			file = pageLayoutFileModel.uploadFaviconImage(pageLayoutPK, content, externalPath);
		}
		else if (pageLayoutFile == PageLayoutFile.HEADER_IMAGE) {
			file = pageLayoutFileModel.uploadHeaderImage(pageLayoutPK, content, language, externalPath);
		}
		else if (pageLayoutFile == PageLayoutFile.FOOTER_IMAGE) {
			file = pageLayoutFileModel.uploadFooterImage(pageLayoutPK, content, language, externalPath);
		}

		// observe new File
		pageLayoutFileModel.addListener(pageLayoutFileModelListener, file.getId());

		// fire Event
		observerSupport.fire();
	}


	@Override
	public void delete() throws Exception {
		if (pageLayoutFile == PageLayoutFile.FAVICON_IMAGE) {
			pageLayoutFileModel.deleteFaviconImage(pageLayoutPK);
		}
		else if (pageLayoutFile == PageLayoutFile.HEADER_IMAGE) {
			pageLayoutFileModel.deleteHeaderImage(pageLayoutPK, language);
		}
		else if (pageLayoutFile == PageLayoutFile.FOOTER_IMAGE) {
			pageLayoutFileModel.deleteFooterImage(pageLayoutPK, language);
		}
	}


	@Override
	public String getWebServiceUrl() throws Exception {
		String webServiceUrl = ServerModel.getInstance().getWebServiceUrl();

		String internalPath = null;
		if (pageLayoutFile == PageLayoutFile.FAVICON_IMAGE) {
			internalPath = PageLayoutFile.FAVICON_IMAGE.getInternalPath(pageLayoutPK, language);
		}
		else if (pageLayoutFile == PageLayoutFile.HEADER_IMAGE) {
			internalPath = PageLayoutFile.HEADER_IMAGE.getInternalPath(pageLayoutPK, language);
		}
		else if (pageLayoutFile == PageLayoutFile.FOOTER_IMAGE) {
			internalPath = PageLayoutFile.FOOTER_IMAGE.getInternalPath(pageLayoutPK, language);
		}

		String url = FileContentUrlHelper.buildUrl(webServiceUrl, internalPath);
		return url;
	}


	// **************************************************************************
	// * Observation
	// *

	@Override
	public void addObserver(Observer<DefaultEvent> observer) {
		observerSupport.addObserver(observer);
	}


	@Override
	public void removeObserver(Observer<DefaultEvent> observer) {
		observerSupport.removeObserver(observer);
	}

	// *
	// * Observation
	// **************************************************************************

}

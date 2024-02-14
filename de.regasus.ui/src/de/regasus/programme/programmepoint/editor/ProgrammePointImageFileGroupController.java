package de.regasus.programme.programmepoint.editor;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointFile;
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
import de.regasus.programme.ProgrammePointFileModel;

public class ProgrammePointImageFileGroupController implements ImageFileGroupController {

	/**
	 * Define if this PageLayoutImageFileGroupController is handling the favicon, the header or footer of the PageLayout.
	 */
	private ProgrammePointFile programmePointFile;

	private Long programmePointPK;
	private String language;

	private ProgrammePointFileModel programmePointFileModel = ProgrammePointFileModel.getInstance();

	protected ObserverSupport<DefaultEvent> observerSupport = new ObserverSupport<>(this);


	public ProgrammePointImageFileGroupController(String language, ProgrammePointFile programmePointFile) {
		// language can be null

		this.language = language;
		this.programmePointFile = programmePointFile;
	}


	private CacheModelListener<Long> programmePointFileModelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			observerSupport.fire();
		}
	};


	public void setProgrammePointPK(Long programmePointPK) {
		if (this.programmePointPK == null && programmePointPK != null) {
			this.programmePointPK = programmePointPK;

			observerSupport.fire();
		}
		else if ( ! EqualsHelper.isEqual(this.programmePointPK, programmePointPK) ) {
			throw new RuntimeException("Setting different programmePointPK is not allowed "
				+ "(old value: " + this.programmePointPK + ", new value: " + programmePointPK + ").");
		}
	}


	@Override
	public void dispose() {
		programmePointFileModel.remove(programmePointFileModelListener);
	}


	@Override
	public File read() throws Exception {
		try {
			// temporarily disable observerSupport, because read operation might cause a CacheModelEvent
			observerSupport.setEnabled(false);

			File file = null;
			if (programmePointFile == ProgrammePointFile.IMAGE) {
				file = programmePointFileModel.getImageFile(programmePointPK, language);
			}

    		if (file != null) {
        		// observe after knowing the id
        		programmePointFileModel.addListener(programmePointFileModelListener, file.getId());
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
		programmePointFileModel.remove(programmePointFileModelListener);

		File file = null;
		if (programmePointFile == ProgrammePointFile.IMAGE) {
			file = programmePointFileModel.uploadImage(programmePointPK, content, language, externalPath);
		}

		// observe new File
		programmePointFileModel.addListener(programmePointFileModelListener, file.getId());

		// fire Event
		observerSupport.fire();
	}


	@Override
	public void delete() throws Exception {
		if (programmePointFile == ProgrammePointFile.IMAGE) {
			programmePointFileModel.deleteImage(programmePointPK, language);
		}
	}


	@Override
	public String getWebServiceUrl() throws Exception {
		String url = null;

		if (programmePointFile == ProgrammePointFile.IMAGE) {
			String webServiceUrl = ServerModel.getInstance().getWebServiceUrl();
			String internalPath = ProgrammePointFile.IMAGE.getInternalPath(programmePointPK, language);
			url = FileContentUrlHelper.buildUrl(webServiceUrl, internalPath);
		}

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

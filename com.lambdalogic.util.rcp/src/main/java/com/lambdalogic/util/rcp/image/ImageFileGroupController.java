package com.lambdalogic.util.rcp.image;


import com.lambdalogic.util.observer.DefaultEvent;
import com.lambdalogic.util.observer.Observer;

import de.regasus.common.File;

public interface ImageFileGroupController {

	File read() throws Exception;

	void persist(byte[] content, String externalPath) throws Exception;

	void delete() throws Exception;

	String getWebServiceUrl() throws Exception;

	public void addObserver(Observer<DefaultEvent> observer);

	void removeObserver(Observer<DefaultEvent> observer);

	void dispose();

}

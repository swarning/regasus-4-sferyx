package de.regasus.app.main;

import org.eclipse.ui.application.WorkbenchAdvisor;


/**
 * This class controls all aspects of the application's execution
 */
public class Application extends de.regasus.core.ui.AbstractApplication {

	@Override
	public WorkbenchAdvisor getWorkbenchAdvisor() {
		return new ApplicationWorkbenchAdvisor();
	}

}

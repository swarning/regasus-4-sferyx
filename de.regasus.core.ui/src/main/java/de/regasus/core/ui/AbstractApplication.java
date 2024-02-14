package de.regasus.core.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.regasus.core.ui.action.LoginAction;

/**
 * This class controls all aspects of the application's execution
 */
public abstract class AbstractApplication implements IApplication {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	/**
	 * The concrete subclass needs to override this method in order to give
	 * an ApplicationWorkbenchAdvisor that returns the particular initial
	 * perspective-id.
	 *
	 * @return A workbenchAdvisor that returns the particular initial
	 * perspective-id
	 */
	public abstract WorkbenchAdvisor getWorkbenchAdvisor();

	/**
	 * Buffer für Ausgaben auf System.out und System.err.
	 * Solange die Debug Console noch nicht geöffnet ist, werden die Ausgaben
	 * hier gespeichert. Die Umleitung erfolgt in start().
	 * Im ApplicationWorkbenchAdvisor werden System.out und System.err dann
	 * auf die Debug Console umgeleitet und zuvor der Inhalt von sysOutErrBuffer
	 * auf der Debug Console ausgegeben. Anschließend wird sysOutErrBuffer
	 * in ApplicationWorkbenchAdvisor auf null gesetzt.
	 */
	public static ByteArrayOutputStream sysOutErrBuffer = new ByteArrayOutputStream();


	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		log.info("");
		log.info("Starting application");

		Display display = PlatformUI.createDisplay();

		/*
		 * I want to be able to see the standard output in the console, by starting the app with
		 * -DnoRedirectionOfSystemOutAndErr=true
		 */
		if (System.getProperty("noRedirectionOfSystemOutAndErr") == null) {
			System.setOut(new PrintStream(sysOutErrBuffer));
			System.out.println("System.out --> sysOutErrBuffer");
			System.setErr(new PrintStream(sysOutErrBuffer));
			System.out.println("System.err --> sysOutErrBuffer");
		}

		// Config-URL aus Startparameter lesen
		String user = null;
		String password = null;
		String host = null;
		boolean autoLogin = false;

		String[] applicationArgs = (String[]) context.getArguments().get("application.args");
		if (applicationArgs != null) {
			final String userKey = "user=";
			final String passwordKey = "password=";
			final String hostKey = "host=";
			final String autoLoginKey = "autoLogin=";

			for (int i = 0; i < applicationArgs.length; i++) {
				if (applicationArgs[i].startsWith(userKey) && applicationArgs[i].length() > userKey.length()) {
					user = applicationArgs[i].substring(userKey.length());
				}
				else if (applicationArgs[i].startsWith(passwordKey) && applicationArgs[i].length() > passwordKey.length()) {
					password = applicationArgs[i].substring(passwordKey.length());
				}
				else if (applicationArgs[i].startsWith(hostKey) && applicationArgs[i].length() > hostKey.length()) {
					host = applicationArgs[i].substring(hostKey.length());
				}
				else if (applicationArgs[i].equals(autoLoginKey)) {
					autoLogin = true;
				}
			}
		}

		LoginAction.login(user, password, host, autoLogin);


		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, getWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			else {
				return IApplication.EXIT_OK;
			}
		}
		finally {
			display.dispose();

			log.info("Application stopped");
			log.info("");
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
		log.info("Stopping application");
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return;
		}

		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				if (!display.isDisposed()) {
					workbench.close();
				}
			}
		});
	}

}

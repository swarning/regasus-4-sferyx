package de.regasus.core.ui;

import java.io.PrintStream;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.internal.registry.EditorRegistry;

import de.regasus.core.ServerModel;

public abstract class AbstractApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private SelectionTracker selectionTracker = new SelectionTracker();
	private PartTracker partTracker = new PartTracker();
	private EditorSelectionProvider editorSelectionProvider = new  EditorSelectionProvider();


	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}


	/**
	 * A concrete subclass needs to override this method in order to give the application's initial perspective-id.
	 *
	 * @return A string containing the initial perspective-id
	 */
	@Override
	public abstract String getInitialWindowPerspectiveId();


	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);

		configurer.setSaveAndRestore(true);

		// put the PerspectiveBar right to the ToolBar
		IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
		prefStore.setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, IWorkbenchPreferenceConstants.TOP_RIGHT);
		prefStore.setDefault(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);

		try {
			MessageConsole myConsole = new MessageConsole("Debug Console", null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { myConsole });
			MessageConsoleStream msgStream = myConsole.newMessageStream();

			msgStream.println("Debug Console");
			msgStream.println("Application.sysOutErrBuffer:");
			msgStream.write(AbstractApplication.sysOutErrBuffer.toByteArray());
			AbstractApplication.sysOutErrBuffer = null;

			/*
			 * I want to be able to see the standard output in the console, by starting the app with
			 * -DnoRedirectionOfSystemOutAndErr=true
			 */
			if (System.getProperty("noRedirectionOfSystemOutAndErr") == null) {
				System.setOut(new PrintStream(msgStream));
				System.out.println("System.out --> Debug Console");

				System.setErr(new PrintStream(msgStream));
				System.err.println("System.err --> Debug Console");
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	private void closeEmptyEditors() {
		try {
			IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage activePage = workbenchWindow.getActivePage();
			IEditorReference[] editorReferences = activePage.getEditorReferences();
			for (IEditorReference editorReference : editorReferences) {
				String editorId = editorReference.getId();
				if (EditorRegistry.EMPTY_EDITOR_ID.equals(editorId)) {
					String label = "id: " + editorId + ", name: '" + editorReference.getName() + "', title: '" + editorReference.getTitle() + "'";
					System.out.println("Closing editor: " + label);

					activePage.closeEditors(new IEditorReference[] { editorReference }, false);
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	public void postStartup() {
		closeEmptyEditors();

		// init SelectionTracker
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addPostSelectionListener(selectionTracker);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		// init PartTracker
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(partTracker);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		// init EditorSelectionProvider
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(editorSelectionProvider);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@Override
	public boolean preShutdown() {
		// de-register SelectionTracker
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().removePostSelectionListener(selectionTracker);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		// de-register PartTracker
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(partTracker);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		// de-register EditorSelectionProvider
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(editorSelectionProvider);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		try {
			ServerModel.getInstance().shutdown();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return true;
	}

}

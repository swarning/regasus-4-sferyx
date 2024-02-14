package com.lambdalogic.util.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.util.rcp.error.ErrorHandler;

/**
 * Helper class to simply show the platform specific busy/wait cursor (eg an hourglas)
 * during the execution of any Runnable or {@link IRunnableWithProgress}. Use it like this:
 *
 * <pre>
 * 	BusyCursorHelper.busyCursorWhile(new Runnable() {
 * 	public void run() {
 * 		// Operationen während der der Busy-Cursor angezeigt werden soll
 * 	}
 * });
 *
 * <pre>
 *
 * The benefit of using this class is
 * <ul>
 * 	<li>nested calls are allowed</li>
 * 	<li>you save some try-catch-blocks</li>
 * </ul>
 *
 * Note however that it is recommended practice use {@link IRunnableWithProgress}, to give progress
 * information during execution and to allow the user to cancel the current task. If you only use
 * Runnable, the user may click the Cancel button, but the task continues anyway.
 */
public class BusyCursorHelper {

	/**
	 * Consider to use {@link BusyCursorHelper#busyCursorWhile(IRunnableWithProgress)} instead.
	 */
	public static void busyCursorWhile(final Runnable runnable) {
		// Wrap the runnable and delegate to method below
		busyCursorWhile(new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				runnable.run();
			}
		});
	}


	public static void busyCursorWhile(final IRunnableWithProgress runnable) {
		try {
			if (Display.getCurrent() != null) {
				// We are in the UI Thread, so we execute the Runnable in background while the ProgressService makes
				// sure that a busy cursor appears after a short period of time
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
			}
			else {
				// Not in Display-Thread, executing Runnable synchronously since we expect a busy cursor already switched on
				runnable.run(new NullProgressMonitor());
			}
		}
		catch (Throwable t) {
			ErrorHandler.handleApplicationError(Activator.PLUGIN_ID, BusyCursorHelper.class.getName(), t);
		}
	}

}

package com.lambdalogic.util.rcp.tree;

import org.eclipse.jface.viewers.TreeViewer;

import com.lambdalogic.util.rcp.widget.SWTHelper;


public class TreeHelper {

	/**
	 * Execute the Runnable, but wait until the TreeViewer is not busy.
	 * If the TreeViewer is not busy, the Runnable is executed immediately in the current Thread.
	 * If the TreeViewer is busy, a new Thread is started that checks if the TreeViewer is busy
	 * every 200 ms until the TreeViewer is not busy anymore (but at most 50 times / 10 s).
	 * 
	 * Anyway, the Runnable is executed asynchronously in the Display-Thread.  
	 * 
	 * @param runnable
	 */
	public static void runWhenNotBusy(final Runnable runnable, final TreeViewer treeViewer) {
		if (treeViewer.isBusy()) {
			Thread t = new Thread() {
				public void run() {
					boolean done = false;
					int count = 0;
					while (!done && count < 50) {
						count++;
						if (treeViewer.isBusy()) {
							try {
								Thread.sleep(200);
							}
							catch (InterruptedException e) {
								com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
							}
						}
						else {
							SWTHelper.asyncExecDisplayThread(runnable);
							
							done = true;
						}
					}
					
					if (!done) {
						System.err.println("TreeViewer could not be updated, because it was busy for more than 10s.");
					}
				}
			};
			t.start();
		}
		else {
			SWTHelper.asyncExecDisplayThread(runnable);
		}
	}

}

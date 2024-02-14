package com.lambdalogic.util.rcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

public class ProgressMonitorStreamHelper {

	/**
	 * Copy data from an <code>InputStream</code> to an <code>OutputStream</code> until the InputStream signals an EOF.
	 * This method may block a while if data is yet to be produced on the providing side.
	 * @param inputStream
	 * @param outputStream
	 * @param bufferSize
	 * @param progressMonitor {@link IProgressMonitor#worked(1)} is called after every buffer that has been read
	 * @throws IOException
	 */
	public static void copy(
		InputStream inputStream,
		OutputStream outputStream,
		int bufferSize,
		IProgressMonitor progressMonitor
	)
	throws IOException {
		if (inputStream != null && outputStream != null) {
			byte[] buffer = new byte[bufferSize];
			int read = 0;
			do {
				read = inputStream.read(buffer);
				if (read > 0) {
					outputStream.write(buffer, 0, read);
				}
				outputStream.flush();

				if (progressMonitor != null) {
					progressMonitor.worked(1);
				}
			}
			while (read > 0);
		}
	}

}

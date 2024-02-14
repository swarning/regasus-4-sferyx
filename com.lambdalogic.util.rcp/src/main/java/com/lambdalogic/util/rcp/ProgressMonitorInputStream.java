package com.lambdalogic.util.rcp;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * {@link InputStream} that reports the number of read bytes to a {@link IProgressMonitor}.
 */
public class ProgressMonitorInputStream extends InputStream {

	private InputStream inputStream;
	private IProgressMonitor monitor;
	private int unitSize;

	private int read;


	/**
	 * @param inputStream
	 * @param monitor
	 * @param unitSize number of bytes that correspond to a work-unit of the {@link IProgressMonitor}
	 */
	public ProgressMonitorInputStream(InputStream inputStream, IProgressMonitor monitor, int unitSize) {
		this.monitor = monitor;
		this.inputStream = inputStream;
		this.unitSize = unitSize;
	}


	@Override
	public int read() throws IOException {
		int data = inputStream.read();

		read ++;
		while (read >= unitSize) {
			monitor.worked(1);
			read -= unitSize;
		}

		return data;
	}

}

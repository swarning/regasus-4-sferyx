package de.regasus.report.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ui.Activator;

public class ReportTreeTransferContainer {
	
	private long id;
	private String className;

	
	public ReportTreeTransferContainer() {
	}

	
	public ReportTreeTransferContainer(byte[] bytes) {
		fromByteArray(bytes);
	}

	
	public ReportTreeTransferContainer(long id, String className) {
		super();
		this.id = id;
		this.className = className;
	}
	
	
	public byte[] toByteArray() {
		/**
		 * Serialized version is:
		 * (long) numeric primary key
		 * (String) full class name
		 */
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);

		byte[] bytes = null;
		try {
			// write id
			out.writeLong(id);
			// write className
			if (className != null) {
				out.writeUTF(className);
			}
			else {
				out.writeUTF(""); 
			}
			
			out.close();
			bytes = byteOut.toByteArray();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return bytes;
	}
	
	
	public void fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			// read id
			id = in.readLong();
			
			// read className
			className = in.readUTF();
		}			
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getClassName() {
		return className;
	}


	public void setClassName(String className) {
		this.className = className;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(className);
		sb.append("("); 
		sb.append(id);
		sb.append(")"); 
		return sb.toString();
	}
}

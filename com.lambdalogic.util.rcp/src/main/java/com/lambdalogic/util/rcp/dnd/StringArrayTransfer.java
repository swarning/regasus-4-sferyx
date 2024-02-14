package com.lambdalogic.util.rcp.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.error.ErrorHandler;

/**
 * A base class for all Transfers that want to exchange String arrays. 
 * 
 * @author manfred
 *
 */
abstract public class StringArrayTransfer extends ByteArrayTransfer {

	@Override
	protected void javaToNative(Object object, TransferData transferData) {

		String[] strings = (String[]) object;
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);

		byte[] bytes = null;
		try {
			// write length
			out.writeInt(strings.length);
			
			// write keys
			for (int i = 0; i < strings.length; i++) {
				if (strings[i] != null) {
					out.writeUTF(strings[i]);
				}
				else {
					out.writeUTF(""); 
				}
			}
			
			out.close();
			bytes = byteOut.toByteArray();
		}
		catch (Throwable t) {
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		// TODO Auto-generated method stub
		super.javaToNative(bytes, transferData);
	}
	
	
	@Override
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

		try {
			// read length
			int length = in.readInt();
			String[] keys = new String[length];
			
			// read keys
			for (int i = 0; i < keys.length; i++) {
				keys[i] = in.readUTF();	
			}
			return keys;
		}			
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}

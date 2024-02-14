package de.regasus.report.view;

import java.util.logging.Logger;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.report.ui.Activator;


public class ReportTreeTransfer extends ByteArrayTransfer {
	private static Logger log = Logger.getLogger("ui.UserReportTransfer"); 
	
	private static ReportTreeTransfer instance = new ReportTreeTransfer();
	private static final String TYPE_NAME = "ReportTreeTransferFormat"; 
	private static final int TYPEID = registerType(TYPE_NAME);

	
	/**
	 * Returns the singleton transfer instance.
	 */
	public static ReportTreeTransfer getInstance() {
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private ReportTreeTransfer() {
	}

	
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	/*
	 * Method declared on Transfer.
	 */
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		log.info("javaToNative"); 
		
		try {
			ReportTreeTransferContainer reportTreeTransferContainer = (ReportTreeTransferContainer) object;
			byte[] bytes = reportTreeTransferContainer.toByteArray();
			super.javaToNative(bytes, transferData);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}

	/*
	 * Method declared on Transfer.
	 */
	@Override
	protected Object nativeToJava(TransferData transferData) {
		log.info("nativeToJava"); 
		ReportTreeTransferContainer reportTreeTransferContainer = null;
		try {
			byte[] bytes = (byte[]) super.nativeToJava(transferData);
			reportTreeTransferContainer = new ReportTreeTransferContainer(bytes);
		} 
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		log.info("nativeToJava returning: " + reportTreeTransferContainer); 
		return reportTreeTransferContainer;
	}

}

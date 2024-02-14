package de.regasus.report.wizard.participant.list.dnd;

import com.lambdalogic.util.rcp.dnd.StringArrayTransfer;

/**
 * This class serves for the dedicated exchange of a String array whose entries are keys of SQLFields.
 * 
 * @author manfred
 *
 */
public class SQLFieldKeysTransfer extends StringArrayTransfer {
	
	
	private static SQLFieldKeysTransfer instance = new SQLFieldKeysTransfer();

	private static final String TYPE_NAME = "SQLFieldKeysTransfer"; 
	private static final int TYPEID = registerType(TYPE_NAME);

	
	/**
	 * Returns the singleton transfer instance.
	 */
	public static SQLFieldKeysTransfer getInstance() {
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private SQLFieldKeysTransfer() {
	}

	
	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}


}

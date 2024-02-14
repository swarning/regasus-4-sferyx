package com.lambdalogic.util.rcp;

import com.lambdalogic.util.rcp.dnd.StringArrayTransfer;

/**
 * This class is to identify in the Clipboard copied String arrays with a length dividable by three, containing
 * the class name and the PK of the entity that is to be copied, as well as its label in the tree.
 */
public class ClassKeyNameTransfer extends StringArrayTransfer {

	private static ClassKeyNameTransfer instance = new ClassKeyNameTransfer();

	private static final String TYPE_NAME = "ClassKeyNameTransfer";
	private static final int TYPEID = registerType(TYPE_NAME);


	/**
	 * Returns the singleton transfer instance.
	 */
	public static ClassKeyNameTransfer getInstance() {
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private ClassKeyNameTransfer() {
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

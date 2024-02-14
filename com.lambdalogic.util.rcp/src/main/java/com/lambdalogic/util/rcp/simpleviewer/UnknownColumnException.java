package com.lambdalogic.util.rcp.simpleviewer;

public class UnknownColumnException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnknownColumnException(Enum<?> columnEnum) {
		super("Unknown column: " + columnEnum);
	}

}
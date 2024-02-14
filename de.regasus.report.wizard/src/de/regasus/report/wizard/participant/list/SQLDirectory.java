package de.regasus.report.wizard.participant.list;

import com.lambdalogic.messeinfo.kernel.interfaces.SQLFieldPath;

public class SQLDirectory {

	private SQLFieldPath path;
	private String label;
	
	

	public SQLDirectory(SQLFieldPath path, String label) {
		this.path = path;
		this.label = label;
	}

	public SQLFieldPath getPath() {
		return path;
	}

	public void setPath(SQLFieldPath path) {
		this.path = path;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}

package de.regasus.report.wizard.participant.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;

public class SQLFieldContainer {

	private SQLField sqlField;

	public SQLFieldContainer(SQLField sqlField) {
		super();
		this.sqlField = sqlField;
	}

	public SQLField getSqlField() {
		return sqlField;
	}

	public void setSqlField(SQLField sqlField) {
		this.sqlField = sqlField;
	}

	
	public static List<SQLFieldContainer> wrap(Collection<SQLField> sqlFields) {
		List<SQLFieldContainer> result = null;
		if (sqlFields != null) {
			result = new ArrayList<SQLFieldContainer>(sqlFields.size());
			for (SQLField sqlField : sqlFields) {
				result.add(new SQLFieldContainer(sqlField));
			}
		}
		return result;
	}
	
	
	public static List<SQLField> unwrap(Collection<SQLFieldContainer> sqlFieldContainers) {
		List<SQLField> result = null;
		if (sqlFieldContainers != null) {
			result = new ArrayList<SQLField>(sqlFieldContainers.size());
			for (SQLFieldContainer sqlFieldContainer : sqlFieldContainers) {
				result.add(sqlFieldContainer.getSqlField());
			}
		}
		return result;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sqlField == null) ? 0 : sqlField.hashCode());
		return result;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SQLFieldContainer other = (SQLFieldContainer) obj;
		if (sqlField == null) {
			if (other.sqlField != null)
				return false;
		} else if (!sqlField.equals(other.sqlField))
			return false;
		return true;
	}
	
}

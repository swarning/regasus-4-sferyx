package de.regasus.report.wizard.participant.list;

import com.lambdalogic.messeinfo.kernel.interfaces.SQLField;

public class SQLColumnData {
	
	private SQLField sqlField;
	private boolean show = true;
	private Integer orderIndex = null;
	private boolean orderDescent = false; 
	
	
	public SQLColumnData(SQLField sqlField) {
		this.sqlField = sqlField;
	}


	public SQLField getSqlField() {
		return sqlField;
	}


	public void setSqlField(SQLField sqlField) {
		this.sqlField = sqlField;
	}


	public boolean isShow() {
		return show;
	}


	public void setShow(boolean show) {
		this.show = show;
	}


	public Integer getOrderIndex() {
		return orderIndex;
	}


	public void setOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex;
	}


	public boolean isOrderDescent() {
		return orderDescent;
	}


	public void setOrderDescent(boolean orderDescent) {
		this.orderDescent = orderDescent;
	}

}

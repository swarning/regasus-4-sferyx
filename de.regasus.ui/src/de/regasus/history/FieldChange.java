package de.regasus.history;


public class FieldChange {

	private String field;
	private Object oldValue;
	private Object newValue;
	
	
	public FieldChange(String field, Object oldValue, Object newValue) {
		super();
		this.field = field;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public String toString() {
		return field + ": Old value=" + oldValue + ", new value = " + newValue + "\n";
	}


	public String getField() {
		return field;
	}


	public Object getOldValue() {
		return oldValue;
	}


	public Object getNewValue() {
		return newValue;
	}

}

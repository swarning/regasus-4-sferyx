package com.lambdalogic.util.rcp.datetime;

public class CopyAndPasteEvent {

	private String text;
	private boolean cancel = false;
	
	
	public CopyAndPasteEvent() {
		super();
	}
	
	
	public CopyAndPasteEvent(String text) {
		super();
		this.text = text;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public boolean isCanceled() {
		return cancel;
	}


	public void cancel() {
		this.cancel = true;
	}
	
}

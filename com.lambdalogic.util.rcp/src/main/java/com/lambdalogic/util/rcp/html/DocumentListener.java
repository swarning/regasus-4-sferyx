package com.lambdalogic.util.rcp.html;

import java.util.Objects;

import javax.swing.event.DocumentEvent;

import com.lambdalogic.util.rcp.ModifySupport;

public class DocumentListener implements javax.swing.event.DocumentListener{

	private ModifySupport modifySupport;


	public DocumentListener(ModifySupport modifySupport) {
		this.modifySupport = Objects.requireNonNull(modifySupport);
	}


	@Override
	public void removeUpdate(DocumentEvent e) {
		modifySupport.fire();
	}


	@Override
	public void insertUpdate(DocumentEvent e) {
		modifySupport.fire();
	}


	@Override
	public void changedUpdate(DocumentEvent e) {
		modifySupport.fire();
	}

}

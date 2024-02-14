package com.lambdalogic.util.rcp.widget;

import java.util.ArrayList;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;

public class DecorationController {

	public static final Image ERROR_IMAGE = 
		FieldDecorationRegistry.getDefault().
		getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

	public static final Image REQUIRED_IMAGE = 
		FieldDecorationRegistry.getDefault().
		getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage();
	
	public static final String REQUIRED_TEXT = UtilI18N.InputRequired;

	
	private ArrayList<Label> labelList = new ArrayList<Label>();
	private ArrayList<Control> controlList = new ArrayList<Control>();
	private ArrayList<ControlDecoration> labelDecorationList = new ArrayList<ControlDecoration>();

	private boolean errorsEnabled = false;
	
	
	public void add(Label label, Text text) {
		if (label == null) {
			System.err.println("Parameter label is null");
			return;
		}
		if (text == null) {
			System.err.println("Parameter text is null");
			return;
		}
		
		_add(label, text);
	}

	
	public void add(Label label, Combo combo) {
		if (label == null) {
			System.err.println("Parameter label is null");
			return;
		}
		if (combo == null) {
			System.err.println("Parameter combo is null");
			return;
		}
		
		_add(label, combo);
	}

	
	private void _add(Label label, Control control) {
		labelList.add(label);
		controlList.add(control);
		
		ControlDecoration controlDecoration = new ControlDecoration(label, SWT.RIGHT | SWT.TOP);
		
		controlDecoration.setImage(REQUIRED_IMAGE);
		controlDecoration.setDescriptionText(REQUIRED_TEXT);
		controlDecoration.setShowHover(true);

		labelDecorationList.add(controlDecoration);
		
//		control.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				decorate();
//			}
//		});

	}
	
	
	public void enableErrors() {
		if ( ! errorsEnabled) {
			errorsEnabled = true;

			for (int i = 0; i < labelList.size(); i++) {
				final Control control = controlList.get(i);
				final ControlDecoration controlDecoration = labelDecorationList.get(i);
				
				if (control instanceof Text) {
					final Text text = (Text) control;
					TextModifyListener textModifyListener = new TextModifyListener(text, controlDecoration);
					text.addModifyListener(textModifyListener);
					textModifyListener.modifyText(null);
				}
	//			else if (control instanceof Combo) {
	//				final Combo combo = (Combo) control;
	//				combo.addModifyListener(new ModifyListener() {
	//					public void modifyText(ModifyEvent e) {
	//						if (StringHelper.isEmpty(text.getText())) {
	//							controlDecoration.setImage(ERROR_IMAGE);
	//						}
	//						else {
	//							controlDecoration.setImage(REQUIRED_IMAGE);
	//						}
	//					}
	//				});
	//			}
			}
		}
	}

	
	private class TextModifyListener implements ModifyListener {
		private Text text;
		private ControlDecoration controlDecoration;
		
		TextModifyListener(Text text, ControlDecoration controlDecoration) {
			this.text = text;
			this.controlDecoration = controlDecoration;
		}
		
		public void modifyText(ModifyEvent e) {
			if (StringHelper.isEmpty(text.getText())) {
				controlDecoration.setImage(ERROR_IMAGE);
			}
			else {
				controlDecoration.setImage(REQUIRED_IMAGE);
			}
		}

	}
}

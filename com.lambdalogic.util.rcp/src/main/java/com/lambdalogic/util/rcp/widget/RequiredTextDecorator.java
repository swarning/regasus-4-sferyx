package com.lambdalogic.util.rcp.widget;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;

import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.rcp.UtilI18N;

public class RequiredTextDecorator extends ControlDecoration {

	public static final Image ERROR_IMAGE = 
		FieldDecorationRegistry.getDefault().
		getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

	public static final Image REQUIRED_IMAGE = 
		FieldDecorationRegistry.getDefault().
		getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage();
	
	public static final String REQUIRED_TEXT = UtilI18N.InputRequired;
	
	
	private Text control;
	
	
	public RequiredTextDecorator(final Text control) {
		super(control, SWT.RIGHT | SWT.TOP);
		
		this.control = control;
		
		setImage(ERROR_IMAGE);
		setDescriptionText(REQUIRED_TEXT);
		setShowHover(true);

		control.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				decorate();
			}
		});

		decorate();
	}


	public void decorate() {
		if (StringHelper.isEmpty(control.getText())) {
			show();
		}
		else {
			hide();
		}
	}
	
}

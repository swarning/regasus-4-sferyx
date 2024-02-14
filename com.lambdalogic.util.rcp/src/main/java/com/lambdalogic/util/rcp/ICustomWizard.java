package com.lambdalogic.util.rcp;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Point;

public interface ICustomWizard extends IWizard {

	void setCustomWizardDialog(CustomWizardDialog customWizardDialog);
	
	Point getPreferredSize();
	
}

package com.lambdalogic.util.rcp.datetime;

import java.time.temporal.Temporal;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.ModifySupport;

public abstract class AbstractDateControl extends Composite implements ModifyListener {

	protected  ModifySupport modifySupport = new ModifySupport(this);


	public AbstractDateControl(Composite parent, int style) {
		super(parent, style);
	}

	public abstract Temporal getTemporal();

	public abstract void setTemporal(Temporal value);

	public abstract boolean isWithTime();

	public abstract boolean isNullable();

	public abstract String getEmptyLabelText();

	public abstract void setEmptyLabelText(String text);

	public abstract void addModifyListener(ModifyListener listener);

	public abstract void removeModifyListener(ModifyListener listener);

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	@Override
	public void modifyText(ModifyEvent e) {
		modifySupport.fire(e);
	}

}

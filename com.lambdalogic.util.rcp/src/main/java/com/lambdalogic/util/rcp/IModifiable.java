package com.lambdalogic.util.rcp;

import org.eclipse.swt.events.ModifyListener;

public interface IModifiable {

	void addModifyListener(ModifyListener modifyListener);

	void removeModifyListener(ModifyListener modifyListener);

}

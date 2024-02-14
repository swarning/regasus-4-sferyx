package com.lambdalogic.util.rcp.widget;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.contact.data.DoubleOptInStatus;

public class DoubleOptInStatusCombo extends AbstractComboComposite<DoubleOptInStatus> {

	public DoubleOptInStatusCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);

		setWithEmptyElement(false);
	}


	@Override
	protected Object getEmptyEntity() {
		return null;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				DoubleOptInStatus status = (DoubleOptInStatus) element;
				return status.getString();
			}
		};
	}


	@Override
	protected Collection<DoubleOptInStatus> getModelData() {
		return Arrays.asList(DoubleOptInStatus.values());
	}


	@Override
	protected void initModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected void disposeModel() {
		// do nothing because there is not model involved
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		// return null to keep the original order as in the enum
		return null;
	}


	public DoubleOptInStatus getDoubleOptInStatus() {
		return entity;
	}


	public void setDoubleOptInStatus(DoubleOptInStatus doubleOptInStatus) {
		setEntity(doubleOptInStatus);
	}

}

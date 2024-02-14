package de.regasus.onlineform.combo;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.regasus.UploadableFileType;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;


public class UploadableFileTypeCombo extends AbstractComboComposite<UploadableFileType> {

	public UploadableFileTypeCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
	}


	@Override
	protected void disposeModel() {
		// do nothing
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
				UploadableFileType type = (UploadableFileType) element;
				return type.getString();
			}
		};
	}


	@Override
	protected Collection<UploadableFileType> getModelData() throws Exception {
		return Arrays.asList(UploadableFileType.values());
	}


	@Override
	protected void initModel() {
	}


	public UploadableFileType getUploadableFileType() {
		return entity;
	}


	public void setUploadableFileType(UploadableFileType uploadableFileType) {
		setEntity(uploadableFileType);
	}

}

package de.regasus.event.customfield.editor;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.FormPosition;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

public class FormPositionCombo
extends AbstractComboComposite<FormPosition> {
	
	public FormPositionCombo(Composite parent, int style) throws Exception {
		super(parent, SWT.NONE);
		
		combo.setVisibleItemCount(20);
	}

	
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}

	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				FormPosition formPosition = (FormPosition) element;
				return formPosition.getString();
			}
		};
	}
	
	
	protected Collection<FormPosition> getModelData() {
		return Arrays.asList(FormPosition.values());
	}
	
	
	protected void initModel() {
		// Do nothing because data come from enum and are static
	}
	
	
	protected void disposeModel() {
		// Do nothing because data come from enum and are static
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		// Return null becaue positions are to be shown in natural order 
		return null;
	}
	
	
	public FormPosition getFormPosition() {
		return entity;
	}

	
	public void setFormPosition(FormPosition formPosition) {
		setEntity(formPosition);
	}
	
}

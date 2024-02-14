package de.regasus.event.combo;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.DigitalEventProvider;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

public class DigitalEventProviderCombo extends AbstractComboComposite<DigitalEventProvider>{

	public DigitalEventProviderCombo(Composite parent, int style) throws Exception {
		super(parent, style);
	}


	@Override
	protected void disposeModel() {
		// do nothing
	}


	@Override
	protected Object getEmptyEntity() {
		return EMPTY_ELEMENT;
	}


	@Override
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == EMPTY_ELEMENT) {
					return EMPTY_ELEMENT;
				}

				DigitalEventProvider digitalEventProvider = (DigitalEventProvider) element;
				return digitalEventProvider.getLabel();
			}
		};
	}


	@Override
	protected Collection<DigitalEventProvider> getModelData() throws Exception {
		return Arrays.asList( DigitalEventProvider.values() );
	}


	@Override
	protected void initModel() {
		// do nothing
	}


	public DigitalEventProvider getDigitalEventProvider() {
		return entity;
	}


	public void setDigitalEventProvider(DigitalEventProvider digitalEventProvider) {
		setEntity(digitalEventProvider);
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		return null;
	}

}

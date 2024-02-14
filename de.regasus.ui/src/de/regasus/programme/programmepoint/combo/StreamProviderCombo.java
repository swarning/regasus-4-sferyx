package de.regasus.programme.programmepoint.combo;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.messeinfo.participant.data.StreamProvider;
import com.lambdalogic.util.rcp.widget.AbstractComboComposite;

public class StreamProviderCombo extends AbstractComboComposite<StreamProvider>{

	public StreamProviderCombo(Composite parent, int style) throws Exception {
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

				StreamProvider streamProvider = (StreamProvider) element;
				return streamProvider.getLabel();
			}
		};
	}


	@Override
	protected Collection<StreamProvider> getModelData() throws Exception {
		return Arrays.asList( StreamProvider.values() );
	}


	@Override
	protected void initModel() {
		// do nothing
	}


	public StreamProvider getStreamProvider() {
		return entity;
	}


	public void setStreamProvider(StreamProvider streamProvider) {
		setEntity(streamProvider);
	}


	@Override
	protected ViewerSorter getViewerSorter() {
		return null;
	}

}

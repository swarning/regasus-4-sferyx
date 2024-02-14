package de.regasus.portal.page.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityScrolledComposite;

import de.regasus.portal.component.StreamComponent;


public class StreamComponentContentComposite extends EntityScrolledComposite<StreamComponent> {

	// widgets
	private StreamComponentTitleContentGroup titleContentGroup;
	private StreamComponentDescriptionContentGroup descriptionContentGroup;


	public StreamComponentContentComposite(Composite parent, int style)
	throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		final int COL_COUNT = 1;
		parent.setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory groupGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		// Title Column Content
		titleContentGroup = new StreamComponentTitleContentGroup(parent, SWT.NONE);
		groupGridDataFactory.applyTo(titleContentGroup);
		titleContentGroup.addModifyListener(modifySupport);

		// Description Column Content
		descriptionContentGroup = new StreamComponentDescriptionContentGroup(parent, SWT.NONE);
		groupGridDataFactory.applyTo(descriptionContentGroup);
		descriptionContentGroup.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		titleContentGroup.setEntity(entity);
		descriptionContentGroup.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		titleContentGroup.syncEntityToWidgets();
		descriptionContentGroup.syncEntityToWidgets();
	}

}

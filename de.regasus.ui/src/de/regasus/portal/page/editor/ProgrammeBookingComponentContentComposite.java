package de.regasus.portal.page.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityScrolledComposite;

import de.regasus.portal.component.ProgrammeBookingComponent;


public class ProgrammeBookingComponentContentComposite extends EntityScrolledComposite<ProgrammeBookingComponent> {

	// widgets
	private ProgrammeBookingComponentProgrammeContentGroup programmeContentGroup;
	private ProgrammeBookingComponentDetailContentGroup detailContentGroup;


	public ProgrammeBookingComponentContentComposite(Composite parent, int style)
	throws Exception {
		super(parent, style);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		final int COL_COUNT = 1;
		parent.setLayout( new GridLayout(COL_COUNT, false) );

		GridDataFactory groupGridDataFactory = GridDataFactory.fillDefaults().grab(true, false);

		// ProgrammeColumnContent
		programmeContentGroup = new ProgrammeBookingComponentProgrammeContentGroup(parent, SWT.NONE);
		groupGridDataFactory.applyTo(programmeContentGroup);
		programmeContentGroup.addModifyListener(modifySupport);

		// DetailColumnContent
		detailContentGroup = new ProgrammeBookingComponentDetailContentGroup(parent, SWT.NONE);
		groupGridDataFactory.applyTo(detailContentGroup);
		detailContentGroup.addModifyListener(modifySupport);
	}


	@Override
	protected void syncWidgetsToEntity() {
		programmeContentGroup.setEntity(entity);
		detailContentGroup.setEntity(entity);
	}


	@Override
	public void syncEntityToWidgets() {
		programmeContentGroup.syncEntityToWidgets();
		detailContentGroup.syncEntityToWidgets();
	}

}

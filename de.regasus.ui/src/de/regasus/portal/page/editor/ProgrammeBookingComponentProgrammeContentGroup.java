package de.regasus.portal.page.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.portal.component.ProgrammeBookingComponentProgrammeContent;


public class ProgrammeBookingComponentProgrammeContentGroup extends EntityGroup<ProgrammeBookingComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// widgets
	private List<Button> buttonList;


	public ProgrammeBookingComponentProgrammeContentGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText( ProgrammeBookingComponent.FIELD_PROGRAMME_COLUMN_CONTENT.getString() );
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout() );

		buttonList = new ArrayList<>( ProgrammeBookingComponentProgrammeContent.values().length );
		for (ProgrammeBookingComponentProgrammeContent programmeColumnContent : ProgrammeBookingComponentProgrammeContent.values()) {
			Button button = widgetBuilder.createRadio( programmeColumnContent.getString() );
			buttonList.add(button);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		ProgrammeBookingComponentProgrammeContent programmeColumnContent = entity.getProgrammeColumnContent();
		int ordinal = -1;
		if (programmeColumnContent != null) {
			ordinal = programmeColumnContent.ordinal();
		}

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).setSelection(i == ordinal);
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setProgrammeColumnContent( getProgrammeColumnContent() );
		}
	}


	private ProgrammeBookingComponentProgrammeContent getProgrammeColumnContent() {
		for (int i = 0; i < buttonList.size(); i++) {
			if ( buttonList.get(i).getSelection() ) {
				return ProgrammeBookingComponentProgrammeContent.values()[i];
			}
		}
		return null;
	}

}

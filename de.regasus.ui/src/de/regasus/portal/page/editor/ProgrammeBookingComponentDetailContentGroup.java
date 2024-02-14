package de.regasus.portal.page.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.portal.component.ProgrammeBookingComponentDetailContent;


public class ProgrammeBookingComponentDetailContentGroup extends EntityGroup<ProgrammeBookingComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// widgets
	private List<Button> buttonList;


	public ProgrammeBookingComponentDetailContentGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText( ProgrammeBookingComponent.FIELD_DETAIL_COLUMN_CONTENT.getString() );
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout() );

		buttonList = new ArrayList<>( ProgrammeBookingComponentDetailContent.values().length );
		for (ProgrammeBookingComponentDetailContent detailContent : ProgrammeBookingComponentDetailContent.values()) {
			Button button = widgetBuilder.createRadio( detailContent.getString() );
			buttonList.add(button);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		ProgrammeBookingComponentDetailContent detailColumnContent = entity.getDetailColumnContent();
		int ordinal = -1;
		if (detailColumnContent != null) {
			ordinal = detailColumnContent.ordinal();
		}

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).setSelection(i == ordinal);
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setDetailColumnContent( getDetailColumnContent() );
		}
	}


	private ProgrammeBookingComponentDetailContent getDetailColumnContent() {
		for (int i = 0; i < buttonList.size(); i++) {
			if ( buttonList.get(i).getSelection() ) {
				return ProgrammeBookingComponentDetailContent.values()[i];
			}
		}
		return null;
	}

}

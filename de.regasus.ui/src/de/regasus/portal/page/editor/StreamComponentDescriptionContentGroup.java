package de.regasus.portal.page.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.component.StreamComponent;
import de.regasus.portal.component.StreamComponentDescriptionContent;


public class StreamComponentDescriptionContentGroup extends EntityGroup<StreamComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// widgets
	private List<Button> buttonList;


	public StreamComponentDescriptionContentGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText( StreamComponent.FIELD_DESCRIPTION_CONTENT.getString() );
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout() );

		buttonList = new ArrayList<>( StreamComponentDescriptionContent.values().length );
		for (StreamComponentDescriptionContent descriptionContent : StreamComponentDescriptionContent.values()) {
			Button button = widgetBuilder.createRadio( descriptionContent.getString() );
			buttonList.add(button);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		StreamComponentDescriptionContent descriptionContent = entity.getDescriptionContent();
		int ordinal = -1;
		if (descriptionContent != null) {
			ordinal = descriptionContent.ordinal();
		}

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).setSelection(i == ordinal);
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setDescriptionContent( getDescriptionContent() );
		}
	}


	private StreamComponentDescriptionContent getDescriptionContent() {
		for (int i = 0; i < buttonList.size(); i++) {
			if ( buttonList.get(i).getSelection() ) {
				return StreamComponentDescriptionContent.values()[i];
			}
		}
		return null;
	}

}

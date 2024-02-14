package de.regasus.portal.page.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;

import de.regasus.portal.component.StreamComponent;
import de.regasus.portal.component.StreamComponentTitleContent;


public class StreamComponentTitleContentGroup extends EntityGroup<StreamComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	// widgets
	private List<Button> buttonList;


	public StreamComponentTitleContentGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText( StreamComponent.FIELD_TITLE_CONTENT.getString() );
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout() );

		buttonList = new ArrayList<>( StreamComponentTitleContent.values().length );
		for (StreamComponentTitleContent titleContent : StreamComponentTitleContent.values()) {
			Button button = widgetBuilder.createRadio( titleContent.getString() );
			buttonList.add(button);
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		StreamComponentTitleContent titleContent = entity.getTitleContent();
		int ordinal = -1;
		if (titleContent != null) {
			ordinal = titleContent.ordinal();
		}

		for (int i = 0; i < buttonList.size(); i++) {
			buttonList.get(i).setSelection(i == ordinal);
		}
	}


	@Override
	public void syncEntityToWidgets() {
		if (entity != null) {
			entity.setTitleContent( getTitleContent() );
		}
	}


	private StreamComponentTitleContent getTitleContent() {
		for (int i = 0; i < buttonList.size(); i++) {
			if ( buttonList.get(i).getSelection() ) {
				return StreamComponentTitleContent.values()[i];
			}
		}
		return null;
	}

}

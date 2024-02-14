package de.regasus.portal.page.editor;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.EntityGroup;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.portal.component.StreamComponent;


public class StreamComponentDefaultSettingsGroup extends EntityGroup<StreamComponent> {

	/*
	 * Do not initialize local non-static fields here but in initialize(Object[])!
	 * this.createWidgets() is called by super constructor and therefore run before local fields are initialized.
	 */

	public StreamComponentDefaultSettingsGroup(Composite parent, int style)
	throws Exception {
		super(parent, style);

		setText(UtilI18N.DefaultValues);
	}


	@Override
	protected void createWidgets(Composite parent) throws Exception {
		setLayout( new GridLayout() );

		widgetBuilder.fieldMetadata(StreamComponent.FIELD_OMIT_LOCKED_DEFAULT).createCheckbox();
		widgetBuilder.fieldMetadata(StreamComponent.FIELD_OMIT_UNAVAILABLE_NOW_DEFAULT).createCheckbox();
	}

}

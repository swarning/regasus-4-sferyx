package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;
import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.FieldComponent;
import de.regasus.portal.component.ProfileFieldComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;

public class CreateProfileFieldComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateFieldComponent;


	public CreateProfileFieldComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		FieldComponent component = ProfileFieldComponent.build();
		return component;
	}

}

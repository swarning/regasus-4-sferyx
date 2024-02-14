package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.hotel.EditBookingComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;

public class CreateEditBookingComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateEditBookingComponent;


	public CreateEditBookingComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		EditBookingComponent component = EditBookingComponent.build( getLanguageList() );
		return component;
	}

}

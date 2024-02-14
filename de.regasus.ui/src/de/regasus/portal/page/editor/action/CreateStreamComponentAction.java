package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;
import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.StreamComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;


public class CreateStreamComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateStreamComponent;


	public CreateStreamComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		StreamComponent component = StreamComponent.build( getLanguageList() );
		return component;
	}

}

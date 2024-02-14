package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.hotelspeaker.SpeakerArrivalDepartureComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;

public class CreateSpeakerArrivalDepartureComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateSpeakerArrivalDepartureComponent;


	public CreateSpeakerArrivalDepartureComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		SpeakerArrivalDepartureComponent component = SpeakerArrivalDepartureComponent.build( getLanguageList() );
		return component;
	}

}

package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import com.lambdalogic.messeinfo.participant.data.EventCVO;

import de.regasus.I18N;
import de.regasus.event.EventModel;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.ProgrammeBookingComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;


public class CreateProgrammeBookingComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateProgrammeBookingComponent;


	public CreateProgrammeBookingComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		ProgrammeBookingComponent component = ProgrammeBookingComponent.build( getLanguageList() );

		// init with default currency from Event
		try {
			Long eventId = getPortal().getEventId();
			EventCVO eventCVO = EventModel.getInstance().getEventCVO(eventId);
			String defaultCurrency = eventCVO.getDefaultCurrency();
			component.setCurrency(defaultCurrency);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return component;
	}

}

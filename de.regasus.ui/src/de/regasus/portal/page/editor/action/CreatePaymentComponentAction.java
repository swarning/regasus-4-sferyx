package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.lambdalogic.messeinfo.participant.data.EventCVO;

import de.regasus.I18N;
import de.regasus.event.EventModel;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.PaymentComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.type.standard.group.StandardGroupPortalPageConfig;
import de.regasus.portal.type.standard.registration.StandardRegistrationPortalPageConfig;

public class CreatePaymentComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreatePaymentComponent;


	public CreatePaymentComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		PaymentComponent component = PaymentComponent.build( getLanguageList() );

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


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled =
			   !pageContentTreeComposite.getPage().isStaticAccess()
			&& pageContentTreeComposite.getSelectedItem() != null

			// PaymentComponent must only appear on summary pages or on group member overview page
			&& (pageContentTreeComposite.getPage().getKey().equals( StandardRegistrationPortalPageConfig.SUMMARY_PAGE.getKey() )
				||
				pageContentTreeComposite.getPage().getKey().equals( StandardGroupPortalPageConfig.GROUP_MEMBERS_OVERVIEW_PAGE.getKey()));

		setEnabled(enabled);
	}

}

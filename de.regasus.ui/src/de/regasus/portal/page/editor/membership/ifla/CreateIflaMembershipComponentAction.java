package de.regasus.portal.page.editor.membership.ifla;

import java.lang.invoke.MethodHandles;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.portal.component.Component;
import de.regasus.portal.component.membership.ifla.IflaMembershipComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.page.editor.action.AbstractCreateComponentAction;

public class CreateIflaMembershipComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = "Add IFLA Membership Component";


	public CreateIflaMembershipComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		IflaMembershipComponent component = IflaMembershipComponent.build();
		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}

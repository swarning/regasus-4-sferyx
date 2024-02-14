package de.regasus.portal.page.editor.membership.enets;

import java.lang.invoke.MethodHandles;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.portal.component.Component;
import de.regasus.portal.component.membership.enets.EnetsMembershipComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.page.editor.action.AbstractCreateComponentAction;

public class CreateEnetsMembershipComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = "Add ENETS Membership Component";


	public CreateEnetsMembershipComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		EnetsMembershipComponent component = EnetsMembershipComponent.build();
		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}

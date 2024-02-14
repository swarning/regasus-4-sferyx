package de.regasus.portal.page.editor.membership.neurosciences;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.portal.component.Component;
import de.regasus.portal.component.membership.neurosciences.NeurosciencesMembershipComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.page.editor.action.AbstractCreateComponentAction;

public class CreateNeurosciencesMembershipComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = "Add Neurosciences Membership Component";


	public CreateNeurosciencesMembershipComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		NeurosciencesMembershipComponent component = NeurosciencesMembershipComponent.build();
		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}

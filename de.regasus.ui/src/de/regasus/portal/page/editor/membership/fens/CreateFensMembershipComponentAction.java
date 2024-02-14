package de.regasus.portal.page.editor.membership.fens;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.portal.component.Component;
import de.regasus.portal.component.membership.fens.FensMembershipComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.page.editor.action.AbstractCreateComponentAction;

public class CreateFensMembershipComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = "Add FENS Membership Component";


	public CreateFensMembershipComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		FensMembershipComponent component = FensMembershipComponent.build();
		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}

package de.regasus.portal.page.editor.membership.ecfs;

import java.lang.invoke.MethodHandles;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.portal.component.Component;
import de.regasus.portal.component.membership.ecfs.EcfsMembershipComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.page.editor.action.AbstractCreateComponentAction;

public class CreateEcfsMembershipComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = "Add ECFS Membership Component";


	public CreateEcfsMembershipComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		EcfsMembershipComponent component = EcfsMembershipComponent.build();
		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}

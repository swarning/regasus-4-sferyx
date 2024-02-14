package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.SendLetterOfInvitationComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;

public class CreateSendLetterOfInvitationComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateSendLetterOfInvitationComponent;


	public CreateSendLetterOfInvitationComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		SendLetterOfInvitationComponent component = SendLetterOfInvitationComponent.build( getLanguageList());
		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}

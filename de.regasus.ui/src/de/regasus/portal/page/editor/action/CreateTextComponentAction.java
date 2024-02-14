package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.I18N;
import de.regasus.portal.component.Component;
import de.regasus.portal.component.TextComponent;
import de.regasus.portal.page.editor.PageContentTreeComposite;

public class CreateTextComponentAction extends AbstractCreateComponentAction {

	private static final String ID = MethodHandles.lookup().lookupClass().getName();
	private static final String TEXT = I18N.PageEditor_CreateTextComponent;


	public CreateTextComponentAction(PageContentTreeComposite pageContentTreeComposite) {
		super(ID, TEXT, pageContentTreeComposite);
	}


	@Override
	protected Component buildComponent() {
		TextComponent component = TextComponent.build();
		return component;
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}

}

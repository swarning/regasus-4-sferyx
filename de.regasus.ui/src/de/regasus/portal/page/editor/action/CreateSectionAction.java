package de.regasus.portal.page.editor.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.IdProvider;
import de.regasus.portal.Page;
import de.regasus.portal.Section;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.page.editor.PageHelper;
import de.regasus.ui.Activator;

public class CreateSectionAction extends Action implements ISelectionChangedListener {

	private PageContentTreeComposite pageContentTreeComposite;


	public CreateSectionAction(PageContentTreeComposite pageContentTreeComposite) {
		this.pageContentTreeComposite = pageContentTreeComposite;

		pageContentTreeComposite.addSelectionChangedListener(this);
	}


	@Override
	public String getId() {
		return CreateSectionAction.class.getName();
	}


	@Override
	public String getText() {
		return I18N.PageEditor_CreateSection;
	}


	@Override
	public String getToolTipText() {
		// TODO
		return super.getToolTipText();
	}


	@Override
	public void run() {
		try {
			Page page = pageContentTreeComposite.getPage();

			// determine current selection
			String selectedId = null;
			IdProvider selectedItem = pageContentTreeComposite.getSelectedItem();
			if (selectedItem != null) {
				selectedId = selectedItem.getId().toString();
			}

			Section newSection = Section.build();
			PageHelper.addSection(page, selectedId, newSection);

			pageContentTreeComposite.handleNewItem(newSection);
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = !pageContentTreeComposite.getPage().getKey().equals("LoginPage");
		setEnabled(enabled);
	}

}

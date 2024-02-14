package de.regasus.portal.page.editor.action;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.IdProvider;
import de.regasus.portal.Page;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalModel;
import de.regasus.portal.component.Component;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.portal.page.editor.PageHelper;
import de.regasus.ui.Activator;

public abstract class AbstractCreateComponentAction extends Action implements ISelectionChangedListener {

	private String id;
	private String text;
	protected PageContentTreeComposite pageContentTreeComposite;

	protected Portal portal;


	protected AbstractCreateComponentAction(String id, String text, PageContentTreeComposite pageContentTreeComposite) {
		this.id = id;
		this.text = text;
		this.pageContentTreeComposite = pageContentTreeComposite;

		pageContentTreeComposite.addSelectionChangedListener(this);
	}


	@Override
	public String getId() {
		return id;
	}


	@Override
	public String getText() {
		return text;
	}


	protected abstract Component buildComponent();


	@Override
	public void run() {
		try {
			// determine current selection
			IdProvider selectedItem = pageContentTreeComposite.getSelectedItem();
			if (selectedItem != null) {
				String selectedId = selectedItem.getId().toString();

				Page page = pageContentTreeComposite.getPage();

				// determine languages
				Long portalId = page.getPortalId();
				portal = PortalModel.getInstance().getPortal(portalId);

				Component newComponent = buildComponent();

				PageHelper.addComponent(page, selectedId, newComponent);

				pageContentTreeComposite.handleNewItem(newComponent);
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled =
			   !pageContentTreeComposite.getPage().isStaticAccess()
			&& pageContentTreeComposite.getSelectedItem() != null;

		setEnabled(enabled);
	}


	protected Portal getPortal() {
		return portal;
	}


	protected List<String> getLanguageList() {
		return getPortal().getLanguageList();
	}

}

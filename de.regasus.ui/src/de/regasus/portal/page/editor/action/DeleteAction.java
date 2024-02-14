package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IconRegistry;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.ui.Activator;

public class DeleteAction extends Action implements ISelectionChangedListener {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private PageContentTreeComposite pageContentTreeComposite;


	public DeleteAction(PageContentTreeComposite pageContentTreeComposite) {
		this.pageContentTreeComposite = pageContentTreeComposite;

		pageContentTreeComposite.addSelectionChangedListener(this);
	}


	@Override
	public String getId() {
		return DeleteAction.class.getName();
	}


	@Override
	public String getText() {
		return UtilI18N.Delete;
	}


	@Override
	public String getToolTipText() {
		// TODO
		return super.getToolTipText();
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return IconRegistry.getImageDescriptor("icons/delete.png");
	}


	@Override
	public int getAccelerator() {
		// not working, because this shortcut seems to be in use
		return SWT.DEL;
	}


	@Override
	public void run() {
		log.debug("run()");
		try {
			pageContentTreeComposite.removeItem();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled =  pageContentTreeComposite.getSelectedItem() != null 
						&& pageContentTreeComposite.isSelectedItemDeleteable();
		setEnabled(enabled);
	}

}

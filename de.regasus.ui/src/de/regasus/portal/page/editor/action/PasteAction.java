package de.regasus.portal.page.editor.action;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.page.editor.PageContentTreeComposite;
import de.regasus.ui.Activator;

public class PasteAction extends Action implements ISelectionChangedListener {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private PageContentTreeComposite pageContentTreeComposite;


	public PasteAction(PageContentTreeComposite pageContentTreeComposite) {
		this.pageContentTreeComposite = pageContentTreeComposite;

		pageContentTreeComposite.addSelectionChangedListener(this);
	}


	@Override
	public String getId() {
		return PasteAction.class.getName();
	}


	@Override
	public String getText() {
		return UtilI18N.Paste;
	}


	@Override
	public String getToolTipText() {
		// TODO
		return super.getToolTipText();
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE);
	}


	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED);
	}


	@Override
	public int getAccelerator() {
		// not working, because this shortcut seems to be in use
		return SWT.MOD1 | 'V' ;
	}


	@Override
	public void run() {
		log.debug("run()");
		try {
			pageContentTreeComposite.pasteFromClipboad();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		boolean enabled = !pageContentTreeComposite.getPage().isStaticAccess() &&
        		  		  pageContentTreeComposite.getSelectedItem() != null;
		setEnabled(enabled);
	}

}

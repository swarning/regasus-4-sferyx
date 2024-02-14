package de.regasus.core.ui;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditorSelectionProvider implements IPartListener {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final ISelectionProvider NULL_SELECTION_PROVIDER = new NullSelectionProvider();



	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			try {
				log.debug("Setting NullSelectionProvider to " + PartTracker.buildPartLabel(part));
				part.getSite().setSelectionProvider(NULL_SELECTION_PROVIDER);
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	}


	@Override
	public void partClosed(IWorkbenchPart part) {
	}


	@Override
	public void partActivated(IWorkbenchPart part) {
	}


	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}


	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

}

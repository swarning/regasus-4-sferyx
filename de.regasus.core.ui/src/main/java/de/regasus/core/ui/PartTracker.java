package de.regasus.core.ui;

import java.lang.invoke.MethodHandles;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartTracker implements IPartListener {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );


	@Override
	public void partActivated(IWorkbenchPart part) {
//		log.debug("Part activated: " + buildPartLabel(part));
	}


	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
//		log.debug("Part brought to top: " + buildPartLabel(part));
	}


	@Override
	public void partClosed(IWorkbenchPart part) {
//		log.debug("Part closed: " + buildPartLabel(part));
	}


	@Override
	public void partDeactivated(IWorkbenchPart part) {
//		log.debug("Part deactivated: " + buildPartLabel(part));
	}


	@Override
	public void partOpened(IWorkbenchPart part) {
//		log.debug("Part opened: " + buildPartLabel(part));
	}


	public static String buildPartLabel(IWorkbenchPart part) {
		return new StringBuilder(128)
			.append( part.getClass().getSimpleName() )
			.append(" '")
			.append( part.getTitle() )
			.append("'")
			.toString();
	}

}

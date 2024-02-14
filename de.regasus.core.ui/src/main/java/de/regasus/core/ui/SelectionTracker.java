package de.regasus.core.ui;

import java.lang.invoke.MethodHandles;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectionTracker implements ISelectionListener, INullSelectionListener {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
//		if (selection instanceof IStructuredSelection) {
//			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
//			List<?> selectedEements = structuredSelection.toList();
//			log.debug("IStructuredSelection: " + selectedEements.toString() + ", Part: " + PartTracker.buildPartLabel(part));
//		}
//		else if (selection instanceof ITextSelection) {
//			ITextSelection textSelection = (ITextSelection) selection;
//			log.debug("ITextSelection: " + textSelection.getText() + ", Part: " + PartTracker.buildPartLabel(part));
//		}
//		else if (selection instanceof IMarkSelection) {
//			IMarkSelection markSelection = (IMarkSelection) selection;
//			log.debug("ITextSelection: " + markSelection.toString() + ", Part: " + PartTracker.buildPartLabel(part));
//		}
//		else if (selection == null) {
//			log.debug("selection == null" + ", Part: " + PartTracker.buildPartLabel(part));
//		}
//		else {
//			log.debug("Unexpected selection: " + selection + ", Part: " + PartTracker.buildPartLabel(part));
//		}
	}

}

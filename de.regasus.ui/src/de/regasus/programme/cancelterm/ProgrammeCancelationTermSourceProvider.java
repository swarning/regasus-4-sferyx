package de.regasus.programme.cancelterm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.lambdalogic.messeinfo.participant.data.ProgrammeCancelationTermVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.event.view.ProgrammeCancelationTermTreeNode;
import de.regasus.programme.ProgrammeCancelationTermModel;

public class ProgrammeCancelationTermSourceProvider extends AbstractSourceProvider {

	// name of the variable programmeCancelationTerm.selection ...
	private final static String SELECTION_SOURCE = "programmeCancelationTerm.selection";
	// ... and its values
	private final static String SELECTION_VALUE_NONE = "none";
	private final static String SELECTION_VALUE_ONE = "one";
	private final static String SELECTION_VALUE_MANY = "many";

	// name of the variable programmeCancelationTerm.cancelled ...
	private final static String CANCELLED_SOURCE = "programmeCancelationTerm.cancelled";
	// ... and its values
	private static final String CANCELLED_VALUE_ALL = "all";
	private static final String CANCELLED_VALUE_SOME = "some";
	private static final String CANCELLED_VALUE_NONE = "none";


	// source variables
	private String selectionState = SELECTION_VALUE_NONE;
	private String cancelledState = CANCELLED_VALUE_NONE;


	// selected Programme Cancelation Terms
	private List<ProgrammeCancelationTermVO> programmeCancelationTermList = new ArrayList<>();


	/**
	 * Safe the window because we cannot get it in dispose() with
	 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().
	 */
	private IWorkbenchWindow window;


	private boolean verbose = false;


	public ProgrammeCancelationTermSourceProvider() {

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			init(window);
		}
		else {
			PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

				@Override
				public void windowOpened(IWorkbenchWindow window) {
					init(window);
					PlatformUI.getWorkbench().removeWindowListener(this);
				}

				@Override
				public void windowDeactivated(IWorkbenchWindow arg0) {
				}

				@Override
				public void windowClosed(IWorkbenchWindow arg0) {
				}

				@Override
				public void windowActivated(IWorkbenchWindow arg0) {
				}
			});
		}
	}


	private void init(IWorkbenchWindow window) {
		this.window = window;

		window.getSelectionService().addSelectionListener(selectionListener);

		ProgrammeCancelationTermModel.getInstance().addListener(modelListener);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(selectionListener);

		ProgrammeCancelationTermModel.getInstance().removeListener(modelListener);
	}


	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {
			SELECTION_SOURCE,
			CANCELLED_SOURCE
		};
	}


	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> currentState = new HashMap<>(4);
		currentState.put(SELECTION_SOURCE, selectionState);
		currentState.put(CANCELLED_SOURCE, cancelledState);
		return currentState;
	}


	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
			try {
    			printIfVerbose("ProgrammeCancelationTermSourceProvider.selectionChanged(IWorkbenchPart part, ISelection incoming)");
    			printIfVerbose("	IWorkbenchPart: " +  part);
    			printIfVerbose("	ISelection: " +  incoming);

    			programmeCancelationTermList.clear();

    			if (incoming instanceof IStructuredSelection) {
    				IStructuredSelection selection = (IStructuredSelection) incoming;
    				printIfVerbose("	ISelection is IStructuredSelection, size: " + selection.size());
    				int i = 0;
    				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
    					Object selectedElement = it.next();
    					if (selectedElement instanceof ProgrammeCancelationTermVO) {
    						ProgrammeCancelationTermVO programmeCancelationTermVO = (ProgrammeCancelationTermVO) selectedElement;
    						printIfVerbose("	" + (i++) + " is ProgrammeCancelationTermVO: " + programmeCancelationTermVO.getID());
    						programmeCancelationTermList.add(programmeCancelationTermVO);
    					}
    					else if (selectedElement instanceof ProgrammeCancelationTermTreeNode) {
    						ProgrammeCancelationTermTreeNode programmeCancelationTermTreeNode = (ProgrammeCancelationTermTreeNode) selectedElement;
    						ProgrammeCancelationTermVO programmeCancelationTermVO = programmeCancelationTermTreeNode.getValue();
    						printIfVerbose("	" + (i++) + " is ProgrammeCancelationTermTreeNode: " + programmeCancelationTermVO.getID());
    						programmeCancelationTermList.add(programmeCancelationTermVO);
    					}
    					else {
    						printIfVerbose("	" + (i++) + " is no ProgrammeCancelationTermVO but " + selectedElement.getClass().getName() + ", programmeCancelationTermList.clear(), break");
    						programmeCancelationTermList.clear();
    						break;
    					}
    				}
    			}

    			refreshVariables();
			}
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
		}
	};


	private CacheModelListener<Long> modelListener = new CacheModelListener<Long>() {
    	@Override
    	public void dataChange(CacheModelEvent<Long> event) {
    		if (   event.getOperation() == CacheModelOperation.UPDATE
    			|| event.getOperation() == CacheModelOperation.REFRESH
    		) {
    			try {
    				// replace updated ProgrammePointVOs with new versions from model
    				boolean change = false;
    				List<Long> updatedProgrammeCancelationTermPKs = event.getKeyList();

    				for (ListIterator<ProgrammeCancelationTermVO> li = programmeCancelationTermList.listIterator(); li.hasNext();) {
						Long pctPK = li.next().getID();
						if ( updatedProgrammeCancelationTermPKs.contains(pctPK) ) {
							// replace ProgrammeCancelationTermVO with new version from model
							ProgrammeCancelationTermVO poVO = ProgrammeCancelationTermModel.getInstance().getProgrammeCancelationTermVO(pctPK);
							li.set(poVO);
							change = true;
						}
					}

    				if (change) {
    					refreshVariables();
    				}
    			}
    			catch (Exception e) {
    				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
    			}
    		}
    	}
	};


	private void refreshVariables() throws Exception {
		printIfVerbose("ProgrammeCancelationTermSourceProvider.refreshVariables()");

		int selectionCount = programmeCancelationTermList.size();
		int cancelledCount = 0;

		for (ProgrammeCancelationTermVO programmeCancelationTermVO : programmeCancelationTermList) {
			if ( programmeCancelationTermVO.isCancelled() ) {
				cancelledCount++;
			}
		}

		printIfVerbose("	--- counts ---");
		printIfVerbose("	selectionCount:  " + selectionCount);
		printIfVerbose("	cancelledCount:   " + cancelledCount);

		String oldSelectionState = selectionState;
		String oldCancelledState = cancelledState;

		// set selectionState
		if (selectionCount == 0) {
			selectionState = SELECTION_VALUE_NONE;
		}
		else if (selectionCount == 1) {
			selectionState = SELECTION_VALUE_ONE;
		}
		else {
			selectionState = SELECTION_VALUE_MANY;
		}

		// set cancelledState
		if (cancelledCount == 0) {
			cancelledState = CANCELLED_VALUE_NONE;
		}
		else if (cancelledCount == selectionCount) {
			cancelledState = CANCELLED_VALUE_ALL;
		}
		else {
			cancelledState = CANCELLED_VALUE_SOME;
		}


		if (   selectionState != oldSelectionState
			|| cancelledState != oldCancelledState
		) {

			Map<String, String> currentState = new HashMap<>(3);

			printIfVerbose("	--- states changed ---");

			if (selectionState != oldSelectionState) {
				currentState.put(SELECTION_SOURCE, selectionState);
				printIfVerbose("	selectionState:  " + selectionState);
			}

			if (cancelledState != oldCancelledState) {
				currentState.put(CANCELLED_SOURCE, cancelledState);
				printIfVerbose("	cancelledState:   " + cancelledState);
			}

			printIfVerbose("fireSourceChanged(ISources.WORKBENCH, currentState): " + currentState);
			fireSourceChanged(ISources.WORKBENCH, currentState);
		}
		else {
			printIfVerbose("	--- states not changed ---");
		}

		// Please don't do this, since output that at each kind of event makes that
		// i cannot copy and paste anything in the applications console view
		// printIfVerbose("participantSelectionState=" + participantSelectionState);
	}


	private void printIfVerbose(String string) {
		if (verbose) {
			System.out.println(string);
		}
	}

}

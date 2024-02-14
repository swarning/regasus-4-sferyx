package de.regasus.event;

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

import com.lambdalogic.messeinfo.participant.data.EventCVO;
import com.lambdalogic.messeinfo.participant.data.EventVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.event.view.EventGroupTreeNode;
import de.regasus.event.view.EventTreeNode;

public class EventSourceProvider extends AbstractSourceProvider {

	// name of the variable event.selection ...
	private final static String SELECTION_SOURCE = "event.selection";
	// ... and its values
	private final static String SELECTION_VALUE_NONE = "none";
	private final static String SELECTION_VALUE_ONE = "one";
	private final static String SELECTION_VALUE_MANY = "many";

	// name of the variable event.closed ...
	private final static String CLOSED_SOURCE = "event.closed";
	// ... and its values
	private static final String CLOSED_VALUE_ALL = "all";
	private static final String CLOSED_VALUE_SOME = "some";
	private static final String CLOSED_VALUE_NONE = "none";


	// source variables
	private String selectionState = SELECTION_VALUE_NONE;
	private String closedState = CLOSED_VALUE_NONE;


	// selected Events
	private List<EventVO> eventList = new ArrayList<>();


	/**
	 * Safe the window because we cannot get it in dispose() with
	 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().
	 */
	private IWorkbenchWindow window;


	private boolean verbose = false;


	public EventSourceProvider() {

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

		EventModel.getInstance().addListener(modelListener);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(selectionListener);

		EventModel.getInstance().removeListener(modelListener);
	}


	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {
			SELECTION_SOURCE,
			CLOSED_SOURCE
		};
	}


	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> currentState = new HashMap<>(4);
		currentState.put(SELECTION_SOURCE, selectionState);
		currentState.put(CLOSED_SOURCE, closedState);
		return currentState;
	}


	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
			try {
    			printIfVerbose("EventSourceProvider.selectionChanged(IWorkbenchPart part, ISelection incoming)");
    			printIfVerbose("	IWorkbenchPart: " +  part);
    			printIfVerbose("	ISelection: " +  incoming);

    			eventList.clear();

    			if (incoming instanceof IStructuredSelection) {
    				IStructuredSelection selection = (IStructuredSelection) incoming;
    				printIfVerbose("	ISelection is IStructuredSelection, size: " + selection.size());
    				int i = 0;
    				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
    					Object selectedElement = it.next();
    					if (selectedElement instanceof EventVO) {
    						EventVO eventVO = (EventVO) selectedElement;
    						printIfVerbose("	" + (i++) + " is EventVO, name: " + eventVO.getName());
    						eventList.add(eventVO);
    					}
    					else if (selectedElement instanceof EventCVO) {
    						EventCVO eventCVO = (EventCVO) selectedElement;
    						printIfVerbose("	" + (i++) + " is EventCVO, name: " + eventCVO.getName());
    						eventList.add( eventCVO.getVO() );
    					}
    					else if (selectedElement instanceof EventTreeNode) {
    						EventTreeNode eventTreeNode = (EventTreeNode) selectedElement;
    						EventVO eventVO = eventTreeNode.getValue();
    						printIfVerbose("	" + (i++) + " is EventTreeNode, name: " + eventVO.getName());
    						eventList.add(eventVO);
    					}
    					else if (selectedElement instanceof EventGroupTreeNode) {
    						eventList.addAll( EventModel.getInstance().getAllEventVOs() );
    						printIfVerbose("	" + (i++) + " is EventGroupTreeNode");
    					}
    					else {
    						printIfVerbose("	" + (i++) + " is no EventVO and EventCVO but " + selectedElement.getClass().getName() + ", eventList.clear(), break");
    						eventList.clear();
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
    				// replace updated EventVOs with new versions from model
    				boolean change = false;
    				List<Long> updatedEventPKs = event.getKeyList();

    				for (ListIterator<EventVO> li = eventList.listIterator(); li.hasNext();) {
						Long eventPK = li.next().getID();
						if ( updatedEventPKs.contains(eventPK) ) {
							// replace EventVO with new version from model
							EventVO poVO = EventModel.getInstance().getEventVO(eventPK);
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
		printIfVerbose("EventSourceProvider.refreshVariables()");

		int selectionCount = 0;
		int closedCount = 0;

		if (eventList != null) {
			for (EventVO eventVO : eventList) {
				if (eventVO != null && eventVO.getPK() != null) {
					selectionCount++;

					if ( eventVO.isClosed() ) {
						closedCount++;
					}
				}
			}
		}

		printIfVerbose("	--- counts ---");
		printIfVerbose("	selectionCount:  " + selectionCount);
		printIfVerbose("	closedCount:     " + closedCount);

		String oldSelectionState = selectionState;
		String oldCancelledState = closedState;

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

		// set closedState
		if (closedCount == 0) {
			closedState = CLOSED_VALUE_NONE;
		}
		else if (closedCount == selectionCount) {
			closedState = CLOSED_VALUE_ALL;
		}
		else {
			closedState = CLOSED_VALUE_SOME;
		}


		if (   selectionState != oldSelectionState
			|| closedState != oldCancelledState
		) {

			Map<String, String> currentState = new HashMap<>(3);

			printIfVerbose("	--- states changed ---");

			if (selectionState != oldSelectionState) {
				currentState.put(SELECTION_SOURCE, selectionState);
				printIfVerbose("	selectionState:  " + selectionState);
			}

			if (closedState != oldCancelledState) {
				currentState.put(CLOSED_SOURCE, closedState);
				printIfVerbose("	closedState:     " + closedState);
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

package de.regasus.programme.programmepoint;

import java.lang.invoke.MethodHandles;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointCVO;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;

import de.regasus.event.view.ProgrammePointListTreeNode;
import de.regasus.event.view.ProgrammePointTreeNode;
import de.regasus.programme.ProgrammePointModel;

public class ProgrammePointSourceProvider extends AbstractSourceProvider {

	private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	// Selection of ProgrammePointListTreeNodes which include all Programme Points of one Event
	// name of the variable programmePoint.event.selection ...
	private final static String EVENT_SELECTION_SOURCE = "programmePoint.event.selection";
	// ... and its values
	private final static String EVENT_SELECTION_VALUE_NONE = "none";
	private final static String EVENT_SELECTION_VALUE_ONE = "one";
	private final static String EVENT_SELECTION_VALUE_MANY = "many";


	// Selection of single Programme Points
	// name of the variable programmePoint.selection ...
	private final static String SELECTION_SOURCE = "programmePoint.selection";
	// ... and its values
	private final static String SELECTION_VALUE_NONE = "none";
	private final static String SELECTION_VALUE_ONE = "one";
	private final static String SELECTION_VALUE_MANY = "many";


	// name of the variable programmePoint.cancelled ...
	private final static String CANCELLED_SOURCE = "programmePoint.cancelled";
	// ... and its values
	private static final String CANCELLED_VALUE_ALL = "all";
	private static final String CANCELLED_VALUE_SOME = "some";
	private static final String CANCELLED_VALUE_NONE = "none";


	// source variables
	private String eventSelectionState = null;
	private String selectionState = null;
	private String cancelledState = null;


	// selected Programme Points
	private List<ProgrammePointVO> programmePointList = new ArrayList<>();

	// Event PKs of selected ProgrammePointListTreeNodes
	private List<Long> eventIdList = new ArrayList<>();


	/**
	 * Safe the window because we cannot get it in dispose() with
	 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().
	 */
	private IWorkbenchWindow window;



	public ProgrammePointSourceProvider() {

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

		ProgrammePointModel.getInstance().addListener(modelListener);
	}


	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(selectionListener);

		ProgrammePointModel.getInstance().removeListener(modelListener);
	}


	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {
			EVENT_SELECTION_SOURCE,
			SELECTION_SOURCE,
			CANCELLED_SOURCE
		};
	}


	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> currentState = new HashMap<>(4);
		currentState.put(EVENT_SELECTION_SOURCE, eventSelectionState);
		currentState.put(SELECTION_SOURCE, selectionState);
		currentState.put(CANCELLED_SOURCE, cancelledState);
		return currentState;
	}


	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
			try {
    			log.debug("ProgrammePointSourceProvider.selectionChanged(IWorkbenchPart part, ISelection incoming)");
    			log.debug("	IWorkbenchPart: " +  part);
    			log.debug("	ISelection: " +  incoming);

    			programmePointList.clear();
    			eventIdList.clear();

    			if (incoming instanceof IStructuredSelection) {
    				IStructuredSelection selection = (IStructuredSelection) incoming;
    				log.debug("	ISelection is IStructuredSelection, size: " + selection.size());
    				int i = 0;
    				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
    					Object selectedElement = it.next();
    					if (selectedElement instanceof ProgrammePointVO) {
    						ProgrammePointVO programmePointVO = (ProgrammePointVO) selectedElement;
    						log.debug("	" + (i++) + " is ProgrammePointVO, name: " + programmePointVO.getName());
    						programmePointList.add(programmePointVO);
    					}
    					else if (selectedElement instanceof ProgrammePointCVO) {
    						ProgrammePointCVO programmePointCVO = (ProgrammePointCVO) selectedElement;
    						log.debug("	" + (i++) + " is ProgrammePointCVO, name: " + programmePointCVO.getPpName());
    						programmePointList.add( programmePointCVO.getVO() );
    					}
    					else if (selectedElement instanceof ProgrammePointTreeNode) {
    						ProgrammePointTreeNode programmePointTreeNode = (ProgrammePointTreeNode) selectedElement;
    						ProgrammePointVO programmePointVO = programmePointTreeNode.getValue();
    						log.debug("	" + (i++) + " is ProgrammePointTreeNode, name: " + programmePointVO.getName());
    						programmePointList.add(programmePointVO);
    					}
    					else if (selectedElement instanceof ProgrammePointListTreeNode) {
    						ProgrammePointListTreeNode programmePointListTreeNode = (ProgrammePointListTreeNode) selectedElement;
    						Long eventId = programmePointListTreeNode.getEventId();
    						eventIdList.add(eventId);
    						log.debug("	" + (i++) + " is ProgrammePointListTreeNode, eventPK: " + eventId);
    						programmePointList.addAll( ProgrammePointModel.getInstance().getProgrammePointVOsByEventPK(eventId, true) );
    					}
    					else {
    						log.debug("	" + (i++) + " is no ProgrammePointVO and ProgrammePointCVO but " + selectedElement.getClass().getName() + ", programmePointList.clear(), break");
    						programmePointList.clear();
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
    		try {
        		if (   event.getOperation() == CacheModelOperation.UPDATE
        			|| event.getOperation() == CacheModelOperation.REFRESH
        		) {
        			try {
        				// replace updated ProgrammePointVOs with new versions from model
        				boolean change = false;
        				List<Long> updatedProgrammePointPKs = event.getKeyList();

        				for (ListIterator<ProgrammePointVO> li = programmePointList.listIterator(); li.hasNext();) {
    						Long ppPK = li.next().getID();
    						if ( updatedProgrammePointPKs.contains(ppPK) ) {
    							// replace ProgrammePointVO with new version from model
    							ProgrammePointVO ppVO = ProgrammePointModel.getInstance().getProgrammePointVO(ppPK);
    							li.set(ppVO);
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
			catch (Exception e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
			}
    	}
	};


	private void refreshVariables() {
		log.debug("ProgrammePointSourceProvider.refreshVariables()");

		int eventSelectionCount = eventIdList.size();
		int selectionCount = programmePointList.size();
		int cancelledCount = 0;

		for (ProgrammePointVO programmePointVO : programmePointList) {
			if ( programmePointVO.isCancelled() ) {
				cancelledCount++;
			}
		}

		log.debug("	--- counts ---");
		log.debug("	eventSelectionCount: " + eventSelectionCount);
		log.debug("	selectionCount: " + selectionCount);
		log.debug("	cancelledCount: " + cancelledCount);

		String oldEventSelectionState = eventSelectionState;
		String oldSelectionState = selectionState;
		String oldCancelledState = cancelledState;

		// set eventSelectionState
		if (eventSelectionCount == 0) {
			eventSelectionState = EVENT_SELECTION_VALUE_NONE;
		}
		else if (eventSelectionCount == 1) {
			eventSelectionState = EVENT_SELECTION_VALUE_ONE;
		}
		else {
			eventSelectionState = EVENT_SELECTION_VALUE_MANY;
		}


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


		if (   eventSelectionState != oldEventSelectionState
			|| selectionState != oldSelectionState
			|| cancelledState != oldCancelledState
		) {

			Map<String, String> currentState = new HashMap<>(3);

			log.debug("	--- states changed ---");

			if (eventSelectionState != oldEventSelectionState) {
				currentState.put(EVENT_SELECTION_SOURCE, eventSelectionState);
			}

			if (selectionState != oldSelectionState) {
				currentState.put(SELECTION_SOURCE, selectionState);
			}

			if (cancelledState != oldCancelledState) {
				currentState.put(CANCELLED_SOURCE, cancelledState);
			}

			log.debug("fireSourceChanged(ISources.WORKBENCH, currentState): " + currentState);
			fireSourceChanged(ISources.WORKBENCH, currentState);
		}
		else {
			log.debug("	--- states not changed ---");
		}

		// Please don't do this, since output that at each kind of event makes that
		// i cannot copy and paste anything in the applications console view
		// log.debug("participantSelectionState=" + participantSelectionState);
	}

}

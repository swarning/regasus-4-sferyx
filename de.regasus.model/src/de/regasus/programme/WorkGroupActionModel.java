package de.regasus.programme;

import static de.regasus.LookupService.*;

import java.util.ArrayList;
import java.util.List;

import com.lambdalogic.util.exception.ErrorMessageException;

public class WorkGroupActionModel {

	private static WorkGroupActionModel singleton;
	
	protected List<WorkGroupActionModelListener> listenerList = new ArrayList<WorkGroupActionModelListener>();
	
	private WorkGroupActionModel() {
	}
	

	public static WorkGroupActionModel getInstance() {
		if (singleton == null) {
			singleton = new WorkGroupActionModel();
		}
		return singleton;
	}

	
	// **************************************************************************
	// * Entity Listener
	// *

	public void addListener(WorkGroupActionModelListener listener) {
		if (listener != null) {
			listenerList.add(listener);
		}
	}


	public void removeListener(WorkGroupActionModelListener listener) {
		if (listener != null) {
			listenerList.remove(listener);
		}
	}

	// *
	// * Entity Listener
	// **************************************************************************

	
	public List<Exception> assignWorkGroupsByProgrammePoint(Long programmePointPK, boolean stopOnErrors)
	throws ErrorMessageException {
		List<Exception> exceptions = getProgrammeBookingMgr().assignWorkGroups(
			programmePointPK, 
			false,			// all
			stopOnErrors	// throwOverlapException
		);
		
		
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleAssignWorkGroups(null, programmePointPK);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
		
		return exceptions;
	}

	
	public List<Exception> assignWorkGroupsByEvent(Long eventPK, boolean stopOnErrors)
	throws ErrorMessageException {
		List<Exception> exceptions = getProgrammeBookingMgr().assignWorkGroupsByEvent(
			eventPK, 
			false,			// all
			stopOnErrors	// throwOverlapException
		);
		
		
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleAssignWorkGroups(eventPK, null);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
		
		return exceptions;
	}
	
	
	public void removeWorkGroupAssociationByEvent(Long eventPK)
	throws ErrorMessageException {
        if (eventPK == null) {
            throw new IllegalArgumentException("Parameter 'eventPK' is null.");
        }

        getProgrammeBookingMgr().removeWorkGroupAssociationByEvent(eventPK);
        
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleAssignWorkGroups(eventPK, null);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
	}
	

	public void removeWorkGroupAssociationByProgrammePoint(Long programmePointPK)
	throws ErrorMessageException {
        if (programmePointPK == null) {
            throw new IllegalArgumentException("Parameter 'programmePointPK' is null.");
        }

        getProgrammeBookingMgr().removeWorkGroupAssociationByProgrammePoint(programmePointPK);
        
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleAssignWorkGroups(null, programmePointPK);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
	}

	
	public void fixWorkGroupsByProgrammePoint(Long programmePointPK)
	throws Exception {
		getProgrammeBookingMgr().setWorkGroupFixByProgrammePoint(programmePointPK, true);
		
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleFixWorkGroups(null, programmePointPK);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
	}
	
	
	public void fixWorkGroupsByEvent(Long eventPK)
	throws Exception {
		getProgrammeBookingMgr().setWorkGroupFixByEvent(eventPK, true);
		
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleFixWorkGroups(eventPK, null);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
	}
	
	
	public void unfixWorkGroupsByProgrammePoint(Long programmePointPK)
	throws Exception {
		getProgrammeBookingMgr().setWorkGroupFixByProgrammePoint(programmePointPK, false);
		
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleUnfixWorkGroups(null, programmePointPK);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
	}

	
	public void unfixWorkGroupsByEvent(Long eventPK)
	throws Exception {
		getProgrammeBookingMgr().setWorkGroupFixByEvent(eventPK, false);
		
		for (WorkGroupActionModelListener listener : listenerList) {
			try {
				listener.handleUnfixWorkGroups(eventPK, null);
			}
			catch (Throwable e) {
				com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				listenerList.remove(listener);
			}
		}
	}

}

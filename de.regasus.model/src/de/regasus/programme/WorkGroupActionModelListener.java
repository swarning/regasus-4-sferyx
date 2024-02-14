package de.regasus.programme;


public interface WorkGroupActionModelListener {
	
	void handleAssignWorkGroups(Long eventPK, Long programmePointPK);
	
	void handleFixWorkGroups(Long eventPK, Long programmePointPK);
	
	void handleUnfixWorkGroups(Long eventPK, Long programmePointPK);
	
}

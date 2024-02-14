package de.regasus.participant.editor.lead;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.event.LocationModel;
import de.regasus.programme.ProgrammePointModel;

enum PresenceTableColumns {
	PROGRAMME_POINT, LOCATION
};

public class AttendenceTable extends SimpleTable<Long, PresenceTableColumns> {

	private ProgrammePointModel programmePointModel = ProgrammePointModel.getInstance();
	
	private LocationModel locationModel = LocationModel.getInstance();
	
	public AttendenceTable(Table table) {
		super(table, PresenceTableColumns.class);
	}

	
	@Override
	public String getColumnText(Long programmePointPK, PresenceTableColumns column) {
		try {
			ProgrammePointVO programmePointVO = programmePointModel.getProgrammePointVO(programmePointPK);;
			switch (column) {
			case PROGRAMME_POINT:
				return programmePointVO.getName().getString();
			case LOCATION:
				Long locationPK = programmePointVO.getLocationPK();
				if (locationPK != null) {
					return locationModel.getLocationVO(locationPK).getName();
				}
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
		return null;
	}
}

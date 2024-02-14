package de.regasus.participant.editor.lead;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.LeadDirection;
import com.lambdalogic.messeinfo.participant.data.LeadResponse;
import com.lambdalogic.messeinfo.participant.data.LeadSource;
import com.lambdalogic.messeinfo.participant.data.LeadVO;
import com.lambdalogic.util.FormatHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.IconRegistry;
import de.regasus.event.LocationModel;
import de.regasus.programme.ProgrammePointModel;

enum LeadsTableColumns {
	DIR, CAPTURE, PROGRAMME_POINT, LOCATION, SOURCE, RESPONSE;
};

public class LeadsTable extends SimpleTable<LeadVO, LeadsTableColumns> {

	private FormatHelper formatHelper = FormatHelper.getDefaultLocaleInstance();

	private ProgrammePointModel programmePointModel = ProgrammePointModel.getInstance();

	private LocationModel locationModel = LocationModel.getInstance();

	static final HashMap<Object, String> source2StringMap = new HashMap<Object, String>();
	static {
		source2StringMap.put(LeadSource.AUTO, ParticipantLabel.AUTO.getString());
		source2StringMap.put(LeadSource.MANUAL, ParticipantLabel.MANUAL.getString());
		source2StringMap.put(LeadSource.TAG, ParticipantLabel.TAG.getString());
	}

	static final HashMap<Object, String> response2StringMap = new HashMap<Object, String>();
	static {
		response2StringMap.put(LeadResponse.ALLOWED, ParticipantLabel.ALLOWED.getString());
		response2StringMap.put(LeadResponse.DENIED, ParticipantLabel.DENIED.getString());
		response2StringMap.put(LeadResponse.NOT_REQUIRED, ParticipantLabel.NOT_REQUIRED.getString());
	}


	public LeadsTable(Table table) {
		super(table, LeadsTableColumns.class);
	}


	@Override
	public String getColumnText(LeadVO leadVO, LeadsTableColumns column) {
		try {
			switch (column) {
			case DIR:
				return null; // Show an icon, not a text
			case CAPTURE:
				return formatHelper.formatDateTime(leadVO.getCollectTime());
			case PROGRAMME_POINT:
				return programmePointModel.getProgrammePointVO(leadVO.getProgrammePointPK()).getName().getString();
			case LOCATION:
				Long locationPK = leadVO.getLocationPK();
				if (locationPK != null) {
					return locationModel.getLocationVO(locationPK).getName();
				}
				else {
					return "";
				}
			case SOURCE:
				return source2StringMap.get(leadVO.getSource());
			case RESPONSE:
				return response2StringMap.get(leadVO.getResponse());
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return null;
	}

	/**
	 * MIRCP-438 - "Im TN-Editor sollen Leads sollen standardmäßig chronologisch sortiert werden"
	 */
	@Override
	protected LeadsTableColumns getDefaultSortColumn() {
		return LeadsTableColumns.CAPTURE;
	}
	
	@Override
	protected Comparable<? extends Object> getColumnComparableValue(LeadVO leadVO, LeadsTableColumns column) {
		switch (column) {
		case DIR:
			// Since the getColumnText return nothing here, we explicitly give the String as order criterium
			return leadVO.getDirection();
		case CAPTURE:
			return leadVO.getCollectTime();
		default:
			return super.getColumnComparableValue(leadVO, column);
		}
	}


	@Override
	public Image getColumnImage(LeadVO leadVO, LeadsTableColumns column) {
		if (column == LeadsTableColumns.DIR) {
			LeadDirection dir = leadVO.getDirection();
			if (dir == LeadDirection.LOGICAL_OUT ||  dir == LeadDirection.PHYSICAL_OUT) {
				return IconRegistry.getImage("icons/out.png");
			} else if (dir == LeadDirection.LOGICAL_IN ||  dir == LeadDirection.PHYSICAL_IN) {
				return IconRegistry.getImage("icons/in.png");
			} else if (dir == LeadDirection.PASS) {
				return IconRegistry.getImage("icons/pass.png");
			}
		}
		return null;
	}
}

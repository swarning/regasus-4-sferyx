/**
 * GateDeviceTable.java
 * created on 24.09.2013 17:19:42
 */
package de.regasus.common.gatedevice.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.data.GateDeviceVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum GateDeviceTableColumns {NAME, SERIAL_NO};

public class GateDeviceTable 
extends SimpleTable<GateDeviceVO, GateDeviceTableColumns> {
	
	public GateDeviceTable(Table table) {
		super(table, GateDeviceTableColumns.class);
	}


	@Override
	public String getColumnText(GateDeviceVO gateDeviceVO, GateDeviceTableColumns column) {
		String label = null;
		
		switch (column) {
			case NAME:
				label = gateDeviceVO.getName();
				break;
			case SERIAL_NO:
				label = gateDeviceVO.getSerialNo();
				break;
		}
		
		if (label == null) {
			label = "";
		}
		return label;
	}

	
	@Override
	protected GateDeviceTableColumns getDefaultSortColumn() {
		return GateDeviceTableColumns.NAME;
	}
	
}

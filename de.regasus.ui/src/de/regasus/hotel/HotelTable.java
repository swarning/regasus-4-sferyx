package de.regasus.hotel;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.hotel.data.HotelCVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum HotelTableColumns {NAME, CITY}

public class HotelTable extends SimpleTable<HotelCVO, HotelTableColumns> {

	public HotelTable(Table table) {
		super(table, HotelTableColumns.class);
	}


	@Override
	public String getColumnText(HotelCVO hotelCVO, HotelTableColumns column) {
		String label = null;

		switch (column) {
			case NAME:
				label = hotelCVO.getName();
				break;
			case CITY:
				label = hotelCVO.getMainAddress().getCity();
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected HotelTableColumns getDefaultSortColumn() {
		return HotelTableColumns.NAME;
	}

}

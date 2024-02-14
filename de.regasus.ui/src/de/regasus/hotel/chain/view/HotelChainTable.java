package de.regasus.hotel.chain.view;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.hotel.HotelChain;


enum HotelChainTableColumns {NAME};

public class HotelChainTable extends SimpleTable<HotelChain, HotelChainTableColumns> {

	public HotelChainTable(Table table) {
		super(table, HotelChainTableColumns.class);
	}

	@Override
	public String getColumnText(
		HotelChain hotelChain,
		HotelChainTableColumns column
	) {
		String label = null;

		switch (column) {
			case NAME:
				label = hotelChain.getName();
			break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected HotelChainTableColumns getDefaultSortColumn() {
		return HotelChainTableColumns.NAME;
	}

}

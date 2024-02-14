package de.regasus.common.country.view;

import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import de.regasus.common.Country;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum CountryTableColumns {ID, NAME};

public class CountryTable extends SimpleTable<Country, CountryTableColumns> {

	public CountryTable(Table table) {
		super(table, CountryTableColumns.class);
	}

	@Override
	public String getColumnText(Country country, CountryTableColumns column) {
		String label = null;

		switch (column) {
			case ID:
				label = country.getId();
				break;
			case NAME:
				if (country.getName() != null) {
					label = country.getName().getString(Locale.getDefault());
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected CountryTableColumns getDefaultSortColumn() {
		return CountryTableColumns.NAME;
	}

}

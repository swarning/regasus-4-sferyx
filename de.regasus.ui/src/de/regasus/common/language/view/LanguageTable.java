package de.regasus.common.language.view;

import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.Language;

enum LanguageTableColumns {ID, NAME};

public class LanguageTable extends SimpleTable<Language, LanguageTableColumns> {

	public LanguageTable(Table table) {
		super(table, LanguageTableColumns.class);
	}

	@Override
	public String getColumnText(Language language, LanguageTableColumns column) {
		String label = null;

		switch (column) {
			case ID:
				label = language.getId();
				break;
			case NAME:
				if (language.getName() != null) {
					label = language.getName().getString(Locale.getDefault());
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected LanguageTableColumns getDefaultSortColumn() {
		return LanguageTableColumns.NAME;
	}

}

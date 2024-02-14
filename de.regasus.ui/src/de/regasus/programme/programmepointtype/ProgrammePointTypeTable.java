package de.regasus.programme.programmepointtype;

import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum ProgrammePointTypeTableColumns {NAME, REFERENCE_CODE}

public class ProgrammePointTypeTable extends SimpleTable<ProgrammePointTypeVO, ProgrammePointTypeTableColumns> {

	private String language;


	public ProgrammePointTypeTable(Table table) {
		super(table, ProgrammePointTypeTableColumns.class);
		language = Locale.getDefault().getLanguage();
	}


	@Override
	public String getColumnText(ProgrammePointTypeVO programmePointTypeVO, ProgrammePointTypeTableColumns column) {
		String label = null;
		switch (column) {
			case NAME:
				label = programmePointTypeVO.getName().getString(language);
				break;

			case REFERENCE_CODE:
				label = programmePointTypeVO.getReferenceCode();
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected ProgrammePointTypeTableColumns getDefaultSortColumn() {
		return ProgrammePointTypeTableColumns.NAME;
	}

}

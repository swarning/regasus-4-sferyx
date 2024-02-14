package de.regasus.programme.programmepoint;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.data.ProgrammePointVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum ProgrammePointTableColumns {NAME, START_TIME, END_TIME}

public class ProgrammePointTable extends SimpleTable<ProgrammePointVO, ProgrammePointTableColumns> {

	private String language;

	private final static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());


	public ProgrammePointTable(Table table) {
		super(table, ProgrammePointTableColumns.class);
		language = Locale.getDefault().getLanguage();
	}


	@Override
	public String getColumnText(ProgrammePointVO programmePointVO, ProgrammePointTableColumns column) {
		String label = null;
		switch (column) {
			case NAME:
				label = programmePointVO.getName(language);
				break;

			case START_TIME:
				final Date startTime = programmePointVO.getStartTime();
				if (startTime != null) {
					label = dateFormat.format(startTime);
				}
				break;

			case END_TIME:
				final Date endTime = programmePointVO.getEndTime();
				if (endTime != null) {
					label = dateFormat.format(endTime);
				}
				break;
		}

		if (label == null) {
			label = "";
		}

		return label;
	}


	@Override
	protected Comparable<?> getColumnComparableValue(ProgrammePointVO programmePointVO, ProgrammePointTableColumns column) {
		/* Für Werte, die nicht vom Typ String sind, die Originalwerte (z.B. Date, Integer)
		 * zurückgeben.
		 * Werte vom Typ String können pauschal über super.getColumnComparableValue(eventVO, column)
		 * zurückgegeben werden, weil sie Sortierwerte den angezeigten entsprechen.
		 */
		switch (column) {
		case START_TIME:
			return programmePointVO.getStartTime();
		case END_TIME:
			return programmePointVO.getEndTime();
		default:
			return super.getColumnComparableValue(programmePointVO, column);
		}
	}


	@Override
	protected ProgrammePointTableColumns getDefaultSortColumn() {
		return ProgrammePointTableColumns.NAME;
	}

}

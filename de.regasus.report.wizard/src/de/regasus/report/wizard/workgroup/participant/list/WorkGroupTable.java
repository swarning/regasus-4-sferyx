package de.regasus.report.wizard.workgroup.participant.list;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.data.WorkGroupCVO;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

enum WorkGroupTableColumns {WORK_GROUP, PROGRAMME_POINT, START_TIME, END_TIME, LOCATION};

public class WorkGroupTable extends SimpleTable<WorkGroupCVO, WorkGroupTableColumns> {

	private String language;
	
	private final static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
	
	
	public WorkGroupTable(Table table) {
		super(table, WorkGroupTableColumns.class);
		language = Locale.getDefault().getLanguage();
	}
	
	
	@Override
	public String getColumnText(WorkGroupCVO workGroupCVO, WorkGroupTableColumns column) {
		String label = null;
		switch (column) {
			case WORK_GROUP:
				label = workGroupCVO.getVO().getName();
				break;

			case PROGRAMME_POINT:
				label = workGroupCVO.getProgrammePointCVO().getPpName(language);
				break;

			case START_TIME:
				final Date startTime = workGroupCVO.getVO().getStartTime();
				if (startTime != null) {
					label = dateTimeFormat.format(startTime);
				}
				break;

			case END_TIME:
				final Date endTime = workGroupCVO.getVO().getEndTime();
				if (endTime != null) {
					label = dateTimeFormat.format(endTime);
				}
				break;

			case LOCATION:
				label = workGroupCVO.getVO().getLocation();
				break;
		}

		if (label == null) {
			label = "";
		}
		
		return label;
	}

	@Override
	protected Comparable<?> getColumnComparableValue(WorkGroupCVO workGroupCVO, WorkGroupTableColumns column) {
		/* Für Werte, die nicht vom Typ String sind, die Originalwerte (z.B. Date, Integer)
		 * zurückgeben.
		 * Werte vom Typ String können pauschal über super.getColumnComparableValue(eventVO, column)
		 * zurückgegeben werden, weil sie Sortierwerte den angezeigten entsprechen.
		 */
		switch (column) {
		case START_TIME:
			return workGroupCVO.getVO().getStartTime();
		case END_TIME:
			return workGroupCVO.getVO().getEndTime();
		default:
			return super.getColumnComparableValue(workGroupCVO, column);	
		}
	}

	@Override
	protected WorkGroupTableColumns getDefaultSortColumn() {
		return WorkGroupTableColumns.START_TIME;
	}

	

}

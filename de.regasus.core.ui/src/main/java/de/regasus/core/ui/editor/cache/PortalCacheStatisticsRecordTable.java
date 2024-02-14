package de.regasus.core.ui.editor.cache;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.portal.PortalCacheStatisticsRecord;


enum PortalCacheStatisticsRecordTableColumns {MNEMONIC, REQUEST_COUNT, LAST_ACCESS}

public class PortalCacheStatisticsRecordTable extends SimpleTable<PortalCacheStatisticsRecord, PortalCacheStatisticsRecordTableColumns> {

	private NumberFormat integerNumberFormat;
	private DateTimeFormatter dateTimeFormatter;


	public PortalCacheStatisticsRecordTable(Table table) {
		super(table, PortalCacheStatisticsRecordTableColumns.class);

		integerNumberFormat = NumberFormat.getNumberInstance();
		integerNumberFormat.setMinimumFractionDigits(0);
		integerNumberFormat.setMaximumFractionDigits(0);
		integerNumberFormat.setGroupingUsed(true);

		dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);
	}


	@Override
	public String getColumnText(
		PortalCacheStatisticsRecord statisticsRecord,
		PortalCacheStatisticsRecordTableColumns column
	) {
		String label = null;

		try {
			switch (column) {
				case MNEMONIC:
					label = statisticsRecord.getMnemonic();
					break;
				case REQUEST_COUNT:
					label = integerNumberFormat.format( statisticsRecord.getRequestCount() );
					break;
				case LAST_ACCESS:
					label = statisticsRecord.getLastAccess().format(dateTimeFormatter);
					break;
			}

			if (label == null) {
				label = "";
			}
		}
		catch (Exception e) {
			label = e.getMessage();
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return label;
	}


	@Override
	protected Comparable<? extends Object> getColumnComparableValue(
		PortalCacheStatisticsRecord statisticsRecord,
		PortalCacheStatisticsRecordTableColumns column
	) {
		switch (column) {
			case MNEMONIC:
				return statisticsRecord.getMnemonic();
			case REQUEST_COUNT:
				return statisticsRecord.getRequestCount();
			case LAST_ACCESS:
				return statisticsRecord.getLastAccess();
			default:
				return 0;
		}
	}


	@Override
	protected PortalCacheStatisticsRecordTableColumns getDefaultSortColumn() {
		return PortalCacheStatisticsRecordTableColumns.MNEMONIC;
	}

}

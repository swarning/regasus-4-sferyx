package de.regasus.core.ui.editor.cache;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.simpleviewer.SimpleTable;

import de.regasus.common.FileCacheStatisticsRecord;


enum FileCacheStatisticsRecordTableColumns {INTERNAL_PATH, SIZE, REQUEST_COUNT, LAST_ACCESS}

public class FileCacheStatisticsRecordTable extends SimpleTable<FileCacheStatisticsRecord, FileCacheStatisticsRecordTableColumns> {

	private NumberFormat integerNumberFormat;
	private DateTimeFormatter dateTimeFormatter;

	public FileCacheStatisticsRecordTable(Table table) {
		super(table, FileCacheStatisticsRecordTableColumns.class);

		integerNumberFormat = NumberFormat.getNumberInstance();
		integerNumberFormat.setMinimumFractionDigits(0);
		integerNumberFormat.setMaximumFractionDigits(0);
		integerNumberFormat.setGroupingUsed(true);

		dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);
	}


	@Override
	public String getColumnText(
		FileCacheStatisticsRecord statisticsRecord,
		FileCacheStatisticsRecordTableColumns column
	) {
		String label = null;

		try {
			switch (column) {
				case INTERNAL_PATH:
					label = statisticsRecord.getInternalPath();
					break;
				case SIZE:
					label = FileHelper.computeReadableFileSize( statisticsRecord.getSize() );
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
		FileCacheStatisticsRecord statisticsRecord,
		FileCacheStatisticsRecordTableColumns column
	) {
		switch (column) {
			case INTERNAL_PATH:
				return statisticsRecord.getInternalPath();
			case SIZE:
				return statisticsRecord.getSize();
			case REQUEST_COUNT:
				return statisticsRecord.getRequestCount();
			case LAST_ACCESS:
				return statisticsRecord.getLastAccess();
			default:
				return 0;
		}
	}


	@Override
	protected FileCacheStatisticsRecordTableColumns getDefaultSortColumn() {
		return FileCacheStatisticsRecordTableColumns.INTERNAL_PATH;
	}

}

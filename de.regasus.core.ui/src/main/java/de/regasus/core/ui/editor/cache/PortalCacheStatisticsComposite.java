package de.regasus.core.ui.editor.cache;

import java.text.NumberFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.portal.PortalCacheStatistics;

public class PortalCacheStatisticsComposite extends Composite {

	// the entity
	private PortalCacheStatistics cacheStatistics;


	// **************************************************************************
	// * Widgets
	// *

	private Label cacheCountLabel;
	private Label cacheHitLabel;
	private Label cacheMissLabel;

	private Table table;
	private TableViewer tableViewer;

	// *
	// * Widgets
	// **************************************************************************


	protected NumberFormat integerNumberFormat = null;


	public PortalCacheStatisticsComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		integerNumberFormat = NumberFormat.getNumberInstance();
		integerNumberFormat.setMinimumFractionDigits(0);
		integerNumberFormat.setMaximumFractionDigits(0);
		integerNumberFormat.setGroupingUsed(true);


		createWidgets();
	}


	private void createWidgets() throws Exception {
		final int COL_COUNT = 6;
		setLayout( new GridLayout(COL_COUNT, false) );


		GridDataFactory widgetGridDataFactory = GridDataFactory.swtDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false);

		/*
		 * Row 1
		 */
		SWTHelper.createLabel(this, "Cache Count:");
		cacheCountLabel = new Label(this, SWT.NONE);
		widgetGridDataFactory.applyTo(cacheCountLabel);


		SWTHelper.createLabel(this, "Cache Hits:");
		cacheHitLabel = new Label(this, SWT.NONE);
		widgetGridDataFactory.applyTo(cacheHitLabel);


		SWTHelper.createLabel(this, "Cache Misses:");
		cacheMissLabel = new Label(this, SWT.NONE);
		widgetGridDataFactory.applyTo(cacheMissLabel);

		/*
		 * Row 2
		 */
		Composite tableComposite = new Composite(this, SWT.BORDER);
		GridDataFactory.fillDefaults()
			.span(COL_COUNT, 1)
			.grab(true, true)
			.applyTo(tableComposite);

		createTable(tableComposite);
	}


	private void createTable(Composite parent) {
		TableColumnLayout layout = new TableColumnLayout();
		parent.setLayout(layout);

		table = new Table(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn internalPathTableColumn = new TableColumn(table, SWT.LEFT);
		internalPathTableColumn.setText("Internal Path");
		layout.setColumnData(internalPathTableColumn, new ColumnWeightData(90));

		TableColumn requestCountTableColumn = new TableColumn(table, SWT.RIGHT);
		requestCountTableColumn.setText("Request Count");
		layout.setColumnData(requestCountTableColumn, new ColumnWeightData(10));

		TableColumn lastAccessTableColumn = new TableColumn(table, SWT.RIGHT);
		lastAccessTableColumn.setText("Last Access");
		layout.setColumnData(lastAccessTableColumn, new ColumnWeightData(10));

		PortalCacheStatisticsRecordTable portalStatisticsRecordTable = new PortalCacheStatisticsRecordTable(table);
		tableViewer = portalStatisticsRecordTable.getViewer();
	}


	public void setCacheStatistics(PortalCacheStatistics cacheStatistics) {
		this.cacheStatistics = cacheStatistics;
		syncWidgetsToEntity();
	}


	private void syncWidgetsToEntity() {
		if (cacheStatistics != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						int count = cacheStatistics.getTotalCount();
						cacheCountLabel.setText( integerNumberFormat.format(count) );

						long hitCount = cacheStatistics.getCacheHitCount();
						cacheHitLabel.setText( integerNumberFormat.format(hitCount) );

						long missCount = cacheStatistics.getCacheMissCount();
						cacheMissLabel.setText( integerNumberFormat.format(missCount) );

						tableViewer.setInput( cacheStatistics.getPortalStatisticsRecordList() );

						layout();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}

}

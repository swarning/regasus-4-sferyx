package de.regasus.core.ui.view;

import static de.regasus.LookupService.getInvoiceExporterTimer;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.messeinfo.invoice.export.TimerInfo;
import com.lambdalogic.messeinfo.invoice.interfaces.IInvoiceExporterTimer;
import com.lambdalogic.util.exception.ErrorMessageException;
import com.lambdalogic.util.rcp.CopyAction;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class TimerBeanView extends ViewPart implements SelectionListener {

	protected Button cancelAllTimersButton;

	protected Button getInfoButton;

	protected Button startOneShotTimerButton;

	protected Button startDailyTimerButton;

	protected Button startDailyTimerHhmmButton;

	protected TimerInfoSimpleTable timerInfoSimpleTable;


	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		getInfoButton = new Button(parent, SWT.PUSH);
		getInfoButton.setText("Get timer infos");
		getInfoButton.addSelectionListener(this);

		cancelAllTimersButton = new Button(parent, SWT.PUSH);
		cancelAllTimersButton.setText("Cancel all timers");
		cancelAllTimersButton.addSelectionListener(this);

		startOneShotTimerButton = new Button(parent, SWT.PUSH);
		startOneShotTimerButton.setText("Start one-shot timer");
		startOneShotTimerButton.addSelectionListener(this);

		startDailyTimerButton = new Button(parent, SWT.PUSH);
		startDailyTimerButton.setText("Start daily timer as defined in database");
		startDailyTimerButton.addSelectionListener(this);

		Table table;
		table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION); // | SWT.FULL_SELECTION |
																					// SWT.HIDE_SELECTION
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableColumn stringTableColumn = new TableColumn(table, SWT.RIGHT);
		stringTableColumn.setWidth(160);
		stringTableColumn.setText("Next Timeout");

		final TableColumn intTableColumn = new TableColumn(table, SWT.RIGHT);
		intTableColumn.setWidth(160);
		intTableColumn.setText("Time remaining");

		final TableColumn dateTableColumn = new TableColumn(table, SWT.RIGHT);
		dateTableColumn.setWidth(140);
		dateTableColumn.setText("Info");

		timerInfoSimpleTable = new TimerInfoSimpleTable(table);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// MIRCP-284 - Copy und Paste
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());
	}


	@Override
	public void setFocus() {
		getInfoButton.setFocus();

	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		try {
			List<TimerInfo> timerInfos = null;
			IInvoiceExporterTimer bean = getInvoiceExporterTimer();
			if (e.widget == getInfoButton) {
				timerInfos = bean.getTimerInfos();

			}
			else if (e.widget == startOneShotTimerButton) {
				timerInfos = bean.startOneShotTimer();

			}
			else if (e.widget == cancelAllTimersButton) {
				timerInfos = bean.cancelAllTimers();
			}
			else if (e.widget == startDailyTimerButton) {
				timerInfos = bean.startDailyTimer();
			}
			if (timerInfos != null) {
				timerInfoSimpleTable.getViewer().setInput(timerInfos);
			}
		}
		catch (ErrorMessageException e1) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
		}
		catch (Throwable e1) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e1);
		}
	}
}

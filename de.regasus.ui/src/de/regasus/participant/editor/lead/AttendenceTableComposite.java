package de.regasus.participant.editor.lead;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;

public class AttendenceTableComposite extends Composite {

	private Table table;
	private AttendenceTable attendenceTable;
	private TableViewer tableViewer;
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public AttendenceTableComposite(Composite parent, int style, String label) {
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		Label titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText(label);
		
		Composite tableComposite = new Composite(this, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		table = new Table(tableComposite, SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Create the columns  Programmpunkt, Ort
		
		// Programmpunkt
		final TableColumn numberTableColumn = new TableColumn(table, SWT.LEFT);
		numberTableColumn.setText(ParticipantLabel.ProgrammePoint.getString());
		layout.setColumnData(numberTableColumn, new ColumnWeightData(15));

		// Ort
		final TableColumn locationTableColumn = new TableColumn(table, SWT.LEFT);
		locationTableColumn.setText(ParticipantLabel.Location.getString());
		layout.setColumnData(locationTableColumn, new ColumnWeightData(15));
		
		attendenceTable = new AttendenceTable(table);
		tableViewer = attendenceTable.getViewer();
	}
}

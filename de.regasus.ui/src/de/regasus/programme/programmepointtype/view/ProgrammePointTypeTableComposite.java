package de.regasus.programme.programmepointtype.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.ProgrammePointTypeVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.programme.ProgrammePointTypeModel;
import de.regasus.programme.programmepointtype.ProgrammePointTypeTable;

public class ProgrammePointTypeTableComposite
extends AbstractTableComposite<ProgrammePointTypeVO>
implements CacheModelListener<Long> {

	private final static int NAME_COLUMN_WEIGHT = 150;
	private final static int REFERENCE_CODE_COLUMN_WEIGHT = 100;

	// Model
	private ProgrammePointTypeModel programmePointTypeModel;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ProgrammePointTypeTableComposite(Composite parent, int style) throws Exception {
		super(parent, style);
	}


	@Override
	public void initializeTableViewer(int style) {
		if (tableViewer == null) {
			TableColumnLayout layout = new TableColumnLayout();
			setLayout(layout);
			Table table = new Table(this, style);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(NAME_COLUMN_WEIGHT));
			nameTableColumn.setText(ParticipantLabel.ProgrammePointType.getString());

			final TableColumn categoryTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(categoryTableColumn, new ColumnWeightData(REFERENCE_CODE_COLUMN_WEIGHT));
			categoryTableColumn.setText(ParticipantLabel.ProgrammePointType_ReferenceCode.getString());

			final ProgrammePointTypeTable programmePointTypeTable = new ProgrammePointTypeTable(table);
			tableViewer = programmePointTypeTable.getViewer();
		}
	}


	@Override
	protected Collection<ProgrammePointTypeVO> getModelData() {
		Collection<ProgrammePointTypeVO> modelData = null;
		try {
			modelData = programmePointTypeModel.getAllUndeletedProgrammePointTypeVOs();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		programmePointTypeModel = ProgrammePointTypeModel.getInstance();
		programmePointTypeModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (programmePointTypeModel != null) {
			programmePointTypeModel.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

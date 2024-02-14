package de.regasus.participant.type.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.event.ParticipantType;
import de.regasus.participant.ParticipantTypeModel;

public class ParticipantTypeTableComposite
extends AbstractTableComposite<ParticipantType>
implements CacheModelListener<String> {

	private final static int NAME_COLUMN_WEIGHT = 150;
	private final static int CATEGORY_COLUMN_WEIGHT = 100;
	private final static int PROOF_REQUIRED_COLUMN_WEIGHT = 80;

	// Model
	private ParticipantTypeModel participantTypeModel;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ParticipantTypeTableComposite(Composite parent, int style) throws Exception {
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

			// name
			{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    			layout.setColumnData(tableColumn, new ColumnWeightData(NAME_COLUMN_WEIGHT));
    			tableColumn.setText( Participant.PARTICIPANT_TYPE.getString() );
			}

			// category
			{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    			layout.setColumnData(tableColumn, new ColumnWeightData(CATEGORY_COLUMN_WEIGHT));
    			tableColumn.setText( ParticipantType.CATEGORY.getString() );
			}

			// proof required
			{
    			TableColumn tableColumn = new TableColumn(table, SWT.NONE);
    			layout.setColumnData(tableColumn, new ColumnWeightData(PROOF_REQUIRED_COLUMN_WEIGHT));
    			tableColumn.setText( ParticipantType.PROOF_REQUIRED.getString() );
			}

			ParticipantTypeTable participantTypeTable = new ParticipantTypeTable(table);
			tableViewer = participantTypeTable.getViewer();
		}
	}


	@Override
	protected Collection<ParticipantType> getModelData() {
		Collection<ParticipantType> modelData = null;
		try {
			modelData = participantTypeModel.getAllUndeletedParticipantTypes();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		participantTypeModel = ParticipantTypeModel.getInstance();
		participantTypeModel.addListener(this);

	}


	@Override
	protected void disposeModel() {
		if (participantTypeModel != null) {
			participantTypeModel.removeListener(this);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

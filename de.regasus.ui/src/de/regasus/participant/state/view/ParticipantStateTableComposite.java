/**
 * ParticipantStateTableComposite.java
 * Created on 16.04.2012
 */
package de.regasus.participant.state.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.ParticipantState;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.participant.ParticipantStateModel;

/**
 * @author huuloi
 *
 */
public class ParticipantStateTableComposite
extends AbstractTableComposite<ParticipantState>
implements CacheModelListener<Long> {

	private final static int NAME_COLUMN_WEIGHT = 150;
	private final static int DESCRIPTION_COLUMN_WEIGHT = 250;
//	private final static int REQUIRED_BY_SYSTEM_WEIGHT = 80;
	private final static int BADGE_PRINT_WEIGHT = 80;

	// Model
	private ParticipantStateModel participantStateModel;


	public ParticipantStateTableComposite(Composite parent, int style) throws Exception {
		super(parent, style);
	}

	@Override
	protected void disposeModel() {
		if (participantStateModel != null) {
			participantStateModel.removeListener(this);
		}
	}

	@Override
	protected Collection<ParticipantState> getModelData() throws Exception {
		Collection<ParticipantState> modelData = null;
		try {
			modelData = participantStateModel.getParticipantStates();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}

	@Override
	protected void initModel() {
		participantStateModel = ParticipantStateModel.getInstance();
		participantStateModel.addListener(this);
	}

	@Override
	protected void initializeTableViewer(int style) {
		if (tableViewer == null) {
			TableColumnLayout layout = new TableColumnLayout();
			setLayout(layout);
			Table table = new Table(this, style);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(NAME_COLUMN_WEIGHT));
			nameTableColumn.setText( Participant.PARTICIPANT_STATE.getString() );

			final TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(descriptionTableColumn, new ColumnWeightData(DESCRIPTION_COLUMN_WEIGHT));
			descriptionTableColumn.setText(UtilI18N.Description);

//			final TableColumn requiredBySystemTableColumn = new TableColumn(table, SWT.NONE);
//			layout.setColumnData(requiredBySystemTableColumn, new ColumnWeightData(REQUIRED_BY_SYSTEM_WEIGHT));
//			requiredBySystemTableColumn.setText(ParticipantLabel.RequiredBySystem.getString());

			final TableColumn badgePrintTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(badgePrintTableColumn, new ColumnWeightData(BADGE_PRINT_WEIGHT));
			badgePrintTableColumn.setText(ParticipantLabel.BadgePrint.getString());

			final ParticipantStateTable participantStateTable = new ParticipantStateTable(table);
			tableViewer = participantStateTable.getViewer();
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

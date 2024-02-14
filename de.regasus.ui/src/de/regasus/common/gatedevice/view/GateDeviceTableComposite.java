/**
 * GateDeviceTableComposite.java
 * created on 24.09.2013 16:18:47
 */
package de.regasus.common.gatedevice.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.messeinfo.participant.data.GateDeviceVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.common.GateDeviceModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class GateDeviceTableComposite 
extends AbstractTableComposite<GateDeviceVO>
implements CacheModelListener<Long> {
	
	private final static int NAME_COLUMN_WEIGHT = 150;
	private final static int SERIAL_NO_COLUMN_WEIGHT = 100;
	
	// Model
	private GateDeviceModel gateDeviceModel;

	
	public GateDeviceTableComposite(Composite parent, int style) throws Exception {
		super(parent, style);
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

	
	@Override
	protected void initializeTableViewer(int style) {
		if (tableViewer == null) {
			TableColumnLayout layout = new TableColumnLayout();
			setLayout(layout);
			Table table = new Table(this, style);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			
			TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(NAME_COLUMN_WEIGHT));
			nameTableColumn.setText(ParticipantLabel.GateDevice.getString());
			
			TableColumn serialNoTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(serialNoTableColumn, new ColumnWeightData(SERIAL_NO_COLUMN_WEIGHT));
			serialNoTableColumn.setText(ParticipantLabel.GateDevice_SerialNo.getString());
			
			GateDeviceTable gateDeviceTable = new GateDeviceTable(table);
			tableViewer = gateDeviceTable.getViewer();
		}
	}

	
	@Override
	protected Collection<GateDeviceVO> getModelData() throws Exception {
		Collection<GateDeviceVO> modelData = null;
		try {
			modelData = gateDeviceModel.getAllUndeletedGateDeviceVOs();
		}
		catch (Throwable e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
		return modelData;
	}

	
	@Override
	protected void initModel() {
		gateDeviceModel = GateDeviceModel.getInstance();
		gateDeviceModel.addListener(this);
	}

	
	@Override
	protected void disposeModel() {
		if (gateDeviceModel != null) {
			gateDeviceModel.removeListener(this);
		}
	}

}

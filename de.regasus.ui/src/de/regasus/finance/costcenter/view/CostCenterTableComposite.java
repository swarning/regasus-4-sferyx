package de.regasus.finance.costcenter.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.invoice.data.CostCenterVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.CostCenter1Model;
import de.regasus.ui.Activator;

public class CostCenterTableComposite
extends AbstractTableComposite<CostCenterVO>
implements CacheModelListener<String> {

	// Model
	private CostCenter1Model costCenter1Model;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception 
	 */
	public CostCenterTableComposite(Composite parent, int style) throws Exception {
		super(parent, style);
	}

	
	public void initializeTableViewer(int style) {
		if (tableViewer == null) {
			TableColumnLayout layout = new TableColumnLayout();
			setLayout(layout);
			Table table = new Table(this, style);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			final TableColumn idTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(idTableColumn, new ColumnWeightData(60));
			
			idTableColumn.setText(UtilI18N.Number);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(200));
			nameTableColumn.setText(UtilI18N.Name);

			final CostCenterTable costCenter1Table = new CostCenterTable(table);
			tableViewer = costCenter1Table.getViewer();
		}
	}

	
	protected Collection<CostCenterVO> getModelData() {
		Collection<CostCenterVO> modelData = null;
		try {
			modelData = costCenter1Model.getAllCostCenterVOs();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}
	
	
	protected void initModel() {
		costCenter1Model = CostCenter1Model.getInstance();
		costCenter1Model.addListener(this);
	}
	
	
	protected void disposeModel() {
		if (costCenter1Model != null) {
			costCenter1Model.removeListener(this);
		}
	}

	
	public void dataChange(CacheModelEvent<String> event) {
		try {
			handleModelChange();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

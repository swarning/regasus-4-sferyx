package de.regasus.finance.customeraccount.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.invoice.data.CustomerAccountVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.CustomerAccountModel;
import de.regasus.ui.Activator;

public class CustomerAccountTableComposite
extends AbstractTableComposite<CustomerAccountVO>
implements CacheModelListener<String> {

	// Model
	private CustomerAccountModel customerAccountModel;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception 
	 */
	public CustomerAccountTableComposite(Composite parent, int style) throws Exception {
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

			final CustomerAccountTable customerAccountTable = new CustomerAccountTable(table);
			tableViewer = customerAccountTable.getViewer();
		}
	}

	
	protected Collection<CustomerAccountVO> getModelData() {
		Collection<CustomerAccountVO> modelData = null;
		try {
			modelData = customerAccountModel.getAllCustomerAccountVOs();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}
	
	
	protected void initModel() {
		customerAccountModel = CustomerAccountModel.getInstance();
		customerAccountModel.addListener(this);
	}
	
	
	protected void disposeModel() {
		if (customerAccountModel != null) {
			customerAccountModel.removeListener(this);
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

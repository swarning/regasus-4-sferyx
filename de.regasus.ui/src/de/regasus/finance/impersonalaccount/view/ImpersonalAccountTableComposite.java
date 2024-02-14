package de.regasus.finance.impersonalaccount.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.ImpersonalAccountVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.ImpersonalAccountModel;
import de.regasus.ui.Activator;

public class ImpersonalAccountTableComposite
extends AbstractTableComposite<ImpersonalAccountVO>
implements CacheModelListener<String> {

	// Model
	private ImpersonalAccountModel impersonalAccountModel;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ImpersonalAccountTableComposite(Composite parent, int style) throws Exception {
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

			final TableColumn idTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(idTableColumn, new ColumnWeightData(80));

			idTableColumn.setText(UtilI18N.Number);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(200));
			nameTableColumn.setText(UtilI18N.Name);

			final TableColumn financeAccountTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(financeAccountTableColumn, new ColumnWeightData(80));
			financeAccountTableColumn.setText(InvoiceLabel.FinanceAccount.getString());

			final ImpersonalAccountTable impersonalAccountTable = new ImpersonalAccountTable(table);
			tableViewer = impersonalAccountTable.getViewer();
		}
	}


	@Override
	protected Collection<ImpersonalAccountVO> getModelData() {
		Collection<ImpersonalAccountVO> modelData = null;
		try {
			modelData = impersonalAccountModel.getAllImpersonalAccountVOs();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		impersonalAccountModel = ImpersonalAccountModel.getInstance();
		impersonalAccountModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (impersonalAccountModel != null) {
			impersonalAccountModel.removeListener(this);
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

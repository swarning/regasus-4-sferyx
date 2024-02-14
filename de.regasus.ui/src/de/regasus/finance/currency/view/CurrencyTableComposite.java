package de.regasus.finance.currency.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.CurrencyVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.finance.CurrencyModel;

public class CurrencyTableComposite
extends AbstractTableComposite<CurrencyVO>
implements CacheModelListener<String> {

	private final static int NAME_COLUMN_WEIGHT = 30;
	private final static int DESCRIPTION_COLUMN_WEIGHT = 300;
	
	// Model
	private CurrencyModel currencyModel;

	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception 
	 */
	public CurrencyTableComposite(Composite parent, int style) throws Exception {
		super(parent, style);
	}

	
	public void initializeTableViewer(int style) {
		if (tableViewer == null) {
			TableColumnLayout layout = new TableColumnLayout();
			setLayout(layout);
			Table table = new Table(this, style);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(NAME_COLUMN_WEIGHT));
			nameTableColumn.setText(InvoiceLabel.Currency.getString());

			final TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(descriptionTableColumn, new ColumnWeightData(DESCRIPTION_COLUMN_WEIGHT));
			descriptionTableColumn.setText(UtilI18N.Description);

			final CurrencyTable currencyTable = new CurrencyTable(table);
			tableViewer = currencyTable.getViewer();
		}
	}

	
	protected Collection<CurrencyVO> getModelData() {
		Collection<CurrencyVO> modelData = null;
		try {
			modelData = currencyModel.getAllCurrencyVOs();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}
	
	
	protected void initModel() {
		currencyModel = CurrencyModel.getInstance();
		currencyModel.addListener(this);
	}
	
	
	protected void disposeModel() {
		if (currencyModel != null) {
			currencyModel.removeListener(this);
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

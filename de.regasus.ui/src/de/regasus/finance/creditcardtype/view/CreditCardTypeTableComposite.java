package de.regasus.finance.creditcardtype.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.contact.data.CreditCardTypeVO;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.CreditCardTypeModel;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.ui.Activator;

public class CreditCardTypeTableComposite
extends AbstractTableComposite<CreditCardTypeVO>
implements CacheModelListener<String> {

	// Model
	private CreditCardTypeModel creditCardTypeModel;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public CreditCardTypeTableComposite(Composite parent, int style) throws Exception {
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
			layout.setColumnData(idTableColumn, new ColumnWeightData(200));

			idTableColumn.setText(UtilI18N.Name);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(60));
			nameTableColumn.setText(UtilI18N.Mnemonic);

			final CreditCardTypeTable creditCardTypeTable = new CreditCardTypeTable(table);
			tableViewer = creditCardTypeTable.getViewer();
		}
	}


	@Override
	protected Collection<CreditCardTypeVO> getModelData() {
		Collection<CreditCardTypeVO> modelData = null;
		try {
			modelData = creditCardTypeModel.getAllCreditCardTypeVOs();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		creditCardTypeModel = CreditCardTypeModel.getInstance();
		creditCardTypeModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (creditCardTypeModel != null) {
			creditCardTypeModel.removeListener(this);
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

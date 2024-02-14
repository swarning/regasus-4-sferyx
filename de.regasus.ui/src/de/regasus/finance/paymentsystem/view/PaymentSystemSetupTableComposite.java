package de.regasus.finance.paymentsystem.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.finance.PaymentSystemSetup;
import de.regasus.finance.PaymentSystemSetupModel;
import de.regasus.finance.easycheckout.EasyCheckoutSetup;

public class PaymentSystemSetupTableComposite
extends AbstractTableComposite<PaymentSystemSetup>
implements CacheModelListener<Long> {

	private final static int NAME_COLUMN_WEIGHT = 80;
	private final static int PAYMENT_SYSTEM_COLUMN_WEIGHT = 80;
	private final static int TEST_COLUMN_WEIGHT = 40;

	// Model
	private PaymentSystemSetupModel paymentSystemSetupModel;


	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public PaymentSystemSetupTableComposite(Composite parent, int style) throws Exception {
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
			nameTableColumn.setText( PaymentSystemSetup.NAME.getString() );

			final TableColumn paymentSystemTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(paymentSystemTableColumn, new ColumnWeightData(PAYMENT_SYSTEM_COLUMN_WEIGHT));
			paymentSystemTableColumn.setText( PaymentSystemSetup.PAYMENT_SYSTEM.getString() );

			final TableColumn testTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(testTableColumn, new ColumnWeightData(TEST_COLUMN_WEIGHT));
			// use the label of EasyCheckoutSetup, we could use PayEngineSetup.TEST_ONLY as well
			testTableColumn.setText( EasyCheckoutSetup.TEST_ENVIRONMENT.getString() );

			final PaymentSystemSetupTable paymentSystemSetupTable = new PaymentSystemSetupTable(table);
			tableViewer = paymentSystemSetupTable.getViewer();
		}
	}


	@Override
	protected Collection<PaymentSystemSetup> getModelData() {
		Collection<PaymentSystemSetup> modelData = null;
		try {
			modelData = paymentSystemSetupModel.getAllPaymentSystemSetups();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		paymentSystemSetupModel = PaymentSystemSetupModel.getInstance();
		paymentSystemSetupModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (paymentSystemSetupModel != null) {
			paymentSystemSetupModel.removeListener(this);
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

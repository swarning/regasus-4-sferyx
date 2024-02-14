package de.regasus.common.country.view;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.kernel.KernelLabel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.common.Country;
import de.regasus.core.CountryModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class CountryTableComposite extends AbstractTableComposite<Country> implements CacheModelListener<String> {

	// Model
	private CountryModel countryModel;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public CountryTableComposite(Composite parent, int style) throws Exception {
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
			layout.setColumnData(idTableColumn, new ColumnWeightData(60));

			idTableColumn.setText(KernelLabel.Mnemonic.getString());

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(200));
			nameTableColumn.setText( ContactLabel.Country.getString() );

			final CountryTable countryTable = new CountryTable(table);
			tableViewer = countryTable.getViewer();
		}
	}


	@Override
	protected Collection<Country> getModelData() {
		List<Country> modelData = null;
		try {
			modelData = countryModel.getAllUndeletedCountries();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		countryModel = CountryModel.getInstance();
		countryModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (countryModel != null) {
			countryModel.removeListener(this);
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

package de.regasus.hotel.chain.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.hotel.HotelLabel;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.hotel.HotelChain;
import de.regasus.hotel.HotelChainModel;

public class HotelChainTableComposite extends AbstractTableComposite<HotelChain> implements CacheModelListener<Long> {

	private final static int NAME_COLUMN_WEIGHT = 100;

	// Model
	private HotelChainModel hotelChainModel;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public HotelChainTableComposite(Composite parent, int style) throws Exception {
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
			nameTableColumn.setText(HotelLabel.HotelChain.getString());

			final HotelChainTable hotelChainTable = new HotelChainTable(table);
			tableViewer = hotelChainTable.getViewer();
		}
	}


	@Override
	protected Collection<HotelChain> getModelData() {
		Collection<HotelChain> modelData = null;
		try {
			modelData = hotelChainModel.getAllHotelChains();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		hotelChainModel = HotelChainModel.getInstance();
		hotelChainModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (hotelChainModel != null) {
			hotelChainModel.removeListener(this);
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

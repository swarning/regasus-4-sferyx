package de.regasus.common.language.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.common.Language;
import de.regasus.core.LanguageModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;

public class LanguageTableComposite extends AbstractTableComposite<Language> implements CacheModelListener<String> {

	// Model
	private LanguageModel languageModel;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public LanguageTableComposite(Composite parent, int style) throws Exception {
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

			idTableColumn.setText(UtilI18N.Mnemonic);

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(200));
			nameTableColumn.setText(UtilI18N.Name);

			final LanguageTable languageTable = new LanguageTable(table);
			tableViewer = languageTable.getViewer();
		}
	}


	@Override
	protected Collection<Language> getModelData() {
		Collection<Language> modelData = null;
		try {
			modelData = languageModel.getAllUndeletedLanguages();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		languageModel = LanguageModel.getInstance();
		languageModel.addListener(this);

	}


	@Override
	protected void disposeModel() {
		if (languageModel != null) {
			languageModel.removeListener(this);
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

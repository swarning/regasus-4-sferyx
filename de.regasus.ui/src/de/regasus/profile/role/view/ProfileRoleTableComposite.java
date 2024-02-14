package de.regasus.profile.role.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.profile.ProfileRole;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.I18N;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.profile.ProfileRoleModel;

public class ProfileRoleTableComposite 
extends AbstractTableComposite<ProfileRole> 
implements CacheModelListener<Long> {
	
	private static final int NAME_COLUMN_WEIGHT = 1;
	private static final int DESC_COLUMN_WEIGHT = 2;

	
	private ProfileRoleModel profileRoleModel;
	

	public ProfileRoleTableComposite(Composite parent, int style) throws Exception {
		super(parent, style);
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
			nameTableColumn.setText(I18N.ProfileRole_Name);
			
			TableColumn descTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(descTableColumn, new ColumnWeightData(DESC_COLUMN_WEIGHT));
			descTableColumn.setText(I18N.ProfileRole_Desc);
			

			ProfileRoleTable profileRoleTable = new ProfileRoleTable(table);
			tableViewer = profileRoleTable.getViewer();
		}
	}


	@Override
	protected Collection<ProfileRole> getModelData() throws Exception {
		Collection<ProfileRole> modelData = null;
		try {
			modelData = profileRoleModel.getAllProfileRoles();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		profileRoleModel = ProfileRoleModel.getInstance();
		profileRoleModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (profileRoleModel != null) {
			profileRoleModel.removeListener(this);
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

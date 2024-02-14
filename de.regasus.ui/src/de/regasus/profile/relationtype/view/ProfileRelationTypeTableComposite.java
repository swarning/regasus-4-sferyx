package de.regasus.profile.relationtype.view;

import java.util.Collection;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.profile.ProfileLabel;
import com.lambdalogic.messeinfo.profile.ProfileRelationType;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.AbstractTableComposite;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.profile.ProfileRelationTypeModel;

public class ProfileRelationTypeTableComposite 
extends AbstractTableComposite<ProfileRelationType> 
implements CacheModelListener<Long> {
	
	private static final int NAME_COLUMN_WEIGHT = 50;
	private static final int ROLE1_COLUMN_WEIGHT = 50;
	private static final int ROLE2_COLUMN_WEIGHT = 50;
	private static final int DESC12_COLUMN_WEIGHT = 80;
	private static final int DESC21_COLUMN_WEIGHT = 80;
	
	private ProfileRelationTypeModel profileRelationTypeModel;
	

	public ProfileRelationTypeTableComposite(Composite parent, int style) throws Exception {
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

			final TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(nameTableColumn, new ColumnWeightData(NAME_COLUMN_WEIGHT));
			nameTableColumn.setText(ProfileLabel.ProfileRelationType_Name.getString());
			
			final TableColumn role1TableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(role1TableColumn, new ColumnWeightData(ROLE1_COLUMN_WEIGHT));
			role1TableColumn.setText(ProfileLabel.ProfileRelationType_Role1.getString());
			
			final TableColumn role2TableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(role2TableColumn, new ColumnWeightData(ROLE2_COLUMN_WEIGHT));
			role2TableColumn.setText(ProfileLabel.ProfileRelationType_Role2.getString());
			
			final TableColumn desc12TableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(desc12TableColumn, new ColumnWeightData(DESC12_COLUMN_WEIGHT));
			desc12TableColumn.setText(ProfileLabel.ProfileRelationType_Desc12.getString());
			
			final TableColumn desc21TableColumn = new TableColumn(table, SWT.NONE);
			layout.setColumnData(desc21TableColumn, new ColumnWeightData(DESC21_COLUMN_WEIGHT));
			desc21TableColumn.setText(ProfileLabel.ProfileRelationType_Desc21.getString());
			
			final ProfileRelationTypeTable profileRelationTypeTable = new ProfileRelationTypeTable(table);
			tableViewer = profileRelationTypeTable.getViewer();
		}
	}


	@Override
	protected Collection<ProfileRelationType> getModelData() throws Exception {
		Collection<ProfileRelationType> modelData = null;
		try {
			modelData = profileRelationTypeModel.getAllProfileRelationTypes();
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
		return modelData;
	}


	@Override
	protected void initModel() {
		profileRelationTypeModel = ProfileRelationTypeModel.getInstance();
		profileRelationTypeModel.addListener(this);
	}


	@Override
	protected void disposeModel() {
		if (profileRelationTypeModel != null) {
			profileRelationTypeModel.removeListener(this);
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

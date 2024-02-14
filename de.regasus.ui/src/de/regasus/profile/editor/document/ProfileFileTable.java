package de.regasus.profile.editor.document;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.Activator;

import de.regasus.common.FileSummary;
import de.regasus.common.composite.FileTable;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.profile.ProfileFileModel;


public class ProfileFileTable extends FileTable {

	public ProfileFileTable(Table table) {
		super(table);
	}


	@Override
	protected boolean updateOnChange(FileSummary fileSummary){
		boolean updated = false;
		try {
			ProfileFileModel.getInstance().update(fileSummary);
			updated = true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return updated;
	}

}

package de.regasus.participant.editor.document;

import org.eclipse.swt.widgets.Table;

import com.lambdalogic.util.rcp.Activator;

import de.regasus.common.FileSummary;
import de.regasus.common.composite.FileTable;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantFileModel;


public class ParticipantFileTable extends FileTable {

	public ParticipantFileTable(Table table) {
		super(table);
	}


	@Override
	protected boolean updateOnChange(FileSummary fileSummary){
		boolean updated = false;
		try {
			ParticipantFileModel.getInstance().update(fileSummary);
			updated = true;
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		return updated;
	}

}

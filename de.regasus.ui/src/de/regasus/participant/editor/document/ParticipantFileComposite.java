package de.regasus.participant.editor.document;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.lambdalogic.messeinfo.participant.Participant;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.FileSummary;
import de.regasus.common.composite.FileTable;
import de.regasus.common.composite.PersonDocumentComposite;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.participant.ParticipantFileModel;
import de.regasus.ui.Activator;

public class ParticipantFileComposite extends PersonDocumentComposite {

	// entity
	private Participant participant;

	// model
	private ParticipantFileModel participantFileModel;


	public ParticipantFileComposite(final Composite tabFolder, int style) {
		super(tabFolder, style);

		participantFileModel = ParticipantFileModel.getInstance();
	}


	@Override
	protected void createPartControl() throws Exception {
		super.createPartControl();

		participantFileModel.addForeignKeyListener(this, participant.getID());
	}


	@Override
	protected FileTable createSimpleTable(Table table) {
		return new ParticipantFileTable(table);
	}


	@Override
	protected void details() {
		ISelection selection = fileTable.getViewer().getSelection();
		FileSummary fileSummary = SelectionHelper.getUniqueSelected(selection);

		ParticipantFileDetailsDialog dialog = new ParticipantFileDetailsDialog(getShell(), fileSummary);
		dialog.create();
		dialog.getShell().setSize(600, 500);
		dialog.open();
	}


	@Override
	protected void upload() {
		UploadParticipantFileDialog dialog = new UploadParticipantFileDialog(getShell(), participant.getID());
		dialog.create();
		dialog.getShell().setSize(600, 400);
		dialog.open();
	}


	@Override
	protected void delete() {
		ISelection selection = fileTable.getViewer().getSelection();
		FileSummary fileSummary = SelectionHelper.getUniqueSelected(selection);
		String message = NLS.bind(UtilI18N.ReallyDeleteOne, UtilI18N.Document, fileSummary.getName());
		boolean answer = MessageDialog.openQuestion(getShell(), UtilI18N.Question, message);
		if (answer) {
			try {
				participantFileModel.delete(fileSummary);
				syncWidgetsToEntity();
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	@Override
	protected void syncWidgetsToEntity() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					if ( ! isDisposed()) {
    					if (participant != null && participant.getID() != null) {
    						Long participantID = participant.getID();
    						List<FileSummary> fileSummaryList = participantFileModel.getParticipantFileSummaryListByParticipantId(participantID);
    						fileTable.setInput(fileSummaryList);
    					}

    					updateButtonStates();
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	public Participant getParticipant() {
		return participant;
	}


	public void setParticipant(Participant participant) {
		if (participant == null) {
			throw new IllegalArgumentException("Parameter 'participant' must not be null");
		}

		if (this.participant != null &&
			this.participant.getID() != null &&
			! this.participant.getID().equals(participant.getID())
		) {
			throw new IllegalArgumentException("Participant.ID must not change");
		}

		this.participant = participant;

		if (isInitialized()) {
			syncWidgetsToEntity();
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (participant != null && participant.getID() != null) {
			participantFileModel.removeForeignKeyListener(this, participant.getID());
		}
	}

}

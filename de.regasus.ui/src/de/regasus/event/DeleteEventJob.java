package de.regasus.event;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.lambdalogic.messeinfo.participant.data.EventVO;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;
import de.regasus.event.EventModel;

/**
 * A job that asks the server to delete an Event.
 */
public class DeleteEventJob extends Job {

	EventVO eventVO;


	public DeleteEventJob(String name, EventVO eventVO) {
		super(name);
		this.eventVO = eventVO;
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			EventModel.getInstance().delete(eventVO);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

}

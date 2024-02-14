package de.regasus.core;

import static de.regasus.LookupService.*;

import java.util.Date;

import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;

/**
 * A runnable that is called every 60 seconds to renew (or recreate if possible) a session on the server.
 *
 * @author manfred
 */
public class SessionRenewer implements Runnable {

	private final ServerModel serverModel;


	public SessionRenewer(ServerModel serverModel) {
		this.serverModel = serverModel;
	}


	@Override
	public void run() {
		try {
			if (getSessionMgr() == null) {
				/*
				 * Maybe the netwock cable is unplugged, we don't try to renew session for now, the user can't work
				 * anyway.
				 */
				return;
			}

			boolean renewed = getSessionMgr().renewSession(serverModel.sessionID);

			/*
			 * The session might not be renewed when this clients renewal didn't reach the server for some time (maybe
			 * network cable unplugged) and another user logged in and removed this ones session.
			 */
			if (!renewed) {

				try {
					// Try to create new session without that the user needs to know
					Long newSessionID = getSessionMgr().createSession(serverModel.getUser(), serverModel.getLocalHostName());

					// If a new session could be created, store its ID for the next renewal
					serverModel.sessionID = newSessionID;
				}
				catch (Exception e) {
					// Tell the user that the session could not be renewed, presumably because there are no more
					// sessions
					RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);

					// Logout, but in the Display thread
					SWTHelper.asyncExecDisplayThread(new Runnable() {
						@Override
						public void run() {
							serverModel.logout();
						}
					});
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception when trying to renew session at " + new Date());
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

	}

}

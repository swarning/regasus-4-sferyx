package de.regasus.core;

import de.regasus.client.JBossClient;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.Activator;

public class ServerModelData implements Cloneable {
	private JBossClient client;
	private boolean shutdown = false;


	public ServerModelData() {
	}

	public ServerModelData(JBossClient client, String host) {
		this.client = client;
	}


	@Override
	public ServerModelData clone() {
		try {
			return (ServerModelData) super.clone();
		}
		catch (CloneNotSupportedException e) {
			RegasusErrorHandler.handleApplicationError(Activator.PLUGIN_ID, getClass().getName(), e);
			return null;
		}
	}

	public JBossClient getClient() {
		return client;
	}

	public void setClient(JBossClient client) {
		this.client = client;
	}


	public boolean isConnected() {
		return client != null && client.isLoggedIn();
	}


	public String getUser() {
		String user = null;
		if (client != null) {
			user = client.getUserName();
		}
		return user;
	}


	public String getHost() {
		String host = null;
		if (client != null) {
			host = client.getHost();
		}
		return host;
	}


	public String getHostWithoutPath() {
		String host = getHost();
		if (host != null) {
			if ( host.endsWith(JBossClient.STANDARD_PATH) ) {
				host = host.substring(0, host.length() - JBossClient.STANDARD_PATH.length());
			}
		}
		return host;
	}


	public boolean isShutdown() {
		return shutdown;
	}


	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

}

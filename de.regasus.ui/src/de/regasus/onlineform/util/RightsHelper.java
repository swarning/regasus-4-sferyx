package de.regasus.onlineform.util;

import de.regasus.core.ServerModel;

public class RightsHelper {


	/**
	 * Currently, creation of of online forms is only allowed to the admin.
	 * <p>
	 * The check for the presence of the plugin com.lambdalogic.mi.formedit.admin is 
	 * not done anymore, as it is part of any client installation anyhow.
	 * 
	 */
	public static boolean isCreateAllowed() {
		boolean allow = false;

		String user = ServerModel.getInstance().getUser();
		if ("admin".equals(user)) {
			allow = true;
		}

		return allow;
	}
}

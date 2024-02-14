package de.regasus.core.ui;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.lambdalogic.util.Version;


public class ClientVersionFactory {

	public static Version getInstance() {
		Version version = new Version("de.regasus.core.ui");

		String productVersion = getProductVersion();
		System.out.println("Version: " + productVersion);

		String[] versionArray = productVersion.split("\\.");
		if (versionArray != null) {
			if (versionArray.length > 0) {
				version.setMain(Integer.parseInt(versionArray[0]));
			}
			if (versionArray.length > 1) {
				version.setMajor(Integer.parseInt(versionArray[1]));
			}
			if (versionArray.length > 2) {
				version.setMinor(Integer.parseInt(versionArray[2]));
			}
			if (versionArray.length > 3) {
				version.setBuild(Integer.parseInt(versionArray[3]));
			}
		}

		return version;
	}


	public static String getProductVersion() {
		String version = null;

		try {
			Bundle bundle = Platform.getBundle("de.regasus.core.ui");
			version = bundle.getHeaders().get("Bundle-Version").toString();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		return version;
	}

}

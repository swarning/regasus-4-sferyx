package com.lambdalogic.util.rcp;

import org.eclipse.swt.SWT;

public class OsHelper {

    public static final String WS_WIN32 = "win32";
    public static final String WS_MOTIF = "motif";
    public static final String WS_GTK = "gtk";
    public static final String WS_PHOTON = "photon";
    public static final String WS_CARBON = "carbon";
    public static final String WS_COCOA = "cocoa";
    public static final String WS_WPF = "wpf";
    public static final String WS_UNKNOWN = "unknown";

    public static final boolean IS_WINDOWS;
    public static final boolean IS_MAC;
    public static final boolean IS_LINUX;
    static {
    	final String ws = SWT.getPlatform();

    	IS_WINDOWS = WS_WIN32.equals(ws) || WS_WPF.equals(ws);
    	IS_MAC = WS_CARBON.equals(ws) || WS_COCOA.equals(ws);
    	IS_LINUX = WS_MOTIF.equals(ws) || WS_GTK.equals(ws);
    }


	public static final boolean isWindows() {
		return IS_WINDOWS;
	}


	public static final boolean isMac() {
		return IS_MAC;
	}


	public static final boolean isLinux() {
		return IS_LINUX;
	}

}

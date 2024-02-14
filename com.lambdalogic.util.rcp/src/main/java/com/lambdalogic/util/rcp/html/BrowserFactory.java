package com.lambdalogic.util.rcp.html;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.html.pref.HtmlPreference;

public class BrowserFactory {

	public static Browser createBrowser(Composite parent) {
		return createBrowser(parent, SWT.NONE);
	}


	public static Browser createBrowser(Composite parent, int style) {
		// Decide whether SWT.WEBKIT or SWT.MOZILLA should (and may) be set.

		com.lambdalogic.util.rcp.html.pref.Browser desiredBrowser = HtmlPreference.getInstance().getBrowser();

		System.out.println("Desired Browser is " + desiredBrowser);

		if (desiredBrowser == null) {
			desiredBrowser = com.lambdalogic.util.rcp.html.pref.Browser.DEFAULT;
			System.out.println("No Browser yet preferred; choosing DEFAULT");
		}

		if (desiredBrowser == com.lambdalogic.util.rcp.html.pref.Browser.WEBKIT) {
			style |= SWT.WEBKIT;
			System.out.println("Setting Browser style to WEBKIT");
		}

		if (style == SWT.NONE) {
			System.out.println("Using default Browser style");
		}

		Browser browser = null;

		try {
			browser = new Browser(parent, style);
		}
		catch (SWTError e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		if (browser == null) {
			System.out.println("Fallback to default Browser style");
			browser = new Browser(parent, style);
		}

		System.out.println("Created browser type: " + browser.getBrowserType());

		return browser;
	}

}

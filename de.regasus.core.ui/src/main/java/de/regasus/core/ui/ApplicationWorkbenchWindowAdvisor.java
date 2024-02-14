package de.regasus.core.ui;

import static de.regasus.LookupService.*;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.lambdalogic.i18n.I18NString;
import com.lambdalogic.util.Version;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
	public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

        // Switch off the old style of the tabs (angular): they appear round
        String propStyle = IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS;
		PlatformUI.getPreferenceStore().setValue(propStyle, false);

		// Activate the dock and display it at the top right
		PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR, IWorkbenchPreferenceConstants.TOP_RIGHT);

		// After having switched to Eclipse 3.5, we use this to make the Perspective Bar wider
		PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_SIZE, 400);

        configurer.setInitialSize(new Point(1024, 768));
        configurer.setShowCoolBar(true);
        configurer.setShowMenuBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowPerspectiveBar(true);

        // now set by Product Configuration
        //configurer.setTitle("MesseInfo");

        // enable the Progress Indicator, needed by UpdateAction
        configurer.setShowProgressIndicator(true);

        // Switches on a theme that makes the view tabs in the "LambdaLogic" colors of the welcome page
        PlatformUI.getWorkbench().getThemeManager().setCurrentTheme("de.regasus.core.ui.theme");


        checkVersion();
    }


    private void checkVersion() {
		try {
			if (ServerModel.getInstance().isLoggedIn()) {
				// determine version of client
				Version clientVersion = ClientVersionFactory.getInstance();

				// let server check if the version of the client is compatible with its own version
				List<I18NString> versionWarnings = getKernelMgr().getVersionWarnings(clientVersion);

				if (versionWarnings != null) {
					for (I18NString msg : versionWarnings) {
						final String finalMsg = msg.getString();
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialog.openWarning(null, UtilI18N.Warning, finalMsg);
							}
						});
					}
				}
			}
		}
		catch (Throwable t) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
    }

}

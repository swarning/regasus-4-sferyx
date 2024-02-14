package de.regasus.core.ui;

import static org.eclipse.ui.IWorkbenchActionConstants.*;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.action.ChangePasswordAction;
import de.regasus.core.ui.action.EditPropertiesAction;
import de.regasus.core.ui.action.LoginAction;
import de.regasus.core.ui.action.LogoutAction;
import de.regasus.core.ui.statusline.CountHandlesStatusLineContribution;
import de.regasus.core.ui.statusline.ServerStatusLineContribution;


public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	public static final String SYSTEM_MENU_ID = "System";

	public static final String DOCUMENT_MENU_ID = "Document";
	public static final String SYSTEM_MENU_DOCUMENT_ADDITIONS = "DocumentAdditions";

	public static final String TEMPLATE_MENU_ID = "Template";
	public static final String SYSTEM_MENU_TEMPLATE_ADDITIONS = "TemplateAdditions";

	public static final String SYSTEM_MENU_CONFIG_ADDITIONS = "ConfigAdditions";
	public static final String SYSTEM_MENU_EXPORT_ADDITIONS = "ExportAdditions";
	public static final String SYSTEM_MENU_FINANCE_EXPORT_ADDITIONS = "FinanceExportAdditions";
	public static final String SYSTEM_MENU_PRIVACY_ADDITIONS = "PrivacyAdditions";

	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;

	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;

	//TODO delete it
//	Show this action only when there is maintained intro content
//	private IWorkbenchAction welcomeAction;

	private IWorkbenchAction preferencesAction;

	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;

	private ChangePasswordAction changePasswordAction;
	private Action loginAction;
	private Action logoutAction;

	private IContributionItem perspectivesMenu;
	private IContributionItem viewsMenu;
	private IWorkbenchAction resetPerspectiveAction;

	private EditPropertiesAction editPropertiesAction;


    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }


    @Override
	protected void makeActions(IWorkbenchWindow window) {
    	// Standard
    	saveAction = ActionFactory.SAVE.create(window);
    	register(saveAction);
    	saveAllAction  = ActionFactory.SAVE_ALL.create(window);
    	register(saveAllAction);

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);

		// Close
		closeAction = ActionFactory.CLOSE.create(window);
		register(closeAction);
		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		register(closeAllAction);

		// Edit
		cutAction = ActionFactory.CUT.create(window);
    	register(cutAction);
		copyAction = ActionFactory.COPY.create(window);
		register(copyAction);
		pasteAction = ActionFactory.PASTE.create(window);
		register(pasteAction);

		preferencesAction = ActionFactory.PREFERENCES.create(window);
		register(preferencesAction);

		//TODO delete it
//		Show this action only when there is maintained intro content
// 		Seems not to work when no intro-plugin is properly configured
//		try {
//			welcomeAction = ActionFactory.INTRO.create(window);
//			register(welcomeAction);
//		}
//		catch (Exception e) {
//			System.err.println("Couldn't create INTRO-Action: " + e.getMessage());
//		}
//
		loginAction = new LoginAction();
		register (loginAction);
		logoutAction = new LogoutAction();
		register (logoutAction);

		changePasswordAction = new ChangePasswordAction();
		register(changePasswordAction);

		perspectivesMenu = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
		viewsMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);

		editPropertiesAction = new EditPropertiesAction(window);
		register(editPropertiesAction);
    }


    @Override
	protected void fillMenuBar(IMenuManager menuBar) {
    	// MesseInfo
		MenuManager systemMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_SystemMenu, SYSTEM_MENU_ID);

		//TODO delete it
//		Show this action only when there is maintained intro content
//		if (welcomeAction != null) {
//			messeInfoMenu.add(welcomeAction);
//			messeInfoMenu.add(new Separator());
//		}

		systemMenu.add(loginAction);
		systemMenu.add(logoutAction);
		systemMenu.add(changePasswordAction);

		systemMenu.add(new Separator());
		systemMenu.add(saveAction);
		systemMenu.add(saveAllAction);

		systemMenu.add(new Separator());
		systemMenu.add(closeAction);
		systemMenu.add(closeAllAction);

		systemMenu.add(new Separator());
		systemMenu.add(preferencesAction);
		systemMenu.add(editPropertiesAction);

		// sub-menu for uploading documents
		MenuManager documentMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_DocumentMenu, DOCUMENT_MENU_ID);
		documentMenu.add(new GroupMarker(SYSTEM_MENU_DOCUMENT_ADDITIONS));
		systemMenu.add(documentMenu);

		// sub-menu for uploading templates
		MenuManager templateMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_TemplateMenu, TEMPLATE_MENU_ID);
		templateMenu.add(new GroupMarker(SYSTEM_MENU_TEMPLATE_ADDITIONS));
		systemMenu.add(templateMenu);

		systemMenu.add(new Separator());
		systemMenu.add(new GroupMarker(SYSTEM_MENU_CONFIG_ADDITIONS));
		systemMenu.add(new Separator());
		systemMenu.add(new GroupMarker(SYSTEM_MENU_EXPORT_ADDITIONS));
		systemMenu.add(new Separator());
		systemMenu.add(new GroupMarker(SYSTEM_MENU_FINANCE_EXPORT_ADDITIONS));
		systemMenu.add(new Separator());
		systemMenu.add(new GroupMarker(SYSTEM_MENU_PRIVACY_ADDITIONS));
		systemMenu.add(new Separator());
		systemMenu.add(new GroupMarker(MB_ADDITIONS));
		systemMenu.add(new Separator());
		systemMenu.add(exitAction);

		// Edit
		MenuManager editMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_EditMenu, M_FILE);
		editMenu.add(cutAction);
		editMenu.add(copyAction);
		editMenu.add(pasteAction);

		// Window
		MenuManager windowMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_WindowMenu, M_WINDOW);

		// Window.OpenPerspective
		MenuManager openPerspectiveMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_OpenPerspectiveMenu);
		windowMenu.add(openPerspectiveMenu);
		openPerspectiveMenu.add(perspectivesMenu);

		MenuManager showViewMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_ShowViewMenu);
		windowMenu.add(showViewMenu);
		showViewMenu.add(viewsMenu);

		windowMenu.add(new Separator());
		windowMenu.add(resetPerspectiveAction);

		// Help-Menu
		MenuManager helpMenu = new MenuManager(CoreI18N.ApplicationActionBarAdvisor_HelpMenu, M_HELP);
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add(new Separator());
		helpMenu.add(aboutAction);

		// add Menus
		menuBar.add(systemMenu);
		menuBar.add(editMenu);
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
    }


    @Override
	protected void fillCoolBar(ICoolBarManager coolBarManager) {
    	IToolBarManager mainToolBar = new ToolBarManager(SWT.FLAT);
    	coolBarManager.add(mainToolBar);
    }


    /**
     * Creates the 2 status line contributions from this plugin, as well as all status lines
     * contributions that are registered elsewhere as extensions to the extension point
     * de.regasus.core.ui.statusLineContribution
     */
    @Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
    	statusLine.appendToGroup(StatusLineManager.BEGIN_GROUP, CountHandlesStatusLineContribution.getInstance());
    	statusLine.add(new ServerStatusLineContribution());

		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(Activator.PLUGIN_ID, "statusLineContribution");
		try {
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions != null) {
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] configurationElements = extensions[i].getConfigurationElements();
					for (IConfigurationElement configurationElement : configurationElements) {
						if (configurationElement.getName().equals("contributionItem")) {
							IContributionItem contributionItem = (IContributionItem) configurationElement.createExecutableExtension("class");
							statusLine.add(contributionItem);

						}
					}
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

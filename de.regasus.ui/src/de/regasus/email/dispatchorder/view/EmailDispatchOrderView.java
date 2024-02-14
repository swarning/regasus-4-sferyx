package de.regasus.email.dispatchorder.view;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.lambdalogic.util.model.ModelEvent;
import com.lambdalogic.util.model.ModelListener;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.model.ServerModelEvent;
import de.regasus.core.model.ServerModelEventType;
import de.regasus.email.dispatchorder.view.pref.EmailDispatchOrderViewPreference;
import de.regasus.ui.Activator;

/**
 * A view showing the {@link EmailDispatchOrderComposite}, which in turn shows an EventCombo and an
 * EmailDispatchOrderTreeTable.
 *
 * @author manfred
 *
 */
public class EmailDispatchOrderView extends ViewPart {

	public static final String ID = "EmailDispatchOrderView";

	private EmailDispatchOrderViewPreference preference;


	// *************************************************************************
	// * Actions
	// *

	private RefreshEmailDispatchOrderTreeTableAction refreshAction;
	private CancelScheduledEmailDispatchOrderOrDispatchAction cancelAction;

	private EmailDispatchOrderComposite emailDispatchOrderComposite;


	private ShowOnlyUnsuccessfulEmailDispatchesAction showOnlyUnsuccessfulEmailDispatchesAction;


	public EmailDispatchOrderView() {
		preference = EmailDispatchOrderViewPreference.getInstance();

		preference.getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				initFromPreferences();
			}
		});
	}


	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		emailDispatchOrderComposite = new EmailDispatchOrderComposite(parent, SWT.NONE);

		createActions();
		initializeMenu();
		initializeToolBar();
		hookContextMenu();

		showOnlyUnsuccessfulEmailDispatchesAction.setTreeTable(emailDispatchOrderComposite.getTreeTable());

		getSite().setSelectionProvider(emailDispatchOrderComposite.getTreeTable().getTreeViewer());

		initFromPreferences();

		// observer ServerModel to init from preferences on login and save t preferences on logout
		ServerModel.getInstance().addListener(serverModelListener);
	}


	@Override
	public void dispose() {
		try {
			ServerModel.getInstance().removeListener(serverModelListener);
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		super.dispose();
	}


	@Override
	public void setFocus() {
		try {
			if (emailDispatchOrderComposite != null && !emailDispatchOrderComposite.isDisposed()  && emailDispatchOrderComposite.isEnabled() ) {
				emailDispatchOrderComposite.setFocus();
			}
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	/**
	 * Create the actions
	 */
	private void createActions() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();


		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());

		cancelAction = new CancelScheduledEmailDispatchOrderOrDispatchAction(window);

		refreshAction = new RefreshEmailDispatchOrderTreeTableAction();

		showOnlyUnsuccessfulEmailDispatchesAction = new ShowOnlyUnsuccessfulEmailDispatchesAction();
	}


	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				EmailDispatchOrderView.this.fillContextMenu(manager);
			}
		});
		TreeViewer viewer = emailDispatchOrderComposite.getTreeTable().getTreeViewer();
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		manager.add(showOnlyUnsuccessfulEmailDispatchesAction);
		manager.add(new Separator());
		manager.add(cancelAction);
		manager.add(new Separator());
		manager.add(refreshAction);
	}


	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

		menuManager.add(new Separator());
		menuManager.add(cancelAction);
		menuManager.add(new Separator());
		menuManager.add(refreshAction);

	}


	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(cancelAction);
		manager.add(new Separator());
		manager.add(refreshAction);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}


	public Long getEventPK() {
		return emailDispatchOrderComposite.getEventPK();

	}


	// *****************************************************************************************************************
	// * Preferences
	// *

	private ModelListener serverModelListener = new ModelListener() {
		@Override
		public void dataChange(ModelEvent event) {
			ServerModelEvent serverModelEvent = (ServerModelEvent) event;
			if (serverModelEvent.getType() == ServerModelEventType.BEFORE_LOGOUT) {
				// save values to preferences before the logout will remove them
				savePreferences();
			}
			else if (serverModelEvent.getType() == ServerModelEventType.LOGIN) {
				SWTHelper.asyncExecDisplayThread(new Runnable() {
					@Override
					public void run() {
						initFromPreferences();
					}
				});
			}
		}
	};


	private void savePreferences() {
		preference.setEventId( emailDispatchOrderComposite.getEventPK() );
		preference.setEventFilter( emailDispatchOrderComposite.getEventFilter() );

		preference.save();
	}


	private void initFromPreferences() {
		try {
			if (emailDispatchOrderComposite != null && !emailDispatchOrderComposite.isDisposed()) {
        		// eventPK
       			emailDispatchOrderComposite.setEventPK( preference.getEventId() );

        		// eventFilter
        		emailDispatchOrderComposite.setEventFilter( preference.getEventFilter() );
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	// *
	// * Preferences
	// *****************************************************************************************************************

}

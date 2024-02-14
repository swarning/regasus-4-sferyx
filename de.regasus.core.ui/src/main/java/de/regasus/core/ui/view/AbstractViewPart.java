package de.regasus.core.ui.view;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPage;

import com.lambdalogic.messeinfo.config.parameterset.ConfigParameterSet;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ConfigParameterSetModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;


/**
 * Base class for configurable views.
 * Configurable means that the view is able to hide and show its content dynamically if the
 * ConfigParameterSet changes. 
 */
abstract public class AbstractViewPart {
	
	/**
	 * The parent component of the view.
	 * It is set in the method createPartControl() and used in implementations of the method
	 * createWidgets().
	 */
	private Composite parent;

	
	/**
	 * Injected {@link IWorkbenchPage}.
	 */
	protected IWorkbenchPage workbenchPage;

	/**
	 * The instance of the ConfigParameterSetModel.
	 * AbstractView is observing the ConfigParameterSetModel to get the current ConfigParameterSet.
	 */
	private ConfigParameterSetModel configParameterSetModel;
	
	/**
	 * The current ConfigParameterSet.
	 */
	private ConfigParameterSet configParameterSet;

	/**
	 * Implementation of CacheModelListener.
	 * Has to be stored, to be removed as listener in dispose().
	 */
	private CacheModelListener<Long> configParameterSetModelListener;
	
	/**
	 * The last value of isVisible().
	 * Used in initWidgets() to decide if the value of isVisible() has changed.
	 */
	private boolean visible = false;

	
	/**
	 * Sub classes have to create their widgets by implementing this method.
	 * It is a replacement for createPartControl().
	 * @param parent
	 */
	protected abstract void createWidgets(Composite parent) throws Exception;
	
	
	/**
	 * Shows if the content of the view is visible.
	 * This method is just a default implementation.
	 * Sub classes, that change their content dynamically when the ConfigParameterSet changes, have
	 * to override this method.
	 * @return
	 */
	protected boolean isVisible() {
		return true;
	}
	
	
	/**
	 * Return the parent Composite.
	 * @return
	 */
	protected Composite getParent() {
		return parent;
	}
	
	
	/**
	 * Returns the ConfigParameterSet.
	 * The result may be null if the user is not logged in.
	 * @return
	 */
	protected ConfigParameterSet getConfigParameterSet() {
		return configParameterSet;
	}
	
	
	/**
	 * Create the content of the view part.
	 * @param parent
	 */
	@PostConstruct
	public void createPartControl(Composite parent, IWorkbenchPage workbenchPage) {
		this.parent = parent;
		this.workbenchPage = workbenchPage;
		
		try {
			// get an instance of the ConfigParameterSetModel
			configParameterSetModel = ConfigParameterSetModel.getInstance();
			
			// initialize configParameterSet
			configParameterSet = configParameterSetModel.getConfigParameterSet();
			
			// observe the ConfigParameterSetModel
			configParameterSetModelListener = new CacheModelListener<Long>() {
				@Override
				public void dataChange(CacheModelEvent<Long> event) {
					try {
						configParameterSet = configParameterSetModel.getConfigParameterSet();
						initWidgets();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			};
			configParameterSetModel.addListener(configParameterSetModelListener);

			// initialize visible
			visible = isVisible();
			
			// initialize the View's widgets
			createWidgets(parent);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			closeLater();
		}
	}

	
	@Focus
	public void setFocus() {
		try {
			parent.setFocus();
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}
	}


	@PreDestroy
	public void preDestroy() {
		// stop observing the ConfigParameterSetModel 
		if (configParameterSetModelListener != null) {
			configParameterSetModel.removeListener(configParameterSetModelListener);
		}
	}
	
	
	/**
	 * Sub classes have to implement removeListener method, if any listener is added in createWidgets().
	 */
	protected void removeListener() {
	}


	/**
	 * Dispose all widgets and remove all Actions from the tool bar and the menu generically.
	 */
	private void destroyWidgets() {
		if (parent != null) {
			removeListener();
			for (Control control : parent.getChildren()) {
				control.dispose();
			}
		}
	
		setContributionItemsVisible(false);
	}
	
	
	/**
	 * Rebuild the View's widgets if the value of isVisible() has changed.
	 * This may happen if the ConfigParameterSet changes.
	 */
	private void initWidgets() {
		boolean oldVisible = visible;
		visible = isVisible();
		
		if (oldVisible != visible) {
			
    		SWTHelper.asyncExecDisplayThread(new Runnable() {
    			public void run() {
    				try {
    					destroyWidgets();
    					createWidgets(parent);
    					parent.layout();
    				}
    				catch (Throwable t) {
    					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
    				}
    			}
    		});
    		
		}
	}
	
	
	protected void setContributionItemsVisible(boolean visible) {
		// TODO
//		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
//		IContributionItem[] contributionItems = toolBarManager.getItems();
//		for (int i = 0; i < contributionItems.length; i++) {
//			IContributionItem contributionItem = contributionItems[i];
//			contributionItem.setVisible(visible);
//		}
//		toolBarManager.update(true);
//		
//		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
//		contributionItems = menuManager.getItems();
//		for (int i = 0; i < contributionItems.length; i++) {
//			IContributionItem contributionItem = contributionItems[i];
//			contributionItem.setVisible(visible);
//		}
//		menuManager.update(true);
	}
	
	
	protected void closeLater() {
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					sleep(100);
				}
				catch (InterruptedException e) {
					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
				}
				close();
			}
		};
		
		t.start();
	}
	
	
	/**
	 * Closes this view asynchronously.
	 */
	protected void close() {
//		SWTHelper.syncExecDisplayThread(new Runnable() {
//			public void run() {
//				try {
//					getSite().getPage().hideView(AbstractViewPart.this);
//				}
//				catch (Exception e) {
//					com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
//				}
//			}
//		});
	}
	
}

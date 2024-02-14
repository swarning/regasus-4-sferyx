package com.lambdalogic.util.rcp;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

import com.lambdalogic.util.rcp.error.ErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;


/**
 * Composite for lazy initialization.
 *
 * Sub-classes of this composite are not initialized in their constructor, but in the overwritten
 * method createPartControl() which is called when the Composite is set visible the first time.
 *
 * If the Composite is set as the control of a TabItem, they are first not visible. Only when the
 * TabItem is selected it sets its control (the Composite) visible. This allows the widgets of a
 * TabItem to be initialized not before the TabItem is selected.
 *
 * Sub-classes have to
 * - implement createPartControl()
 * - be the control of of a TabItem
 *
 * Implementation notes:
 *
 * The implementation of createPartControl() creates the widgets and calls syncWidgetsToEntity()
 * afterwards.
 *
 * syncWidgetsToEntity() may be called before the widgets are initialized. So it should check this like:
 * 		if (isInitialized() && myEntity != null) {
 */
abstract public class LazyComposite extends Composite {

	private boolean initialized = false;

	/**
	 * If this composite has to open a pop-up menu it needs to know the site
	 * of the editor or view it is in.
	 */
	protected  IWorkbenchPartSite site;


	public LazyComposite(Composite parent, int style) {
		this(parent, style, null);
	}


	public LazyComposite(Composite parent, int style, IWorkbenchPartSite site) {
		super(parent, style);
		this.site = site;
	}


	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			init();
		}
		super.setVisible(visible);
	}


	public void init() {
		if (!initialized) {

			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						createPartControl();
						layout();
						initialized = true;
					}
					catch (Exception e) {
						ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});

		}
	}


	public boolean isInitialized() {
		return initialized;
	}


	abstract protected void createPartControl() throws Exception;

}

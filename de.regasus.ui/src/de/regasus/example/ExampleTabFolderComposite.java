package de.regasus.example;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;


/**
 * This is an example {@link Composite} with a {@link TabFolder} that can be used as a template.
 */
public class ExampleTabFolderComposite extends Composite {

	// entity
	private Foo foo;


	// **************************************************************************
	// * Widgets
	// *

	private TabFolder tabFolder;

	private FooComposite fooComposite;

	// *
	// * Widgets
	// **************************************************************************

	private ModifySupport modifySupport = new ModifySupport(this);


	public ExampleTabFolderComposite(Composite parent) {
		super(parent, SWT.NONE);

		addDisposeListener(disposeListener);

		createWidgets();
	}


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			// TODO: free system resources
		}
	};


	public void createWidgets() {
		// layout without margin, because it works only as a container for the TabFolder
		setLayout( new FillLayout() );

		// tabFolder
		tabFolder = new TabFolder(this, SWT.NONE);

		// Foo Tab
		{
    		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    		tabItem.setText("Foo"); // TODO: i18n
    		fooComposite = new FooComposite(tabFolder);
    		tabItem.setControl(fooComposite);;
    		fooComposite.addModifyListener(modifySupport);
		}
	}


	/**
	 * Set a new entity.
	 * @param foo
	 */
	public void setFoo(Foo foo) {
		this.foo = foo;
		syncWidgetsToEntity();
	}


	/**
	 * Copy the values from the entity to the widgets.
	 */
	private void syncWidgetsToEntity() {
		if (foo != null) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					modifySupport.setEnabled(false);
					try {
						fooComposite.setFoo(foo);
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
					finally {
						modifySupport.setEnabled(true);
					}
				}
			});
		}
	}


	/**
	 * Copy the values from the widgets to the entity.
	 */
	public void syncEntityToWidgets() {
		if (foo != null) {
			fooComposite.syncEntityToWidgets();
		}
	}


	// **************************************************************************
	// * Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}

	// *
	// * Modifying
	// **************************************************************************

}

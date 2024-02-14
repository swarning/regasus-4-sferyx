package com.lambdalogic.util.rcp.widget;

import java.util.Collection;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.util.rcp.Activator;
import com.lambdalogic.util.rcp.error.ErrorHandler;

public abstract class AbstractTableComposite<EntityType>
extends Composite
implements DisposeListener {

	// Widgets
	protected TableViewer tableViewer;


	/* Dient der Unterscheidung von Modify-Events, die durch Benutzeraktionen
	 * ausgelöst werden von solchen, die durch Setter-Methoden ausgelöst werden.
	 * Werte größer 0 zeigen an, dass Widgets gerade synchronisiert werden.
	 * Die dabei aufgerufenen Setter-Methode lösen Modify-Events aus, die von
	 * der Methode modifyText(ModifyEvent event) ignoriert werden sollen.
	 */
	protected boolean sync = false;


	public AbstractTableComposite(Composite parent, int style) throws Exception {
		super(parent, style);

		addDisposeListener(this);

		setLayout(new FillLayout());

		initModel();

		initializeTableViewer(style);

		syncTableToModel();
	}

	protected abstract void initializeTableViewer(int style);

	protected abstract Collection<EntityType> getModelData() throws Exception;

	protected abstract void initModel();

	protected abstract void disposeModel();


	@Override
	public void widgetDisposed(DisposeEvent e) {
		try {
			disposeModel();
		}
		catch (Throwable t) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(t);
			ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
		}
	}


	/**
	 * Synchronisiert die Werteliste der Table mit dem Model.
	 * @throws Exception
	 */
	private synchronized void syncTableToModel() throws Exception {
		final Collection<EntityType> list = getModelData();

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					sync = true;

					tableViewer.setInput(list);
				}
				catch (Throwable t) {
					ErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), t);
				}
				finally {
					sync = false;
				}
			}
		});
	}


	protected void handleModelChange() throws Exception {
		syncTableToModel();
	}


	public TableViewer getTableViewer() {
		return tableViewer;
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

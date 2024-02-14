package de.regasus.core.ui.editor.property;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

import com.lambdalogic.messeinfo.kernel.interfaces.IKernelManager;
import com.lambdalogic.util.CloneHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.model.CacheModelOperation;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.widget.NullableSpinner;
import com.lambdalogic.util.rcp.widget.SWTHelper;
import com.lambdalogic.util.rcp.widget.WidgetSizer;

import de.regasus.common.Property;
import de.regasus.core.PropertyModel;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.editor.IRefreshableEditorPart;

public class PropertiesEditor extends EditorPart
implements ModifyListener, CacheModelListener<String>, IRefreshableEditorPart {

	public static final String ID = "PropertiesEditor";

	private NullableSpinner serverIDSpinner;

	private Table table;

	private PropertyModel propertyModel;

	private boolean dirty;

	private PropertyTable propertyTable;

	private Collection<Property> editableModelData;


	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		try {
			setPartName(input.getName());
			setTitleToolTip(input.getToolTipText());
			propertyModel = PropertyModel.getInstance();
			propertyModel.addListener(this);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			throw new PartInitException(e.getMessage(), e);
		}
	}


	@Override
	public void dispose() {
		if (propertyModel != null) {
			try {
				propertyModel.removeListener(this);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		super.dispose();
	}


	@Override
	public boolean isDirty() {
		return dirty;
	}


	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}


	@Override
	public void createPartControl(Composite parent) {
		try {
			parent.setLayout(new GridLayout());

			Composite topComposite = new Composite(parent, SWT.NONE);
			topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			topComposite.setLayout(new GridLayout(2, false));

			{
				Label serverIDLabel = new Label(topComposite, SWT.NONE);
				serverIDLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
				serverIDLabel.setText("Server-ID");

				serverIDSpinner = new NullableSpinner(topComposite, SWT.BORDER);
				serverIDSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				serverIDSpinner.setMinimum(IKernelManager.MIN_SERVER_ID);
				serverIDSpinner.setMaximum(IKernelManager.MAX_SERVER_ID);
				WidgetSizer.setWidth(serverIDSpinner);
				serverIDSpinner.addModifyListener(this);
			}

			/*
			 * For the usage of TableColumnLayout see also http://eclipsenuggets.blogspot.com/2007_11_01_archive.html
			 */
			Composite tableComposite = new Composite(parent, SWT.NONE);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			tableComposite.setLayout(tableColumnLayout);

			table = new Table(tableComposite, SWT.MULTI | SWT.FULL_SELECTION);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			final TableColumn keyTableColumn = new TableColumn(table, SWT.LEFT);

			keyTableColumn.setText("KEY");
			tableColumnLayout.setColumnData(keyTableColumn, new ColumnWeightData(1));

			final TableColumn valueTableColumn = new TableColumn(table, SWT.LEFT);
			tableColumnLayout.setColumnData(valueTableColumn, new ColumnWeightData(2));
			valueTableColumn.setText("VALUE");
			propertyTable = new PropertyTable(table);
			propertyTable.addModifyListener(this);

			getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());


			refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.editor.IRefreshableEditorPart#refresh()
	 */
	@Override
	public void refresh() {
		try {
			propertyModel.refresh();
			refreshFromModel();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.editor.IRefreshableEditorPart#isNew()
	 */
	@Override
	public boolean isNew() {
		return false;
	}


	private void refreshFromModel() throws Exception, CloneNotSupportedException {
		final Integer serverID = propertyModel.getServerID();

		Collection<Property> modelData = propertyModel.getPublicPropertyList();
		editableModelData = CloneHelper.deepCloneCollection(modelData);

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				propertyTable.getViewer().setInput(editableModelData);
				serverIDSpinner.setValue(serverID);
				dirty = false;
				firePropertyChange(PROP_DIRTY);
			}
		});
	}


	@Override
	public void setFocus() {
		table.setFocus();
	}


	@Override
	public void modifyText(ModifyEvent e) {
		dirty = true;
		firePropertyChange(PROP_DIRTY);
	}


	@Override
	public void dataChange(CacheModelEvent<String> event) {
		try {
			if (event.getSource() == propertyModel) {
				if (event.getOperation() == CacheModelOperation.REFRESH) {
					refreshFromModel();
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			monitor.beginTask(de.regasus.core.ui.CoreI18N.Saving, 1);

			propertyModel.update(editableModelData, serverIDSpinner.getValueAsInteger());

			monitor.worked(1);
			monitor.done();

			dirty = false;
			firePropertyChange(PROP_DIRTY);
		}
		catch (Exception e) {
			monitor.setCanceled(true);
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void doSaveAs() {
	}


	/**
	 * Closes this editor asynchronous.
	 */
	private void close() {
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				getSite().getPage().closeEditor(PropertiesEditor.this, false /* save */);
			}
		});
	}

}

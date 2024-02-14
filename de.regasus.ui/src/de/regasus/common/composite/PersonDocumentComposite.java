package de.regasus.common.composite;

import static com.lambdalogic.util.rcp.widget.SWTHelper.buildMenuItem;

import java.io.File;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.common.FileSummary;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.file.job.DownloadFileJob;
import de.regasus.ui.Activator;

public abstract class PersonDocumentComposite
extends LazyComposite
implements DisposeListener, CacheModelListener<Long> {

	// widgets
	protected FileTable fileTable;

	private Button detailsDocumentButton;

	private Button uploadDocumentButton;

	private Button deleteDocumentButton;

	private Button downloadDocumentButton;

	private Button viewDocumentButton;


	public PersonDocumentComposite(final Composite tabFolder, int style) {
		super(tabFolder, style);
	}


	@Override
	protected void createPartControl() throws Exception {
		setLayout(new GridLayout(1, false));

		Composite tableComposite = buildTableComposite(this);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		buildButtonComposite(this);

		fileTable.getViewer().addDoubleClickListener(e -> view());

		syncWidgetsToEntity();

		updateButtonStates();
	}


	private Composite buildTableComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);

		TableColumnLayout layout = new TableColumnLayout();
		composite.setLayout(layout);
		Table table = new Table(composite, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Icon
		TableColumn iconTableColumn = new TableColumn(table, SWT.CENTER);
		layout.setColumnData(iconTableColumn, new ColumnWeightData(20));

		// File Name
		TableColumn fileNameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(fileNameTableColumn, new ColumnWeightData(100));
		fileNameTableColumn.setText(UtilI18N.File);

		// File Size
		TableColumn sizeTableColumn = new TableColumn(table, SWT.RIGHT);
		layout.setColumnData(sizeTableColumn, new ColumnWeightData(50));
		sizeTableColumn.setText(UtilI18N.Size);

		// Document Name
		TableColumn documentNameTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(documentNameTableColumn, new ColumnWeightData(100));
		documentNameTableColumn.setText(UtilI18N.Name);

		// Description
		TableColumn descriptionTableColumn = new TableColumn(table, SWT.NONE);
		layout.setColumnData(descriptionTableColumn, new ColumnWeightData(250));
		descriptionTableColumn.setText(UtilI18N.Description);

		table.addListener(SWT.Selection, e -> updateButtonStates());

		fileTable = createSimpleTable(table);

		buildContextMenu();

		return composite;
	}


	private void buildContextMenu() {
		Menu menu = new Menu (getShell(), SWT.POP_UP);

		buildMenuItem(menu, UtilI18N.View, e -> view(), IconRegistry.getImage(IImageKeys.EYE));
		buildMenuItem(menu, UtilI18N.Details, e -> details());
		buildMenuItem(menu, UtilI18N.Download, e -> download());
		buildMenuItem(menu, UtilI18N.Delete, e -> delete(), IconRegistry.getImage(IImageKeys.DELETE));

		// disable all menu items is nothing is selected
		menu.addListener(SWT.Show, new Listener() {
			@Override
			public void handleEvent(Event event) {
				boolean somethingSelected = getSelectedDocument() != null;
				for (MenuItem menuItem : menu.getItems()) {
					menuItem.setEnabled(somethingSelected);
				}
			}
		});

		fileTable.getViewer().getTable().setMenu(menu);
	}


	private Composite buildButtonComposite(Composite parent) {
		Composite composite = new Composite(this, SWT.NONE);
		RowLayout buttonCompositeLayout = new RowLayout();
		buttonCompositeLayout.pack = false;
		buttonCompositeLayout.spacing = 5;
		buttonCompositeLayout.wrap = false;
		buttonCompositeLayout.justify = true;
		composite.setLayout(buttonCompositeLayout);

		// View
		viewDocumentButton = new Button(composite, SWT.PUSH);
		viewDocumentButton.setText(UtilI18N.View);
		viewDocumentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					view();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}

		});


		// Edit Button
		detailsDocumentButton = new Button(composite, SWT.PUSH);
		detailsDocumentButton.setText(UtilI18N.Details);
		detailsDocumentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					details();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}

		});

		// Upload Button
		uploadDocumentButton = new Button(composite, SWT.PUSH);
		uploadDocumentButton.setText(UtilI18N.Upload);
		uploadDocumentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					upload();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}

		});

		// Download Button
		downloadDocumentButton = new Button(composite, SWT.PUSH);
		downloadDocumentButton.setText(UtilI18N.Download);
		downloadDocumentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					download();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}

			}

		});

		// Delete Button
		deleteDocumentButton = new Button(composite, SWT.PUSH);
		deleteDocumentButton.setText(UtilI18N.Delete);
		deleteDocumentButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					delete();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}

			}

		});

		return composite;
	}


	protected FileSummary getSelectedDocument() {
		ISelection selection = fileTable.getViewer().getSelection();
		FileSummary fileSummary = SelectionHelper.getUniqueSelected(selection);
		return fileSummary;
	}


	protected void updateButtonStates() {
		boolean somethingSelected = getSelectedDocument() != null;

		viewDocumentButton.setEnabled(somethingSelected);
		detailsDocumentButton.setEnabled(somethingSelected);
		downloadDocumentButton.setEnabled(somethingSelected);
		deleteDocumentButton.setEnabled(somethingSelected);

		uploadDocumentButton.setEnabled(fileTable.getViewer().getTable().getItemCount() < 10);
	}


	protected void view() {
		try {
			FileSummary fileSummary = getSelectedDocument();
			if (fileSummary != null) {
				String extension = FileHelper.getExtension( fileSummary.getExternalPath() );

				File tmpFile = File.createTempFile("regasus", "." + extension);
				tmpFile.deleteOnExit();

				DownloadFileJob job = new DownloadFileJob(fileSummary, tmpFile);
				job.setUser(true);
				job.addJobChangeListener( new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						Program.launch( tmpFile.getAbsolutePath() );
					}
				} );
				job.schedule();
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void download() {
		try {
			FileSummary fileSummary = getSelectedDocument();
			if (fileSummary != null) {
    			FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
    			String fileName = fileSummary.getExternalPath();
    			fileDialog.setFileName(fileName);
    			String saveFile = fileDialog.open();

    			// If dialog was not cancelled, fetch contents from server and save in file
    			if (saveFile != null) {
    				File file = new File(saveFile);
    				if (file.exists()) {
    					boolean confirm = MessageDialog.openConfirm(getShell(), UtilI18N.Confirm, UtilI18N.FileExistsOverrideQuestion);
    					if (! confirm) {
    						return;
    					}
    				}

    				DownloadFileJob job = new DownloadFileJob(fileSummary, file);
    				job.setUser(true);
//    				job.addJobChangeListener( new JobDoneNotifier() );
    				job.schedule();
    			}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	@Override
	public void dataChange(CacheModelEvent<Long> event) {
		syncWidgetsToEntity();
	}


	protected abstract void syncWidgetsToEntity();

	protected abstract void delete();

	protected abstract void details();

	protected abstract void upload() throws Exception;

	protected abstract FileTable createSimpleTable(Table table);

}

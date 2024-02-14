package de.regasus.portal.portal.editor;

import static com.lambdalogic.util.StringHelper.isNotEmpty;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.File;
import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.portal.Portal;
import de.regasus.portal.PortalFileHelper;
import de.regasus.portal.PortalFileModel;
import de.regasus.ui.Activator;


public class PortalFileComposite extends Composite {

	private static final PortalFileModel MODEL = PortalFileModel.getInstance();

	private final Portal portal;

	private boolean ignoreRefresh = false;


	// widgets
	private Table table;
	private TableViewer tableViewer;

	private Button uploadButton;
	private Button deleteButton;
	private Button showButton;
	private Button downloadButton;
	private Button urlButton;


	public PortalFileComposite(Composite parent, int style, Portal portal) {
		super(parent, style);
		this.portal = portal;

		createWidgets();
	}


	private void createWidgets() {
		setLayout(new GridLayout(1, false));

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createTableWidgets(composite);
		createButtonWidgets(composite);

		registerListener();

		syncWidgetsToModel();
		handleButtonState();
	}


	private void createTableWidgets(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.BORDER);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumnLayout layout = new TableColumnLayout();
		tableComposite.setLayout(layout);
		table = new Table(tableComposite, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn mnemonicTableColumn = new TableColumn(table, SWT.NONE);
		mnemonicTableColumn.setText(UtilI18N.Mnemonic);
		layout.setColumnData(mnemonicTableColumn, new ColumnWeightData(1));

		TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		languageTableColumn.setText(UtilI18N.Language);
		layout.setColumnData(languageTableColumn, new ColumnWeightData(1));

		TableColumn extPathTableColumn = new TableColumn(table, SWT.NONE);
		extPathTableColumn.setText(UtilI18N.File);
		layout.setColumnData(extPathTableColumn, new ColumnWeightData(3));

		TableColumn urlTableColumn = new TableColumn(table, SWT.NONE);
		urlTableColumn.setText(UtilI18N.URL);
		layout.setColumnData(urlTableColumn, new ColumnWeightData(3));

		PortalFileTable portalFileTable = new PortalFileTable(table);
		tableViewer = portalFileTable.getViewer();
	}


	private void createButtonWidgets(Composite parent) {
		GridDataFactory gridDataFactory = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL);

		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
		buttonComposite.setLayout(new GridLayout(5, true));

		uploadButton = new Button(buttonComposite, SWT.PUSH);
		uploadButton.setText(UtilI18N.Upload.toString());
		gridDataFactory.applyTo(uploadButton);

		deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText(UtilI18N.Delete.toString());
		gridDataFactory.applyTo(deleteButton);

		showButton = new Button(buttonComposite, SWT.PUSH);
		showButton.setText(UtilI18N.Show.toString());
		gridDataFactory.applyTo(showButton);

		downloadButton = new Button(buttonComposite, SWT.PUSH);
		downloadButton.setText(UtilI18N.Download.toString());
		gridDataFactory.applyTo(downloadButton);

		urlButton = new Button(buttonComposite, SWT.PUSH);
		urlButton.setText(UtilI18N.CopyURL.toString());
		gridDataFactory.applyTo(urlButton);
	}


	private void registerListener() {
		MODEL.addForeignKeyListener(modelListener, portal.getId());
		addDisposeListener(disposeListener);

		table.addSelectionListener(tableSelectionListener);

		uploadButton.addSelectionListener(uploadButtonListener);
		deleteButton.addSelectionListener(deleteButtonListener);
		showButton.addSelectionListener(showButtonListener);
		downloadButton.addSelectionListener(downloadButtonListener);
		urlButton.addSelectionListener(urlButtonListener);
	}


	private void syncWidgetsToModel() {
		Collection<File> files;
		try {
			synchronized (MODEL) {
				files = MODEL.getPortalFiles( portal.getId() );
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			return;
		}

		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				try {
					tableViewer.setInput(files);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	private void handleButtonState() {
		int selectionCount = table.getSelectionCount();
		boolean oneSelected = (selectionCount == 1);
		boolean oneOrMoreSelected = (selectionCount > 0);

		uploadButton.setEnabled(true);
		deleteButton.setEnabled(oneOrMoreSelected);
		showButton.setEnabled(oneSelected);
		downloadButton.setEnabled(oneSelected);
		urlButton.setEnabled(oneSelected);
	}


	private CacheModelListener<Long> modelListener = new CacheModelListener<Long>() {
		@Override
		public void dataChange(CacheModelEvent<Long> event) throws Exception {
			if (ignoreRefresh) {
				return;
			}

			syncWidgetsToModel();
		}
	};


	private DisposeListener disposeListener = new DisposeListener() {
		@Override
		public void widgetDisposed(DisposeEvent e) {
			MODEL.removeForeignKeyListener(modelListener, portal.getId());
		}
	};


	private SelectionListener tableSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			handleButtonState();
		}
	};


	private SelectionListener uploadButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				// determine values for AddPortalFileDialog
				Collection<String> availableLanguages = portal.getLanguageList();

				String mnemonic = null;
				String language = null;
				java.io.File file = null;

				File selectedFile = SelectionHelper.getUniqueSelected( tableViewer.getSelection() );
				if (selectedFile != null) {
    				mnemonic = PortalFileHelper.extractFileMnemonic( selectedFile.getInternalPath() );
    				language = selectedFile.getLanguage();
    				file = new java.io.File( selectedFile.getExternalPath() );
				}

				// init AddPortalFileDialog
				AddPortalFileDialog fileDialog = new AddPortalFileDialog( getShell() );
				fileDialog.create();
				fileDialog.setLanguageFilter(availableLanguages);
				fileDialog.setMnemonic(mnemonic);
				fileDialog.setLanguage(language);
				fileDialog.setFile(file);

				// open AddPortalFileDialog
				int result = fileDialog.open();
				if (result == AddPortalFileDialog.OK) {
					// upload File
					Long portalId = portal.getId();
					mnemonic = fileDialog.getMnemonic();
					language = fileDialog.getLanguage();
					file = fileDialog.getFile();

					byte[] content = FileHelper.readFile(file);

					MODEL.upload(portalId, mnemonic, language, file.getAbsolutePath(), content);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private SelectionListener deleteButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			List<File> selectedFiles = SelectionHelper.toList( tableViewer.getSelection() );
			if (selectedFiles.isEmpty()) {
				return;
			}

			MessageDialog confirmDialog = new MessageDialog(
				getShell(),
				null,
				null,
				UtilI18N.DeleteFileConfirmationDialogMessage.toString(),
				MessageDialog.CONFIRM,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, },
				IDialogConstants.CANCEL_ID
			);

			if (confirmDialog.open() != IDialogConstants.OK_ID) {
				return;
			}

			try {
				MODEL.delete(selectedFiles);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private SelectionListener showButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				File selectedFile = getSelectedFileWithContent();
				if (selectedFile == null) {
					return;
				}

				String externalPath = selectedFile.getExternalPath();
				String extension = FileHelper.getExtension(externalPath);

				java.io.File tmpFile = java.io.File.createTempFile("regasus", "." + extension);
				tmpFile.deleteOnExit();
				FileHelper.writeFile(tmpFile, selectedFile.getContent());

				Program.launch(tmpFile.getAbsolutePath());
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private SelectionListener downloadButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
    			File selectedFile = getSelectedFileWithContent();
    			if (selectedFile == null) {
    				return;
    			}

    			// Open Save-as-Dialog
    			FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);

    			String fileName = selectedFile.getExternalPath();
    			if ( isNotEmpty(fileName) ) {
    				java.io.File file = new java.io.File(fileName);
    				java.io.File dir = file.getParentFile();
    				if (dir != null && dir.exists()) {
    					fileDialog.setFilterPath(dir.getPath());
    				}
    				fileDialog.setFileName(file.getName());
    			}

    			String saveFileName = fileDialog.open();
    			if (saveFileName != null) {
					byte[] content = selectedFile.getContent();
					FileHelper.writeFile(new java.io.File(saveFileName), content);
    			}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}

		}
	};


	private SelectionListener urlButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {
				File selectedFile = getSelectedFile();
				if (selectedFile != null) {
					String url = PortalFileModel.buildWebServiceUrl(selectedFile);
					ClipboardHelper.copyToClipboard(url);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	private File getSelectedFile() throws Exception {
		return SelectionHelper.getUniqueSelected( tableViewer.getSelection() );
	}


	private File getSelectedFileWithContent() throws Exception {
		File selectedFile = getSelectedFile();
		if (selectedFile != null) {
			byte[] content = selectedFile.getContent();
			if (content == null) {
				selectedFile = MODEL.getExtendedFile( selectedFile.getId() );
			}
		}

		return selectedFile;
	}

}

package de.regasus.common.document.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.imageio.stream.FileImageInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;

import com.lambdalogic.util.CloneHelper;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.ClipboardHelper;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.SelectionHelper;
import de.regasus.core.error.RegasusErrorHandler;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.common.FileSummary;
import de.regasus.core.ui.Activator;
import com.lambdalogic.util.rcp.UtilI18N;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.TemplateHelper;
import de.regasus.core.ui.dialog.FileWithLanguageUploadDialog;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.document.DocumentModel;
import de.regasus.portal.PortalFileModel;

/**
 * An editor use to manage the document.
 * The document which is opened by this editor will be stored in the data store under the specific path
 */
abstract class DocumentEditor extends EditorPart implements IRefreshableEditorPart {

	private DocumentModel model;

	private Table table;
	private DocumentTable documentTable;

	private Button uploadButton;
	private Button deleteButton;
	private Button showButton;
	private Button downloadButton;
	private Button urlButton;


	public DocumentEditor(DocumentModel documentModel) {
		Objects.requireNonNull(documentModel);
		this.model = documentModel;
	}


	private CacheModelListener<String> modelListener = new CacheModelListener<String>() {
		@Override
		public void dataChange(CacheModelEvent<String> event) {
			try {
				if (event.getSource() == model) {
					updateTable(model);
				}
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	};


	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		try {
			setPartName(input.getName());
			setTitleToolTip(input.getToolTipText());

			model.addListener(modelListener);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			throw new PartInitException(e.getMessage(), e);
		}
	}


	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout( new GridLayout() );

		createTable(parent);
		createButtonWidgets(parent);

		getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());


		try {
			refresh();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}

		updateButtonEnabledStates();
	}


	private void createTable(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableComposite.setLayout(new GridLayout(1, false));

		table = new Table(tableComposite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createTableColumn(60, UtilI18N.Language);
		createTableColumn(400, UtilI18N.File);
		createTableColumn(800, UtilI18N.URL);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 100;
		table.setLayoutData(gridData);

		documentTable = new DocumentTable(table);

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonEnabledStates();
			}
		});
	}


	private void createTableColumn(int width, String text) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(width);
		column.setText(text);
	}


	private void createButtonWidgets(Composite parent) {
		Composite buttonComposite = createButtonComposite(parent);

		createUploadButton(buttonComposite);
		createDeleteButton(buttonComposite);
		createShowButton(buttonComposite);
		createDownloadButton(buttonComposite);
		createUrlButton(buttonComposite);
	}


	private Composite createButtonComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		RowLayout layout = new RowLayout();
		layout.pack = false;
		layout.spacing = 5;
		layout.wrap = false;
		layout.justify = true;
		composite.setLayout(layout);
		return composite;
	}


	private void createUploadButton(Composite parentComposite) {
		uploadButton = new Button(parentComposite, SWT.PUSH);
		uploadButton.setText(CoreI18N.TemplateUpload_Text);
		uploadButton.setToolTipText(CoreI18N.TemplateUpload_ToolTip);
		uploadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showFileUploadDialog();
				updateButtonEnabledStates();
			}
		});
	}


	private void createDeleteButton(Composite buttonComposite) {
		deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText(CoreI18N.TemplateDelete_Text);
		deleteButton.setToolTipText(CoreI18N.TemplateDelete_ToolTip);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean confirmDelete = MessageDialog.openQuestion(
					Display.getDefault().getActiveShell(),
					de.regasus.I18N.GlobalDocumentDelete_ConfirmTitle,
					de.regasus.I18N.GlobalDocumentDelete_ConfirmMessage
				);
				if (confirmDelete) {
					delete();
				}
				updateButtonEnabledStates();
			}
		});
	}


	private void createShowButton(Composite buttonComposite) {
		showButton = new Button(buttonComposite, SWT.PUSH);
		showButton.setText(UtilI18N.Show.toString());
//		showButton.setToolTipText(...);
		showButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					de.regasus.common.File selectedFile = getSelectedFile();
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
		});
	}


	private void createDownloadButton(Composite buttonComposite) {
		downloadButton = new Button(buttonComposite, SWT.PUSH);
		downloadButton.setText(CoreI18N.TemplateDownload_Text);
		downloadButton.setToolTipText(CoreI18N.TemplateDownload_ToolTip);
		downloadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					download();
					updateButtonEnabledStates();
				}
				catch (Exception ex) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
				}
			}
		});
	}


	private void createUrlButton(Composite buttonComposite) {
		urlButton = new Button(buttonComposite, SWT.PUSH);
		urlButton.setText(UtilI18N.CopyURL.toString());
//		urlButton.setToolTipText(...);
		urlButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					FileSummary fileSummary = getSelectedFileSummary();
					if (fileSummary != null) {
						String url = PortalFileModel.buildWebServiceUrl(fileSummary);
						ClipboardHelper.copyToClipboard(url);
					}
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		});
	}


	@Override
	public void setFocus() {
		table.setFocus();
	}


	@Override
	public boolean isDirty() {
		return false;
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
	}


	@Override
	public void doSaveAs() {
	}


	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}


	@Override
	public void refresh() throws Exception {
		model.refresh();
		updateTable(model);
	}


	@Override
	public void dispose() {
		if (model != null) {
			try {
				model.removeListener(modelListener);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
		super.dispose();
	}


	private void showFileUploadDialog() {
		Shell parent = getEditorSite().getShell();
		FileWithLanguageUploadDialog dialog = new FileWithLanguageUploadDialog(
			parent,
			de.regasus.I18N.GlobalDocumentUploadDialog_Title,
			"*.pdf;*.html"
		);
		dialog.setLanguageRequired(true);
		dialog.create();

		FileSummary fileSummary = getSelectedFileSummary();
		if (fileSummary != null) {
    		dialog.setLanguage( fileSummary.getLanguage() );
    		dialog.setFilePath( fileSummary.getExternalPath() );
		}

		int resultCode = dialog.open();
		if (resultCode == Window.OK) {
			try {
				String filePath = dialog.getFilePath();
				String language = dialog.getLanguage();
				performUpload(filePath, language);
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			}
		}
	}


	private void performUpload(String filePath, String language) {
		try {
			File file = new File(filePath);
			FileImageInputStream fis = new FileImageInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			model.upload(filePath, language, content);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void updateTable(DocumentModel model) throws Exception {
		final List<FileSummary> files = CloneHelper.deepCloneList( model.getAllEntities() );
		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				documentTable.setData(files);
			}
		});
	}


	@Override
	public boolean isNew() {
		return false;
	}


	private void delete() {
		try {
			IStructuredSelection selection = (IStructuredSelection) documentTable.getViewer().getSelection();
			Iterator<FileSummary> iterator = selection.iterator();
			while (iterator.hasNext()) {
				FileSummary file = iterator.next();
				if (file.getId() != null) {
					model.delete(file);
				}
			}
			updateButtonEnabledStates();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void download() {
		// Find selected DataStoreVO - there should be precisely one.
		IStructuredSelection selection = (IStructuredSelection) documentTable.getViewer().getSelection();
		final FileSummary fileSummary = (FileSummary) selection.getFirstElement();

		// Make sure extFileName ends with extension
		String externalPath = fileSummary.getExternalPath();
		java.io.File originalFile = new java.io.File(externalPath);

		// Open Dialog with originalFile's name and path (if exists)
		FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
		File parentFile = originalFile.getParentFile();
		if (parentFile != null && parentFile.exists()) {
			fileDialog.setFilterPath(originalFile.getParent());
		}
		fileDialog.setFileName(originalFile.getName());
		final String saveFile = fileDialog.open();

		// If dialog was not cancelled, fetch contents from server and save in file
		if (saveFile != null) {

			try {
				BusyCursorHelper.busyCursorWhile(new Runnable() {

					@Override
					public void run() {
						try {
							de.regasus.common.File downloadFile = model.download( fileSummary.getInternalPath() );
							if (downloadFile != null) {
								FileOutputStream fos = new FileOutputStream(saveFile);
								fos.write( downloadFile.getContent() );
								fos.close();
							}
						}
						catch (Exception e) {
							RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
						}
					}
				});
			}
			catch (Exception e) {
				RegasusErrorHandler.handleError(Activator.PLUGIN_ID, TemplateHelper.class.getName(), e);
			}

		}
		updateButtonEnabledStates();
	}


	private void updateButtonEnabledStates() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				int selectionCount = table.getSelectionCount();
				boolean oneSelected = (selectionCount == 1);
				boolean oneOrMoreSelected = (selectionCount > 0);

				uploadButton.setEnabled(true);
				deleteButton.setEnabled(oneOrMoreSelected);
				showButton.setEnabled(oneSelected);
				downloadButton.setEnabled(oneSelected);
				urlButton.setEnabled(oneSelected);
			}
		});
	}


	private FileSummary getSelectedFileSummary() {
		return SelectionHelper.getUniqueSelected( documentTable.getViewer().getSelection() );
	}


	private de.regasus.common.File getSelectedFile() throws Exception {
		FileSummary fileSummary = getSelectedFileSummary();
		if (fileSummary != null) {
			de.regasus.common.File file = model.download( fileSummary.getInternalPath() );
			return file;
		}
		return null;
	}

}

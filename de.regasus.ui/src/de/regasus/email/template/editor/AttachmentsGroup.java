package de.regasus.email.template.editor;

import static de.regasus.LookupService.getReportMgr;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.lambdalogic.messeinfo.contact.data.SimplePersonSearchData;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.report.od.ODRecord;
import com.lambdalogic.report.od.TextDocument;
import com.lambdalogic.report.oo.OpenOfficeConstants;
import com.lambdalogic.report.script.ScriptContext;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.email.SampleRecipientListener;
import de.regasus.email.template.SampleRecipientModel;
import de.regasus.ui.Activator;

/**
 * An SWT group that shows files to be attached and allows their addition, editing and deletion.
 * <p>
 * Also possible is the preview of ODT documents after their formatting using given variables.
 */
public class AttachmentsGroup extends Group {

	// *************************************************************************
	// * Widgets
	// *

	private EmailAttachmentTable emailAttachmentTable;

	private TableViewer tableViewer;

	/**
	 * The table that shows the attachment files
	 */
	private Table table;

	/**
	 * The button to add an attachment
	 */
	private ToolItem addButton;

	/**
	 * The button to open selected attachments with their associated programs
	 */
	private ToolItem editButton;

	/**
	 * The button to delete selected attachments
	 */
	private ToolItem deleteButton;

	/**
	 * The button to preview selected ODT-attachments
	 */
	private ToolItem previewButton;

	/**
	 * The menu to open selected attachments with their associated programs
	 */
	private MenuItem editItem;

	/**
	 * The menu item to delete selected attachments
	 */
	private MenuItem deleteItem;

	/**
	 * The menu item to preview selected ODT-attachments
	 */
	private MenuItem previewItem;

	/**
	 * A label to show the total size of all attachments together
	 */
	private Label totalSizeLabel;

	// *************************************************************************
	// * Other Attributes
	// *


	/**
	 * The files which are to be attached.
	 */
	private List<File> fileList = new ArrayList<>();


	/**
	 * The listeners who are to be notified when the set of attached files changes
	 */
	private ModifySupport modifySupport = new ModifySupport(this);


	private SampleRecipientModel sampleRecipientModel = SampleRecipientModel.INSTANCE;

	private EmailTemplateEditor emailTemplateEditor;


	// *************************************************************************
	// * Constructor
	// *

	public AttachmentsGroup(Composite parent, int style, EmailTemplateEditor emailTemplateEditor) {
		super(parent, style);

		this.emailTemplateEditor = Objects.requireNonNull(emailTemplateEditor);

		setText(EmailLabel.FileAttachments.getString());

		final int NUM_COLUMN = 2;
		setLayout(new GridLayout(NUM_COLUMN, false));

		// The first row

		// The label to show the total size of all attached files
		totalSizeLabel = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(totalSizeLabel);

		ToolBar toolBar = buildToolBar(this);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(toolBar);


		// table that shows the attachment files
		Composite tableComposite = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().span(NUM_COLUMN, 1).grab(true, true).applyTo(tableComposite);

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);


		table = new Table(tableComposite, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, NUM_COLUMN, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableColumn fileNameTableColumn = new TableColumn(table, SWT.NONE);
		fileNameTableColumn.setText(UtilI18N.File);
		tableColumnLayout.setColumnData(fileNameTableColumn, new ColumnWeightData(180));

//		final TableColumn observeTableColumn = new TableColumn(table, SWT.NONE);
//		observeTableColumn.setText("");
//		tableColumnLayout.setColumnData(observeTableColumn, new ColumnWeightData(20));

		emailAttachmentTable = new EmailAttachmentTable(table);


		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					deleteSelectedAttachments();
				}
				else if (e.keyCode == SWT.CR) {
					openSelectedAttachments();
				}
			}
		});

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonStates();
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openSelectedAttachments();
			}
		});

		// The JFace viewer that provides text and images for the fileList
		tableViewer = emailAttachmentTable.getViewer();
//		tableViewer.setContentProvider(new ArrayContentProvider());
//		tableViewer.setLabelProvider(new FileLabelProvider());
		tableViewer.setInput(fileList);

		fillContextMenu();
		initDragAndDrop();

		// Preview is only possible if sample recipient is selected
		sampleRecipientModel.addSampleRecipientListener(new SampleRecipientListener() {
			@Override
			public void changed(Long eventPK, SimplePersonSearchData psd) throws Exception {
				updateButtonStates();
			}
		});
	}


	private ToolBar buildToolBar(Composite parent) {
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT);

		// The button to add an attachment
		addButton = new ToolItem(toolBar, SWT.PUSH);
		addButton.setImage(IconRegistry.getImage(IImageKeys.CREATE));
		addButton.setToolTipText(UtilI18N.Add + UtilI18N.Ellipsis);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addAttachment();
			}
		});

		// The button to open selected attachments
		editButton = new ToolItem(toolBar, SWT.PUSH);
		editButton.setImage(IconRegistry.getImage(IImageKeys.EDIT));
		editButton.setToolTipText(UtilI18N.Edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openSelectedAttachments();
			}
		});

		// The button to delete selected attachments
		deleteButton = new ToolItem(toolBar, SWT.PUSH);
		deleteButton.setImage(IconRegistry.getImage(IImageKeys.DELETE));
		deleteButton.setToolTipText(UtilI18N.Delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelectedAttachments();
			}
		});

		new ToolItem(toolBar, SWT.SEPARATOR);

		previewButton  = new ToolItem(toolBar, SWT.PUSH);
		previewButton.setImage(IconRegistry.getImage(IImageKeys.EYE));
		previewButton.setToolTipText(EmailLabel.PreviewText.getString());
		previewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				previewSelectedAttachments();
			}
		});

		return toolBar;
	}


	/**
	 * The dragging of files onto the table shall add them as attachments
	 */
	protected void initDragAndDrop() {
		Transfer[] types = new Transfer[] { FileTransfer.getInstance() };
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		DropTarget target = new DropTarget(table, operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				String[] pathes = ((String[]) event.data);
				for (String path : pathes) {
					addPath(path);
				}
			}
		});
	}


	/**
	 * Editing and deletion is only possible when something is selected.
	 */
	protected void updateButtonStates() {
		ISelection selection = tableViewer.getSelection();

		boolean somethingSelected = !selection.isEmpty();
		editButton.setEnabled(somethingSelected);
		editItem.setEnabled(somethingSelected);

		deleteButton.setEnabled(somethingSelected);
		deleteItem.setEnabled(somethingSelected);

		// Decide if preview is possible
		boolean sampleRecipientSelected = false;
		try {
			Long eventPK = emailTemplateEditor.getEmailTemplate().getEventPK();
			Object sampleRecipient = sampleRecipientModel.getSampleRecipient(eventPK);
			sampleRecipientSelected = sampleRecipient != null;
		}
		catch (Exception e) {
			com.lambdalogic.util.rcp.error.ErrorHandler.logError(e);
		}

		boolean allSelectedFilesAreOpenOfficeDocuments = true;
		List<File> selectedFiles = SelectionHelper.getSelection(tableViewer, File.class);
		for (File file : selectedFiles) {
			if (! file.getName().toLowerCase().endsWith("." + OpenOfficeConstants.FORMAT_KEY_ODT)) {
				allSelectedFilesAreOpenOfficeDocuments = false;
				break;
			}
		}

		boolean previewPossible = somethingSelected && allSelectedFilesAreOpenOfficeDocuments && sampleRecipientSelected;
		previewButton.setEnabled(previewPossible);
		previewItem.setEnabled(previewPossible);
	}


	/**
	 * All selected files are tried to be opened by their respective associated program.
	 */
	protected void openSelectedAttachments() {
		List<File> selectedFiles = SelectionHelper.getSelection(tableViewer, File.class);
		for (File file : selectedFiles) {
			boolean success = Program.launch(file.getPath());
			if (success) {
				// Could been opened, so make editor dirty, so that changed attachments are stored in DB
				modifySupport.fire();
			}
			else {
				// Couldn't been opened
				String message = NLS.bind(CoreI18N.AttachmentCouldntBeOpened, file.getName());
				MessageDialog.openWarning(getShell(), UtilI18N.Warning, message);
			}
		}
	}


	/**
	 * All selected files are tried to be opened by their respective associated program.
	 */
	protected void previewSelectedAttachments() {
		try {
			emailTemplateEditor.loadSampleParticipantOrProfile();
			ScriptContext scriptContext = emailTemplateEditor.getContext();

			List<File> selectedFiles = SelectionHelper.getSelection(tableViewer, File.class);
			for (File file : selectedFiles) {

				TextDocument textDocument = new TextDocument(
					file,
					emailTemplateEditor.getEmailTemplate().getLanguage(),
					Thread.currentThread().getContextClassLoader()
				);

				// MIRCP-1950 - Support dates and formatting in personalized email attachments
				// We add all variables from the context
				Map<String, Object> variables = scriptContext.getVariables();
				variables.put("day", new Date());

				// add ODRecord under the key "data" for backward compatibility:
				// support old variables like ${data.p.lastName}
				// support new variables like &{p.lastName}
				ODRecord data = new ODRecord();
				data.putAll(variables);
				variables.put(ScriptContext.DATA, data);

				textDocument.setValues(variables);

				byte[] evaluatedContent = textDocument.getDocumentData();

				// Converting to PDF
				byte[] pdfData = getReportMgr().convertDocument(
					OpenOfficeConstants.FORMAT_KEY_ODT,
					OpenOfficeConstants.FORMAT_KEY_PDF,
					evaluatedContent
				);
				File pdfFile = new File(FileHelper.removeExtension(file.getPath()) + ".pdf");
				FileHelper.writeFile(pdfFile, pdfData);

				Program.launch(pdfFile.getPath());
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

	/**
	 * After a confirmation by the user, all selectd files are removed from the list (which does
	 * not mean that the files are deleted from the hard disk).
	 * <p>
	 * If being part of an editor, that one is notified so that it can set itself to dirty.
	 */
	protected void deleteSelectedAttachments() {
		List<File> selectedFiles = SelectionHelper.getSelection(tableViewer, File.class);
		if (selectedFiles.size() > 0) {
			boolean reallyDelete =
				MessageDialog.openQuestion(getShell(), UtilI18N.Question, CoreI18N.DeleteSelectedAttachments);

			if (reallyDelete) {
				fileList.removeAll(selectedFiles);
				tableViewer.refresh();
				modifySupport.fire();
				updateFileSize();
				updateButtonStates();
			}
		}
	}


	/**
	 * The sizes of all attachment files are added up and shown in a readable form.
	 */
	protected void updateFileSize() {
		long totalSize = 0;
		for (File file : fileList) {
			totalSize += file.length();
		}
		String size = FileHelper.computeReadableFileSize(totalSize);
		totalSizeLabel.setText(EmailLabel.TotalSize.getString() + ": " + size);
	}



	/**
	 * The context menu shows the three items to add, edit and delete attachments.
	 */
	protected void fillContextMenu() {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		MenuItem addItem = new MenuItem(menu, SWT.PUSH);
		addItem.setText(UtilI18N.Add + UtilI18N.Ellipsis);
		addItem.setImage(IconRegistry.getImage(IImageKeys.CREATE));
		addItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addAttachment();
			}
		});

		editItem = new MenuItem(menu, SWT.PUSH);
		editItem.setText(UtilI18N.Edit);
		editItem.setImage(IconRegistry.getImage(IImageKeys.EDIT));
		editItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openSelectedAttachments();
			}
		});

		deleteItem = new MenuItem(menu, SWT.PUSH);
		deleteItem.setText(UtilI18N.Delete);
		deleteItem.setImage(IconRegistry.getImage(IImageKeys.DELETE));
		deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelectedAttachments();
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);

		previewItem = new MenuItem(menu, SWT.PUSH);
		previewItem.setText(EmailLabel.PreviewText.getString());
		previewItem.setImage(IconRegistry.getImage(IImageKeys.EYE));
		previewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				previewSelectedAttachments();
			}
		});


		table.setMenu(menu);
	}


	/**
	 * Opens a file selection dialog and - if a file is selected - adds it to the list of attachments.
	 */
	protected void addAttachment() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		String path = dialog.open();
		if (path != null) {
			addPath(path);
		}
	}


	/**
	 * For a given path it is checked whether an according file exists, and if yes it is not added to
	 * the attachment list.
	 *
	 * <p>
	 * If being part of an editor, that one is notified so that it can set itself to dirty.
	 */
	protected void addPath(String path) {
		File file = new File(path);
		if (file.exists() && !fileList.contains(file)) {
			fileList.add(file);
			modifySupport.fire();
			tableViewer.refresh();
			updateFileSize();
			updateButtonStates();
		}
	}


	public List<File> getFileList() {
		return fileList;
	}


	public void setFileList(List<File> fileList) {
		this.fileList.clear();
		this.fileList.addAll(fileList);
		tableViewer.refresh();
		updateFileSize();
		updateButtonStates();
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


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

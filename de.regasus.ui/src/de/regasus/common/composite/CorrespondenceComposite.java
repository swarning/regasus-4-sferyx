package de.regasus.common.composite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.lambdalogic.messeinfo.contact.AbstractCorrespondence;
import com.lambdalogic.messeinfo.contact.ContactLabel;
import com.lambdalogic.messeinfo.contact.CorrespondenceType;
import com.lambdalogic.messeinfo.email.EmailLabel;
import com.lambdalogic.messeinfo.participant.ParticipantLabel;
import com.lambdalogic.util.StringHelper;
import com.lambdalogic.util.ZipHelper;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.SWTConstants;
import com.lambdalogic.util.rcp.SelectionHelper;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.datetime.DateTimeComposite;
import com.lambdalogic.util.rcp.simpleviewer.FileLabelProvider;
import com.lambdalogic.util.rcp.widget.MultiLineText;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.ServerModel;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.IImageKeys;
import de.regasus.core.ui.IconRegistry;
import de.regasus.ui.Activator;

/**
 * A composite that shows the contents of one Correspondence. Since many of such composites are to be shown together on one
 * tab item in the participant editor, the table of attachments and the text for remarks try to be as small as possible:
 * the table shows only as much items as it contains, and the text shows its content only if an "expand" button was
 * pressed.
 *
 */

public class CorrespondenceComposite<T extends AbstractCorrespondence> extends Group {

	// *************************************************************************
	// * Widgets and other Attributes
	// *

	/**
	 * The entity
	 */
	private T correspondence;

	/**
	 * Remember if something has been modified.
	 */
	boolean modified;


	/**
	 * Widget für Kontaktzeit, Datum und Uhrzeit zu dem der Kontakt stattgefunden hat
	 */
	private DateTimeComposite correspondenceDateTime;

	/**
	 * Kontaktart, zB Email, Telefon, Fax. The types and the labels are taken from the enum {@link CorrespondenceType}.
	 */
	private Combo typeCombo;

	/**
	 * Mitarbeiter, Name des Mitarbeitern, der den Kontakt hatte (standardmäßig der Nutzername des angemeldeten Nutzers)
	 */
	private Text editUserText;

	/**
	 * Betreff, kurze Zusammenfassung, worum es bei dem Kontakt ging
	 */
	private Text subjectText;

	/**
	 * Bemerkung
	 */
	private MultiLineText remarkText;

	/**
	 * A flag that tells whether the remarkText is expanded
	 */
	protected boolean remarkTextExpanded = false;

	/**
	 * The button to switch the size of the remark text
	 */
	private ToolItem largerSmallerButton;


	/**
	 * An image showing a right arrow (next) as in Eclipse's compiler preferences to indicate that here is unexpanded
	 * data.
	 */
	private Image nextImage;

	/**
	 * An image showing a down arrow as in Eclipse's compiler preferences to indicate that here is expanded data.
	 */
	private Image downImage;

	/**
	 * The JFace viewer that allows - together with the {@link FileLabelProvider} - to see the attached files with their
	 * desktop icon.
	 */
	private TableViewer fileListViewer;

	/**
	 * The table that shows the attachment files
	 */
	private Table fileListTable;

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
	 * The files which are attached.
	 */
	private List<File> fileList = new ArrayList<>();

	/**
	 * The listeners who are to be notified when the set of attached files changes
	 */
	private ModifySupport modifySupport = new ModifySupport(this);

	/**
	 * Used to change the table size via the {@link GridData#heightHint}
	 */
	private GridData tableLayoutData;

	private GridData remarkTextLayoutData;

	private boolean editable = true;

	// *************************************************************************
	// * Constructor
	// *

	public CorrespondenceComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(5, false));

		// First Row
		Label contactDateTimeLabel = new Label(this, SWT.NONE);
		contactDateTimeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		contactDateTimeLabel.setText(ParticipantLabel.ContactTime.getString());

		correspondenceDateTime = new DateTimeComposite(this, SWT.BORDER);
		correspondenceDateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		correspondenceDateTime.addModifyListener(modifySupport);

		Label editUserLabel = new Label(this, SWT.NONE);
		editUserLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		editUserLabel.setText(ParticipantLabel.ContactEmployee.getString());

		editUserText = new Text(this, SWT.BORDER);
		editUserText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		editUserText.setEditable(false);

		// Second Row
		Label subjectLabel = new Label(this, SWT.NONE);
		subjectLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		subjectLabel.setText(ParticipantLabel.Subject.getString());

		subjectText = new Text(this, SWT.BORDER);
		subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		subjectText.addModifyListener(modifySupport);

		Label typeLabel = new Label(this, SWT.NONE);
		typeLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		typeLabel.setText(ContactLabel.CorrespondenceType.getString());

		typeCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		for (CorrespondenceType type : CorrespondenceType.values()) {
			typeCombo.add(type.getString());
		}
		typeCombo.setVisibleItemCount(13);

		typeCombo.addSelectionListener(modifySupport);

		// Third Row
		Label remarkLabel = new Label(this, SWT.NONE);
		remarkLabel.setText(ParticipantLabel.Remark.getString());
		GridData remarkLabelLayoutData = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		remarkLabelLayoutData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		remarkLabel.setLayoutData(remarkLabelLayoutData);

		ToolBar textToolBar = new ToolBar(this, SWT.FLAT);
		textToolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		nextImage = imageRegistry.get("next");
		downImage = imageRegistry.get("down");

		largerSmallerButton = new ToolItem(textToolBar, SWT.PUSH);
		largerSmallerButton.setImage(nextImage);

		remarkText = new MultiLineText(this, SWT.BORDER, false);
		remarkTextLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		remarkText.setLayoutData(remarkTextLayoutData);

		remarkText.addModifyListener(modifySupport);

		// What happens when the user switches the text size
		largerSmallerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleRemarkTextExpanded();
			}
		});

		// Fourth Row
		Label attachmentsLabel = new Label(this, SWT.NONE);
		attachmentsLabel.setText(EmailLabel.Attachments.getString());
		GridData attachmentsLabelLayoutData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		attachmentsLabelLayoutData.verticalIndent = SWTConstants.VERTICAL_INDENT;
		attachmentsLabel.setLayoutData(attachmentsLabelLayoutData);

		ToolBar attachmentsToolBar = new ToolBar(this, SWT.FLAT);
		attachmentsToolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

		// The button to add an attachment
		addButton = new ToolItem(attachmentsToolBar, SWT.PUSH);
		addButton.setImage(IconRegistry.getImage(IImageKeys.CREATE));
		addButton.setToolTipText(UtilI18N.Add + UtilI18N.Ellipsis);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addAttachment();
			}
		});

		// The button to open selected attachments
		editButton = new ToolItem(attachmentsToolBar, SWT.PUSH);
		editButton.setImage(IconRegistry.getImage(IImageKeys.EYE));
		editButton.setToolTipText(UtilI18N.View);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openSelectedAttachments();
			}
		});

		// The button to delete selected attachments
		deleteButton = new ToolItem(attachmentsToolBar, SWT.PUSH);
		deleteButton.setImage(IconRegistry.getImage(IImageKeys.DELETE));
		deleteButton.setToolTipText(UtilI18N.Delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelectedAttachments();
			}
		});

		// The SWT table that shows the attachment files
		fileListTable = new Table(this, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);

		tableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		tableLayoutData.heightHint = 5;
		fileListTable.setLayoutData(tableLayoutData);
		fileListTable.addKeyListener(new KeyAdapter() {
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

		fileListTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonStates();
			}
		});

		fileListTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openSelectedAttachments();
			}
		});

		// The JFace viewer that provides text and images for the fileList
		fileListViewer = new TableViewer(fileListTable);
		fileListViewer.setContentProvider(new ArrayContentProvider());
		fileListViewer.setLabelProvider(new FileLabelProvider());
		fileListViewer.setInput(fileList);
		initDragAndDrop();
		updateButtonStates();

		// remember if something has been modified
		modifySupport.addListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				modified = true;
			}
		});
	}


	public T getCorrespondence() throws Exception {
		correspondence.setSubject(StringHelper.trim(subjectText.getText()));
		correspondence.setRemark(StringHelper.trim(remarkText.getText()));
		correspondence.setCorrespondenceTime(correspondenceDateTime.getDate());
		correspondence.setType(CorrespondenceType.values()[typeCombo.getSelectionIndex()]);
		correspondence.setEditUser(StringHelper.trim(editUserText.getText()));
		correspondence.setAttachments(ZipHelper.zip(fileList));

		return correspondence;
	}


	public void setCorrespondence(T correspondence) throws Exception {
		// Store it in attribute, to be the holder of updated values
		// Must be cloned by the caller
		this.correspondence = correspondence;

		subjectText.setText(StringHelper.avoidNull(correspondence.getSubject()));
		remarkText.setText(StringHelper.avoidNull(correspondence.getRemark()));
		correspondenceDateTime.setDate(correspondence.getCorrespondenceTime());
		editUserText.setText(correspondence.getNewUser());
		if (correspondence.getType() != null) {
			typeCombo.select(correspondence.getType().ordinal());
		}
		byte[] attachments = correspondence.getAttachments();
		// unzip files read only
		List<File> fileList = ZipHelper.unzip(attachments, true);
		setFileList(fileList);

		adaptRemarkText();

		modified = false;

		String currentUser = ServerModel.getInstance().getModelData().getUser();
		if (! this.correspondence.getNewUser().equals(currentUser)) {
			setEditable(false);
		}
	}


	// **************************************************************************
	// * Synchronizing and Modifying
	// *

	public void addModifyListener(ModifyListener modifyListener) {
		modifySupport.addListener(modifyListener);
	}


	public void removeModifyListener(ModifyListener modifyListener) {
		modifySupport.removeListener(modifyListener);
	}


	// *
	// * Synchronizing and Modifying
	// **************************************************************************

	// *************************************************************************
	// * Getter and setter
	// *

	public List<File> getFileList() {
		return fileList;
	}


	public void setFileList(List<File> fileList) {
		this.fileList.clear();
		this.fileList.addAll(fileList);
		fileListViewer.refresh();
		updateButtonStates();
	}


	// *************************************************************************
	// * Internal methods
	// *



	/**
	 * The dragging of files onto the table shall add them as attachments
	 */
	protected void initDragAndDrop() {
		Transfer[] types = new Transfer[] { FileTransfer.getInstance() };
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		DropTarget target = new DropTarget(fileListTable, operations);
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


	protected void updateTableSize() {
		int itemHeight = fileListTable.getItemHeight();
		int visibleCount = fileList.size();
		tableLayoutData.heightHint = visibleCount * itemHeight - 8;
		CorrespondenceComposite.this.getParent().pack();
	}


	/**
	 * Editing and deletion is only possible when something is selected.
	 */
	protected void updateButtonStates() {
		ISelection selection = fileListViewer.getSelection();

		boolean somethingSelected = !selection.isEmpty();
		editButton.setEnabled(somethingSelected);
		deleteButton.setEnabled(somethingSelected & editable);
	}

	/**
	 * All selected files are tried to be opened by their respective associated program.
	 */
	protected void openSelectedAttachments() {
		List<File> selectedFiles = SelectionHelper.getSelection(fileListViewer, File.class);
		for (File file : selectedFiles) {
			boolean success = Program.launch(file.getPath());
			if (success) {
				// Could been opened, so make editor dirty, so that changed attachments are stored in DB
//				modifyText(null);
			}
			else {
				// Couldn't been opened
				String message = NLS.bind(CoreI18N.AttachmentCouldntBeOpened, file.getName());
				MessageDialog.openWarning(getShell(), UtilI18N.Warning, message);
			}
		}
	}


	/**
	 * After a confirmation by the user, all selectd files are removed from the list (which does not mean that the files
	 * are deleted from the hard disk).
	 * <p>
	 * If being part of an editor, that one is notified so that it can set itself to dirty.
	 */
	protected void deleteSelectedAttachments() {
		List<File> selectedFiles = SelectionHelper.getSelection(fileListViewer, File.class);
		if (selectedFiles.size() > 0) {
			boolean reallyDelete =
				MessageDialog.openQuestion(getShell(), UtilI18N.Question, CoreI18N.DeleteSelectedAttachments);

			if (reallyDelete) {
				fileList.removeAll(selectedFiles);
				fileListViewer.refresh();
				modifySupport.fire();
				updateButtonStates();
				updateTableSize();
			}
		}
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
	 * For a given path it is checked whether an according file exists, and if yes it is not added to the attachment
	 * list.
	 *
	 * <p>
	 * If being part of an editor, that one is notified so that it can set itself to dirty.
	 */
	protected void addPath(String path) {
		File file = new File(path);
		if (file.exists() && !fileList.contains(file)) {
			fileList.add(file);
			modifySupport.fire();
			fileListViewer.refresh();
			updateButtonStates();
			updateTableSize();
		}
	}

	private void adaptRemarkText() {
		int lineCount;
		if (remarkTextExpanded) {
			String string = remarkText.getText();
			// Use the actual line count if not empty
			if (!StringHelper.isEmpty(string)) {
				StringHelper.getLines(string).size();
				lineCount = StringHelper.getLines(string).size();
			}
			else {
				lineCount = 10;
			}
			largerSmallerButton.setImage(downImage);
		}
		else {
			lineCount = 1;
			largerSmallerButton.setImage(nextImage);
		}
		int height = SWTHelper.computeTextWidgetHeightForLineCount(remarkText, lineCount);
		remarkTextLayoutData.heightHint = height;
		getParent().pack();
	}


	public void setEditable(boolean editable) {
		this.editable = editable;
		addButton.setEnabled(editable);
		correspondenceDateTime.setEnabled(editable);
		deleteButton.setEnabled(editable);
		remarkText.setEditable(editable);
		subjectText.setEditable(editable);
		typeCombo.setEnabled(editable);

		fileListTable.setBackground(editUserText.getBackground());
		correspondenceDateTime.setForeground(editUserText.getForeground());
		typeCombo.setForeground(editUserText.getForeground());
	}


	public boolean isModified() {
		return modified;
	}


	@SuppressWarnings("rawtypes")
	public void toggleRemarkTextExpanded() {
		remarkTextExpanded = !remarkTextExpanded;

		adaptRemarkText();

		// The managementComposite may have to adapt its scrollbars so that all remarkTexts are visible
		AbstractCorrespondenceManagementComposite correspondenceManagementComposite = (AbstractCorrespondenceManagementComposite) getParent().getParent().getParent();
		correspondenceManagementComposite.refreshScrollbar();
	}


	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}

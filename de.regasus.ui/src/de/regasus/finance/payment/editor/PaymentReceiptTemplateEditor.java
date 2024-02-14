package de.regasus.finance.payment.editor;

import static de.regasus.LookupService.getPaymentMgr;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.stream.FileImageInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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

import com.lambdalogic.messeinfo.invoice.InvoiceLabel;
import com.lambdalogic.messeinfo.invoice.data.PaymentReceiptType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.messeinfo.kernel.data.DataStore_DocType_Language_Comparator;
import com.lambdalogic.util.CloneHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.model.CacheModelEvent;
import com.lambdalogic.util.model.CacheModelListener;
import com.lambdalogic.util.rcp.BusyCursorHelper;
import com.lambdalogic.util.rcp.CopyAction;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.Activator;
import de.regasus.core.ui.CoreI18N;
import de.regasus.core.ui.TemplateHelper;
import de.regasus.core.ui.editor.IRefreshableEditorPart;
import de.regasus.finance.PaymentReceiptTemplateModel;
import de.regasus.finance.PaymentType;
import de.regasus.finance.payment.dialog.UploadPaymentTemplateDialog;


public class PaymentReceiptTemplateEditor extends EditorPart implements IRefreshableEditorPart {

	public static final String ID = "PaymentReceiptTemplateEditor";


	private PaymentReceiptTemplateModel model;


	// template list
	private Collection<DataStoreVO> templateCol;

	// true if the user set the checkbox to do not ask when refresh anymore
	private boolean refreshDontAsk;

	// widgets
	private PaymentReceiptTemplateTable templateTable;
	private Table table;
	private Button editTemplateButton;
	private Button uploadTemplateButton;
	private Button deleteTemplateButton;
	private Button dowloadTemplateButton;
	private Button refreshTemplateButton;


	private CacheModelListener<String> modelListener = new CacheModelListener<String>() {
		@Override
		public void dataChange(CacheModelEvent<String> event) {
			try {
				if (event.getSource() == model) {
					refreshFromModel();
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

			model = PaymentReceiptTemplateModel.getInstance();

			// get entity
			templateCol = model.getAllTemplates();

			model.addListener(modelListener);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			throw new PartInitException(e.getMessage(), e);
		}
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


	@Override
	public boolean isDirty() {
		return false;
	}


	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}


	@Override
	public void createPartControl(Composite parent) {
		try {
			parent.setLayout(new GridLayout());

			Composite tableComposite = new Composite(parent, SWT.NONE);
			tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			tableComposite.setLayout(new GridLayout(1, false));


			table = new Table(tableComposite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableColumn paymentReceiptTypeTableColumn = new TableColumn(table, SWT.NONE);
			paymentReceiptTypeTableColumn.setWidth(140);
			paymentReceiptTypeTableColumn.setText(UtilI18N.Type);

			TableColumn paymentTypeTableColumn = new TableColumn(table, SWT.NONE);
			paymentTypeTableColumn.setWidth(100);
			paymentTypeTableColumn.setText(InvoiceLabel.PaymentType.getString());

			TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
			languageTableColumn.setWidth(60);
			languageTableColumn.setText(UtilI18N.Language);

			TableColumn fileTableColumn = new TableColumn(table, SWT.NONE);
			fileTableColumn.setWidth(300);
			fileTableColumn.setText(UtilI18N.File);

			templateTable = new PaymentReceiptTemplateTable(table);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.heightHint = 100;
			table.setLayoutData(gridData);
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateButtonEnabledStates();
				}
			});
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					if (SystemHelper.canEditOpenOfficeDocs()) {
						editTemplate();
					}
				}
			});


			// copy and paste
			getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyAction());


			Composite buttonComposite = new Composite(parent, SWT.NONE);
			RowLayout layout = new RowLayout();
			layout.pack = false;
			layout.spacing = 5;
			layout.wrap = false;
			layout.justify = true;
			buttonComposite.setLayout(layout);

			// Edit Button
			// Hide button until editing its working again (https://lambdalogic.atlassian.net/browse/MIRCP-4283)
			boolean deactivated = true;
			if (!deactivated && SystemHelper.canEditOpenOfficeDocs()) {
				editTemplateButton = new Button(buttonComposite, SWT.PUSH);
				editTemplateButton.setText(CoreI18N.TemplateEdit_Text);
				editTemplateButton.setToolTipText(CoreI18N.TemplateEdit_ToolTip);
				editTemplateButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						editTemplate();
					}
				});
			}

			// Upload Button
			uploadTemplateButton = new Button(buttonComposite, SWT.PUSH);
			uploadTemplateButton.setText(CoreI18N.TemplateUpload_Text);
			uploadTemplateButton.setToolTipText(CoreI18N.TemplateUpload_ToolTip);
			uploadTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					uploadTemplate();
				}
			});

			// Refresh Button
			refreshTemplateButton = new Button(buttonComposite, SWT.PUSH);
			refreshTemplateButton.setText(de.regasus.core.ui.CoreI18N.TemplateRefresh_Text);
			refreshTemplateButton.setToolTipText(de.regasus.core.ui.CoreI18N.TemplateRefresh_ToolTip);
			refreshTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						refreshTemplate();
					}
					catch (Exception e1) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e1);
					}
				}
			});

			// Download Button
			dowloadTemplateButton = new Button(buttonComposite, SWT.PUSH);
			dowloadTemplateButton.setText(CoreI18N.TemplateDownload_Text);
			dowloadTemplateButton.setToolTipText(CoreI18N.TemplateDownload_ToolTip);
			dowloadTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						downloadTemplate();
					}
					catch (Exception ex) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), ex);
					}

				}

			});

			// Delete Button
			deleteTemplateButton = new Button(buttonComposite, SWT.PUSH);
			deleteTemplateButton.setText(CoreI18N.TemplateDelete_Text);
			deleteTemplateButton.setToolTipText(CoreI18N.TemplateDelete_ToolTip);
			deleteTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					deleteTemplate();
				}

			});


			refreshFromModel();
			updateButtonEnabledStates();
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
			close();
		}
	}


	protected void updateButtonEnabledStates() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				int count = table.getSelectionIndices().length;

				// all selected templates represent a record in table DATA_STORE (and not a file in JAR)
				//boolean allDataStore = true;
				// all selected templates represent a file in JAR (and not a record in table DATA_STORE)
				boolean allJAR = true;

				IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
				Iterator<DataStoreVO> iterator = selection.iterator();
				while (iterator.hasNext()) {
					DataStoreVO dataStoreVO = iterator.next();
					if (dataStoreVO.getID() == null) {
						//allDataStore = false;
					}
					else {
						allJAR = false;
					}
				}

				if (editTemplateButton != null) {
					editTemplateButton.setEnabled(count == 1);
				}
				dowloadTemplateButton.setEnabled(count == 1);

				// enable delete button if at least 1 selected item represent a record in table DATA_STORE
				deleteTemplateButton.setEnabled(count > 0 && ! allJAR);

				// upload button is always enabled
				//uploadTemplateButton.setEnabled(true);

				refreshTemplateButton.setEnabled(count == 1);
			}
		});
	}


	/* (non-Javadoc)
	 * @see de.regasus.core.ui.editor.IRefreshableEditorPart#refresh()
	 */
	@Override
	public void refresh() {
		try {
			model.refresh();
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


	private void refreshFromModel() throws Exception {
		Collection<DataStoreVO> allDataStoreVOs = model.getAllTemplates();
		final List<DataStoreVO> orderedDataStoreVOs = CloneHelper.deepCloneArrayList(allDataStoreVOs);

		Collections.sort(orderedDataStoreVOs, DataStore_DocType_Language_Comparator.getInstance());

		SWTHelper.asyncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				templateTable.getViewer().setInput(orderedDataStoreVOs);
			}
		});
	}


	@Override
	public void setFocus() {
		table.setFocus();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
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
				getSite().getPage().closeEditor(PaymentReceiptTemplateEditor.this, false /* save */);
			}
		});
	}


	private void editTemplate() {
		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		if (selection.size() == 1) {
			DataStoreVO dataStoreVO = (DataStoreVO) selection.getFirstElement();
			TemplateHelper.editTemplate(dataStoreVO);
		}
	}


	private void refreshTemplate() throws Exception {
		// determine selection
		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		if (selection.size() == 1) {
			DataStoreVO dataStoreVO = (DataStoreVO) selection.getFirstElement();

			// determine file of selected item
			String fileName = dataStoreVO.getExtFileName();
			File file = new File(fileName);

			if (file.exists()) {
				boolean refresh;

				if (refreshDontAsk) {
					refresh = true;
				}
				else {
					MessageDialogWithToggle dialogWithToggle = MessageDialogWithToggle.openOkCancelConfirm(
						getSite().getShell(),
						UtilI18N.Hint,
						de.regasus.core.ui.CoreI18N.TemplateRefresh_ToolTip,
						UtilI18N.DontShowThisConfirmDialogAgain, false, null, null
					);

					refresh = (Window.OK == dialogWithToggle.getReturnCode());
					refreshDontAsk = dialogWithToggle.getToggleState();
				}

				if (refresh) {
					// load file
					FileImageInputStream fis = new FileImageInputStream(file);
					byte[] content = new byte[(int) file.length()];
					fis.read(content);
					fis.close();

					// set file data to selected DataStoreVO
					dataStoreVO.setContent(content);

					/* update DataStoreVO
					 * It is not necessary to check whether the DataStoreVO has an ID or not, because update and
					 * create are based on DataStoreVO.path.
					 */
					model.update(dataStoreVO);
				}
			}
			else {
				uploadTemplate();
			}
		}
	}


	private void uploadTemplate() {
		try {
			// determine the initial file to upload from current selection
			File initFile = null;
			PaymentReceiptType initPaymentReceiptType = null;
			PaymentType initPaymentType = null;
			IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
			DataStoreVO setDataStoreVO = (DataStoreVO) selection.getFirstElement();
			if (setDataStoreVO != null) {
				/* determine initial PaymentReceiptType and PaymentType
				 * DataStoreVO.docType is a combination of PaymentReceiptType and PaymentType, e.g. "PAYMENT.CASH".
				 */
				String docType = setDataStoreVO.getDocType();
				int dotIdx = docType.indexOf('.');
				String paymentReceiptTypeName = docType.substring(0, dotIdx);
				String paymentTypeName = docType.substring(dotIdx + 1);

				initPaymentReceiptType = PaymentReceiptType.valueOf(paymentReceiptTypeName);
				initPaymentType = PaymentType.valueOf(paymentTypeName);

				// determine initial file name
				String extFileName = setDataStoreVO.getExtFileName();
				if (extFileName != null) {
					initFile = new File(extFileName);
				}
			}

			Shell shell = getEditorSite().getShell();
			UploadPaymentTemplateDialog dialog = new UploadPaymentTemplateDialog(
				shell,
				initFile,
				initPaymentReceiptType,
				initPaymentType
			);
			dialog.setExistingPaymentReceiptTemplateList(templateCol);
			int code = dialog.open();
			if (code == Window.OK) {
				String filePath = dialog.getFilePath();

				File file = new File(filePath);
				try {
					FileImageInputStream fis = new FileImageInputStream(file);
					byte[] content = new byte[(int) file.length()];
					fis.read(content);
					fis.close();
					PaymentReceiptType paymentReceiptType = dialog.getPaymentReceiptType();
					PaymentType paymentType = dialog.getPaymentType();
					String language = dialog.getLanguage();

					model.uploadPaymentReceiptTemplate(
						filePath,
						paymentReceiptType,
						paymentType,
						language,
						content
					);
				}
				catch (Exception e) {
					RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	private void downloadTemplate() {
		// Find selected DataStoreVO - there should be precisely one.
		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		final DataStoreVO dataStoreVO = (DataStoreVO) selection.getFirstElement();

		// Make sure extFileName ends with extension
		String extFileName = dataStoreVO.getExtFileName();
		String extension = dataStoreVO.getExtension();
		if (!extFileName.endsWith(extension) && extension != null) {
			extFileName += "." + extension;
		}
		File originalFile = new File(extFileName);

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
							/* determine PaymentReceiptType and PaymentType
							 * DataStoreVO.docType is a combination of PaymentReceiptType and PaymentType, e.g. "PAYMENT.CASH".
							 */
							String docType = dataStoreVO.getDocType();
							int dotIdx = docType.indexOf('.');
							String paymentReceiptTypeName = docType.substring(0, dotIdx);
							String paymentTypeName = docType.substring(dotIdx + 1);

							PaymentReceiptType paymentReceiptType = PaymentReceiptType.valueOf(paymentReceiptTypeName);
							PaymentType paymentType = PaymentType.valueOf(paymentTypeName);
							String lang = dataStoreVO.getLanguage();

							// Get template data from server directly, because it is not handled by the model.
							byte[] data = getPaymentMgr().getPaymentReceiptTemplateData(
								paymentReceiptType,
								paymentType,
								lang
							);

							FileOutputStream fos = new FileOutputStream(saveFile);
							fos.write(data);
							fos.close();
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
	}


	private void deleteTemplate() {
		boolean deleteOK = MessageDialog.openQuestion(
			Display.getDefault().getActiveShell(),
			de.regasus.core.ui.CoreI18N.TemplateDelete_ConfirmTitle,
			de.regasus.core.ui.CoreI18N.TemplateDelete_ConfirmMessage
		);
		if (!deleteOK) {
			return;
		}


		try {
			IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
			Iterator<DataStoreVO> iterator = selection.iterator();
			while (iterator.hasNext()) {
				// delete if dataStoreVO represent a record in table DATA_STORE and not only a file in the JAR
				DataStoreVO dataStoreVO = iterator.next();
				if (dataStoreVO.getID() != null) {
					model.delete(dataStoreVO);
				}
			}
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}

}

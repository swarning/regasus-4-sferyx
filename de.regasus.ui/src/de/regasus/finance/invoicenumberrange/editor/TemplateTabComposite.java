package de.regasus.finance.invoicenumberrange.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.imageio.stream.FileImageInputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.lambdalogic.messeinfo.invoice.data.InvoiceNoRangeVO;
import com.lambdalogic.messeinfo.invoice.interfaces.InvoiceTemplateType;
import com.lambdalogic.messeinfo.kernel.data.DataStoreVO;
import com.lambdalogic.util.FileHelper;
import com.lambdalogic.util.SystemHelper;
import com.lambdalogic.util.rcp.LazyComposite;
import com.lambdalogic.util.rcp.ModifySupport;
import com.lambdalogic.util.rcp.UtilI18N;
import com.lambdalogic.util.rcp.widget.SWTHelper;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.core.ui.TemplateHelper;
import de.regasus.finance.InvoiceNoRangeModel;
import de.regasus.finance.invoicenumberrange.dialog.UploadInvoiceTemplateDialog;
import de.regasus.ui.Activator;


public class TemplateTabComposite extends LazyComposite {

	// the entity
	private InvoiceNoRangeVO invoiceNoRangeVO;

	private ModifySupport modifySupport = new ModifySupport(this);

	// true if the user set the checkbox to do not ask when refresh anymore
	private boolean refreshDontAsk;


	// **************************************************************************
	// * Widgets
	// *

	private InvoiceTemplateTable templateTable;
	private Table table;

	private Button editTemplateButton;
	private Button uploadTemplateButton;
	private Button deleteTemplateButton;
	private Button dowloadTemplateButton;
	private Button refreshTemplateButton;

	private FileNamePatternGroup fileNamePatternGroup;

	// *
	// * Widgets
	// **************************************************************************


	public TemplateTabComposite(Composite parent) {
		super(parent, SWT.NONE);
	}


	@Override
	protected void createPartControl() throws Exception {
		setLayout( new GridLayout(1, false) );

		Composite templateTableComposite = createTemplateTableComposite(this);
		templateTableComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true) );

		Composite buttonComposite = createTemplateButtonComposite(this);
		buttonComposite.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false) );

		fileNamePatternGroup = new FileNamePatternGroup(this, SWT.NONE);
		fileNamePatternGroup.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false) );
		fileNamePatternGroup.setInvoiceNoRange(invoiceNoRangeVO);
		fileNamePatternGroup.addModifyListener(modifySupport);
	}


	private Composite createTemplateTableComposite(Composite parent) throws Exception {
		Composite templateTableComposite = new Composite(parent, SWT.NONE);

		templateTableComposite.setLayout(new GridLayout(1, false));
		table = new Table(templateTableComposite, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn nameTableColumn = new TableColumn(table, SWT.NONE);
		nameTableColumn.setWidth(100);
		nameTableColumn.setText(UtilI18N.Type);

		TableColumn languageTableColumn = new TableColumn(table, SWT.NONE);
		languageTableColumn.setWidth(60);
		languageTableColumn.setText(UtilI18N.Language);

		TableColumn fileTableColumn = new TableColumn(table, SWT.NONE);
		fileTableColumn.setWidth(300);
		fileTableColumn.setText(UtilI18N.File);

		templateTable = new InvoiceTemplateTable(table);

		templateTable.setInput( getInvoiceTemplates() );
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

		return templateTableComposite;
	}


	private Composite createTemplateButtonComposite(Composite parent) {
		Composite templateButtonComposite = new Composite(parent, SWT.NONE);

		RowLayout layout = new RowLayout();
		layout.pack = false;
		layout.spacing = 5;
		layout.wrap = false;
		layout.justify = true;
		templateButtonComposite.setLayout(layout);

		// Edit Button
		// Hide button until editing its working again (https://lambdalogic.atlassian.net/browse/MIRCP-4283)
		boolean deactivated = true;
		if (!deactivated && SystemHelper.canEditOpenOfficeDocs()) {
			editTemplateButton = new Button(templateButtonComposite, SWT.PUSH);
			editTemplateButton.setText(de.regasus.core.ui.CoreI18N.TemplateEdit_Text);
			editTemplateButton.setToolTipText(de.regasus.core.ui.CoreI18N.TemplateEdit_ToolTip);
			editTemplateButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					editTemplate();
				}
			});
		}

		// Upload Button
		uploadTemplateButton = new Button(templateButtonComposite, SWT.PUSH);
		uploadTemplateButton.setText(de.regasus.core.ui.CoreI18N.TemplateUpload_Text);
		uploadTemplateButton.setToolTipText(de.regasus.core.ui.CoreI18N.TemplateUpload_ToolTip);
		uploadTemplateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				uploadTemplate();
			}
		});

		// Refresh Button
		refreshTemplateButton = new Button(templateButtonComposite, SWT.PUSH);
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
		dowloadTemplateButton = new Button(templateButtonComposite, SWT.PUSH);
		dowloadTemplateButton.setText(de.regasus.core.ui.CoreI18N.TemplateDownload_Text);
		dowloadTemplateButton.setToolTipText(de.regasus.core.ui.CoreI18N.TemplateDownload_ToolTip);
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
		deleteTemplateButton = new Button(templateButtonComposite, SWT.PUSH);
		deleteTemplateButton.setText(de.regasus.core.ui.CoreI18N.TemplateDelete_Text);
		deleteTemplateButton.setToolTipText(de.regasus.core.ui.CoreI18N.TemplateDelete_ToolTip);
		deleteTemplateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteTemplate();
			}

		});

		return templateButtonComposite;
	}


	private void editTemplate() {
		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		if (selection.size() == 1) {
			DataStoreVO dataStoreVO = (DataStoreVO) selection.getFirstElement();
			TemplateHelper.editTemplate(dataStoreVO);
		}
	}


	private void refreshTemplate() throws Exception {
		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		if (selection.size() == 1) {
			DataStoreVO dataStoreVO = (DataStoreVO) selection.getFirstElement();
			String fileName = dataStoreVO.getExtFileName();
			String language = dataStoreVO.getLanguage();
			File file = new File(fileName);

			if (file.exists()) {
				if (!InvoiceNoRangeEditor.saveActiveEditor()) {
					return;
				}



				boolean refresh;

				if (refreshDontAsk) {
					refresh = true;
				}
				else {
					MessageDialogWithToggle dialogWithToggle = MessageDialogWithToggle.openOkCancelConfirm(
						getShell(),
						UtilI18N.Hint,
						de.regasus.core.ui.CoreI18N.TemplateRefresh_ToolTip,
						UtilI18N.DontShowThisConfirmDialogAgain,
						false,
						null,
						null
					);

					refresh = (Window.OK == dialogWithToggle.getReturnCode());
					refreshDontAsk = dialogWithToggle.getToggleState();
				}

				if (refresh) {
					byte[] content = FileHelper.readFile(file);

					InvoiceTemplateType invoiceTemplateType = InvoiceTemplateType.valueOf(dataStoreVO.getDocType());

					InvoiceNoRangeModel.getInstance().uploadTemplate(
						invoiceTemplateType,
						invoiceNoRangeVO.getPK(),
						language,
						fileName,
						content
					);
				}
			}
			else {
				uploadTemplate();
			}
		}
	}


	private void uploadTemplate() {
		if (!InvoiceNoRangeEditor.saveActiveEditor()) {
			return;
		}


		try {
			// determine the initial file to upload from current selection
			File initFile = null;
			InvoiceTemplateType initInvoiceTemplateType = null;
			IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
			DataStoreVO dataStoreVO = (DataStoreVO) selection.getFirstElement();
			if (dataStoreVO != null) {
				// determine initial InvoiceTemplateType
				String docType = dataStoreVO.getDocType();
				initInvoiceTemplateType = InvoiceTemplateType.valueOf(docType);

				// determine initial file name
				String extFileName = dataStoreVO.getExtFileName();
				if (extFileName != null) {
					initFile = new File(extFileName);
				}
			}

			UploadInvoiceTemplateDialog dialog = new UploadInvoiceTemplateDialog(getShell(), initFile, initInvoiceTemplateType);

			dialog.setExistingTemplateVOList( getInvoiceTemplates() );
			int code = dialog.open();
			if (code == Window.OK) {
				String filePath = dialog.getFilePath();

				File file = new File(filePath);
				try {
					FileImageInputStream fis = new FileImageInputStream(file);
					byte[] content = new byte[(int) file.length()];
					fis.read(content);
					fis.close();
					InvoiceTemplateType invoiceTemplateType = dialog.getInvoiceTemplateType();
					String language = dialog.getLanguage();

					InvoiceNoRangeModel.getInstance().uploadTemplate(
						invoiceTemplateType,
						invoiceNoRangeVO.getPK(),
						language,
						filePath,
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
		DataStoreVO dataStoreVO = (DataStoreVO) selection.getFirstElement();
		TemplateHelper.downloadTemplate(dataStoreVO);
	}


	private void deleteTemplate() {
		if (!InvoiceNoRangeEditor.saveActiveEditor()) {
			return;
		}


		boolean deleteOK = MessageDialog.openQuestion(
			Display.getDefault().getActiveShell(),
			de.regasus.core.ui.CoreI18N.TemplateDelete_ConfirmTitle,
			de.regasus.core.ui.CoreI18N.TemplateDelete_ConfirmMessage
		);
		// Wenn Nutzer im Dialog 'Nein' geantwortet hat
		if (!deleteOK) {
			return;
		}


		Collection<DataStoreVO> dataStoreVOs = new ArrayList<>();

		IStructuredSelection selection = (IStructuredSelection) templateTable.getViewer().getSelection();
		Iterator<DataStoreVO> iterator = selection.iterator();
		while (iterator.hasNext()) {
			DataStoreVO dataStoreVO = iterator.next();
			dataStoreVOs.add(dataStoreVO);
		}


		try {
			InvoiceNoRangeModel.getInstance().deleteTemplates(invoiceNoRangeVO.getPK(), dataStoreVOs);
		}
		catch (Exception e) {
			RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
		}
	}


	protected void updateButtonEnabledStates() {
		SWTHelper.syncExecDisplayThread(new Runnable() {
			@Override
			public void run() {
				int count = table.getSelectionIndices().length;

				if (editTemplateButton != null) {
					editTemplateButton.setEnabled(count == 1);
				}
				dowloadTemplateButton.setEnabled(count == 1);
				deleteTemplateButton.setEnabled(count > 0);
				uploadTemplateButton.setEnabled(true);
				refreshTemplateButton.setEnabled(count == 1);
			}
		});

	}





	public void setInvoiceNoRange(InvoiceNoRangeVO invoiceNoRangeVO) {
		this.invoiceNoRangeVO = invoiceNoRangeVO;
		syncWidgetsToEntity();
	}


	private List<DataStoreVO> getInvoiceTemplates() throws Exception {
    	List<DataStoreVO> invoiceTemplateList = InvoiceNoRangeModel
    		.getInstance()
    		.getExtendedInvoiceNoRangeCVO( invoiceNoRangeVO.getId() )
    		.getInvoiceTemplateVOList();

    	return invoiceTemplateList;
	}


	private void syncWidgetsToEntity() {
		if (invoiceNoRangeVO != null && isInitialized()) {
			SWTHelper.syncExecDisplayThread(new Runnable() {
				@Override
				public void run() {
					try {
						templateTable.setInput( getInvoiceTemplates() );

						fileNamePatternGroup.setInvoiceNoRange(invoiceNoRangeVO);

						updateButtonEnabledStates();
					}
					catch (Exception e) {
						RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
					}
				}
			});
		}
	}


	public void syncEntityToWidgets() {
		if (invoiceNoRangeVO != null && isInitialized()) {
			fileNamePatternGroup.syncEntityToWidgets();
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
